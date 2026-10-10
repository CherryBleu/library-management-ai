# 数据库设计

## 1. 设计边界

数据库保存读者、借阅证、管理员、目录、馆藏副本、借阅、预约、策略、罚款和审计数据。领域继承通过 `item_type`/`reader_type` 鉴别列映射，规则通过版本和生效时间保存；历史记录采用停用或逻辑删除，保证可追溯。

## 2. 表结构

### readers

| 字段 | 类型 | 可空 | 主键 | 外键 | 唯一 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| reader_id | BIGINT | 否 | 是 |  | 是 | identity | 读者身份 |
| name | VARCHAR(100) | 否 |  |  |  |  | 姓名 |
| department | VARCHAR(150) | 否 |  |  |  |  | 院系/单位 |
| contact | VARCHAR(100) | 是 |  |  |  |  | 联系方式 |
| reader_type | VARCHAR(30) | 否 |  |  |  |  | UNDERGRADUATE/GRADUATE/DOCTORAL/JUNIOR_COLLEGE/TEACHER |
| status | VARCHAR(20) | 否 |  |  |  | ACTIVE | ACTIVE/SUSPENDED/CLOSED |
| registered_at | TIMESTAMP | 否 |  |  |  | current_timestamp | 注册时间 |

### borrow_cards

| 字段 | 类型 | 可空 | 主键 | 外键 | 唯一 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| card_id | BIGINT | 否 | 是 |  | 是 | identity | 证件身份 |
| card_no | VARCHAR(20) | 否 |  |  | 是 |  | `CARD+YYYY+6位序号`，全局不复用 |
| reader_id | BIGINT | 否 |  | readers.reader_id | 否 |  | 持有人 |
| holder_name | VARCHAR(100) | 否 |  |  |  |  | 办证时快照 |
| department | VARCHAR(150) | 否 |  |  |  |  | 办证时快照 |
| status | VARCHAR(20) | 否 |  |  |  | ACTIVE | ACTIVE/INACTIVE/EXPIRED |
| issued_at | DATE | 否 |  |  |  | current_date | 办证日期 |
| expires_at | DATE | 否 |  |  |  |  | 有效期 |
| deleted_at | TIMESTAMP | 是 |  |  |  |  | 逻辑删除时间 |

约束：同一读者最多一张有效证件；有借阅、预约或未结罚款时不得物理删除。

### librarians

| 字段 | 类型 | 可空 | 主键 | 外键 | 唯一 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| librarian_id | BIGINT | 否 | 是 |  | 是 | identity | 图书管理员 |
| name | VARCHAR(100) | 否 |  |  |  |  | 姓名 |
| employee_no | VARCHAR(50) | 否 |  |  | 是 |  | 管理员标识 |
| status | VARCHAR(20) | 否 |  |  |  | ACTIVE | ACTIVE/INACTIVE |

### system_admins

| 字段 | 类型 | 可空 | 主键 | 外键 | 唯一 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| admin_id | BIGINT | 否 | 是 |  | 是 | identity | 系统管理员 |
| name | VARCHAR(100) | 否 |  |  |  |  | 姓名 |
| employee_no | VARCHAR(50) | 否 |  |  | 是 |  | 管理员标识 |
| status | VARCHAR(20) | 否 |  |  |  | ACTIVE | ACTIVE/INACTIVE |

应用服务保证最后一个 ACTIVE 管理员不可停用。

### book_titles

| 字段 | 类型 | 可空 | 主键 | 外键 | 唯一 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| title_id | BIGINT | 否 | 是 |  | 是 | identity | 书目标题 |
| title | VARCHAR(300) | 否 |  |  |  |  | 标题 |
| authors | VARCHAR(500) | 否 |  |  |  |  | 作者 |
| isbn_or_standard_no | VARCHAR(50) | 是 |  |  | 是 |  | ISBN/标准号 |
| publisher | VARCHAR(200) | 是 |  |  |  |  | 出版社 |
| language | VARCHAR(30) | 否 |  |  |  |  | ZH/FOREIGN |
| item_type | VARCHAR(30) | 否 |  |  |  |  | BOOK/MAGAZINE/THESIS |
| status | VARCHAR(20) | 否 |  |  |  | ACTIVE | ACTIVE/INACTIVE |

### library_items

| 字段 | 类型 | 可空 | 主键 | 外键 | 唯一 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| item_id | BIGINT | 否 | 是 |  | 是 | identity | 馆藏副本 |
| title_id | BIGINT | 否 |  | book_titles.title_id | 否 |  | 所属标题 |
| barcode | VARCHAR(80) | 否 |  |  | 是 |  | 馆员录入，系统校验全局唯一 |
| item_type | VARCHAR(30) | 否 |  |  |  |  | 具体借出物类型 |
| language | VARCHAR(30) | 否 |  |  |  |  | 语言快照 |
| loan_eligibility | VARCHAR(20) | 否 |  |  |  | BORROWABLE | BORROWABLE/REFERENCE_ONLY |
| location | VARCHAR(100) | 否 |  |  |  |  | 馆藏位置 |
| status | VARCHAR(20) | 否 |  |  |  | AVAILABLE | AVAILABLE/ON_LOAN/RESERVED/LOST/INACTIVE |

### loans

| 字段 | 类型 | 可空 | 主键 | 外键 | 唯一 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| loan_id | BIGINT | 否 | 是 |  | 是 | identity | 借阅记录 |
| reader_id | BIGINT | 否 |  | readers.reader_id | 否 |  | 读者 |
| card_id | BIGINT | 否 |  | borrow_cards.card_id | 否 |  | 借阅证 |
| item_id | BIGINT | 否 |  | library_items.item_id | 否 |  | 副本 |
| librarian_id | BIGINT | 否 |  | librarians.librarian_id | 否 |  | 经办人 |
| borrowed_at | TIMESTAMP | 否 |  |  |  | current_timestamp | 借出时间 |
| due_at | TIMESTAMP | 否 |  |  |  |  | 应还时间 |
| returned_at | TIMESTAMP | 是 |  |  |  |  | 实际归还时间 |
| status | VARCHAR(20) | 否 |  |  |  | BORROWED | BORROWED/RETURNED/OVERDUE |
| policy_version | VARCHAR(40) | 否 |  |  |  |  | 借出时规则快照 |

部分唯一索引：`UNIQUE(item_id) WHERE status IN ('BORROWED','OVERDUE')`，保证副本最多一条有效借阅。

### reservations

| 字段 | 类型 | 可空 | 主键 | 外键 | 唯一 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| reservation_id | BIGINT | 否 | 是 |  | 是 | identity | 预约记录 |
| reader_id | BIGINT | 否 |  | readers.reader_id | 否 |  | 读者 |
| title_id | BIGINT | 否 |  | book_titles.title_id | 否 |  | 预约标题 |
| queue_no | INTEGER | 否 |  |  |  |  | 标题内唯一顺序 |
| status | VARCHAR(20) | 否 |  |  |  | WAITING | WAITING/READY/FULFILLED/CANCELLED/EXPIRED |
| created_at | TIMESTAMP | 否 |  |  |  | current_timestamp | 排队依据 |
| hold_until | DATE | 是 |  |  |  |  | 队首保留截止日期 |

唯一约束：同一读者和标题的有效预约只能一条；应用服务限制每名读者最多 5 条有效预约。

### borrow_policies

| 字段 | 类型 | 可空 | 主键 | 外键 | 唯一 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| policy_id | BIGINT | 否 | 是 |  | 是 | identity | 借阅策略 |
| reader_type | VARCHAR(30) | 否 |  |  | 否 |  | 读者类型 |
| max_active_loans | INTEGER | 否 |  |  |  |  | 最大借阅数 |
| loan_period_days | INTEGER | 否 |  |  |  |  | 自然日期限 |
| effective_from | DATE | 否 |  |  |  |  | 生效日期 |
| effective_to | DATE | 是 |  |  |  |  | 失效日期 |
| version | VARCHAR(40) | 否 |  |  | 是 |  | 版本 |
| status | VARCHAR(20) | 否 |  |  |  | ACTIVE | ACTIVE/INACTIVE |

同一读者类型的有效日期区间不得重叠。初始值为 5/30、10/60、15/90、5/30、20/90。

### fine_rules

| 字段 | 类型 | 可空 | 主键 | 外键 | 唯一 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| rule_id | BIGINT | 否 | 是 |  | 是 | identity | 罚款规则 |
| item_type | VARCHAR(30) | 否 |  |  | 否 |  | 借出物类型 |
| daily_rate | DECIMAL(10,2) | 否 |  |  |  |  | 每日费率 |
| maximum_amount | DECIMAL(10,2) | 否 |  |  |  |  | 封顶金额 |
| effective_from | DATE | 否 |  |  |  |  | 生效日期 |
| effective_to | DATE | 是 |  |  |  |  | 失效日期 |
| version | VARCHAR(40) | 否 |  |  | 是 |  | 版本 |

初始费率/封顶：中文图书 0.50/100、外文图书 1.00/200、中文杂志 1.00/100、外文杂志 2.00/200、论文 2.00/300。

### fine_records

| 字段 | 类型 | 可空 | 主键 | 外键 | 唯一 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| fine_id | BIGINT | 否 | 是 |  | 是 | identity | 罚款记录 |
| loan_id | BIGINT | 否 |  | loans.loan_id | 是 |  | 对应借阅 |
| overdue_days | INTEGER | 否 |  |  |  | 0 | 自然日超期天数 |
| amount | DECIMAL(10,2) | 否 |  |  |  | 0 | 计算金额 |
| rule_version | VARCHAR(40) | 否 |  |  |  |  | 规则快照 |
| status | VARCHAR(20) | 否 |  |  |  | UNPAID | UNPAID/PAID/WAIVED |
| adjustment_reason | VARCHAR(500) | 是 |  |  |  |  | 减免/作废原因 |

## 3. 审计和索引

`audit_logs` 保存操作人、角色、动作、对象类型/ID、结果、错误码、原因和时间。建议索引：`borrow_cards(card_no)`、`library_items(barcode)`、`book_titles(title, isbn_or_standard_no)`、`loans(reader_id,status)`、`reservations(title_id,status,queue_no)`、`audit_logs(actor_id,created_at)`。

## 4. 领域映射与事务

Reader 聚合映射 `readers` 与 `borrow_cards`；Catalog 聚合映射 `book_titles` 与 `library_items`；Loan 聚合映射 `loans` 与 `fine_records`；Reservation 聚合映射 `reservations`；Rule 聚合映射策略和罚款规则表。借书事务锁定 `library_items` 行并同时写入 Loan，归还事务同时更新 Loan、副本、罚款和预约状态；失败整体回滚。


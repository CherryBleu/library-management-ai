# 实验二详细设计

## 1. 设计输入与实现边界

输入为 02、03、05、07、13 和 constitution。实现 Java 17 兼容的 Spring Boot 3.x REST 服务，H2 文件数据库用于演示，内存数据库用于自动化测试。采用 Spring JDBC 显式映射关系模型，避免给纯领域类增加框架依赖；Spring Data JPA 并非必须，持久化端口保持可替换。金额使用 BigDecimal，日期使用 LocalDate，统一 Clock（Asia/Shanghai）。不实现真实认证，X-Role/X-Actor-Id 仅是本地教学身份模拟，必须检查对应数据库账户是否有效，禁止部署为公开服务。

## 2. 与实验一的术语和存储映射

- DOCTOR 对应实验一 DOCTORAL；实现统一 DOCTORAL，并保留专科生 JUNIOR_COLLEGE。
- 实验二副本 BORROWED/REMOVED 对应实验一 ON_LOAN/INACTIVE；实现沿用 ON_LOAN/INACTIVE，API、测试和数据库一致。
- BOOK/MAGAZINE/THESIS 表达继承；CHINESE_BOOK 等五种 ItemType 是收费分类，包含语言维度，不新增五个实体子类。
- 预约对象为标题，READY 另关联分配的 item_id 和 hold_until；WAITING/READY 算有效预约。借走保留副本时只有被分配的读者可借，预约转 FULFILLED。
- Loan 的 OVERDUE 是未归还且 due_date < today 的查询派生状态，数据库保存 BORROWED/RETURNED；按 returned_at IS NULL 判定当前借阅。
- 数据库补充 identity_no（注册唯一标识）、return_librarian_id、borrow_policy_id/fine_rule_id、借出时费率和封顶快照、reservation.item_id、审计和计数器表。属于详细设计对概念模型的细化，不改实验一文件。
- H2 不支持 PostgreSQL 部分索引：使用可空 active_item_id、active_reader_id、active_key 唯一列维护“一个副本一个活动借阅”“一个读者一张活动证”“同一标题一个活动预约”。历史行清空活动键，保留外键。

## 3. 分层类职责

| 层 | 类/接口 | 职责 |
| --- | --- | --- |
| presentation | ReaderController、LibrarianController、AdminController、ApiErrors | 参数验证、请求映射、HTTP 错误；不得调用 Repository |
| dto | Requests 的 record 请求及 Views 的响应记录 | 只承担输入输出，不暴露持久化对象 |
| application | ReaderService、CardService、CatalogService、CirculationService、ReservationService、QueryService、AdminService、AuthorizationService | 编排用例、授权、事务与审计 |
| domain | Reader/StudentReader/TeacherReader、BorrowCard、BookTitle、LibraryItem/Book/Magazine/Thesis、Loan、Reservation、FineRecord | 业务属性和状态规则 |
| domain | BorrowPolicyService、FineRuleService、FineService、DomainFactory | 选择可替换规则，检查借阅资格，计算罚款，构建读者和馆藏子类型 |
| domain repository | ReaderRepository、BorrowCardRepository、BookTitleRepository、LibraryItemRepository、LoanRepository、ReservationRepository、RuleRepository、AdminRepository | 定义业务持久化端口；LibraryRepository 组合这些端口 |
| infrastructure | JdbcLibraryRepository、schema.sql、data.sql | SQL 映射、唯一约束、锁、规则初始数据；不决定用户业务权限 |

## 4. 用例详细设计

下表的 DTO 均由 Requests/Views 定义；所有修改操作使用 Spring 事务并首先取得数据库 circulation_lock 单行锁，统一串行化教学规模的写入。锁跨连接生效，不用仅对单 JVM 有效的 synchronized。失败整体回滚，成功审计同事务提交；权限拒绝通过独立事务 AuditService 留痕。查询无写锁。

| 用例 | 入口 | Application / Domain Service | Entity/Aggregate | Repository | 请求 → 响应 | 异常与事务 |
| --- | --- | --- | --- | --- | --- | --- |
| 注册 FR-001 | ReaderController.register | ReaderService / DomainFactory | Reader | ReaderRepository | RegisterReader → ReaderView | identity 重复、非法类型；单事务 |
| 办证 FR-002/003 | AdminController.issueCard/deactivateCard | CardService / BorrowCard | Reader、BorrowCard | Reader/BorrowCard/Loan/Reservation | IssueCard(readerId,expiresAt) → CardView | 读者不存在、重复有效证、有效期非法、活动关联阻断；序号与新证原子提交 |
| 标题 FR-006/007 | AdminController.addTitle/removeTitle | CatalogService | BookTitle | BookTitleRepository | AddTitle → TitleView | 标准号重复、标题缺失、有活动副本不可停用；保存或逻辑停用 |
| 副本 FR-008/009 | AdminController.addItem/removeItem | CatalogService / DomainFactory | BookTitle、LibraryItem | BookTitle/LibraryItem/Loan | AddItem → ItemView | 条码重复、标题无效、活动借阅阻断；状态原子更新 |
| 借书 FR-011 | LibrarianController.borrow | CirculationService / BorrowPolicyService | Reader、BorrowCard、LibraryItem、Loan、Reservation | Reader/BorrowCard/Loan/LibraryItem/Reservation/Rule | BorrowBook(cardNo,barcode) → LoanView | INVALID_CARD、BORROW_LIMIT、OVERDUE_LOAN、ITEM_UNAVAILABLE；所有变化一个事务 |
| 还书 FR-012 | LibrarianController.returnBook | CirculationService / FineService、FineRuleService | Loan、LibraryItem、FineRecord、Reservation | LibraryItem/Loan/Reservation/Rule | ReturnBook(barcode) → ReturnView | ITEM_NOT_FOUND、LOAN_NOT_FOUND；归还、罚款、队首分配同时提交 |
| 预约 FR-014 | ReaderController.reserve/cancel | ReservationService | Reader、BorrowCard、BookTitle、Reservation | Reader/BorrowCard/BookTitle/LibraryItem/Reservation | ReserveBook(titleId) → ReservationView | READER_NOT_FOUND、TITLE_NOT_FOUND、DUPLICATE_RESERVATION、RESERVATION_LIMIT、ITEM_AVAILABLE；锁内分配单调序号 |
| 查询 FR-010/013 | ReaderController.search/loans | QueryService / Loan.isOverdue | BookTitle、LibraryItem、Loan | BookTitle/LibraryItem/Loan/Reader | keyword / readerId → 列表 | FORBIDDEN、READER_NOT_FOUND；读者仅本人，馆员/系统管理员全馆 |
| 罚款 FR-015 | 还书调用 / AdminController.adjustFine | FineService / FineRuleService | Loan、FineRule、FineRecord | Loan/Rule | 借出快照、日期 → 金额 | 未逾期零元，min(days*rate,cap)；减免必须有理由，不能增加金额 |
| 维护 FR-004/005/016 | AdminController | AdminService | Librarian、SystemAdmin、BorrowPolicy、FineRule | Admin/Rule | AddStaff/UpdatePolicy/UpdateFineRule → 配置 | FORBIDDEN、最后管理员、非法范围；追加新版本，旧规则失效 |

## 5. 核心算法和状态约束

借书顺序：授权 → 锁 → 有效证/读者 → 当前数量 >= 上限拒绝 → 逾期未还拒绝 → 标题/副本/参考资料资格 → 保留副本所属读者 → 保存期限、规则 ID 和费率快照 → 创建 Loan → 副本 ON_LOAN → 必要时完成预约。数量通过有效 Loan 查询，不保存可能漂移的计数。

还书不要求有效证：授权 → 锁 → 馆藏条码 → 唯一未归还 Loan → 依借出快照计算罚款 → RETURNED 并记录经办人 → 生成唯一 FineRecord（零金额也可追溯）→ 副本 AVAILABLE → 按 (created_at,id) 提升该标题队首，设置 READY、item_id、hold_until=today+3。每天维护任务及相关流通操作在同一锁下释放到期 READY，然后补位；领取截止日仍可领取，today > hold_until 才超时。

预约限额 5；仅无可借且可外借副本的标题允许排队；重复有效预约明确失败（遵循实验二要求）。取消只允许本人或系统管理员，释放保留副本后补位。排队仅向仍有有效证、有效账户的读者分配，失效读者预约转 EXPIRED。参考资料永不分配。

罚款：本地自然日差 max(0,returnDate-dueDate)，到期日零元，次日一天；小数保留两位。规则修改只影响新 Loan。初值和实验一 Q08/Q09/Q13 一致。

## 6. 模式与验证

Strategy：BorrowPolicy/FineRule 记录实现规则行为，由规则服务选择数据库当前版本。Factory：DomainFactory 根据分类创建 StudentReader/TeacherReader 和 Book/Magazine/Thesis。Repository：纯 Java 端口与 JDBC 适配器。MVC：HTTP controller + DTO view + domain model。Service Layer：每个场景事务集中在应用服务。

验证：领域规则单测、H2 关系约束/回滚/并发集成测试、MockMvc 权限/验证/异常测试，具体编号见 15。架构检查禁止 presentation 导入 repository/infrastructure，禁止 domain 导入 Spring/JPA。实验一文字与图示有差异时以本文件上述逐项映射和 02/03 的业务行为为准。

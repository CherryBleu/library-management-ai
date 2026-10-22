# REST API 规范

## 公共约定

前缀 /api，JSON 使用 camelCase。成功创建 201，查询/操作 200；业务冲突 409，不存在 404，权限不足 403，未提供身份 401，输入非法 400。错误体 {code,message}，服务端 500 不返回 SQL 或堆栈。除注册和健康检查外，请求头 X-Role=READER|LIBRARIAN|SYSTEM_ADMIN，X-Actor-Id 为正整数；角色与实际有效账户匹配，仅用于教学模拟，不是生产认证。

金额十进制，两位；日期 yyyy-MM-dd。身份 ID/条码/标准号重复返回 CONFLICT；请求字段通过 Bean Validation 检查。以下修改成功均写审计，失败不得部分落库。所有列表按 ID 或业务队列排序。

## 接口清单

| 方法/路径 | 请求参数 | 响应 | 权限 | 成功场景 | 失败场景 |
| --- | --- | --- | --- | --- | --- |
| POST /readers | identityNo,name,department,readerType,contact(可选) | ReaderView(id,name,readerType,status) | 匿名 | 唯一身份注册 | 400类型/空字段；409重复 |
| POST /admin/borrow-cards | readerId,expiresAt | CardView(id,cardNo,readerId,expiresAt,status) | SYSTEM_ADMIN | 为有效读者办证 | 404读者；409重复有效证；400日期 |
| DELETE /admin/borrow-cards/{id} | 路径id | {status} | SYSTEM_ADMIN | 逻辑停用 | 404；409未归还/预约/未结罚款 |
| POST /admin/librarians | name,employeeNo | {id} | SYSTEM_ADMIN | 新增馆员 | 409重复；403 |
| DELETE /admin/librarians/{id} | id | {status} | SYSTEM_ADMIN | 停用且保留历史 | 404；403 |
| POST /admin/system-admins | name,employeeNo | {id} | SYSTEM_ADMIN | 新增管理账户 | 409重复 |
| DELETE /admin/system-admins/{id} | id | {status} | SYSTEM_ADMIN | 保留至少一名有效管理员 | 409最后管理员 |
| POST /admin/book-titles | title,authors,standardNo(可选),publisher(可选),itemType | TitleView | SYSTEM_ADMIN | 新增有效书目 | 409标准号；400 |
| DELETE /admin/book-titles/{id} | id | {status} | SYSTEM_ADMIN | 停用无活动副本书目 | 409活动副本；404 |
| POST /admin/library-items | titleId,barcode,location,borrowable | ItemView | SYSTEM_ADMIN | 新增 AVAILABLE 副本 | 409条码；404标题 |
| DELETE /admin/library-items/{id} | id | {status} | SYSTEM_ADMIN | 副本 INACTIVE，保留历史 | 409借出/保留；404 |
| GET /books | keyword可选，最长100字 | TitleView[] (含copies) | 三种角色 | 标题/作者/标准号匹配，空查询全部 | 401/403/400 |
| POST /circulation/borrow | cardNo,barcode | LoanView | LIBRARIAN | 借出并计算到期日 | 409 INVALID_CARD/BORROW_LIMIT/OVERDUE_LOAN/ITEM_UNAVAILABLE；404 ITEM_NOT_FOUND |
| POST /circulation/return | barcode | ReturnView(loan,fine) | LIBRARIAN | 归还并结算、提升预约 | 404 ITEM_NOT_FOUND/LOAN_NOT_FOUND |
| GET /readers/{id}/loans | readerId | LoanView[] | 本人/馆员/系统管理员 | 当前和历史，实时派生逾期状态 | 403他人；404读者 |
| POST /reservations | titleId | ReservationView(id,readerId,titleId,queueNo,status,holdUntil,itemId) | READER | 按标题排队 | 404读者/标题；409无效证/重复/5条上限/可直接借 |
| DELETE /reservations/{id} | id | {status} | 本人或 SYSTEM_ADMIN | 取消，保留副本重新分配 | 404；403他人；409已完成 |
| POST /admin/reservations/expire | 无 | {status} | SYSTEM_ADMIN | 处理到期保留、提升后继 | 403 |
| PUT /admin/borrow-policies/{readerType} | maxBorrowCount,borrowDays | {status} | SYSTEM_ADMIN | 立即生成新版本 | 400负值/零/类型 |
| PUT /admin/fine-rules/{itemType} | dailyRate,maximumAmount | {status} | SYSTEM_ADMIN | 立即生成新版本 | 400负值/类型 |
| GET /admin/rules | 无 | {borrowPolicies,fineRules} | SYSTEM_ADMIN | 返回当前规则 | 403 |
| PATCH /admin/fines/{id} | amount,reason | {status} | SYSTEM_ADMIN | 减免到非负且不大于原金额，0为WAIVED | 404；400无原因/增加金额 |
| GET /admin/audit | 无 | 审计列表 | SYSTEM_ADMIN | 查看经办记录 | 403 |
| GET /health | 无 | {status:UP} | 匿名 | 服务存活 | 500 |

## 数据分类与请求示例

readerType：UNDERGRADUATE、GRADUATE、DOCTORAL、JUNIOR_COLLEGE、TEACHER。
itemType：CHINESE_BOOK、FOREIGN_BOOK、CHINESE_MAGAZINE、FOREIGN_MAGAZINE、THESIS。

注册：{"identityNo":"S001","name":"张三","department":"计算机学院","readerType":"UNDERGRADUATE","contact":null}
办证：{"readerId":1,"expiresAt":"2030-12-31"}
借书：{"cardNo":"CARD2026000001","barcode":"LIB0001"}
罚款规则：{"dailyRate":0.50,"maximumAmount":100.00}

LoanView：id,readerId,itemId,borrowedAt,dueAt,returnedAt,status；status 为 BORROWED/OVERDUE/RETURNED。
FineView：id,loanId,overdueDays,amount,status。
TitleView：id,title,authors,standardNo,itemType,status,copies；ItemView：id,titleId,barcode,location,status,borrowable。

## 应用服务边界

Controller 将 Actor 和 Requests 传入对应服务，服务返回 Views 或基础结果。CirculationService.borrow/returnBook 组织借还；ReservationService.reserve/cancel/expire 组织排队；QueryService.books/loans 仅查询。系统管理员身份不自动获得柜台借还权限。规则配置更新使用当前时间追加版本，借出 Loan 的快照不被更新。

# 领域模型

## 1. 建模范围与原则

本模型从业务概念和用例行为出发，不把数据库表或 Controller 直接当作领域类。领域对象负责维护不变量；应用服务负责编排跨聚合用例；Repository 仅作为持久化端口在架构层定义。领域模型中的状态名和规则名应与 `02-requirements.md`、`03-use-cases.md` 一致。

## 2. 分类总览

| 分类 | 类型 | 说明 |
| --- | --- | --- |
| 实体（Entity） | `Reader`、`BorrowCard`、`BookTitle`、`LibraryItem`、`Loan`、`Reservation`、`FineRecord`、`Librarian`、`SystemAdmin` | 有稳定身份和生命周期，需要保留历史关联 |
| 值对象（Value Object） | `ReaderType`、`ItemType`、`CardNo`、`Barcode`、`LoanStatus`、`ReservationStatus`、`Money`、`DateRange`、`RuleVersion` | 由值定义相等性，不单独承担生命周期 |
| 策略/规则对象（Policy/Strategy） | `BorrowPolicy`、`FineRule` | 封装可配置的借阅上限、期限和罚款计算，支持版本化 |
| 领域服务（Domain Service） | `BorrowingService`、`ReturnService`、`ReservationQueueService`、`FineCalculationService` | 协调多个实体/策略且自身无明显持久身份 |

## 3. 核心实体

### 3.1 Reader（读者）

- **职责**：表示可注册的读者及其分类，持有借阅证关联，维护借阅资格和有效借阅统计。
- **关键属性**：`readerId`、`name`、`departmentOrOrganization`、`contact`、`readerType: ReaderType`、`status`、`registeredAt`。
- **关键行为**：`register()`、`changeType()`、`canBorrow(policy, currentLoans)`、`hasOverdueLoans()`、`activeLoanCount()`。
- **约束**：`readerId` 唯一；停用读者不能借书或预约；借阅资格由有效 BorrowCard 和 BorrowPolicy 共同决定。
- **关系**：一名 Reader 至多拥有一张有效 BorrowCard；拥有多条 Loan 和 Reservation；按类型泛化为 StudentReader 或 TeacherReader。

### 3.2 StudentReader（学生读者）

- **职责**：Reader 的学生分类，携带本科生、研究生、博士生或专科生等 `studentLevel`。
- **关键属性**：继承 Reader；`studentLevel`。
- **关键行为**：使用对应 `BorrowPolicy`，不在类中硬编码数量和期限。
- **约束**：`studentLevel` 必须是受支持的学生类型。
- **关系**：泛化自 Reader；与 BorrowPolicy 按读者类型关联。

### 3.3 TeacherReader（教师读者）

- **职责**：Reader 的教师分类。
- **关键属性**：继承 Reader；可选职称/单位属性。
- **关键行为**：使用教师 `BorrowPolicy`。
- **约束**：教师读者必须绑定教师类型策略。
- **关系**：泛化自 Reader；与 BorrowPolicy 按读者类型关联。

### 3.4 BorrowCard（借阅证）

- **职责**：作为图书馆授权借阅和预约的凭证，表达有效期和状态。
- **关键属性**：`cardNo: CardNo`、`readerId`、`holderName`、`departmentOrOrganization`、`status`（ACTIVE/INACTIVE/EXPIRED）、`issuedAt`、`expiresAt`。
- **关键行为**：`activate()`、`deactivate()`、`isValid(at)`、`assertCanBorrow()`。
- **约束**：`cardNo` 全局唯一；同一 Reader 同时最多一张有效证；停用/过期证不能产生 Loan 或 Reservation。
- **关系**：属于 Reader；被 Loan 和 Reservation 引用其身份快照或 ID。

### 3.5 BookTitle（图书标题/书目）

- **职责**：描述可拥有多个馆藏副本的书目元数据。
- **关键属性**：`titleId`、`title`、`authors`、`isbnOrStandardNo`、`publisher`、`language`、`itemType: ItemType`、`status`（ACTIVE/INACTIVE）。
- **关键行为**：`addItem()`、`deactivate()`、`isReservable()`、`availableItemCount()`。
- **约束**：标准号（若有）在有效标题中唯一；有历史关联时不能物理删除；停用标题不能新增借阅。
- **关系**：聚合多个 LibraryItem；被 Reservation 引用；LibraryItem 的类型由标题或副本快照确定。

### 3.6 LibraryItem（馆藏借出物）

- **职责**：表示图书馆实际持有的、可独立借还的资源副本。
- **关键属性**：`itemId`、`barcode: Barcode`、`titleId`、`itemType: ItemType`、`location`、`status`（AVAILABLE/ON_LOAN/RESERVED/INACTIVE）、`isOwnedByLibrary`。
- **关键行为**：`markBorrowed()`、`markReturned()`、`reserveFor(reader)`、`deactivate()`、`canBeBorrowed()`。
- **约束**：`barcode` 全局唯一；非本馆、停用或已借出副本不可借；状态转换必须遵循状态机。
- **关系**：属于 BookTitle；一段时间内最多一条有效 Loan；可被 ReservationQueueService 分配。

### 3.7 Book（图书）

- **职责**：LibraryItem 的图书分类，可表达中文/外文图书的业务分类。
- **关键属性**：继承 LibraryItem；可选 `edition`、`volume`。
- **关键行为**：继承并实现图书可借规则。
- **约束**：具体借阅期限和罚款不硬编码在 Book 中，由 ItemType 与 FineRule 决定。
- **关系**：泛化自 LibraryItem。

### 3.8 Magazine（杂志）

- **职责**：LibraryItem 的杂志分类，覆盖中文/外文杂志。
- **关键属性**：继承 LibraryItem；`issue`、`publicationDate`。
- **关键行为**：继承借还状态行为。
- **约束**：是否可借由馆藏属性和规则决定。
- **关系**：泛化自 LibraryItem。

### 3.9 Thesis（论文）

- **职责**：LibraryItem 的论文分类。
- **关键属性**：继承 LibraryItem；`author`、`institution`、`defenseYear`。
- **关键行为**：继承借还状态行为。
- **约束**：仅供馆内阅览的论文应以不可借状态表示。
- **关系**：泛化自 LibraryItem。

### 3.10 Loan（借阅记录）

- **职责**：记录一次副本从借出到归还的完整生命周期。
- **关键属性**：`loanId`、`readerId`、`cardNo`、`itemId`、`borrowedAt`、`dueAt`、`returnedAt`、`status`（BORROWED/RETURNED/OVERDUE）、`librarianId`、`borrowPolicySnapshot`、`fineRecordId`。
- **关键行为**：`create()`、`markOverdue(at)`、`returnAt(at, librarian)`、`isOverdue(at)`、`attachFine()`。
- **约束**：同一副本同时最多一条 BORROWED/OVERDUE；`returnedAt` 只能从空变为有效时间一次；借出时保存期限和规则版本快照。
- **关系**：关联 Reader、BorrowCard、LibraryItem、Librarian；可关联一个 FineRecord。

### 3.11 Reservation（预约）

- **职责**：表达读者对标题的排队预约和领取状态。
- **关键属性**：`reservationId`、`readerId`、`cardNo`、`titleId`、`queuePosition`、`createdAt`、`status`（WAITING/READY/COLLECTED/CANCELLED/EXPIRED）、`expiresAt`、`assignedItemId`。
- **关键行为**：`enqueue()`、`markReady(item, at)`、`collect()`、`cancel()`、`expire()`。
- **约束**：同一 Reader 对同一标题最多一个有效预约；队列位置按创建时间稳定排序；只有有效证件可新建预约。
- **关系**：关联 Reader、BorrowCard、BookTitle；可被 LibraryItem 分配。

### 3.12 FineRecord（罚款记录）

- **职责**：保存一次借阅超期的计算结果和可追溯依据。
- **关键属性**：`fineId`、`loanId`、`overdueDays`、`amount: Money`、`ruleVersion`、`rateSnapshot`、`capSnapshot`、`status`（OUTSTANDING/WAIVED/PAID/VOID）、`calculatedAt`、`adjustmentReason`。
- **关键行为**：`calculate()`、`waive(reason, admin)`、`adjust(reason, admin)`、`markPaid()`。
- **约束**：计算使用 Loan 的借出物类型和规则快照；金额不可为负；调整必须有授权管理员和原因。
- **关系**：属于 Loan 的罚款结果；与 FineRule 通过版本快照关联。

### 3.13 Librarian（图书管理员）

- **职责**：作为代理执行借书和还书并留下经办身份。
- **关键属性**：`librarianId`、`name`、`status`、`role = LIBRARIAN`。
- **关键行为**：`authorizeCirculation()`、`recordOperation()`。
- **约束**：停用管理员不能办理流通操作；不能维护系统规则和管理员账户。
- **关系**：经办 Loan、归还和相关审计记录。

### 3.14 SystemAdmin（系统管理员）

- **职责**：维护借阅证、管理员、书目、馆藏和规则。
- **关键属性**：`adminId`、`name`、`status`、`role = SYSTEM_ADMIN`。
- **关键行为**：`maintainCard()`、`maintainCatalog()`、`maintainPolicy()`、`maintainFineRule()`、`manageLibrarian()`。
- **约束**：只有有效 SystemAdmin 能执行管理操作；最后一个有效系统管理员不得被停用或删除。
- **关系**：维护 BorrowCard、BookTitle、LibraryItem、BorrowPolicy 和 FineRule。

## 4. 值对象

### ReaderType

枚举值包括 `UNDERGRADUATE`、`GRADUATE`、`DOCTORAL`、`JUNIOR_COLLEGE`、`TEACHER`。决定选择哪一条 BorrowPolicy，不保存可变规则本身。

### ItemType

枚举值至少包括 `CHINESE_BOOK`、`FOREIGN_BOOK`、`CHINESE_MAGAZINE`、`FOREIGN_MAGAZINE`、`THESIS`。用于选择 FineRule 和可借属性。

### CardNo 与 Barcode

封装格式校验、规范化和唯一性边界；不把生成计数器放进实体业务行为。

### Money

封装金额和币种，禁止负金额，提供加法、比较和封顶操作。

### DateRange、RuleVersion、状态枚举

封装有效期、规则生效区间、Loan/Reservation/LibraryItem 状态转换所需的合法值。

## 5. 策略与规则对象

### BorrowPolicy（借阅策略）

- **职责**：按 `ReaderType` 定义最大同时借阅数、借阅期限、可借资源限制和版本/生效时间。
- **关键属性**：`policyId`、`readerType`、`maxActiveLoans`、`loanPeriodDays`、`effectiveFrom`、`effectiveTo`、`version`、`status`。
- **关键行为**：`allows(activeLoanCount)`、`dueAt(borrowedAt)`、`isEffective(at)`。
- **约束**：上限和期限必须为正；同一读者类型的有效时间区间不能重叠；历史 Loan 使用快照。
- **初始配置**：本科生 5 本/30 个自然日、研究生 10 本/60 个自然日、博士生 15 本/90 个自然日、专科生 5 本/30 个自然日、教师 20 本/90 个自然日；借出当天不计为完整借阅日。
- **类型**：策略实体/可版本化领域规则，不是 Reader 的属性表机械翻译。

### FineRule（罚款规则）

- **职责**：按 `ItemType` 将超期天数转换为罚款金额。
- **关键属性**：`ruleId`、`itemType`、`dailyRate: Money`、`maximumAmount: Money`、`effectiveFrom`、`effectiveTo`、`version`、`status`。
- **关键行为**：`calculate(overdueDays)`、`isEffective(at)`。
- **约束**：费率和封顶金额不可为负；金额为 `min(overdueDays * dailyRate, maximumAmount)`；规则版本必须可追溯。
- **初始配置**：中文图书 0.50 元/日封顶 100 元、外文图书 1.00 元/日封顶 200 元、中文杂志 1.00 元/日封顶 100 元、外文杂志 2.00 元/日封顶 200 元、论文 2.00 元/日封顶 300 元；归还当天不额外计费。
- **类型**：策略实体/领域服务输入。

## 6. 领域服务

### BorrowingService

组织 UC-011：验证 BorrowCard、Reader、BorrowPolicy 和 LibraryItem，创建 Loan，并以事务要求协调副本状态。它不取代实体的不变量。

### ReturnService

组织 UC-012：验证馆藏归属和有效 Loan，调用 FineCalculationService，完成 Loan 归还、FineRecord 关联和预约分配。

### FineCalculationService

选择与 ItemType 和日期匹配的 FineRule，返回可复现的 FineRecord 计算结果；不负责支付。

### ReservationQueueService

按创建时间维护标题预约队列，处理入队、取消、过期和副本归还后的队首分配，保证队列位置不重复。

### AuthorizationService（领域边界外的应用授权协作）

在应用服务入口检查角色和资源范围；领域对象仍负责其业务不变量。它不把权限判断散落在 Controller。

## 7. 聚合与一致性边界

- **Reader 聚合**：Reader、有效 BorrowCard 的关联和读者资格摘要。BorrowCard 可作为独立实体持久化，但办证/停用通过聚合规则完成。
- **Catalog 聚合**：BookTitle 及其 LibraryItem 集合；标题停用、添加副本和副本状态约束在此边界内协调。
- **Loan 聚合**：Loan 及其规则快照、FineRecord 关联；借还状态转换和罚款依据必须一致提交。
- **Reservation 聚合**：同一标题的 Reservation 队列；队列位置和状态转换由 ReservationQueueService 协调。
- **Rule 聚合**：BorrowPolicy、FineRule 的版本和生效区间；规则修改不回写历史 Loan。

跨聚合的借还操作由应用服务开启事务并调用领域服务，避免实体之间直接持久化耦合。

## 8. 关键不变量汇总

1. 有效 BorrowCard、Reader 状态和 Librarian 权限同时满足才可借书。
2. active loan 数量不得超过 BorrowPolicy 上限，且存在超期未还时禁止新借。
3. LibraryItem 必须属于本馆、标题有效且状态为 AVAILABLE 才可借。
4. 一个副本不能同时拥有两条有效 Loan。
5. 归还只能关闭一条未结清 Loan；非本馆条码不能创建归还记录。
6. FineRule 和 BorrowPolicy 按版本生效，历史 Loan 使用快照。
7. Reservation 队列按创建时间排序，同一读者同一标题只有一个有效预约，每名读者最多 5 个有效预约；队首保留 3 个自然日，超时自动转下一位。
8. 有历史关联的实体采用停用/逻辑删除，禁止破坏可追溯性。

## 9. 与需求和用例的追踪

- FR-001/002/003 -> Reader、BorrowCard、Reader 聚合。
- FR-004/005 -> Librarian、SystemAdmin 和授权协作。
- FR-006/007/008/009/010 -> BookTitle、LibraryItem、Book/Magazine/Thesis。
- FR-011/012/013 -> Loan、BorrowingService、ReturnService。
- FR-014 -> Reservation、ReservationQueueService。
- FR-015 -> FineRule、FineRecord、FineCalculationService。
- FR-016 -> BorrowPolicy、FineRule、Rule 聚合。

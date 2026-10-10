# 人工审查清单

## 需求规格

- [ ] `FR-001` 至 `FR-016` 连续且每条有验收标准。
- [ ] Q01-Q28 的决定已反映到需求，不再以“待确认”掩盖已定规则。
- [ ] 借阅证有效性、借阅数量、30/60/90 天期限和超期阻断均有规则。
- [ ] 借出物类型、罚款费率/封顶、自然日计算和罚款调整审计均有规则。
- [ ] 读者只能查本人记录，管理员权限边界清晰。

## 用例文本与用例图

- [ ] 每个 `UC-001` 至 `UC-016` 有参与者、前置/后置条件、基本/备选/异常流和需求引用。
- [ ] 借书包含借阅证、数量、逾期、馆藏和并发失败分支。
- [ ] 还书包含本馆验证、有效 Loan、自然日罚款和预约分配分支。
- [ ] 预约包含唯一有效预约、5 条上限、队列和 3 日保留超时。
- [ ] 用例图包含 Reader、Student、Teacher、Librarian、SystemAdmin 及泛化关系。
- [ ] PlantUML 文件有 `@startuml`/`@enduml`，include/extend 关系与文字一致。

## 领域模型与类图

- [ ] Reader、StudentReader、TeacherReader、LibraryItem、Book、Magazine、Thesis 均存在。
- [ ] BorrowCard、Loan、Reservation、BorrowPolicy、FineRule、FineRecord 均存在。
- [ ] 实体、值对象、策略和领域服务分类清楚。
- [ ] BorrowPolicy/FineRule 版本化，Loan 保存规则快照。
- [ ] 类图不含 Controller、Repository、DTO 等架构实现类。

## 架构与包图

- [ ] 具备 Presentation/Controller、Application、Domain、Infrastructure、DTO、Test 分层。
- [ ] Controller 不直接依赖 Repository；Infrastructure 实现 Repository 抽象。
- [ ] 权限在应用服务入口校验，业务规则不写入 Controller。
- [ ] 借还事务、异常回滚、并发锁和审计策略明确。
- [ ] Strategy、Factory、Repository、Service Layer 和 MVC 的使用位置合理。

## 数据库设计

- [ ] `borrow_cards.card_no` 和 `library_items.barcode` 全局唯一。
- [ ] `loans` 能区分 BORROWED、RETURNED、OVERDUE，且有效借阅部分唯一。
- [ ] `reservations` 能表示 queue_no、READY、hold_until 和超时转移。
- [ ] `borrow_policies` 支持读者类型、上限、期限和版本。
- [ ] `fine_rules` 支持借出物类型、费率、封顶和版本。
- [ ] 读者、借阅证、标题、副本、借阅、预约和审计外键完整。
- [ ] 停用/逻辑删除策略不破坏历史关联。

## Agent 与提交审查

- [ ] `19-ai-usage-log.md` 完整记录需求分析、UML 建模、架构设计、详细设计、编码实现、测试生成六阶段。
- [ ] 暂未执行的实验二阶段明确标记为未执行，没有虚构源码或测试结果。
- [ ] 每份文档的 Git commit message 能说明其变更目的。
- [ ] 人工审查了对应 `git diff`，并确认没有越权修改其他文件。

## 当前结论

实验一规格内容可建立 baseline；PlantUML 渲染和可执行代码测试需在工具和实验二源码具备后完成，不能在当前阶段宣称通过。


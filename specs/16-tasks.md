# 实验二实现任务

共同输入：constitution、02、03、05、07、09、10~12、13、14、15。代码路径均相对 src/main/java/com/example/library/，测试文件位于 src/test/java/com/example/library/，资源位于 src/main/resources/。按序执行，先说明计划，每项完成检查后单独commit/push。

基线后禁止修改 specs/00~18、constitution 及实验一模型；19为执行记录可追加。每个任务不得修改白名单外文件。必要设计变更必须单独记录并审查，不能静默回写基线。

## TASK-001 初始化项目

- 目标：初始化项目。
- 输入规格：共同输入和前序产物。
- 允许修改：pom.xml、.gitignore、LibraryApplication.java、application.properties、StartupTest.java。
- 不允许修改：基线规格及本任务白名单外文件。
- 实现要求：只有结构和启动，无业务。
- 验收标准：编译通过，启动测试通过，diff范围正确。
- 建议测试：mvn test，优先运行启动测试相关测试。

## TASK-002 领域实体

- 目标：领域实体。
- 输入规格：共同输入和前序产物。
- 允许修改：domain/（repository和规则服务除外）。
- 不允许修改：基线规格及本任务白名单外文件。
- 实现要求：实体、分类、状态、Factory，纯Java。
- 验收标准：编译通过，编译通过，diff范围正确。
- 建议测试：mvn test，优先运行编译相关测试。

## TASK-003 Repository

- 目标：Repository。
- 输入规格：共同输入和前序产物。
- 允许修改：domain/repository/、infrastructure/、schema.sql、data.sql、RepositoryTest.java。
- 不允许修改：基线规格及本任务白名单外文件。
- 实现要求：关系表、锁、唯一约束、快照映射。
- 验收标准：编译通过，TC-036通过，diff范围正确。
- 建议测试：mvn test，优先运行TC-036相关测试。

## TASK-004 借阅策略

- 目标：借阅策略。
- 输入规格：共同输入和前序产物。
- 允许修改：domain/circulation/BorrowPolicyService.java、BorrowPolicyTest.java。
- 不允许修改：基线规格及本任务白名单外文件。
- 实现要求：五类规则、上限及逾期。
- 验收标准：编译通过，TC-034通过，diff范围正确。
- 建议测试：mvn test，优先运行TC-034相关测试。

## TASK-005 罚款策略

- 目标：罚款策略。
- 输入规格：共同输入和前序产物。
- 允许修改：domain/fine/FineRuleService.java、FineService.java、FineRuleTest.java。
- 不允许修改：基线规格及本任务白名单外文件。
- 实现要求：BigDecimal、封顶、非正天零元。
- 验收标准：编译通过，TC-034通过，diff范围正确。
- 建议测试：mvn test，优先运行TC-034相关测试。

## TASK-006 读者和借阅证

- 目标：读者和借阅证。
- 输入规格：共同输入和前序产物。
- 允许修改：application/ReaderService.java、CardService.java、AuthorizationService.java、AuditService.java、dto/、presentation/ReaderController.java、AdminController.java、ApiErrors.java、CardTest.java。
- 不允许修改：基线规格及本任务白名单外文件。
- 实现要求：注册办证、模拟身份校验、DTO和异常映射。
- 验收标准：编译通过，TC-001/002/014/015/017通过，diff范围正确。
- 建议测试：mvn test，优先运行TC-001/002/014/015/017相关测试。

## TASK-007 目录管理

- 目标：目录管理。
- 输入规格：共同输入和前序产物。
- 允许修改：application/CatalogService.java、presentation/AdminController.java、CatalogTest.java。
- 不允许修改：基线规格及本任务白名单外文件。
- 实现要求：标题、副本、删除保护。
- 验收标准：编译通过，TC-018/027通过，diff范围正确。
- 建议测试：mvn test，优先运行TC-018/027相关测试。

## TASK-008 借书

- 目标：借书。
- 输入规格：共同输入和前序产物。
- 允许修改：application/CirculationService.java、presentation/LibrarianController.java、BorrowTest.java。
- 不允许修改：基线规格及本任务白名单外文件。
- 实现要求：验证资格、创建借阅和快照、保留副本所属检查。
- 验收标准：编译通过，TC-003~007通过，diff范围正确。
- 建议测试：mvn test，优先运行TC-003~007相关测试。

## TASK-009 还书

- 目标：还书。
- 输入规格：共同输入和前序产物。
- 允许修改：application/CirculationService.java、application/ReservationService.java（队列协作）、ReturnTest.java。
- 不允许修改：基线规格及本任务白名单外文件。
- 实现要求：归还、罚款、队首分配。
- 验收标准：编译通过，TC-008~010/019/025通过，diff范围正确。
- 建议测试：mvn test，优先运行TC-008~010/019/025相关测试。

## TASK-010 预约

- 目标：预约。
- 输入规格：共同输入和前序产物。
- 允许修改：application/ReservationService.java、ReservationScheduler.java、presentation/ReaderController.java、AdminController.java、ReservationTest.java。
- 不允许修改：基线规格及本任务白名单外文件。
- 实现要求：排队、重复、限额、取消、超时。
- 验收标准：编译通过，TC-012/013/016/022~024/033通过，diff范围正确。
- 建议测试：mvn test，优先运行TC-012/013/016/022~024/033相关测试。

## TASK-011 查询

- 目标：查询。
- 输入规格：共同输入和前序产物。
- 允许修改：application/QueryService.java、presentation/ReaderController.java、QueryTest.java。
- 不允许修改：基线规格及本任务白名单外文件。
- 实现要求：目录、历史借阅、所有权。
- 验收标准：编译通过，TC-011/020/021通过，diff范围正确。
- 建议测试：mvn test，优先运行TC-011/020/021相关测试。

## TASK-012 管理员和规则

- 目标：管理员和规则。
- 输入规格：共同输入和前序产物。
- 允许修改：application/AdminService.java、presentation/AdminController.java、AdminTest.java。
- 不允许修改：基线规格及本任务白名单外文件。
- 实现要求：管理员保护、规则版本、罚款减免、审计。
- 验收标准：编译通过，TC-014/025~029/032通过，diff范围正确。
- 建议测试：mvn test，优先运行TC-014/025~029/032相关测试。

## TASK-013 完善测试

- 目标：完善测试。
- 输入规格：共同输入和前序产物。
- 允许修改：src/test/、必要的最小源码修复。
- 不允许修改：基线规格及本任务白名单外文件。
- 实现要求：并发/回滚/架构/接口验证，修复须记录。
- 验收标准：编译通过，TC-030/031/035及全部测试通过，diff范围正确。
- 建议测试：mvn test，优先运行TC-030/031/035及全部测试相关测试。

## TASK-014 README和复盘

- 目标：README和复盘。
- 输入规格：共同输入和前序产物。
- 允许修改：README.md、specs/19-ai-usage-log.md、review/experiment2-results.md。
- 不允许修改：基线规格及本任务白名单外文件。
- 实现要求：真实命令、接口、六阶段记录和证据。
- 验收标准：编译通过，启动和HTTP验收通过，diff范围正确。
- 建议测试：mvn test，优先运行启动和HTTP验收相关测试。


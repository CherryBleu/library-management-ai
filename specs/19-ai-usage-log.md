# AI 使用记录

本记录按提示词手册第 20 条的六个阶段编写。实验一只完成架构设计与 Spec 建模；详细设计、编码实现和自动化测试生成阶段暂不展开。

## 第 1 次使用

### 使用工具

Codex

### 使用阶段

需求分析

### 输入 Prompt 摘要

阅读实验一文档和提示词手册，识别图书管理系统的参与者、业务范围、约束和实验一交付清单，并整理为可独立审查的规格任务。

### Agent 修改文件

- `specs/00-project-brief.md`
- `specs/01-clarifying-questions.md`
- `specs/02-requirements.md`
- `specs/03-use-cases.md`
- `specs/05-domain-model.md`
- `specs/16-tasks.md`

### 输出摘要

形成项目背景、目标用户、借阅和预约业务、读者类型与借出物类型；提出并回答身份认证、借阅证、借阅上限、期限、罚款、预约排队、代理借还和角色权限等澄清问题；建立带 `FR-XXX`、`BR-XXX` 编号和验收标准的需求规格，并将需求映射到用例和领域对象；将实验一及后续实验二工作拆分为可独立审查的 `TASK-001` 至 `TASK-018`。

### 人工审查结果

检查需求是否覆盖实验一原始 13 项业务要求，确认借阅必须验证借阅证、借阅数量和逾期未还记录，确认管理员与图书管理员职责分离；随后完成 Q01-Q28 的人工澄清，并将具体规则同步到需求、用例、领域模型和数据库设计。

### 测试结果

通过人工交叉检查：每个核心需求具有验收标准，核心用例具有需求编号引用；未执行代码测试。

### Git 提交

`add requirements and domain specifications`

## 第 2 次使用

### 使用工具

Codex

### 使用阶段

UML 建模

### 输入 Prompt 摘要

根据需求和用例规格生成 PlantUML 用例图与领域类图，表达 Reader、Student、Teacher、Librarian、SystemAdmin、LibraryItem 层次，以及借阅证、借阅、预约、借阅策略和罚款策略之间的业务关系。

### Agent 修改文件

- `specs/04-use-case-model.puml`
- `specs/06-domain-class-diagram.puml`

### 输出摘要

用例图覆盖注册、查询、预约、借阅、归还、借阅证办理、图书维护、管理员维护和规则维护，并使用泛化及 `include`/`extend` 表达关系；领域类图表达读者和馆藏物继承、借阅证/借阅/预约聚合，以及策略对象对借阅和罚款的作用。

### 人工审查结果

检查 UML 元素与需求编号和用例文字一致，确认领域类图未混入 Controller、Repository、DTO 等架构类，确认借阅和归还的验证关系没有被省略。

### 测试结果

通过 PlantUML 文本结构检查：两个文件均包含 `@startuml`、`@enduml`，关系使用方向明确；若本地未安装 PlantUML，则需在提交前用 PlantUML 渲染器复核图形布局。

### Git 提交

`add use case and domain UML models`

## 第 3 次使用

### 使用工具

Codex

### 使用阶段

架构设计

### 输入 Prompt 摘要

依据需求、领域模型和开发宪章设计分层架构与 MVC 包结构，明确模块、权限、异常、事务边界、设计模式和依赖方向；生成架构说明与 PlantUML 包图。

### Agent 修改文件

- `specs/constitution.md`
- `specs/07-architecture.md`
- `specs/08-package-diagram.puml`

### 输出摘要

建立 Presentation、Application、Domain、Infrastructure 和 Test 分层；划分 reader、card、catalog、circulation、reservation、fine、admin 模块；将借阅和罚款规则放入策略对象，规定 Controller 只能依赖 Application Service，Infrastructure 实现 Repository 抽象，并说明权限、事务和异常处理。

### 人工审查结果

确认架构满足实验一的分层和 MVC 要求，确认 Controller 不直接访问 Repository，确认借阅、归还、预约和罚款计算具有事务边界与权限约束；补充了 Agent 生成内容必须人工复核的宪章条款。

### 测试结果

通过包名、依赖方向和架构约束的人工检查；PlantUML 包图完成文本语法检查，未执行运行时代码测试。

### Git 提交

`add layered architecture and package diagram`

## 第 4 次使用

### 使用工具

Codex

### 使用阶段

详细设计

### 输入 Prompt 摘要

按提示词手册中的详细设计阶段，原计划根据需求、用例、领域模型、架构和数据库设计生成设计模型、时序图和 API 规格。

### Agent 修改文件

无。本阶段按用户要求暂不处理实验二详细设计。

### 输出摘要

仅保留后续可拆分的任务边界：设计模型、借书时序图、还书时序图、预约时序图和 API 规格应分别作为独立任务，输入、允许修改文件和验收标准需在 `16-tasks.md` 中明确后再执行。

### 人工审查结果

确认没有为了填充实验一而虚构详细设计产出，实验一交付范围与实验二范围保持分离。

### 测试结果

不适用；本阶段没有生成文件或代码。

### Git 提交

无

## 第 5 次使用

### 使用工具

Codex

### 使用阶段

编码实现

### 输入 Prompt 摘要

按提示词手册的编码任务模板，原计划读取基线规格并实现一个明确的 `TASK-XXX`。

### Agent 修改文件

无。本阶段按用户要求暂不实现代码。

### 输出摘要

确认实验一只提交规格和模型，不创建未经设计模型约束的源码、数据库迁移或接口实现；后续编码必须以冻结后的规格和独立任务为输入。

### 人工审查结果

确认仓库没有因实验一要求而引入未定义的技术栈或伪实现。

### 测试结果

不适用；本阶段没有代码。

### Git 提交

无

## 第 6 次使用

### 使用工具

Codex

### 使用阶段

测试生成

### 输入 Prompt 摘要

按提示词手册的测试计划阶段，对实验一规格执行文档级验收：检查需求覆盖、用例映射、UML 文本结构、架构依赖、数据库约束、风险和人工审查清单。

### Agent 修改文件

- `specs/13-database-design.md`
- `specs/17-risk-analysis.md`
- `specs/18-review-checklist.md`
- `specs/19-ai-usage-log.md`

### 输出摘要

建立与领域模型一致的关系数据库设计，明确主键、外键、唯一性、状态和索引；整理需求、建模、架构、数据库、权限和 Agent 使用风险；生成可逐项勾选的人工审查清单，并记录本次六阶段工作和验证结果。

### 人工审查结果

确认 `borrow_cards.card_no`、馆藏条码、借阅状态、预约队列、借阅策略和罚款规则均有数据约束或说明；确认审查清单覆盖实验一提交物，并保留 PlantUML 渲染和后续代码测试作为提交前/实验二阶段检查项。

### 测试结果

文档级检查通过；所有必交文件存在，Markdown 标题和编号完整，PlantUML 文件具有成对的起止标记。实验一不包含可执行源码，因此未报告代码单元测试结果。

### Git 提交

`complete experiment 1 specs and review records`

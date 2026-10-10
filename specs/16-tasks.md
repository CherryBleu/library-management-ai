# Agent 任务拆分

本文件将实验一规格工作和后续实验二工作拆分为可由 Agent 一次完成、一次审查的小任务。实验一完成后建立 baseline；baseline 之后不得由 Agent 擅自修改规格。

## 实验一任务

| 编号 | 目标 | 输入规格 | 允许修改 | 不允许修改 | 验收标准 | 建议测试 |
| --- | --- | --- | --- | --- | --- | --- |
| TASK-001 | 编写项目简述 | 实验一文档 | `specs/00-project-brief.md` | 其他规格、源码 | 目标、角色、业务范围、类型和暂不实现范围齐全 | Markdown 结构检查 |
| TASK-002 | 提出并整理澄清问题 | `00-project-brief.md` | `specs/01-clarifying-questions.md` | 其他规格 | 20~30 个分类问题，末尾有人工回答区 | 问题数量和分类检查 |
| TASK-003 | 建立开发宪章 | `00-project-brief.md`、`01-clarifying-questions.md` | `specs/constitution.md` | 其他规格、源码 | 包含 UML-as-Spec、分层 MVC、权限、审查和测试约束 | 规则逐项检查 |
| TASK-004 | 生成需求规格 | `00-project-brief.md`、`01-clarifying-questions.md`、宪章 | `specs/02-requirements.md` | 其他规格、源码 | FR、NFR、BR 编号连续，每个 FR 有验收标准 | 编号和追踪检查 |
| TASK-005 | 生成用例文本 | `02-requirements.md` | `specs/03-use-cases.md` | 其他规格、源码 | 参与者、UC 编号、前后置条件、主/备选/异常流齐全，五个重点用例详细 | 需求覆盖检查 |
| TASK-006 | 生成用例图 | `03-use-cases.md` | `specs/04-use-case-model.puml` | 其他规格、源码 | 包含五类参与者、泛化、include/extend 和核心用例 | PlantUML 语法/渲染检查 |
| TASK-007 | 生成领域模型 | `02-requirements.md`、`03-use-cases.md` | `specs/05-domain-model.md` | 其他规格、源码 | 实体、值对象、策略、服务及聚合不变量清晰 | 领域概念人工审查 |
| TASK-008 | 生成领域类图 | `05-domain-model.md` | `specs/06-domain-class-diagram.puml` | 其他规格、源码 | 继承、聚合、关联和策略作用一致，不含技术设计类 | PlantUML 语法/关系检查 |
| TASK-009 | 生成架构说明 | `02-requirements.md`、`05-domain-model.md`、宪章 | `specs/07-architecture.md` | 其他规格、源码 | 分层 MVC、模块、权限、异常、事务和模式完整 | 架构约束清单 |
| TASK-010 | 生成包图 | `07-architecture.md` | `specs/08-package-diagram.puml` | 其他规格、源码 | 包和依赖方向正确，Controller 不直连 Repository | PlantUML 语法/依赖检查 |
| TASK-011 | 设计数据库 | `05-domain-model.md`、`07-architecture.md` | `specs/13-database-design.md` | 其他规格、源码 | 必需表、字段、主外键、唯一约束、状态、索引和继承映射完整 | 字段与领域模型交叉检查 |
| TASK-012 | 分析风险与生成审查清单 | 全部实验一规格 | `specs/17-risk-analysis.md`、`specs/18-review-checklist.md` | 其他规格、源码 | 风险覆盖需求/模型/架构/数据/权限/Agent；审查项可人工判断 | 清单逐项勾选 |
| TASK-013 | 记录 AI 使用过程 | 提示词手册第 20 条、所有任务结果 | `specs/19-ai-usage-log.md` | 其他规格、源码 | 完整六阶段，每阶段包含八个模板字段，未执行阶段明确说明 | 字段和阶段完整性检查 |

## Baseline 后的后续任务（实验二，暂不执行）

| 编号 | 目标 | 允许修改 | 验收标准 |
| --- | --- | --- | --- |
| TASK-014 | 详细设计模型和三张时序图 | `09-design-model.md`、`10~12-*.puml` | 每个核心用例有 Controller、Service、Domain、Repository、DTO、事务和异常路径 |
| TASK-015 | API 规格与测试计划 | `14-api-spec.md`、`15-test-plan.md` | 接口和测试覆盖注册、办证、查询、借还、预约、维护及权限失败 |
| TASK-016 | 生成实现任务并逐项编码 | `16-tasks.md` 指定的源码文件 | 单次只实现一个 TASK，遵守规格基线并通过对应自动化测试 |
| TASK-017 | 实现核心领域与应用服务 | 仅任务指定源码文件 | 借还、预约、罚款和权限规则可执行，事务失败整体回滚 |
| TASK-018 | 运行测试并修复最小问题 | 任务指定源码和测试文件 | 单元/集成测试通过，失败原因和修复记录在 AI 使用日志 |

## 执行约束

1. 每个任务开始前读取其输入规格，先输出计划，再修改允许文件。
2. 任务完成后必须运行对应检查并由人工审查 `git diff`。
3. 实验一 baseline 建立后，TASK-014 及以后不得回写实验一规格；若发现规格问题，必须创建单独的变更任务并重新审查。


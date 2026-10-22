# 设计文档修订 E2-DOC-001

2026-10-22，PlantUML 1.2025.10 实际渲染检查发现 11、12 顺序图把多行说明写成了单行 note，第二行被当作指令。修订仅把注释改为 note/end note，业务流程、参与对象和 API 不变。

原 experiment2-specs-baseline-v1 标签保留；修正后的图经实际语法检查、SVG渲染后记录 experiment2-specs-baseline-v2。此项为独立文档语法变更，实施代码阶段没有改写需求、架构、数据库、API或测试规格。

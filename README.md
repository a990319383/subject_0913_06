# EVOPS 基础工作区

这是 `001-evops` 题包的初始 Java 工作区。它只提供构建、配置、统一返回、异常处理、MyBatis-Plus 和 H2 本地数据库基础设施，不预先实现 F1 的站点、充电枪、会话和结算业务；F1 由执行模型从这里开始完成。

技术栈：Java 8、Spring Boot 2.7、MyBatis-Plus、H2 本地文件数据库、Shiro、Thymeleaf。

启动前准备：

1. 执行 `mvn -q -DskipTests compile` 验证骨架构建。
2. 执行 `mvn spring-boot:run` 启动应用；Spring Boot 会自动执行 `src/main/resources/schema.sql`。
3. H2 数据文件默认写入工作区 `data/evops`。如需修改路径，编辑 `application.yml` 中的 `jdbc:h2:file:` 连接串。

题包根目录的 `..\..\docs\schema\evops.sql` 是交付副本，应与工作区 `src/main/resources/schema.sql` 保持一致。

题面和质检卷在上一级 `packets/` 目录；模型工作区不得复制 `answers.md`、验收测试或参考修正。

## 极地冰芯样本库闭环（icecore-1）

已实现首个样本流转闭环：钻取任务 → 样本盒 → 冰芯样本 → 分析批次 → 验收/落账，并带冻融温度监测。

**领域对象与业务键（均唯一）**：钻取任务 `task_no`、库位 `location_code`、样本盒 `box_no`、冰芯样本 `sample_no`、分析批次 `batch_no`、批次明细 `(batch_id, sample_id)`。

**样本数据**：层位 `layer_no`、深度区间、温度 `temperature`、融水量 `melt_water_ml`、取样完整度 `integrity_pct`。

**状态流转**：
- 任务：`PLANNED → DRILLING → COMPLETED`，计划/钻取中可 `CANCELLED`
- 样本盒：`EMPTY ↔ IN_USE ↔ FULL` 随样本存取自动切换；`IN_USE/FULL → SEALED → ARCHIVED`
- 库位：`AVAILABLE ↔ MAINTENANCE`；按存放量自动切换 `FULL`
- 样本：`STORED → IN_ANALYSIS → CONSUMED`（由批次生命周期驱动）
- 批次：`CREATED → TESTING → ACCEPTED → POSTED`，建立/检测中可 `CANCELLED`（释放样本回库）；落账时样本核销为已消耗

**删除保护**：已验收或已落账的批次不能删除；批次内、已消耗的样本不能删除；任务/样本盒/库位存在下级关联数据时不能删除。

**REST 接口**（统一 `ApiResponse` 返回，`/api/icecore/**` 匿名放行）：
- `/api/icecore/tasks`、`/locations`、`/boxes`、`/samples`、`/batches`：POST 创建 / PUT 修改 / DELETE 删除 / GET 分页列表 / GET `/{id}` 关联详情
- `POST /api/icecore/{tasks,locations,boxes,batches}/{id}/transition`：状态流转
- `POST /api/icecore/batches`（含 `sampleIds`）建立批次；`POST/DELETE /api/icecore/batches/{id}/samples[/{sampleId}]` 增删批次样本
- `GET /api/icecore/samples?taskId=&boxId=&batchId=&status=` 多维关联查询
- `GET /api/icecore/monitor/temperature?threshold=2`：在库/分析中样本实测温度偏离库位设定温度的告警列表

测试：`mvn test` 运行 `IceCoreClosedLoopTest`（唯一键、批次闭环、取消释放、非法流转、容量、删除保护、温度告警，共 7 例）。

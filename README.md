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

## 极地冰芯运营检索增强（icecore-2）

在闭环之上补充**多租户数据权限**与**冻融环境监测运营检索**，满足大数据量稳定分页。

**多租户与角色数据权限**

- 表：`t_sys_tenant` 租户、`t_sys_account` 账号（角色 `PLATFORM` / `TENANT_ADMIN` / `TENANT_USER`）、`t_data_grant` 对象级授权（`TASK/LOCATION/BOX/SAMPLE/BATCH`）。
- 身份通过请求头 `X-Account-Id`（或 `X-Account-Username`）指定，由 `AccountContextFilter` 解析；缺省按平台运营处理，兼容匿名运营接口。
- 每次查询都强制数据权限：`PLATFORM` 全量；`TENANT_ADMIN` 限本租户 `tenant_id`；`TENANT_USER` 仅见直接授权或经任务/样本盒/库位间接授权的对象（SQL `EXISTS` 半连接）。
- 管理接口：`POST /api/icecore/admin/tenants`、`POST /api/icecore/admin/accounts`、`POST /api/icecore/admin/grants`、`POST /api/icecore/admin/grants/revoke`、`GET /api/icecore/admin/grants?accountId=`。

**运营检索（统一 `/api/icecore/search/**`）**

- `GET /search/samples`：钻取任务、样本盒、库位、分析批次、状态、取样日期范围、层位集合、深度区间、实测温度区间的 **AND/范围组合**（≥4 条件）。批次一对多关联用 `EXISTS` 半连接，**不放大主表**。
- `GET /search/slices`：**冰芯层位切片按层位分区**（`layer_no`）稳定分页。
- `GET /search/temperatures`：**分钟级库温事件**按 `(recorded_at DESC, id DESC)` 倒序稳定分页，支持库位/层位/时间范围/仅告警/最小偏差。
- `GET /search/partitions`：库位层位分区汇总（每层位切片数）。
- 所有检索返回 `PageResult{records,total,pageSize,page,hasMore,nextCursor}`；`pageSize` 严格限定 **1-100**。
- **双分页**：`page/size` 走 offset；不传 `page` 或带 `cursor` 走键集（keyset）游标，排序均以确定性主键 `id` 收尾，深分页不丢不重、不依赖不稳定 offset。
- 关联展示字段（taskNo/boxNo/locationCode 等）以 `LEFT JOIN` 取冗余列，`slice_count` 以标量子查询统计，主表行数不被放大。

**大数据量表**

- `t_freezer_temperature`：分钟级库温事件（压测量级 100,000）。
- `t_ice_layer_slice`：冰芯层位切片（压测量级 500,000），`layer_no` 分区键，按 `(tenant_id, layer_no, id)`、`(location_id, layer_no, id)` 等建索引。
- 一键播种与基准：`POST /api/icecore/loadtest/seed`（默认 100,000 库温 + 500,000 切片，`reset`/`benchmark` 可选），数据隔离在专用 `LOADTEST` 租户。

**测试**：`IceCoreSearchTest`（组合 AND/范围、批次不放大、offset/游标一致、pageSize 校验、租户/角色隔离、层位分区、库温倒序游标，共 7 例）；`IceCoreLoadTest` 在 **100,000 库温 + 500,000 切片**上校验总数、层位分区与时间倒序稳定排序、游标深翻页不丢不重（事务回滚，不污染库）。


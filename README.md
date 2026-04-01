# 城市交通事故预警系统

本项目为 2026 届本科毕业设计实现版本，采用 `Spring Boot + MySQL + Vue3`，并包含：

1. 2D 可视化（ECharts）
2. 3D 风险态势（Three.js，必做）
3. 大模型 API 能力（必做）

## 目录结构

- `server/` 后端服务
- `web/` 前端工程
- `sql/` 初始化脚本
- `docs/` 论文与答辩文档（数据库设计、接口文档、演示脚本）
- `开发指南.md` 论文与开发总指南

## 后端启动

1. 准备 MySQL 数据库：`traffic_warning`
2. 执行建表：
   - 自动建表：启动后端后按 `server/src/main/resources/db/schema.sql` 自动初始化
   - 手动建表：执行 `sql/init.sql`
3. 启动后端：

```bash
cd server
mvn spring-boot:run
```

默认地址：`http://localhost:8080`

默认管理员账号（由系统首次启动自动创建）：

- 用户名：`admin`
- 密码：`Admin@123456`

## 前端启动

```bash
cd web
npm install
npm run dev
```

默认地址：`http://localhost:5173`

## 构建验证

后端：

```bash
cd server
mvn clean package -DskipTests
```

前端：

```bash
cd web
npm run build
```

## 模拟数据生成（答辩推荐）

可用脚本快速生成 `300~2000` 条事故数据 SQL：

```bash
cd sql
./gen_mock_data.sh 500 > mock_accidents_500.sql
mysql -uroot -p traffic_warning < mock_accidents_500.sql
```

## 大模型配置（必做）

后端 `application.yml` 支持以下环境变量：

- `LLM_BASE_URL`（默认 OpenAI 兼容地址）
- `LLM_API_KEY`
- `LLM_MODEL`
- `RISK_SCHEDULER_ENABLED`（是否启用定时风险评估，默认 `false`）
- `RISK_SCHEDULER_CRON`（定时表达式，默认每30分钟一次）

示例：

```bash
export LLM_BASE_URL=https://api.openai.com/v1
export LLM_API_KEY=你的密钥
export LLM_MODEL=gpt-4o-mini
```

未配置密钥时，系统会使用兜底规则文本返回，保证页面可演示。

## 关键接口（仅 GET/POST）

- 认证：`POST /api/auth/login`、`GET /api/auth/me`
- 事故：`GET/POST /api/accidents`、`POST /api/accidents/{id}/update`、`POST /api/accidents/{id}/delete`、`POST /api/accidents/import-csv`
- 预警：`POST /api/risk/evaluate`、`GET /api/warnings`、`POST /api/warnings/{id}/confirm|publish|resolve`、`POST /api/warnings/batch/confirm|publish|resolve`
- 统计：`GET /api/stats/overview`
- 管理：`GET /api/admin/users|rules|logs|ai-logs|rule-change-logs`（`rule-change-logs` 支持 `ruleKey/startTime/endTime`）、
  `GET /api/admin/summary|summary/trend`、
  `POST /api/admin/users`、`POST /api/admin/rules/update`、`GET/POST /api/admin/dicts|/api/admin/dicts/{id}/update|/api/admin/dicts/{id}/delete`
- 字典：`GET /api/dicts/items`
- AI：`POST /api/ai/extract-accident-fields`、`POST /api/ai/generate-warning-explanation`、`POST /api/ai/generate-disposal-suggestions`、`POST /api/ai/assistant-query`

## 已实现页面

- 首页驾驶舱
- 首页驾驶舱支持本周/本月统计口径切换
- 事故管理
- 事故管理支持 CSV 导入、模板下载、导入失败明细弹窗
- 事故新增/编辑/导入与事故类型字典联动校验（避免非法类型入库）
- 地图分析（2D 点位/热力双模式）
- 预警中心
- 预警中心支持批量确认/发布/解除（前端状态预过滤 + 后端失败明细回传）
- 预警发布/解除及批量发布/解除支持二次确认
- 统计报表
- Three.js 三维风险态势
- 大模型能力实验页（含提示词模板管理）
- 管理员配置（用户、规则、系统日志、AI调用日志、规则修改历史）
- 规则修改历史支持 `ruleKey + 时间范围` 筛选与分页
- 管理员字典管理（事故类型/道路类型/预警等级，支持增改删查）
- 管理员汇总看板（用户/事故/预警/AI调用/登录统计）
- 管理员趋势分析（近7天/近30天：事故、新预警、登录成功/失败）
- 支持定时风险评估任务（可通过环境变量开关）
- 登录成功/失败均纳入审计日志（AUTH 模块）

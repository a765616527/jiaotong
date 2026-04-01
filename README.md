# 城市交通事故预警系统

本项目为 2026 届本科毕业设计实现版本，技术栈为 `Spring Boot + MySQL + Vue3`，并已落地以下特色：

1. 二维可视化（ECharts）
2. 三维风险态势（Three.js，必做）
3. 大模型能力接入（必做）
4. 接口约束：仅使用 `GET/POST`

## 目录结构

- `server/`：后端服务（Spring Boot）
- `web/`：前端工程（Vue3 + Vite）
- `sql/`：数据初始化与模拟数据脚本
- `docs/`：论文配套文档（数据库设计、接口文档、演示脚本）
- `开发指南.md`：完整开发说明

## 环境要求

- JDK 17
- Maven 3.6+
- Node.js 18+
- MySQL 8.0+

## 快速启动

### 1) 准备 MySQL

数据库名：`traffic_warning`

建议字符集：`utf8mb4`。

如果使用 Docker（示例端口 `3330`）：

```bash
docker run -d --name xjy-mysql-3330 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=traffic_warning \
  -p 3330:3306 \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci
```

### 2) 启动后端

```bash
cd server
mvn spring-boot:run
```

默认地址：`http://localhost:8080`

可通过环境变量覆盖数据库连接（见 `server/.env.example`）：

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

默认管理员账号（系统首次启动自动创建）：

- 用户名：`admin`
- 密码：`Admin@123456`

### 3) 启动前端

```bash
cd web
npm install
npm run dev
```

默认地址：`http://localhost:5173`

前端 API 地址可通过 `web/.env.example` 中的 `VITE_API_BASE` 配置。

## 数据初始化说明（重要）

- 后端已配置启动时自动执行 `server/src/main/resources/db/schema.sql` 建表。
- `sql/init.sql` **仅插入示例数据，不负责建表**。

手动初始化顺序应为：

1. 执行 `schema.sql` 建表
2. 再执行 `sql/init.sql` 插入示例数据

## 模拟数据生成（答辩推荐）

可快速生成 `300~2000` 条事故数据 SQL：

```bash
cd sql
./gen_mock_data.sh 500 > mock_accidents_500.sql
```

导入时建议指定客户端编码（避免中文乱码）：

```bash
mysql --default-character-set=utf8mb4 -uroot -p traffic_warning < mock_accidents_500.sql
```

## 大模型配置（必做）

后端支持以下环境变量：

- `LLM_BASE_URL`（默认 OpenAI 兼容地址）
- `LLM_API_KEY`
- `LLM_MODEL`
- `RISK_SCHEDULER_ENABLED`（是否启用定时风险评估，默认 `false`）
- `RISK_SCHEDULER_CRON`（定时表达式，默认每 30 分钟）

示例：

```bash
export LLM_BASE_URL=https://api.openai.com/v1
export LLM_API_KEY=你的密钥
export LLM_MODEL=gpt-4o-mini
```

未配置密钥时，系统会返回兜底文本，保证答辩演示流程可用。

## 核心接口（仅 GET/POST）

- 认证：
  - `POST /api/auth/login`
  - `GET /api/auth/me`
- 健康检查：
  - `GET /api/health`
- 事故管理：
  - `GET /api/accidents`
  - `POST /api/accidents`
  - `POST /api/accidents/{id}/update`
  - `POST /api/accidents/{id}/delete`
  - `POST /api/accidents/import-csv`
- 风险与预警：
  - `POST /api/risk/evaluate`
  - `GET /api/risk/hotspots`
  - `GET /api/warnings`
  - `POST /api/warnings/{id}/confirm|publish|resolve`
  - `POST /api/warnings/batch/confirm|publish|resolve`
  - `POST /api/warnings/{id}/disposal-records`
  - `GET /api/warnings/{id}/ai-explanation`
- 统计：
  - `GET /api/stats/overview`
- 管理端：
  - `GET /api/admin/users|rules|logs|ai-logs|rule-change-logs`
  - `GET /api/admin/summary|summary/trend`
  - `GET /api/admin/dicts`
  - `POST /api/admin/users`
  - `POST /api/admin/rules/update`
  - `POST /api/admin/dicts`
  - `POST /api/admin/dicts/{id}/update`
  - `POST /api/admin/dicts/{id}/delete`
- 字典：
  - `GET /api/dicts/items`
- 智能能力：
  - `POST /api/ai/extract-accident-fields`
  - `POST /api/ai/generate-warning-explanation`
  - `POST /api/ai/generate-disposal-suggestions`
  - `POST /api/ai/assistant-query`

## 已实现页面

- 首页驾驶舱（支持本周/本月统计口径切换）
- 事故管理（增删改查、导入、模板下载、导入失败明细）
- 地图分析（二维点位/热力模式）
- 预警中心（单条/批量确认、发布、解除，处置登记，智能解释）
- 统计报表（趋势、占比、导出）
- 三维风险态势（Three.js）
- 智能能力页（抽取、解释、建议、问答、提示词模板管理）
- 管理员配置（用户、规则、日志、规则修改历史、汇总看板、趋势分析）
- 字典管理（事故类型/道路类型/预警等级）

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

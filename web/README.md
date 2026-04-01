# 前端工程说明（web）

本目录是“城市交通事故预警系统”前端，技术栈：

- Vue 3
- TypeScript
- Vite
- Element Plus
- ECharts
- Three.js

## 运行

```bash
npm install
npm run dev
```

默认地址：`http://localhost:5173`

## 环境变量

复制或参考 `.env.example`：

```bash
VITE_API_BASE=http://localhost:8080
```

## 打包

```bash
npm run build
```

## 页面模块

- 首页驾驶舱
- 事故管理
- 地图分析（二维）
- 预警中心
- 统计报表
- 三维风险态势
- 智能能力
- 管理员配置
- 字典管理

## 说明

- 页面文案已统一中文化（含状态、风险等级、角色等枚举映射）。
- 业务接口遵循项目约束，仅调用后端 `GET/POST` 接口。

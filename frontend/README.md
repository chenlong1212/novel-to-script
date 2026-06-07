# 小说转剧本工具 - 前端

基于 Vue 3 + Vite 构建的前端项目，用于将小说文本转换为结构化剧本。

## 技术栈

- **框架**：Vue 3 (Composition API with `<script setup>`)
- **构建工具**：Vite
- **UI 框架**：Bootstrap 5

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

开发服务器将在 [http://localhost:5173](http://localhost:5173) 启动。

### 构建生产版本

```bash
npm run build
```

构建产物将输出到 `dist` 目录。

### 预览生产构建

```bash
npm run preview
```

## 项目结构

```
frontend/
├── public/              # 静态资源
├── src/
│   ├── assets/          # 资源文件（图片等）
│   ├── components/      # Vue 组件
│   ├── views/           # 页面视图
│   │   ├── UploadView.vue       # 文件上传页面
│   │   ├── ConvertView.vue      # 转换结果页面
│   │   ├── QualityView.vue      # 质量评估页面
│   │   └── ProofreadView.vue    # 人工校对页面
│   ├── services/        # API 服务
│   │   └── api.js       # 后端 API 调用
│   ├── App.vue          # 根组件
│   ├── main.js          # 入口文件
│   └── style.css        # 全局样式
├── index.html           # HTML 模板
├── vite.config.js       # Vite 配置
└── package.json         # 项目配置
```

## 功能说明

1. **文件上传**：上传小说文本文件
2. **转换处理**：调用后端 API 进行剧本转换
3. **结果展示**：以可视化方式展示生成的剧本
4. **质量评估**：查看剧本质量报告
5. **人工校对**：对生成的剧本进行人工校对和修改

## 后端接口

前端默认连接的后端地址为 `http://localhost:8080`，如需修改请编辑 `src/services/api.js`。

## 开发说明

确保在启动前端之前，后端服务已经在 `http://localhost:8080` 成功启动，否则 API 调用会失败。

完整项目文档请查看父目录的 [README.md](../README.md)。

# AI 小说转剧本工具 - 快速启动指南

## v1.0.0 版本说明

本版本新增了**可视化Web界面**，使用Vue.js构建现代化前端界面。

## 系统要求

- JDK 17+
- Maven 3.9+
- Node.js 16+
- npm 8+

## 快速启动

### 方法一：分别启动前后端

#### 1. 启动后端

```bash
cd novel-to-script
mvn spring-boot:run
```

后端将在 http://localhost:8080 启动

#### 2. 启动前端（新终端）

```bash
cd novel-to-script/frontend
npm install  # 首次运行需要安装依赖
npm run dev
```

前端将在 http://localhost:5173 启动

### 方法二：使用启动脚本

#### Windows PowerShell

```powershell
.\start.ps1
```

#### Linux/Mac

```bash
chmod +x start.sh
./start.sh
```

## 使用步骤

1. 打开浏览器访问 http://localhost:5173
2. **上传小说**：上传TXT文件或直接粘贴小说内容
3. **开始转换**：点击转换按钮，自动生成剧本
4. **质量评估**：查看各项评分和改进建议
5. **人工校对**：添加和管理修正记录
6. **导出剧本**：下载YAML格式的剧本文件

## 功能特点

### 前端界面
- 现代化的响应式设计
- 上传预览
- 实时转换进度
- 质量报告可视化
- 交互式校对工具

### 后端API
- RESTful API架构
- 文件上传和存储
- 自动质量评估
- 修正管理
- 全局异常处理

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2.x
- Maven 3.9+
- DeepSeek API集成

### 前端
- Vue.js 3.5+
- Vite
- Bootstrap 5.3
- REST API调用

## API端点

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | /api/v1/upload | 上传小说文件 |
| GET | /api/v1/novel/{fileId} | 读取小说内容 |
| POST | /api/v1/convert | 执行转换 |
| POST | /api/v1/quality | 质量评估 |
| POST | /api/v1/corrections | 创建修正 |
| GET | /api/v1/corrections/{scriptId} | 获取修正列表 |
| POST | /api/v1/corrections/{id}/apply | 应用修正 |
| GET | /api/v1/health | 健康检查 |

## 配置文件

后端配置文件：`src/main/resources/application.yml`

环境变量：`.env` 文件（参考 `.env.example`）

## 开发说明

### 后端开发

```bash
# 编译
mvn clean compile

# 运行测试
mvn test

# 打包
mvn package
```

### 前端开发

```bash
# 安装依赖
cd frontend
npm install

# 开发模式
npm run dev

# 生产构建
npm run build
```

## 目录结构

```
novel-to-script/
├── backend/              # Spring Boot后端
│   ├── src/
│   ├── pom.xml
│   └── target/
├── frontend/             # Vue.js前端
│   ├── src/
│   ├── package.json
│   └── dist/
├── start.sh             # Linux/Mac启动脚本
├── start.ps1            # Windows启动脚本
└── README.md
```

## 常见问题

### Q: 前端无法连接后端？

确保后端已启动在 http://localhost:8080，并且CORS配置正确。

### Q: 转换失败？

检查DEEPSEEK_API_KEY环境变量是否设置正确。

### Q: 文件上传失败？

检查上传文件大小是否超过10MB限制。

## 下一步

- 配置DeepSeek API密钥
- 准备测试小说文件
- 开始转换你的第一部小说！

## 许可证

MIT License

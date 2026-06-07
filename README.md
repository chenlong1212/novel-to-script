# AI 小说转剧本工具

demo演示链接：[https://www.bilibili.com/video/BV1zEEh6KEGK/?share_source=copy_web&vd_source=ce6dbec8730d45ad0df75a2e415c4376](https://www.bilibili.com/video/BV1zEEh6KEGK/?share_source=copy_web&vd_source=ce6dbec8730d45ad0df75a2e415c4376)

## 技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17 (LTS) |
| 框架 | Spring Boot | 3.2.x |
| YAML处理 | SnakeYAML | 2.2 |
| 构建工具 | Maven | 3.9+ |
| 测试 | JUnit 5 | 5.10+ |


## 项目结构

```
novel-to-script/
├── src/                           # 后端源代码
│   ├── main/
│   │   ├── java/com/example/novel2script/
│   │   │   ├── config/            # 配置类
│   │   │   │   └── ApplicationConfig.java
│   │   │   ├── model/             # 数据模型
│   │   │   │   ├── Script.java
│   │   │   │   ├── Character.java
│   │   │   │   ├── Scene.java
│   │   │   │   ├── Beat.java
│   │   │   │   └── ...
│   │   │   ├── analyzer/          # 分析器（v1.4重点改进）
│   │   │   │   ├── CharacterAnalyzer.java  # 人物识别（改进）
│   │   │   │   ├── DialogueExtractor.java  # 对话提取（重构）
│   │   │   │   ├── RelationshipAnalyzer.java # 关系分析
│   │   │   │   ├── SceneSplitter.java      # 场景分割（优化）
│   │   │   │   ├── EmotionAnnotator.java   # 情绪标注
│   │   │   │   ├── QualityEvaluator.java   # 质量评估
│   │   │   │   ├── Proofreader.java        # 人工校对
│   │   │   │   └── AnalysisManager.java    # 分析管理（重构）
│   │   │   ├── controller/         # REST API控制器
│   │   │   │   ├── ScriptController.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── client/              # API 客户端
│   │   │   │   └── DeepSeekClient.java
│   │   │   ├── converter/           # 转换器
│   │   │   │   └── NovelToScriptConverter.java
│   │   │   ├── reader/              # 文件读取器
│   │   │   │   └── NovelReader.java
│   │   │   ├── cli/                 # 命令行接口
│   │   │   │   └── ScriptConverterCLI.java
│   │   │   ├── util/                # 工具类
│   │   │   │   └── IDGenerator.java
│   │   │   └── Novel2ScriptApplication.java
│   │   └── resources/
│   │       └── application.yml      # 应用配置
│   └── test/                        # 单元测试
├── frontend/                        # 前端源代码 (Vue.js)
│   ├── src/
│   │   ├── components/              # Vue组件
│   │   ├── views/                   # 页面视图
│   │   │   ├── UploadView.vue       # 上传视图
│   │   │   ├── ConvertView.vue      # 转换视图
│   │   │   ├── QualityView.vue     # 质量评估视图
│   │   │   └── ProofreadView.vue   # 校对视图
│   │   ├── services/                # API服务
│   │   │   └── api.js
│   │   ├── App.vue                  # 根组件
│   │   └── main.js                  # 入口文件
│   ├── public/                      # 静态资源
│   ├── package.json
│   └── vite.config.js
├── docs/
│   └── yaml-schema.md               # Schema定义文档
├── examples/
│   ├── input/                       # 示例小说
│   └── output/                      # 示例输出
├── pom.xml                          # Maven配置
├── package.json                     # 前端依赖
├── start.sh                         # Linux/Mac启动脚本
├── start.ps1                        # Windows启动脚本
└── README.md                        # 项目文档
```

## 快速开始

### 环境要求

**后端：**
- JDK 17+
- Maven 3.9+

**前端：**
- Node.js 18+
- npm 9+

### 前端启动

1. **进入前端目录**
```bash
cd novel-to-script/frontend
```

2. **安装依赖**
```bash
npm install
```

3. **启动开发服务器**
```bash
npm run dev
```

开发服务器将在 `http://localhost:5173` 启动。

4. **构建生产版本**（可选）
```bash
npm run build
```
构建产物将输出到 `frontend/dist` 目录。

### 后端启动

1. **进入项目根目录**
```bash
cd novel-to-script
```

2. **构建项目**
```bash
mvn clean compile
```

3. **运行测试**
```bash
mvn test
```

4. **启动后端服务**
```bash
mvn spring-boot:run
```
后端服务将在 `http://localhost:8080` 启动。

5. **运行命令行界面（CLI）**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=cli
```

### 完整启动流程（前后端都启动）

**终端 1 - 启动后端：**
```bash
cd novel-to-script
mvn spring-boot:run
```

**终端 2 - 启动前端：**
```bash
cd novel-to-script/frontend
npm install
npm run dev
```

然后在浏览器中打开 `http://localhost:5173` 即可使用！

### 配置

1. **配置文件** `src/main/resources/application.yml`：

```yaml
novel2script:
  ai:
    provider: "deepseek"
    model: "deepseek-chat"
    api-key: "${DEEPSEEK_API_KEY}"
    api-url: "https://api.deepseek.com/v1/chat/completions"
```

2. **环境变量配置**：

方法一：使用 `.env` 文件（推荐）

复制 `.env.example` 为 `.env`：

```bash
cp .env.example .env
```

编辑 `.env` 文件：

```env
DEEPSEEK_API_KEY=your-deepseek-api-key-here
```

方法二：手动设置环境变量

```bash
# Linux/Mac
export DEEPSEEK_API_KEY=your-api-key

# Windows (PowerShell)
$env:DEEPSEEK_API_KEY="your-api-key"
```

**注意：** `.env` 文件已添加到 `.gitignore`，不会被提交到 Git 仓库。

## YAML Schema

剧本输出的 YAML 结构遵循 [yaml-schema.md](docs/yaml-schema.md) 定义的规范。

### 核心结构

```yaml
script:
  schema_version: "1.0"
  meta:           # 元信息
  characters:     # 人物列表
  scenes:         # 场景列表
  relationships:  # 人物关系（可选）
```

### 数据模型

| 模型类 | 说明 |
|--------|------|
| `Script` | 剧本根对象 |
| `MetaInformation` | 元信息（标题、作者、版本等） |
| `Character` | 人物（ID、名称、角色类型等） |
| `Scene` | 场景（地点、时间、节拍列表） |
| `Beat` | 场景节拍（动作/对话/旁白/转场） |
| `Location` | 地点信息 |
| `TimeSetting` | 时间设置 |
| `Relationship` | 人物关系 |

### 枚举类型

| 枚举 | 说明 |
|------|------|
| `Character.Role` | 角色类型（protagonist/antagonist/supporting/minor/cameo） |
| `Location.LocationType` | 地点类型（fixed/public/private/outdoor/virtual） |
| `TimeSetting.Period` | 时间段（dawn/morning/noon/afternoon/evening/night/midnight/day/unknown） |
| `Beat.BeatType` | 节拍类型（action/dialogue/narration/transition） |
| `Beat.Emotion` | 情绪（happy/sad/angry/fearful/surprised/disgusted/neutral/sarcastic/loving/guilty/proud/shy） |
| `Beat.Intensity` | 情绪强度（low/medium/high） |
| `Relationship.RelationshipType` | 关系类型（family/friend/colleague/lover/enemy/superior/subordinate/acquaintance/stranger） |

## 核心功能

### 1. 小说文件读取（v0.2）

```java
NovelReader reader = new NovelReader();
String text = reader.readNovel("examples/input/sample.txt");
List<String> paragraphs = reader.splitIntoParagraphs(text);
NovelReader.NovelMetadata metadata = reader.extractMetadata(text);
```

### 2. DeepSeek API 调用（v0.2）

```java
DeepSeekClient client = new DeepSeekClient(config);
String response = client.chat("系统提示词", "用户输入");
boolean connected = client.testConnection();
```

### 3. 人物识别（v0.3+，v1.4重大改进）

```java
// v1.4改进：支持更多格式
CharacterAnalyzer analyzer = new CharacterAnalyzer();
List<CharacterAnalyzer.CharacterInfo> characters = analyzer.analyzeCharacters(text);
// 支持识别：
// - 张三、李四（标准姓名）
// - 老张、小李（口语化称呼）
// - 服务员、风衣男人（职业/特征称呼）
```

### 4. 对话提取（v0.3+，v1.4重构）

```java
// v1.4重构：支持多种对话格式
DialogueExtractor extractor = new DialogueExtractor();
List<DialogueExtractor.DialogueInfo> dialogues = extractor.extractDialogues(text);
// 支持格式：
// 1. 张三说："你好"
// 2. "你好"张三说
// 3. "你好"（继承上一个说话人）
DialogueExtractor.DialogueStatistics stats = extractor.calculateStatistics(dialogues);
```

### 5. 关系分析（v0.3）

```java
RelationshipAnalyzer analyzer = new RelationshipAnalyzer();
List<RelationshipAnalyzer.RelationshipInfo> relationships = 
    analyzer.analyzeRelationships(text, characterNames);
```

### 6. 场景分割（v0.4，v1.4优化）

```java
// v1.4优化：更适合短文本
SceneSplitter splitter = new SceneSplitter();
List<SceneSplitter.SceneInfo> scenes = splitter.splitScenes(text);
// 改进：
// - 最小分割间隔从500字降至100字
// - 即使文本很短也能分割
```

### 7. 情绪标注（v0.4）

```java
EmotionAnnotator annotator = new EmotionAnnotator();
Beat.Emotion emotion = annotator.analyzeEmotion(dialogueText);
Beat.Intensity intensity = annotator.analyzeIntensity(dialogueText, emotion);
String description = annotator.getEmotionDescription(emotion);
String emoji = annotator.getEmotionEmoji(emotion);
```

### 8. 质量评估（v0.5）

```java
QualityEvaluator evaluator = new QualityEvaluator();
QualityReport report = evaluator.evaluate(script, originalText);
double overallScore = report.getOverallScore();
String grade = report.getGrade();
List<QualityReport.QualityIssue> issues = report.getIssues();
List<String> suggestions = report.getSuggestions();
```

### 9. 人工校对（v0.5）

```java
Proofreader proofreader = new Proofreader();
Correction correction = proofreader.createCorrection(
    scriptId, Correction.CorrectionType.CHARACTER,
    characterId, "name", "旧名称", "新名称", "修正人物名称"
);
proofreader.applyCorrection(correction, script);
List<Correction> pending = proofreader.getPendingCorrections(scriptId);
```

### 10. 完整分析（v0.3+，v1.4重构）

```java
// v1.4重构：更好的场景-对话关联
AnalysisManager manager = new AnalysisManager();
AnalysisManager.AnalysisResult result = manager.analyze(text);
Script script = result.getScript();
// 改进：
// - 对话真正关联到场景内容
// - 自动提取动作描述
```

## 使用示例

```java
// ID生成器使用
IDGenerator generator = new IDGenerator();

String charId = generator.generateCharacterId();  // char_001
String sceneId = generator.generateSceneId();     // scene_001

// 创建剧本对象
Script script = new Script();
script.setSchemaVersion("1.0");

MetaInformation meta = new MetaInformation();
meta.setTitle("咖啡厅偶遇");
meta.setAuthor("张作家");
script.setMeta(meta);
```

## 测试

```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=IDGeneratorTest

# 编译测试
mvn test-compile
```

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.4.0 | 2026-06-07 | **核心算法重构**：人物识别、对话提取、场景分割、分析管理器全部优化，更适合短文本处理 |
| v1.0.0 | 2026-06-06 | 可视化Web界面 + REST API |
| v0.5.0 | 2026-06-05 | 质量评估 + 人工校对修正 |
| v0.4.0 | 2026-06-05 | 场景分割 + 情绪标注 |
| v0.3.0 | 2026-06-05 | 人物识别 + 对话提取 + 关系分析 |
| v0.2.0 | 2026-06-05 | 基础转换能力（DeepSeek API、小说读取、转换器、CLI） |
| v0.1.0 | 2026-06-05 | Java项目初始化，Schema定义，数据模型 |

## 许可证

MIT License


# 剧本 YAML Schema 定义文档

## 一、概述

本文档定义了小说转剧本工具输出的 YAML 结构规范。该 Schema 旨在：

1. **结构化存储剧本内容**：将小说转换为可编辑、可扩展的剧本格式
2. **保留关键信息**：人物、场景、对话、动作、情绪等元素完整保留
3. **便于后续处理**：支持导出为多种格式，便于编剧进一步打磨
4. **可扩展性**：预留扩展字段，支持未来功能增强

**v1.4 更新说明**：
- 优化了场景和对话的关联机制
- 改进了短文本处理能力
- 增强了人物识别的灵活性

---

## 二、Schema 结构总览

```yaml
script:
  schema_version: string      # Schema 版本号
  meta: MetaInformation       # 元信息
  characters: Character[]     # 人物列表
  scenes: Scene[]             # 场景列表
  relationships: Relationship[]  # 人物关系（可选）
  notes: string               # 备注（可选）
```

---

## 三、详细字段定义

### 3.1 根节点 `script`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| schema_version | string | 是 | Schema 版本，用于兼容性检查，当前为 `1.0` |
| meta | MetaInformation | 是 | 剧本元信息 |
| characters | Character[] | 是 | 人物列表，至少包含一个主角 |
| scenes | Scene[] | 是 | 场景列表，至少包含一个场景 |
| relationships | Relationship[] | 否 | 人物关系网络 |
| notes | string | 否 | 全局备注或改编说明 |

**设计原因：**
- `schema_version`：便于后续版本升级时的数据迁移和兼容性处理
- `relationships` 设为可选：简单剧本可能不需要复杂的人物关系网络

---

### 3.2 元信息 `MetaInformation`

```yaml
meta:
  title: string              # 剧本标题
  source: string             # 原小说名称
  author: string             # 原作者
  adapter: string            # 改编者
  created_at: date           # 创建日期 (YYYY-MM-DD)
  updated_at: date           # 更新日期 (YYYY-MM-DD)
  version: string            # 剧本版本号
  genre: string[]            # 类型标签（可选）
  synopsis: string           # 故事梗概（可选）
  total_scenes: integer      # 场景总数（自动计算）
  total_characters: integer  # 人物总数（自动计算）
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string | 是 | 剧本标题，默认取原小说名 |
| source | string | 是 | 原小说名称，用于追溯来源 |
| author | string | 是 | 原小说作者 |
| adapter | string | 是 | 执行改编的人员或AI标识 |
| created_at | date | 是 | 创建日期，ISO 8601 格式 |
| updated_at | date | 是 | 最后更新日期 |
| version | string | 是 | 剧本版本，如 `1.0`、`1.1-draft` |
| genre | string[] | 否 | 类型标签，如 `["爱情", "悬疑"]` |
| synopsis | string | 否 | 故事梗概，AI自动生成或人工填写 |
| total_scenes | integer | 是 | 场景总数，由系统自动计算 |
| total_characters | integer | 是 | 人物总数，由系统自动计算 |

**设计原因：**
- 保留 `source` 和 `author`：尊重原作者版权，便于追溯
- `genre` 设为数组：一部作品可能跨多个类型
- 自动计算字段：减少人工维护，保证数据一致性

---

### 3.3 人物 `Character`

```yaml
characters:
  - id: string               # 唯一标识
    name: string             # 人物名称
    role: enum               # 角色类型
    description: string      # 人物描述
    first_appear: string     # 首次出场场景ID
    last_appear: string      # 最后出场场景ID（可选）
    aliases: string[]        # 别名/昵称（可选）
    attributes: object       # 扩展属性（可选）
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | string | 是 | 唯一标识，格式为 `char_XXX`，如 `char_001` |
| name | string | 是 | 人物名称，使用小说中的称呼 |
| role | enum | 是 | 角色类型，见下方枚举定义 |
| description | string | 是 | 人物外貌、性格等描述 |
| first_appear | string | 是 | 首次出场的场景ID |
| last_appear | string | 否 | 最后出场的场景ID |
| aliases | string[] | 否 | 别名列表，如 `["老张", "张叔"]` |
| attributes | object | 否 | 扩展属性，如年龄、职业等 |

**角色类型枚举 `role`：**

| 值 | 说明 |
|------|------|
| protagonist | 主角 |
| antagonist | 反派 |
| supporting | 配角 |
| minor | 次要角色 |
| cameo | 客串/路人 |

**v1.4 人物识别改进：**
- 支持口语化称呼（如"老张"、"小李"）
- 支持1-5字符的人物名称
- 只要出现1次即可识别（适合短文本）
- 支持职业/特征称呼（如"服务员"、"风衣男人"）

**设计原因：**
- `id` 使用前缀 `char_`：便于在对话中引用，避免与场景ID混淆
- `role` 使用枚举：规范角色分类，便于统计分析
- `aliases`：小说中同一人物可能有多个称呼（如"张三"、"老张"、"张总"）
- `attributes` 设为对象：预留扩展空间，如 `{ "age": 30, "job": "程序员" }`

---

### 3.4 场景 `Scene`

```yaml
scenes:
  - id: string               # 唯一标识
    number: integer          # 场景序号
    location: Location       # 地点信息
    time: TimeSetting        # 时间设置
    description: string      # 场景描述
    atmosphere: string       # 氛围（可选）
    beats: Beat[]            # 场景节拍列表
    duration: string         # 预估时长（可选）
    notes: string            # 场景备注（可选）
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | string | 是 | 唯一标识，格式为 `scene_XXX` |
| number | integer | 是 | 场景序号，从 1 开始 |
| location | Location | 是 | 地点信息 |
| time | TimeSetting | 是 | 时间设置 |
| description | string | 是 | 场景描述，用于舞台说明 |
| atmosphere | string | 否 | 氛围，如 `紧张`、`温馨` |
| beats | Beat[] | 是 | 场景内的动作/对话列表 |
| duration | string | 否 | 预估时长，如 `2分钟` |
| notes | string | 否 | 导演备注或特殊说明 |

**v1.4 场景分割改进：**
- 最小分割间隔从500字降至100字，更适合短文本
- 场景最小长度从50字降至20字
- 兜底逻辑：即使没有识别到分割点，也会将整个文本作为一个场景
- 真正按场景文本内容关联对话

**设计原因：**
- 使用 `beats` 而非 `actions` + `dialogues`：剧本中动作和对话交替出现，保持顺序很重要
- `atmosphere`：帮助理解场景基调，指导表演和拍摄
- `duration`：便于估算剧本时长

---

### 3.5 地点信息 `Location`

```yaml
location:
  name: string               # 地点名称
  type: enum                 # 地点类型
  interior: boolean          # 是否为室内
  description: string        # 详细描述（可选）
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 地点名称，如 `咖啡厅`、`张三家` |
| type | enum | 是 | 地点类型，见下方枚举 |
| interior | boolean | 是 | 是否为室内场景 |
| description | string | 否 | 详细描述，如 `装修简约，靠窗位置` |

**地点类型枚举 `type`：**

| 值 | 说明 |
|------|------|
| fixed | 固定场景（如主角家、办公室） |
| public | 公共场所（如街道、餐厅） |
| private | 私密空间（如卧室） |
| outdoor | 户外场景 |
| virtual | 虚拟场景（如梦境、回忆） |

**设计原因：**
- 区分 `interior` 和 `outdoor`：影响灯光、音效等拍摄决策
- `fixed` 场景：便于统计主要场景，控制制作成本

---

### 3.6 时间设置 `TimeSetting`

```yaml
time:
  period: enum               # 时间段
  specific: string           # 具体时间（可选）
  continuity: string         # 时间连续性（可选）
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| period | enum | 是 | 时间段，见下方枚举 |
| specific | string | 否 | 具体时间，如 `2024年春节`、`三年前` |
| continuity | string | 否 | 与上一场景的时间关系 |

**时间段枚举 `period`：**

| 值 | 说明 |
|------|------|
| dawn | 黎明 |
| morning | 上午 |
| noon | 中午 |
| afternoon | 下午 |
| evening | 傍晚 |
| night | 夜晚 |
| midnight | 深夜 |
| day | 白天（不具体） |
| unknown | 不明确 |

**设计原因：**
- `period` 使用枚举：规范时间表达，便于统计分析
- `continuity`：标注时间跳跃，如 `紧接着上一场景`、`三天后`

---

### 3.7 场景节拍 `Beat`

节拍是场景内的最小单元，可以是动作或对话：

```yaml
beats:
  # 动作节拍
  - type: action
    content: string          # 动作描述
    subject: string          # 动作主体（可选）
    
  # 对话节拍
  - type: dialogue
    speaker: string          # 说话人ID
    content: string          # 对话内容
    emotion: enum            # 情绪（可选）
    intensity: enum          # 情绪强度（可选）
    direction: string        # 舞台指示（可选）
    to: string               # 对话对象ID（可选）
    
  # 旁白节拍
  - type: narration
    content: string          # 旁白内容
    
  # 转场节拍
  - type: transition
    content: string          # 转场说明
```

#### 3.7.1 动作节拍 `action`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | string | 是 | 固定值 `action` |
| content | string | 是 | 动作描述 |
| subject | string | 否 | 动作主体的人物ID |

**v1.4 改进：** 当场景中没有对话时，会自动提取场景中的动作描述作为 beats。

#### 3.7.2 对话节拍 `dialogue`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | string | 是 | 固定值 `dialogue` |
| speaker | string | 是 | 说话人的人物ID |
| content | string | 是 | 对话内容 |
| emotion | enum | 否 | 情绪标签 |
| intensity | enum | 否 | 情绪强度 |
| direction | string | 否 | 舞台指示，如 `走向窗边` |
| to | string | 否 | 对话对象的人物ID |

**v1.4 对话提取改进：**
- 支持三种对话模式：
  1. `XX说："对话内容"` 或 `XX说"对话内容"`
  2. `"对话内容"XX说`
  3. 只有引号的对话内容（继承上一个说话人）
- 接受1-4字符的说话人名称
- 智能推断说话人

**情绪枚举 `emotion`：**

| 值 | 说明 |
|------|------|
| happy | 开心 |
| sad | 悲伤 |
| angry | 愤怒 |
| fearful | 恐惧 |
| surprised | 惊讶 |
| disgusted | 厌恶 |
| neutral | 平静 |
| sarcastic | 讽刺 |
| loving | 关爱 |
| guilty | 愧疚 |
| proud | 自豪 |
| shy | 害羞 |

**情绪强度枚举 `intensity`：**

| 值 | 说明 |
|------|------|
| low | 低 |
| medium | 中 |
| high | 高 |

#### 3.7.3 旁白节拍 `narration`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | string | 是 | 固定值 `narration` |
| content | string | 是 | 旁白内容 |

#### 3.7.4 转场节拍 `transition`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | string | 是 | 固定值 `transition` |
| content | string | 是 | 转场说明，如 `切至`、`淡出` |

**设计原因：**
- 使用 `type` 区分节拍类型：便于统一处理，保持顺序
- 对话中包含 `emotion` 和 `direction`：帮助演员理解表演要求
- `to` 字段：明确对话对象，便于分析人物互动

---

### 3.8 人物关系 `Relationship`

```yaml
relationships:
  - from: string             # 人物ID
    to: string               # 人物ID
    type: enum               # 关系类型
    description: string      # 关系描述（可选）
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| from | string | 是 | 关系起点人物ID |
| to | string | 是 | 关系终点人物ID |
| type | enum | 是 | 关系类型 |
| description | string | 否 | 关系描述 |

**关系类型枚举 `type`：**

| 值 | 说明 |
|------|------|
| family | 家人 |
| friend | 朋友 |
| colleague | 同事 |
| lover | 恋人 |
| enemy | 敌人 |
| superior | 上级 |
| subordinate | 下级 |
| acquaintance | 熟人 |
| stranger | 陌生人 |

**设计原因：**
- 使用有向关系：`A 是 B 的上级` 与 `B 是 A 的下属` 不同
- 预留 `description`：某些关系需要额外说明，如 `青梅竹马`

---

## 四、完整示例

```yaml
script:
  schema_version: "1.0"
  
  meta:
    title: "咖啡厅偶遇"
    source: "都市情感小说第三章"
    author: "张作家"
    adapter: "AI Script Converter v1.4"
    created_at: "2026-06-07"
    updated_at: "2026-06-07"
    version: "1.0"
    genre: ["爱情", "都市"]
    synopsis: "张三在咖啡厅偶遇多年未见的老友李四，两人相谈甚欢。"
    total_scenes: 2
    total_characters: 3
    
  characters:
    - id: "char_001"
      name: "张三"
      role: "protagonist"
      description: "30岁，程序员，性格内向，穿着休闲"
      first_appear: "scene_001"
      aliases: ["老张"]
      attributes:
        age: 30
        job: "程序员"
        
    - id: "char_002"
      name: "李四"
      role: "supporting"
      description: "张三的大学同学，现为创业者，性格开朗"
      first_appear: "scene_001"
      
    - id: "char_003"
      name: "服务员"
      role: "minor"
      description: "咖啡厅服务员"
      first_appear: "scene_001"
      
  scenes:
    - id: "scene_001"
      number: 1
      location:
        name: "咖啡厅"
        type: "public"
        interior: true
        description: "装修简约，午后阳光透过落地窗"
      time:
        period: "afternoon"
        specific: "周末下午"
      description: "一家安静的咖啡厅，客人不多"
      atmosphere: "轻松"
      beats:
        - type: "action"
          content: "张三推门走进咖啡厅，环顾四周"
          subject: "char_001"
          
        - type: "action"
          content: "李四坐在靠窗的位置，向张三招手"
          subject: "char_002"
          
        - type: "dialogue"
          speaker: "char_002"
          content: "老张！这边！"
          emotion: "happy"
          direction: "站起身招手"
          
        - type: "action"
          content: "张三走过去，在李四对面坐下"
          subject: "char_001"
          
        - type: "dialogue"
          speaker: "char_001"
          content: "好久不见，你还是老样子。"
          emotion: "happy"
          to: "char_002"
          
        - type: "dialogue"
          speaker: "char_002"
          content: "你也是，还是这么准时。"
          emotion: "happy"
          to: "char_001"
          
    - id: "scene_002"
      number: 2
      location:
        name: "咖啡厅"
        type: "public"
        interior: true
      time:
        period: "afternoon"
        continuity: "紧接着上一场景"
      description: "同一咖啡厅，两人已点好咖啡"
      beats:
        - type: "dialogue"
          speaker: "char_003"
          content: "两位的拿铁，请慢用。"
          emotion: "neutral"
          
        - type: "action"
          content: "服务员放下咖啡离开"
          subject: "char_003"
          
        - type: "dialogue"
          speaker: "char_001"
          content: "说吧，找我什么事？"
          emotion: "neutral"
          to: "char_002"
          
        - type: "dialogue"
          speaker: "char_002"
          content: "有个项目想和你合作..."
          emotion: "excited"
          to: "char_001"
          
  relationships:
    - from: "char_001"
      to: "char_002"
      type: "friend"
      description: "大学同学，多年好友"
      
  notes: |
    本剧本由 AI 自动转换生成（v1.4版本），建议人工审核后使用。
    重点检查：人物情绪标注、舞台指示的合理性。
```

---

## 五、Schema 设计原则总结

### 5.1 为什么选择 YAML 格式？

| 对比项 | YAML | JSON | XML |
|--------|------|------|-----|
| 可读性 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ |
| 注释支持 | ✅ | ❌ | ✅ |
| 编辑友好 | ✅ | ⭐⭐ | ⭐⭐ |
| 解析性能 | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| 工具支持 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |

**结论：** YAML 最适合人工编辑和阅读，符合"让作者可以快速获得可编辑剧本初稿"的需求。

### 5.2 为什么使用 ID 引用而非名称？

- **一致性**：人物名称可能在小说中有多种称呼（"张三"、"老张"、"张总"）
- **重构友好**：修改人物名称时只需改一处
- **数据完整性**：便于检测引用错误

### 5.3 为什么使用枚举而非自由文本？

- **规范化**：统一情绪、角色类型等表达
- **可分析**：便于统计和筛选
- **国际化**：枚举值可映射到多语言

### 5.4 扩展性考虑

- 所有对象都预留了可选字段
- `attributes` 字段支持任意扩展属性
- `schema_version` 支持未来版本升级

---

## 六、校验规则

使用本 Schema 时，应进行以下校验：

1. **必填字段检查**：所有标记为必填的字段必须存在
2. **ID 唯一性检查**：人物ID、场景ID必须唯一
3. **引用完整性检查**：
   - 对话中的 `speaker` 必须存在于 `characters`
   - 场景中的 `first_appear` 必须存在于 `scenes`
4. **枚举值检查**：所有枚举字段必须使用预定义值
5. **顺序性检查**：场景序号必须连续且从1开始

---

## 七、版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-06-05 | 初始版本定义 |
| 1.4 | 2026-06-07 | **核心改进**：
  - 优化场景和对话关联机制
  - 改进短文本处理
  - 支持多种对话格式
  - 放宽人物识别限制 |

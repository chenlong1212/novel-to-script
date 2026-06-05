package com.example.novel2script.converter;

import com.example.novel2script.client.DeepSeekClient;
import com.example.novel2script.model.*;
import com.example.novel2script.reader.NovelReader;
import com.example.novel2script.util.IDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * 小说转剧本转换器
 * 核心转换逻辑
 */
@Component
public class NovelToScriptConverter {

    private static final Logger logger = LoggerFactory.getLogger(NovelToScriptConverter.class);

    private final DeepSeekClient deepSeekClient;
    private final NovelReader novelReader;
    private final IDGenerator idGenerator;

    @Autowired
    public NovelToScriptConverter(
            DeepSeekClient deepSeekClient,
            NovelReader novelReader,
            IDGenerator idGenerator) {
        this.deepSeekClient = deepSeekClient;
        this.novelReader = novelReader;
        this.idGenerator = idGenerator;
    }

    /**
     * 将小说文件转换为剧本
     *
     * @param novelFilePath 小说文件路径
     * @return 剧本对象
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public Script convert(String novelFilePath) throws IOException, InterruptedException {
        logger.info("开始转换小说: {}", novelFilePath);

        // 1. 读取小说
        String novelContent = novelReader.readNovel(novelFilePath);
        
        // 2. 提取元信息
        NovelReader.NovelMetadata metadata = novelReader.extractMetadata(novelContent);
        
        // 3. 调用 AI 进行转换
        String aiResponse = callAIConversion(novelContent);
        
        // 4. 解析 AI 响应，生成剧本结构
        Script script = parseAIResponse(aiResponse, metadata);
        
        logger.info("小说转换完成，生成了 {} 个场景，{} 个人物",
                   script.getScenes().size(), script.getCharacters().size());

        return script;
    }

    /**
     * 调用 AI 进行转换
     */
    private String callAIConversion(String novelContent) throws IOException, InterruptedException {
        logger.info("调用 DeepSeek API 进行转换...");

        String systemPrompt = """
            你是一个专业的剧本创作助手。你的任务是将小说文本转换为符合 YAML 格式的剧本结构。

            转换规则：
            1. 识别并提取所有对话，为每句对话标注说话人
            2. 提取动作描写，转换为舞台指示
            3. 识别场景变化（地点、时间变化）
            4. 为每个角色分配唯一 ID（char_001, char_002...）
            5. 为每个场景分配唯一 ID（scene_001, scene_002...）
            6. 为对话标注情绪（happy, sad, angry, neutral 等）

            输出格式要求：
            - 使用 YAML 格式输出
            - 包含 scenes 和 characters 列表
            - 每个 scene 包含 id, number, location, time, beats
            - 每个 character 包含 id, name, role, description

            请只输出 YAML 内容，不要添加其他说明文字。
            """;

        String userPrompt = "请将以下小说文本转换为剧本 YAML 格式：\n\n" + novelContent;

        String response = deepSeekClient.chat(systemPrompt, userPrompt);

        logger.info("AI 转换完成，响应长度: {} 个字符", response.length());

        return response;
    }

    /**
     * 解析 AI 响应，生成剧本结构
     * 这是一个简化实现，实际可能需要更复杂的解析逻辑
     */
    private Script parseAIResponse(String aiResponse, NovelReader.NovelMetadata metadata) {
        logger.info("开始解析 AI 响应...");

        Script script = new Script();
        script.setSchemaVersion("1.0");

        // 设置元信息
        MetaInformation meta = new MetaInformation();
        meta.setTitle(metadata.getTitle() != null ? metadata.getTitle() : "未命名剧本");
        meta.setSource("小说");
        meta.setAuthor(metadata.getAuthor() != null ? metadata.getAuthor() : "未知作者");
        meta.setAdapter("AI小说转剧本工具");
        meta.setCreatedAt(LocalDate.now());
        meta.setUpdatedAt(LocalDate.now());
        meta.setVersion("1.0");
        script.setMeta(meta);

        // 解析 YAML（简化实现）
        // 实际生产环境建议使用 SnakeYAML 的 Loader
        List<com.example.novel2script.model.Character> characters = extractCharacters(aiResponse);
        List<Scene> scenes = extractScenes(aiResponse);

        script.setCharacters(characters);
        script.setScenes(scenes);

        // 更新元信息
        meta.setTotalCharacters(characters.size());
        meta.setTotalScenes(scenes.size());

        logger.info("解析完成：{} 个人物，{} 个场景", characters.size(), scenes.size());

        return script;
    }

    /**
     * 提取人物列表（简化实现）
     */
    private List<com.example.novel2script.model.Character> extractCharacters(String aiResponse) {
        List<com.example.novel2script.model.Character> characters = new ArrayList<>();
        
        // 简化实现：基于对话中的说话人识别
        // 实际需要更复杂的 NLP 处理
        
        // 示例：识别常见的说话人
        Set<String> speakerNames = new LinkedHashSet<>();
        String[] lines = aiResponse.split("\\n");
        
        for (String line : lines) {
            if (line.contains("speaker:") || line.contains("说话人:")) {
                // 简单提取名字
                String name = line.replaceAll(".*speaker:\\s*", "")
                                  .replaceAll(".*说话人:\\s*", "")
                                  .replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", "")
                                  .trim();
                if (!name.isEmpty()) {
                    speakerNames.add(name);
                }
            }
        }
        
        // 为每个识别到的人物创建 Character 对象
        int index = 1;
        for (String name : speakerNames) {
            com.example.novel2script.model.Character character = new com.example.novel2script.model.Character();
            character.setId("char_" + String.format("%03d", index++));
            character.setName(name);
            character.setRole(com.example.novel2script.model.Character.Role.supporting);
            character.setDescription("从小说中识别的人物");
            character.setFirstAppear("scene_001");
            characters.add(character);
        }
        
        // 如果没有识别到人物，创建默认人物
        if (characters.isEmpty()) {
            com.example.novel2script.model.Character defaultChar = new com.example.novel2script.model.Character();
            defaultChar.setId("char_001");
            defaultChar.setName("角色");
            defaultChar.setRole(com.example.novel2script.model.Character.Role.protagonist);
            defaultChar.setDescription("默认角色");
            defaultChar.setFirstAppear("scene_001");
            characters.add(defaultChar);
        }
        
        return characters;
    }

    /**
     * 提取场景列表（简化实现）
     */
    private List<Scene> extractScenes(String aiResponse) {
        List<Scene> scenes = new ArrayList<>();
        
        // 简化实现：基于 YAML 中的 scene 标记
        // 实际需要更复杂的结构解析
        
        Scene scene = new Scene();
        scene.setId("scene_001");
        scene.setNumber(1);
        
        Location location = new Location();
        location.setName("场景");
        location.setType(Location.LocationType.fixed);
        location.setInterior(true);
        location.setDescription("从小说中识别的场景");
        scene.setLocation(location);
        
        TimeSetting time = new TimeSetting();
        time.setPeriod(TimeSetting.Period.afternoon);
        time.setSpecific("未指定");
        scene.setTime(time);
        
        scene.setDescription("场景描述");
        scene.setAtmosphere("中性");
        
        // 提取对话和动作
        List<Beat> beats = new ArrayList<>();
        String[] lines = aiResponse.split("\\n");
        
        for (String line : lines) {
            String trimmed = line.trim();
            
            if (trimmed.isEmpty()) continue;
            
            Beat beat = new Beat();
            
            if (trimmed.contains("dialogue") || trimmed.contains("对话")) {
                // 对话
                beat.setType(Beat.BeatType.dialogue);
                beat.setContent(extractContent(trimmed));
                beat.setEmotion(Beat.Emotion.neutral);
                beats.add(beat);
            } else if (trimmed.contains("action") || trimmed.contains("动作")) {
                // 动作
                beat.setType(Beat.BeatType.action);
                beat.setContent(extractContent(trimmed));
                beats.add(beat);
            } else if (trimmed.contains("narration") || trimmed.contains("叙述")) {
                // 叙述
                beat.setType(Beat.BeatType.narration);
                beat.setContent(extractContent(trimmed));
                beats.add(beat);
            }
        }
        
        // 如果没有提取到 beats，添加一个默认 beat
        if (beats.isEmpty()) {
            Beat defaultBeat = new Beat();
            defaultBeat.setType(Beat.BeatType.narration);
            defaultBeat.setContent("这是从 AI 响应中提取的内容，需要进一步解析。");
            beats.add(defaultBeat);
        }
        
        scene.setBeats(beats);
        scenes.add(scene);
        
        return scenes;
    }

    /**
     * 提取内容（简化实现）
     */
    private String extractContent(String line) {
        // 简单提取冒号后的内容
        int colonIndex = line.indexOf(':');
        if (colonIndex != -1 && colonIndex < line.length() - 1) {
            return line.substring(colonIndex + 1).trim();
        }
        return line;
    }

    /**
     * 测试转换器
     */
    public void testConverter() {
        logger.info("=== 测试转换器 ===");
        
        // 测试 AI 连接
        boolean connected = deepSeekClient.testConnection();
        if (connected) {
            logger.info("✓ DeepSeek API 连接成功");
        } else {
            logger.warn("✗ DeepSeek API 连接失败");
        }
        
        logger.info("=== 测试完成 ===");
    }
}

package com.example.novel2script.analyzer;

import com.example.novel2script.analyzer.CharacterAnalyzer.CharacterInfo;
import com.example.novel2script.analyzer.DialogueExtractor.DialogueInfo;
import com.example.novel2script.analyzer.DialogueExtractor.DialogueStatistics;
import com.example.novel2script.analyzer.RelationshipAnalyzer.RelationshipInfo;
import com.example.novel2script.analyzer.SceneSplitter.SceneInfo;
import com.example.novel2script.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 分析管理器
 * 整合人物分析、对话提取、关系分析、场景分割、情绪标注
 */
@Component
public class AnalysisManager {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisManager.class);

    @Autowired
    private CharacterAnalyzer characterAnalyzer;

    @Autowired
    private DialogueExtractor dialogueExtractor;

    @Autowired
    private RelationshipAnalyzer relationshipAnalyzer;

    @Autowired
    private SceneSplitter sceneSplitter;

    @Autowired
    private EmotionAnnotator emotionAnnotator;

    /**
     * 完整分析小说
     *
     * @param text 小说文本
     * @return 分析结果
     */
    public AnalysisResult analyze(String text) {
        logger.info("开始完整分析小说...");
        
        AnalysisResult result = new AnalysisResult();
        
        // 1. 分析人物
        logger.info("分析人物...");
        List<CharacterInfo> characterInfos = characterAnalyzer.analyzeCharacters(text);
        result.setCharacterInfos(characterInfos);
        logger.info("识别到 {} 个人物", characterInfos.size());
        
        // 2. 分割场景
        logger.info("分割场景...");
        List<SceneInfo> sceneInfos = sceneSplitter.splitScenes(text);
        result.setSceneInfos(sceneInfos);
        logger.info("识别到 {} 个场景", sceneInfos.size());
        
        // 3. 提取对话
        logger.info("提取对话...");
        List<DialogueInfo> dialogueInfos = dialogueExtractor.extractDialogues(text);
        result.setDialogueInfos(dialogueInfos);
        logger.info("提取到 {} 条对话", dialogueInfos.size());
        
        // 4. 统计对话
        DialogueStatistics dialogueStats = dialogueExtractor.calculateStatistics(dialogueInfos);
        result.setDialogueStatistics(dialogueStats);
        
        // 5. 分析关系
        logger.info("分析关系...");
        List<String> characterNames = characterInfos.stream()
            .map(CharacterInfo::getName)
            .collect(Collectors.toList());
        List<RelationshipInfo> relationshipInfos = relationshipAnalyzer.analyzeRelationships(text, characterNames);
        result.setRelationshipInfos(relationshipInfos);
        logger.info("识别到 {} 个关系", relationshipInfos.size());
        
        // 6. 转换为剧本模型
        logger.info("转换为剧本模型...");
        convertToScriptModel(result, text);
        
        logger.info("小说分析完成");
        
        return result;
    }

    /**
     * 转换为剧本模型
     */
    private void convertToScriptModel(AnalysisResult result, String originalText) {
        Script script = new Script();
        script.setSchemaVersion("1.0");
        
        // 尝试从文本提取标题
        String title = extractTitle(originalText);
        
        // 设置元信息
        MetaInformation meta = new MetaInformation();
        meta.setTitle(title);
        meta.setSource("AI小说分析");
        meta.setAdapter("AI小说转剧本工具 v1.1");
        meta.setCreatedAt(java.time.LocalDate.now());
        meta.setUpdatedAt(java.time.LocalDate.now());
        meta.setVersion("1.0");
        script.setMeta(meta);
        
        // 转换人物
        List<com.example.novel2script.model.Character> characters = new ArrayList<>();
        for (CharacterInfo info : result.getCharacterInfos()) {
            com.example.novel2script.model.Character character = new com.example.novel2script.model.Character();
            character.setId(info.getId());
            character.setName(info.getName());
            character.setRole(info.getRole());
            character.setDescription("出现频率：" + info.getFrequency());
            character.setFirstAppear("scene_001");
            characters.add(character);
        }
        script.setCharacters(characters);
        
        // 转换场景（使用SceneSplitter的结果）
        List<Scene> scenes = new ArrayList<>();
        Map<String, String> nameToId = characterInfosToMap(result.getCharacterInfos());
        
        if (result.getSceneInfos() != null && !result.getSceneInfos().isEmpty()) {
            for (SceneInfo sceneInfo : result.getSceneInfos()) {
                Scene scene = createSceneFromInfo(sceneInfo, nameToId, result.getDialogueInfos());
                scenes.add(scene);
            }
        } else {
            // 如果没有场景信息，使用默认方法
            scenes = createDefaultScenes(result.getDialogueInfos(), nameToId);
        }
        
        script.setScenes(scenes);
        
        // 转换关系
        List<Relationship> relationships = new ArrayList<>();
        for (RelationshipInfo info : result.getRelationshipInfos()) {
            Relationship relationship = new Relationship();
            
            String fromId = nameToId.get(info.getFrom());
            String toId = nameToId.get(info.getTo());
            
            if (fromId != null && toId != null) {
                relationship.setFrom(fromId);
                relationship.setTo(toId);
                relationship.setType(relationshipInfoTypeToRelationshipType(info.getType()));
                relationship.setDescription(info.getDescription());
                relationships.add(relationship);
            }
        }
        script.setRelationships(relationships);
        
        // 更新元信息
        meta.setTotalCharacters(characters.size());
        meta.setTotalScenes(scenes.size());
        
        result.setScript(script);
    }

    /**
     * 从SceneInfo创建Scene
     */
    private Scene createSceneFromInfo(SceneInfo sceneInfo, 
                                     Map<String, String> nameToId,
                                     List<DialogueInfo> allDialogues) {
        Scene scene = new Scene();
        scene.setId("scene_" + String.format("%03d", sceneInfo.getNumber()));
        scene.setNumber(sceneInfo.getNumber());
        
        // 地点
        Location location = new Location();
        location.setName(sceneInfo.getLocation());
        location.setType(Location.LocationType.fixed);
        location.setInterior(isInterior(sceneInfo.getLocation()));
        location.setDescription(sceneInfo.getDescription());
        scene.setLocation(location);
        
        // 时间
        TimeSetting time = new TimeSetting();
        time.setPeriod(parseTimePeriod(sceneInfo.getTime()));
        time.setSpecific(sceneInfo.getTime());
        scene.setTime(time);
        
        // 描述和氛围
        scene.setDescription(sceneInfo.getDescription());
        scene.setAtmosphere(sceneInfo.getAtmosphere());
        
        // 转换对话为beats
        List<Beat> beats = createBeatsForScene(sceneInfo, nameToId, allDialogues);
        scene.setBeats(beats);
        
        return scene;
    }

    /**
     * 创建beats
     */
    private List<Beat> createBeatsForScene(SceneInfo sceneInfo,
                                           Map<String, String> nameToId,
                                           List<DialogueInfo> allDialogues) {
        List<Beat> beats = new ArrayList<>();
        
        // 添加舞台指示
        if (sceneInfo.getStageDirections() != null) {
            for (String direction : sceneInfo.getStageDirections()) {
                Beat beat = new Beat();
                beat.setType(Beat.BeatType.action);
                beat.setContent(direction);
                beats.add(beat);
            }
        }
        
        // 简单处理：添加这个场景相关的对话
        // 实际项目中应该更精确地匹配对话到场景
        int startIndex = (sceneInfo.getNumber() - 1) * 10;
        int endIndex = Math.min(startIndex + 15, allDialogues.size());
        
        for (int i = startIndex; i < endIndex && i < allDialogues.size(); i++) {
            DialogueInfo dialogue = allDialogues.get(i);
            
            Beat beat = new Beat();
            beat.setType(Beat.BeatType.dialogue);
            beat.setContent(dialogue.getContent());
            
            // 使用EmotionAnnotator分析情绪
            Beat.Emotion emotion = emotionAnnotator.analyzeEmotion(dialogue.getContent());
            beat.setEmotion(emotion);
            beat.setIntensity(emotionAnnotator.analyzeIntensity(dialogue.getContent(), emotion));
            
            // 查找说话人ID
            String speakerId = nameToId.get(dialogue.getSpeaker());
            if (speakerId != null) {
                beat.setSpeaker(speakerId);
            }
            
            beats.add(beat);
        }
        
        // 如果没有对话，添加一个叙述
        if (beats.isEmpty()) {
            Beat beat = new Beat();
            beat.setType(Beat.BeatType.narration);
            beat.setContent(sceneInfo.getDescription().substring(0, Math.min(200, sceneInfo.getDescription().length())));
            beats.add(beat);
        }
        
        return beats;
    }

    /**
     * 创建默认场景
     */
    private List<Scene> createDefaultScenes(List<DialogueInfo> dialogues, Map<String, String> nameToId) {
        List<Scene> scenes = new ArrayList<>();
        int sceneNumber = 1;
        List<Beat> beats = new ArrayList<>();
        
        for (DialogueInfo dialogue : dialogues) {
            Beat beat = new Beat();
            beat.setType(Beat.BeatType.dialogue);
            beat.setContent(dialogue.getContent());
            
            // 使用EmotionAnnotator分析情绪
            Beat.Emotion emotion = emotionAnnotator.analyzeEmotion(dialogue.getContent());
            beat.setEmotion(emotion);
            beat.setIntensity(emotionAnnotator.analyzeIntensity(dialogue.getContent(), emotion));
            
            // 查找说话人ID
            String speakerId = nameToId.get(dialogue.getSpeaker());
            if (speakerId != null) {
                beat.setSpeaker(speakerId);
            }
            
            beats.add(beat);
            
            // 每15个对话创建一个新场景
            if (beats.size() >= 15) {
                Scene scene = createBasicScene(sceneNumber++, beats);
                scenes.add(scene);
                beats = new ArrayList<>();
            }
        }
        
        // 添加剩余的对话
        if (!beats.isEmpty()) {
            Scene scene = createBasicScene(sceneNumber, beats);
            scenes.add(scene);
        }
        
        return scenes;
    }

    /**
     * 创建基础场景
     */
    private Scene createBasicScene(int number, List<Beat> beats) {
        Scene scene = new Scene();
        scene.setId("scene_" + String.format("%03d", number));
        scene.setNumber(number);
        
        Location location = new Location();
        location.setName("场景" + number);
        location.setType(Location.LocationType.fixed);
        location.setInterior(true);
        scene.setLocation(location);
        
        TimeSetting time = new TimeSetting();
        time.setPeriod(TimeSetting.Period.afternoon);
        scene.setTime(time);
        
        scene.setDescription("从对话分析生成的场景");
        scene.setBeats(beats);
        
        return scene;
    }

    /**
     * 判断是否是室内场景
     */
    private boolean isInterior(String location) {
        if (location == null) return true;
        String lower = location.toLowerCase();
        return lower.contains("房间") || lower.contains("内") || 
               lower.contains("里") || lower.contains("室") ||
               !lower.contains("公园") && !lower.contains("路") && 
               !lower.contains("街") && !lower.contains("外");
    }

    /**
     * 解析时间段
     */
    private TimeSetting.Period parseTimePeriod(String time) {
        if (time == null) return TimeSetting.Period.unknown;
        
        String lower = time.toLowerCase();
        if (lower.contains("早上") || lower.contains("早晨") || lower.contains("清晨")) {
            return TimeSetting.Period.morning;
        } else if (lower.contains("中午") || lower.contains("正午")) {
            return TimeSetting.Period.noon;
        } else if (lower.contains("下午")) {
            return TimeSetting.Period.afternoon;
        } else if (lower.contains("傍晚") || lower.contains("黄昏")) {
            return TimeSetting.Period.evening;
        } else if (lower.contains("晚上") || lower.contains("夜晚")) {
            return TimeSetting.Period.night;
        } else if (lower.contains("深夜") || lower.contains("午夜")) {
            return TimeSetting.Period.midnight;
        } else if (lower.contains("黎明")) {
            return TimeSetting.Period.dawn;
        } else {
            return TimeSetting.Period.unknown;
        }
    }

    /**
     * 人物信息列表转换为名字到ID的映射
     */
    private Map<String, String> characterInfosToMap(List<CharacterInfo> infos) {
        Map<String, String> map = new HashMap<>();
        for (CharacterInfo info : infos) {
            map.put(info.getName(), info.getId());
        }
        return map;
    }

    /**
     * 从文本提取标题
     */
    private String extractTitle(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "小说剧本";
        }
        
        String[] lines = text.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            
            // 尝试找标题
            if (line.startsWith("#") || line.startsWith("《") || line.startsWith("[")) {
                line = line.replaceAll("^[#\\[《]+", "").replaceAll("[#\\]》]+$", "").trim();
                if (!line.isEmpty()) {
                    return line;
                }
            }
            
            // 第一章之类的
            if (line.length() <= 30 && !line.contains("。") && !line.contains("，") && !line.contains("：")) {
                return line;
            }
            
            break;
        }
        
        return "小说剧本";
    }

    /**
     * 关系信息类型转换为关系类型
     */
    private Relationship.RelationshipType relationshipInfoTypeToRelationshipType(
            RelationshipAnalyzer.RelationshipType type) {
        if (type == null) return Relationship.RelationshipType.friend;
        
        switch (type) {
            case family:
                return Relationship.RelationshipType.family;
            case lover:
                return Relationship.RelationshipType.lover;
            case enemy:
                return Relationship.RelationshipType.enemy;
            case colleague:
                return Relationship.RelationshipType.colleague;
            default:
                return Relationship.RelationshipType.friend;
        }
    }

    /**
     * 分析结果类
     */
    public static class AnalysisResult {
        private List<CharacterInfo> characterInfos;
        private List<DialogueInfo> dialogueInfos;
        private DialogueStatistics dialogueStatistics;
        private List<RelationshipInfo> relationshipInfos;
        private List<SceneInfo> sceneInfos;
        private Script script;

        public List<CharacterInfo> getCharacterInfos() {
            return characterInfos;
        }

        public void setCharacterInfos(List<CharacterInfo> characterInfos) {
            this.characterInfos = characterInfos;
        }

        public List<DialogueInfo> getDialogueInfos() {
            return dialogueInfos;
        }

        public void setDialogueInfos(List<DialogueInfo> dialogueInfos) {
            this.dialogueInfos = dialogueInfos;
        }

        public DialogueStatistics getDialogueStatistics() {
            return dialogueStatistics;
        }

        public void setDialogueStatistics(DialogueStatistics dialogueStatistics) {
            this.dialogueStatistics = dialogueStatistics;
        }

        public List<RelationshipInfo> getRelationshipInfos() {
            return relationshipInfos;
        }

        public void setRelationshipInfos(List<RelationshipInfo> relationshipInfos) {
            this.relationshipInfos = relationshipInfos;
        }

        public List<SceneInfo> getSceneInfos() {
            return sceneInfos;
        }

        public void setSceneInfos(List<SceneInfo> sceneInfos) {
            this.sceneInfos = sceneInfos;
        }

        public Script getScript() {
            return script;
        }

        public void setScript(Script script) {
            this.script = script;
        }
    }
}

package com.example.novel2script.demo;

import com.example.novel2script.model.*;
import com.example.novel2script.util.IDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Component
public class SimpleScriptDemo implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SimpleScriptDemo.class);
    
    private final IDGenerator idGenerator;
    
    @Autowired
    public SimpleScriptDemo(IDGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("=== 开始演示：创建示例剧本 ===");
        
        Script script = createSampleScript();
        logger.info("剧本创建成功");
        
        saveScriptToYaml(script, "examples/output/sample-script.yaml");
        logger.info("剧本已保存到: examples/output/sample-script.yaml");
        
        logger.info("=== 演示完成 ===");
    }
    
    private Script createSampleScript() {
        Script script = new Script();
        script.setSchemaVersion("1.0");
        
        // 元信息
        MetaInformation meta = new MetaInformation();
        meta.setTitle("咖啡厅偶遇");
        meta.setSource("示例小说");
        meta.setAuthor("示例作者");
        meta.setAdapter("AI小说转剧本工具");
        meta.setCreatedAt(LocalDate.now());
        meta.setUpdatedAt(LocalDate.now());
        meta.setVersion("1.0");
        meta.setGenre(Arrays.asList("都市", "情感"));
        meta.setSynopsis("张三在咖啡厅偶遇多年未见的老友李四，两人相谈甚欢。");
        script.setMeta(meta);
        
        // 人物
        String charId1 = idGenerator.generateCharacterId();
        String charId2 = idGenerator.generateCharacterId();
        String charId3 = idGenerator.generateCharacterId();
        
        com.example.novel2script.model.Character char1 = new com.example.novel2script.model.Character();
        char1.setId(charId1);
        char1.setName("张三");
        char1.setRole(com.example.novel2script.model.Character.Role.protagonist);
        char1.setDescription("30岁，程序员，性格内向");
        char1.setFirstAppear("scene_001");
        char1.setAliases(Arrays.asList("老张"));
        char1.setAttributes(Map.of("age", 30, "job", "程序员"));
        
        com.example.novel2script.model.Character char2 = new com.example.novel2script.model.Character();
        char2.setId(charId2);
        char2.setName("李四");
        char2.setRole(com.example.novel2script.model.Character.Role.supporting);
        char2.setDescription("张三的大学同学，现为创业者");
        char2.setFirstAppear("scene_001");
        
        com.example.novel2script.model.Character char3 = new com.example.novel2script.model.Character();
        char3.setId(charId3);
        char3.setName("服务员");
        char3.setRole(com.example.novel2script.model.Character.Role.minor);
        char3.setDescription("咖啡厅的服务员");
        char3.setFirstAppear("scene_001");
        
        script.setCharacters(Arrays.asList(char1, char2, char3));
        
        // 场景
        List<Scene> scenes = new ArrayList<>();
        
        Scene scene1 = new Scene();
        scene1.setId("scene_001");
        scene1.setNumber(1);
        
        Location loc1 = new Location();
        loc1.setName("咖啡厅");
        loc1.setType(Location.LocationType.public_space);
        loc1.setInterior(true);
        loc1.setDescription("装修简约的咖啡厅");
        scene1.setLocation(loc1);
        
        TimeSetting time1 = new TimeSetting();
        time1.setPeriod(TimeSetting.Period.afternoon);
        time1.setSpecific("周末下午");
        scene1.setTime(time1);
        
        scene1.setDescription("一家安静的咖啡厅");
        scene1.setAtmosphere("轻松");
        
        List<Beat> beats1 = new ArrayList<>();
        
        Beat beat1 = new Beat();
        beat1.setType(Beat.BeatType.action);
        beat1.setContent("张三推门走进咖啡厅，环顾四周");
        beat1.setSubject(charId1);
        beats1.add(beat1);
        
        Beat beat2 = new Beat();
        beat2.setType(Beat.BeatType.action);
        beat2.setContent("李四坐在靠窗的位置，向张三招手");
        beat2.setSubject(charId2);
        beats1.add(beat2);
        
        Beat beat3 = new Beat();
        beat3.setType(Beat.BeatType.dialogue);
        beat3.setSpeaker(charId2);
        beat3.setContent("老张！这边！");
        beat3.setEmotion(Beat.Emotion.happy);
        beat3.setDirection("站起身招手");
        beats1.add(beat3);
        
        Beat beat4 = new Beat();
        beat4.setType(Beat.BeatType.action);
        beat4.setContent("张三走过去，在李四对面坐下");
        beat4.setSubject(charId1);
        beats1.add(beat4);
        
        Beat beat5 = new Beat();
        beat5.setType(Beat.BeatType.dialogue);
        beat5.setSpeaker(charId1);
        beat5.setContent("好久不见，你还是老样子。");
        beat5.setEmotion(Beat.Emotion.happy);
        beat5.setTo(charId2);
        beats1.add(beat5);
        
        Beat beat6 = new Beat();
        beat6.setType(Beat.BeatType.dialogue);
        beat6.setSpeaker(charId2);
        beat6.setContent("你也是，还是这么准时。");
        beat6.setEmotion(Beat.Emotion.happy);
        beat6.setTo(charId1);
        beats1.add(beat6);
        
        scene1.setBeats(beats1);
        scenes.add(scene1);
        
        Scene scene2 = new Scene();
        scene2.setId("scene_002");
        scene2.setNumber(2);
        scene2.setLocation(loc1);
        
        TimeSetting time2 = new TimeSetting();
        time2.setPeriod(TimeSetting.Period.afternoon);
        time2.setContinuity("紧接着上一场");
        scene2.setTime(time2);
        
        scene2.setDescription("同一咖啡厅，两人已点好咖啡");
        
        List<Beat> beats2 = new ArrayList<>();
        
        Beat beat7 = new Beat();
        beat7.setType(Beat.BeatType.dialogue);
        beat7.setSpeaker(charId3);
        beat7.setContent("两位的拿铁，请慢用。");
        beat7.setEmotion(Beat.Emotion.neutral);
        beats2.add(beat7);
        
        Beat beat8 = new Beat();
        beat8.setType(Beat.BeatType.action);
        beat8.setContent("服务员放下咖啡离开");
        beat8.setSubject(charId3);
        beats2.add(beat8);
        
        Beat beat9 = new Beat();
        beat9.setType(Beat.BeatType.dialogue);
        beat9.setSpeaker(charId1);
        beat9.setContent("说吧，找我什么事？");
        beat9.setEmotion(Beat.Emotion.neutral);
        beat9.setTo(charId2);
        beats2.add(beat9);
        
        Beat beat10 = new Beat();
        beat10.setType(Beat.BeatType.dialogue);
        beat10.setSpeaker(charId2);
        beat10.setContent("有个项目想和你合作...");
        beat10.setEmotion(Beat.Emotion.happy);
        beat10.setTo(charId1);
        beats2.add(beat10);
        
        scene2.setBeats(beats2);
        scenes.add(scene2);
        
        script.setScenes(scenes);
        
        // 人物关系
        Relationship rel1 = new Relationship();
        rel1.setFrom(charId1);
        rel1.setTo(charId2);
        rel1.setType(Relationship.RelationshipType.friend);
        rel1.setDescription("大学同学，多年好友");
        script.setRelationships(Collections.singletonList(rel1));
        
        script.setNotes("这是一个演示剧本，展示 YAML 输出格式。");
        
        meta.setTotalScenes(scenes.size());
        meta.setTotalCharacters(3);
        
        return script;
    }
    
    private void saveScriptToYaml(Script script, String filePath) throws IOException {
        // 创建 Map 结构用于 YAML 输出
        Map<String, Object> scriptMap = new LinkedHashMap<>();
        scriptMap.put("schema_version", script.getSchemaVersion());
        
        Map<String, Object> metaMap = new LinkedHashMap<>();
        metaMap.put("title", script.getMeta().getTitle());
        metaMap.put("source", script.getMeta().getSource());
        metaMap.put("author", script.getMeta().getAuthor());
        metaMap.put("adapter", script.getMeta().getAdapter());
        metaMap.put("created_at", script.getMeta().getCreatedAt().toString());
        metaMap.put("updated_at", script.getMeta().getUpdatedAt().toString());
        metaMap.put("version", script.getMeta().getVersion());
        metaMap.put("genre", script.getMeta().getGenre());
        metaMap.put("synopsis", script.getMeta().getSynopsis());
        metaMap.put("total_scenes", script.getMeta().getTotalScenes());
        metaMap.put("total_characters", script.getMeta().getTotalCharacters());
        scriptMap.put("meta", metaMap);
        
        List<Map<String, Object>> charList = new ArrayList<>();
        for (com.example.novel2script.model.Character ch : script.getCharacters()) {
            Map<String, Object> charMap = new LinkedHashMap<>();
            charMap.put("id", ch.getId());
            charMap.put("name", ch.getName());
            charMap.put("role", ch.getRole().name());
            charMap.put("description", ch.getDescription());
            charMap.put("first_appear", ch.getFirstAppear());
            charMap.put("aliases", ch.getAliases());
            charMap.put("attributes", ch.getAttributes());
            charList.add(charMap);
        }
        scriptMap.put("characters", charList);
        
        List<Map<String, Object>> sceneList = new ArrayList<>();
        for (Scene scene : script.getScenes()) {
            Map<String, Object> sceneMap = new LinkedHashMap<>();
            sceneMap.put("id", scene.getId());
            sceneMap.put("number", scene.getNumber());
            
            Map<String, Object> locMap = new LinkedHashMap<>();
            locMap.put("name", scene.getLocation().getName());
            locMap.put("type", scene.getLocation().getType().name());
            locMap.put("interior", scene.getLocation().getInterior());
            locMap.put("description", scene.getLocation().getDescription());
            sceneMap.put("location", locMap);
            
            Map<String, Object> timeMap = new LinkedHashMap<>();
            timeMap.put("period", scene.getTime().getPeriod().name());
            timeMap.put("specific", scene.getTime().getSpecific());
            timeMap.put("continuity", scene.getTime().getContinuity());
            sceneMap.put("time", timeMap);
            
            sceneMap.put("description", scene.getDescription());
            sceneMap.put("atmosphere", scene.getAtmosphere());
            
            List<Map<String, Object>> beatList = new ArrayList<>();
            for (Beat beat : scene.getBeats()) {
                Map<String, Object> beatMap = new LinkedHashMap<>();
                beatMap.put("type", beat.getType().name());
                beatMap.put("content", beat.getContent());
                
                if (beat.getSubject() != null) beatMap.put("subject", beat.getSubject());
                if (beat.getSpeaker() != null) beatMap.put("speaker", beat.getSpeaker());
                if (beat.getEmotion() != null) beatMap.put("emotion", beat.getEmotion().name());
                if (beat.getIntensity() != null) beatMap.put("intensity", beat.getIntensity().name());
                if (beat.getDirection() != null) beatMap.put("direction", beat.getDirection());
                if (beat.getTo() != null) beatMap.put("to", beat.getTo());
                
                beatList.add(beatMap);
            }
            sceneMap.put("beats", beatList);
            
            sceneList.add(sceneMap);
        }
        scriptMap.put("scenes", sceneList);
        
        if (script.getRelationships() != null && !script.getRelationships().isEmpty()) {
            List<Map<String, Object>> relList = new ArrayList<>();
            for (Relationship rel : script.getRelationships()) {
                Map<String, Object> relMap = new LinkedHashMap<>();
                relMap.put("from", rel.getFrom());
                relMap.put("to", rel.getTo());
                relMap.put("type", rel.getType().name());
                relMap.put("description", rel.getDescription());
                relList.add(relMap);
            }
            scriptMap.put("relationships", relList);
        }
        
        if (script.getNotes() != null) {
            scriptMap.put("notes", script.getNotes());
        }
        
        Map<String, Object> rootMap = new LinkedHashMap<>();
        rootMap.put("script", scriptMap);
        
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        
        Yaml yaml = new Yaml(options);
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
            yaml.dump(rootMap, writer);
        }
    }
}

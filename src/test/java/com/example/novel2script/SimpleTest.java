package com.example.novel2script;

import com.example.novel2script.analyzer.*;
import com.example.novel2script.model.*;

public class SimpleTest {

    public static void main(String[] args) {
        String novelText = "# 示例小说文本\n\n" +
                "张三推门走进咖啡厅，环顾四周。午后的阳光透过落地窗洒进来，给整个空间镀上一层温暖的金色。\n\n" +
                "李四坐在靠窗的位置，看到张三进来，连忙站起身招手：\"老张！这边！\"\n\n" +
                "张三走过去，在李四对面坐下。\"好久不见，你还是老样子。\"\n\n" +
                "李四笑着摇摇头：\"你也是，还是这么准时。说吧，找我什么事？\"\n\n" +
                "\"有个项目想和你合作...\"张三压低声音，神情变得严肃起来。\n\n" +
                "服务员走过来，放下两杯拿铁：\"两位的拿铁，请慢用。\"\n\n" +
                "李四端起咖啡，轻轻吹了吹：\"先喝口咖啡，慢慢说。\"";

        System.out.println("=== 开始测试完整分析 ===\n");
        
        // 创建各个分析器实例
        CharacterAnalyzer characterAnalyzer = new CharacterAnalyzer();
        DialogueExtractor dialogueExtractor = new DialogueExtractor();
        SceneSplitter sceneSplitter = new SceneSplitter();
        EmotionAnnotator emotionAnnotator = new EmotionAnnotator();
        AnalysisManager analysisManager = new AnalysisManager();
        
        // 手动注入依赖（因为不是 Spring 环境）
        try {
            java.lang.reflect.Field charField = AnalysisManager.class.getDeclaredField("characterAnalyzer");
            charField.setAccessible(true);
            charField.set(analysisManager, characterAnalyzer);
            
            java.lang.reflect.Field dialogueField = AnalysisManager.class.getDeclaredField("dialogueExtractor");
            dialogueField.setAccessible(true);
            dialogueField.set(analysisManager, dialogueExtractor);
            
            java.lang.reflect.Field sceneField = AnalysisManager.class.getDeclaredField("sceneSplitter");
            sceneField.setAccessible(true);
            sceneField.set(analysisManager, sceneSplitter);
            
            java.lang.reflect.Field emotionField = AnalysisManager.class.getDeclaredField("emotionAnnotator");
            emotionField.setAccessible(true);
            emotionField.set(analysisManager, emotionAnnotator);
            
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        // 测试完整分析
        AnalysisManager.AnalysisResult result = analysisManager.analyze(novelText);
        Script script = result.getScript();
        
        System.out.println("\n=== 分析结果 ===");
        System.out.println("人物数量: " + script.getCharacters().size());
        script.getCharacters().forEach(c -> 
            System.out.println("  - " + c.getName() + " (" + c.getRole() + ")")
        );
        
        System.out.println("\n场景数量: " + script.getScenes().size());
        script.getScenes().forEach(s -> {
            System.out.println("\n场景 " + s.getNumber() + ": " + s.getLocation() + " @ " + s.getTime());
            System.out.println("  Beats 数量: " + s.getBeats().size());
            s.getBeats().forEach(b -> {
                String speaker = b.getSpeaker() != null ? b.getSpeaker() : "旁白";
                System.out.println("    [" + b.getType() + "] " + speaker + ": " + b.getContent());
            });
        });
        
        System.out.println("\n=== 测试完成 ===");
    }
}

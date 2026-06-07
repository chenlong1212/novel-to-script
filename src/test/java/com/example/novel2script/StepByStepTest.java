package com.example.novel2script;

import com.example.novel2script.analyzer.*;
import com.example.novel2script.model.*;

import java.util.List;

public class StepByStepTest {

    public static void main(String[] args) {
        String novelText = "# 示例小说文本\n\n" +
                "张三推门走进咖啡厅，环顾四周。午后的阳光透过落地窗洒进来，给整个空间镀上一层温暖的金色。\n\n" +
                "李四坐在靠窗的位置，看到张三进来，连忙站起身招手：\"老张！这边！\"\n\n" +
                "张三走过去，在李四对面坐下。\"好久不见，你还是老样子。\"\n\n" +
                "李四笑着摇摇头：\"你也是，还是这么准时。说吧，找我什么事？\"\n\n" +
                "\"有个项目想和你合作...\"张三压低声音，神情变得严肃起来。\n\n" +
                "服务员走过来，放下两杯拿铁：\"两位的拿铁，请慢用。\"\n\n" +
                "李四端起咖啡，轻轻吹了吹：\"先喝口咖啡，慢慢说。\"";

        System.out.println("=== 开始分步测试 ===\n");
        
        // 1. 测试人物分析
        System.out.println("--- 1. 人物分析 ---");
        CharacterAnalyzer characterAnalyzer = new CharacterAnalyzer();
        List<CharacterAnalyzer.CharacterInfo> characters = characterAnalyzer.analyzeCharacters(novelText);
        System.out.println("识别到 " + characters.size() + " 个人物:");
        for (CharacterAnalyzer.CharacterInfo c : characters) {
            System.out.println("  - " + c.getName() + " (出现 " + c.getFrequency() + " 次, " + c.getRole() + ")");
        }
        
        // 2. 测试对话提取
        System.out.println("\n--- 2. 对话提取 ---");
        DialogueExtractor dialogueExtractor = new DialogueExtractor();
        List<DialogueExtractor.DialogueInfo> dialogues = dialogueExtractor.extractDialogues(novelText);
        System.out.println("提取到 " + dialogues.size() + " 条对话:");
        for (DialogueExtractor.DialogueInfo d : dialogues) {
            System.out.println("  - " + d.getSpeaker() + ": " + d.getContent() + " [" + d.getEmotion() + "]");
        }
        
        // 3. 测试场景分割
        System.out.println("\n--- 3. 场景分割 ---");
        SceneSplitter sceneSplitter = new SceneSplitter();
        List<SceneSplitter.SceneInfo> scenes = sceneSplitter.splitScenes(novelText);
        System.out.println("分割到 " + scenes.size() + " 个场景:");
        for (SceneSplitter.SceneInfo s : scenes) {
            System.out.println("  - 场景 " + s.getNumber() + ": " + s.getLocation() + " @ " + s.getTime());
            System.out.println("    描述: " + s.getDescription().substring(0, Math.min(50, s.getDescription().length())) + "...");
        }
        
        System.out.println("\n=== 测试完成 ===");
    }
}

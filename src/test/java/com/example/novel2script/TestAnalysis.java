package com.example.novel2script;

import com.example.novel2script.analyzer.AnalysisManager;
import com.example.novel2script.analyzer.CharacterAnalyzer;
import com.example.novel2script.analyzer.DialogueExtractor;
import com.example.novel2script.analyzer.SceneSplitter;
import com.example.novel2script.model.Script;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestAnalysis {

    @Autowired
    private AnalysisManager analysisManager;

    @Autowired
    private CharacterAnalyzer characterAnalyzer;

    @Autowired
    private DialogueExtractor dialogueExtractor;

    @Autowired
    private SceneSplitter sceneSplitter;

    @Test
    public void testFullAnalysis() {
        String novelText = "# 示例小说文本\n\n" +
                "张三推门走进咖啡厅，环顾四周。午后的阳光透过落地窗洒进来，给整个空间镀上一层温暖的金色。\n\n" +
                "李四坐在靠窗的位置，看到张三进来，连忙站起身招手：\"老张！这边！\"\n\n" +
                "张三走过去，在李四对面坐下。\"好久不见，你还是老样子。\"\n\n" +
                "李四笑着摇摇头：\"你也是，还是这么准时。说吧，找我什么事？\"\n\n" +
                "\"有个项目想和你合作...\"张三压低声音，神情变得严肃起来。\n\n" +
                "服务员走过来，放下两杯拿铁：\"两位的拿铁，请慢用。\"\n\n" +
                "李四端起咖啡，轻轻吹了吹：\"先喝口咖啡，慢慢说。\"";

        System.out.println("=== 开始测试完整分析 ===");
        
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

package com.example.novel2script.test;

import com.example.novel2script.analyzer.AnalysisManager;
import com.example.novel2script.analyzer.Proofreader;
import com.example.novel2script.analyzer.QualityEvaluator;
import com.example.novel2script.converter.NovelToScriptConverter;
import com.example.novel2script.model.*;
import com.example.novel2script.reader.NovelReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 测试运行器
 * 用于测试各个组件功能
 */
// @Component  // 注释掉，防止启动时自动运行示例
public class TestRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(TestRunner.class);

    @Autowired
    private NovelToScriptConverter converter;
    
    @Autowired
    private NovelReader novelReader;
    
    @Autowired
    private AnalysisManager analysisManager;
    
    @Autowired
    private QualityEvaluator qualityEvaluator;
    
    @Autowired
    private Proofreader proofreader;

    @Override
    public void run(String... args) throws Exception {
        logger.info("=== AI 小说转剧本工具 v0.5 ===");
        logger.info("DeepSeek API 客户端已初始化");
        logger.info("小说转换器已就绪");
        
        // 测试质量评估和校对功能
        testQualityEvaluationAndProofreading();
        
        logger.info("=== 系统准备完成 ===");
    }
    
    /**
     * 测试质量评估和校对功能
     */
    private void testQualityEvaluationAndProofreading() {
        logger.info("开始测试质量评估和校对功能...");
        
        try {
            // 1. 读取示例小说
            String novelText = novelReader.readNovel("examples/input/sample_novel.txt");
            logger.info("读取示例小说成功，长度: {} 字符", novelText.length());
            
            // 2. 进行完整分析
            AnalysisManager.AnalysisResult analysisResult = analysisManager.analyze(novelText);
            Script script = analysisResult.getScript();
            logger.info("分析完成，生成剧本: {}", script.getMeta() != null ? script.getMeta().getTitle() : "unknown");
            
            // 3. 进行质量评估
            QualityReport qualityReport = qualityEvaluator.evaluate(script, novelText);
            logger.info("质量评估完成:");
            logger.info("  综合评分: {}/1.0", qualityReport.getOverallScore());
            logger.info("  等级: {}", qualityReport.getGrade());
            logger.info("  人物准确率: {}", qualityReport.getCharacterAccuracy());
            logger.info("  对话覆盖率: {}", qualityReport.getDialogueCoverage());
            logger.info("  场景完整性: {}", qualityReport.getSceneCompleteness());
            logger.info("  情绪准确率: {}", qualityReport.getEmotionAccuracy());
            logger.info("  格式有效性: {}", qualityReport.getFormatValidity());
            logger.info("  问题数量: {}", qualityReport.getIssues().size());
            
            if (!qualityReport.getSuggestions().isEmpty()) {
                logger.info("  改进建议:");
                for (String suggestion : qualityReport.getSuggestions()) {
                    logger.info("    - {}", suggestion);
                }
            }
            
            // 4. 测试校对功能
            String scriptId = script.getMeta() != null ? script.getMeta().getTitle() : "test_script";
            
            // 创建一个示例修正记录
            if (script.getCharacters() != null && !script.getCharacters().isEmpty()) {
                com.example.novel2script.model.Character firstChar = script.getCharacters().get(0);
                Correction correction = proofreader.createCorrection(
                    scriptId,
                    Correction.CorrectionType.CHARACTER,
                    firstChar.getId(),
                    "description",
                    firstChar.getDescription(),
                    "这是一个更详细的人物描述",
                    "人工校对测试"
                );
                logger.info("创建修正记录: {}", correction.getCorrectionId());
                
                // 应用修正
                proofreader.applyCorrection(correction, script);
                logger.info("应用修正成功");
            }
            
            // 生成建议修正
            List<Correction> suggestedCorrections = proofreader.generateSuggestedCorrections(script, qualityReport);
            logger.info("生成 {} 条建议修正", suggestedCorrections.size());
            
            logger.info("质量评估和校对功能测试完成");
            
        } catch (Exception e) {
            logger.error("测试过程中出错", e);
        }
    }
}

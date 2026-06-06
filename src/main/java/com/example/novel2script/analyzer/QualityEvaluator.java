package com.example.novel2script.analyzer;

import com.example.novel2script.model.*;
import com.example.novel2script.model.QualityReport.QualityIssue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 质量评估器
 */
@Component
public class QualityEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(QualityEvaluator.class);

    /**
     * 评估剧本质量
     */
    public QualityReport evaluate(Script script, String originalText) {
        logger.info("开始质量评估...");
        
        QualityReport report = new QualityReport();
        report.setScriptId(script.getMeta() != null ? script.getMeta().getTitle() : "unknown");
        
        // 1. 评估人物识别准确率
        double characterAccuracy = evaluateCharacterAccuracy(script, report);
        report.setCharacterAccuracy(characterAccuracy);
        
        // 2. 评估对话覆盖率
        double dialogueCoverage = evaluateDialogueCoverage(script, report);
        report.setDialogueCoverage(dialogueCoverage);
        
        // 3. 评估场景完整性
        double sceneCompleteness = evaluateSceneCompleteness(script, report);
        report.setSceneCompleteness(sceneCompleteness);
        
        // 4. 评估情绪准确率
        double emotionAccuracy = evaluateEmotionAccuracy(script, report);
        report.setEmotionAccuracy(emotionAccuracy);
        
        // 5. 评估格式有效性
        double formatValidity = evaluateFormatValidity(script, report);
        report.setFormatValidity(formatValidity);
        
        // 6. 计算综合评分
        report.calculateOverallScore();
        
        // 7. 生成建议
        generateSuggestions(report);
        
        logger.info("质量评估完成，综合评分: {}", report.getGrade());
        
        return report;
    }
    
    /**
     * 评估人物识别准确率
     */
    private double evaluateCharacterAccuracy(Script script, QualityReport report) {
        if (script.getCharacters() == null || script.getCharacters().isEmpty()) {
            report.addIssue(new QualityIssue("character", "high", "未识别到任何人物"));
            return 0.0;
        }
        
        int validCount = 0;
        int totalCount = 0;
        
        for (com.example.novel2script.model.Character character : script.getCharacters()) {
            totalCount++;
            boolean isValid = true;
            
            // 检查ID和名称
            if (character.getId() == null || character.getId().isEmpty()) {
                isValid = false;
            }
            if (character.getName() == null || character.getName().isEmpty()) {
                isValid = false;
            }
            if (character.getRole() == null) {
                isValid = false;
            }
            
            if (isValid) {
                validCount++;
            } else {
                report.addIssue(new QualityIssue("character", "medium",
                    "人物信息不完整: " + character.getName()));
            }
            
            // 检查描述
            if (character.getDescription() == null || character.getDescription().isEmpty()) {
                report.addIssue(new QualityIssue("character", "low",
                    "人物描述缺失: " + character.getName()));
            }
        }
        
        double accuracy = (double) validCount / totalCount;
        return accuracy;
    }
    
    /**
     * 评估对话覆盖率
     */
    private double evaluateDialogueCoverage(Script script, QualityReport report) {
        if (script.getScenes() == null || script.getScenes().isEmpty()) {
            return 0.5;
        }
        
        int totalDialogues = 0;
        int identifiedDialogues = 0;
        
        for (Scene scene : script.getScenes()) {
            if (scene.getBeats() != null) {
                for (Beat beat : scene.getBeats()) {
                    if (beat.getType() == Beat.BeatType.dialogue) {
                        totalDialogues++;
                        if (beat.getSpeaker() != null && !"未知".equals(beat.getSpeaker())) {
                            identifiedDialogues++;
                        }
                    }
                }
            }
        }
        
        if (totalDialogues == 0) {
            return 1.0;
        }
        
        double coverage = (double) identifiedDialogues / totalDialogues;
        return coverage;
    }
    
    /**
     * 评估场景完整性
     */
    private double evaluateSceneCompleteness(Script script, QualityReport report) {
        if (script.getScenes() == null || script.getScenes().isEmpty()) {
            report.addIssue(new QualityIssue("scene", "high", "无场景"));
            return 0.0;
        }
        
        int completeCount = 0;
        
        for (Scene scene : script.getScenes()) {
            boolean complete = true;
            
            if (scene.getLocation() == null) {
                complete = false;
            } else if (scene.getLocation().getName() == null || scene.getLocation().getName().isEmpty()) {
                complete = false;
            } else if (scene.getBeats() == null || scene.getBeats().isEmpty()) {
                complete = false;
            }
            
            if (complete) {
                completeCount++;
            } else {
                report.addIssue(new QualityIssue("scene", "medium",
                    "场景信息不完整: " + scene.getId()));
            }
        }
        
        return (double) completeCount / script.getScenes().size();
    }
    
    /**
     * 评估情绪准确率
     */
    private double evaluateEmotionAccuracy(Script script, QualityReport report) {
        if (script.getScenes() == null) {
            return 0.5;
        }
        
        int emotionScore = 0;
        int totalCount = 0;
        
        for (Scene scene : script.getScenes()) {
            if (scene.getBeats() != null) {
                for (Beat beat : scene.getBeats()) {
                    if (beat.getType() == Beat.BeatType.dialogue) {
                        totalCount++;
                        if (beat.getEmotion() != null) {
                            emotionScore++;
                        }
                    }
                }
            }
        }
        
        if (totalCount == 0) {
            return 0.8;
        }
        
        return (double) emotionScore / totalCount;
    }
    
    /**
     * 评估格式有效性
     */
    private double evaluateFormatValidity(Script script, QualityReport report) {
        int score = 0;
        int total = 5;
        
        // 检查Schema版本
        if (script.getSchemaVersion() != null && !script.getSchemaVersion().isEmpty()) {
            score++;
        }
        
        // 检查元信息
        if (script.getMeta() != null && script.getMeta().getTitle() != null && !script.getMeta().getTitle().isEmpty()) {
            score++;
        }
        
        // 检查人物列表
        if (script.getCharacters() != null && !script.getCharacters().isEmpty()) {
            score++;
        }
        
        // 检查场景列表
        if (script.getScenes() != null && !script.getScenes().isEmpty()) {
            score++;
        }
        
        // 检查场景有序号
        boolean hasNumber = true;
        for (Scene scene : script.getScenes()) {
            if (scene.getNumber() <= 0) {
                hasNumber = false;
                break;
            }
        }
        if (hasNumber) {
            score++;
        }
        
        return (double) score / total;
    }
    
    /**
     * 生成改进建议
     */
    private void generateSuggestions(QualityReport report) {
        if (report.getCharacterAccuracy() < 0.85) {
            report.addSuggestion("建议检查人物识别规则，或提供更多上下文信息");
        }
        
        if (report.getDialogueCoverage() < 0.85) {
            report.addSuggestion("建议核对对话说话人识别，可能需要添加角色名表");
        }
        
        if (report.getSceneCompleteness() < 0.85) {
            report.addSuggestion("建议添加更多场景分隔标记");
        }
        
        if (report.getEmotionAccuracy() < 0.85) {
            report.addSuggestion("建议检查情绪识别，可能需要丰富表情词表");
        }
        
        if (report.getOverallScore() < 0.80) {
            report.addSuggestion("建议进行人工校对以提高质量");
        }
    }
}

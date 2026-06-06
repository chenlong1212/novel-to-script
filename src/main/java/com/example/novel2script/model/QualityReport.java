package com.example.novel2script.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 质量报告模型
 */
public class QualityReport {

    private String reportId;
    private String scriptId;
    private LocalDateTime generatedAt;
    private LocalDateTime updatedAt;
    
    // 评估指标
    private double characterAccuracy;    // 人物识别准确率
    private double dialogueCoverage;     // 对话覆盖率
    private double sceneCompleteness;    // 场景完整性
    private double emotionAccuracy;      // 情绪准确率
    private double formatValidity;       // 格式有效性
    private double overallScore;         // 综合评分
    
    // 问题列表
    private List<QualityIssue> issues = new ArrayList<>();
    
    // 建议
    private List<String> suggestions = new ArrayList<>();
    
    // 是否需要人工校对
    private boolean needsReview;
    
    public QualityReport() {
        this.reportId = "report_" + System.currentTimeMillis();
        this.generatedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getReportId() {
        return reportId;
    }
    
    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
    
    public String getScriptId() {
        return scriptId;
    }
    
    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public double getCharacterAccuracy() {
        return characterAccuracy;
    }
    
    public void setCharacterAccuracy(double characterAccuracy) {
        this.characterAccuracy = characterAccuracy;
        this.updatedAt = LocalDateTime.now();
    }
    
    public double getDialogueCoverage() {
        return dialogueCoverage;
    }
    
    public void setDialogueCoverage(double dialogueCoverage) {
        this.dialogueCoverage = dialogueCoverage;
        this.updatedAt = LocalDateTime.now();
    }
    
    public double getSceneCompleteness() {
        return sceneCompleteness;
    }
    
    public void setSceneCompleteness(double sceneCompleteness) {
        this.sceneCompleteness = sceneCompleteness;
        this.updatedAt = LocalDateTime.now();
    }
    
    public double getEmotionAccuracy() {
        return emotionAccuracy;
    }
    
    public void setEmotionAccuracy(double emotionAccuracy) {
        this.emotionAccuracy = emotionAccuracy;
        this.updatedAt = LocalDateTime.now();
    }
    
    public double getFormatValidity() {
        return formatValidity;
    }
    
    public void setFormatValidity(double formatValidity) {
        this.formatValidity = formatValidity;
        this.updatedAt = LocalDateTime.now();
    }
    
    public double getOverallScore() {
        return overallScore;
    }
    
    public void setOverallScore(double overallScore) {
        this.overallScore = overallScore;
        this.updatedAt = LocalDateTime.now();
    }
    
    public List<QualityIssue> getIssues() {
        return issues;
    }
    
    public void setIssues(List<QualityIssue> issues) {
        this.issues = issues;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void addIssue(QualityIssue issue) {
        this.issues.add(issue);
        this.updatedAt = LocalDateTime.now();
    }
    
    public List<String> getSuggestions() {
        return suggestions;
    }
    
    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void addSuggestion(String suggestion) {
        this.suggestions.add(suggestion);
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isNeedsReview() {
        return needsReview;
    }
    
    public void setNeedsReview(boolean needsReview) {
        this.needsReview = needsReview;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void calculateOverallScore() {
        double[] weights = {0.25, 0.20, 0.20, 0.20, 0.15};
        double[] scores = {
            characterAccuracy,
            dialogueCoverage,
            sceneCompleteness,
            emotionAccuracy,
            formatValidity
        };
        
        double total = 0.0;
        for (int i = 0; i < weights.length; i++) {
            total += weights[i] * scores[i];
        }
        
        this.overallScore = Math.max(0.0, Math.min(1.0, total));
        this.needsReview = this.overallScore < 0.8 || !this.issues.isEmpty();
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getGrade() {
        if (overallScore >= 0.95) return "S";
        if (overallScore >= 0.90) return "A";
        if (overallScore >= 0.80) return "B";
        if (overallScore >= 0.70) return "C";
        if (overallScore >= 0.60) return "D";
        return "F";
    }
    
    public String toSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 质量报告 ===\n");
        sb.append("综合评分: ").append(getGrade()).append(" (").append(String.format("%.1f%%", overallScore * 100)).append(")\n");
        sb.append("\n各项指标:\n");
        sb.append("- 人物识别准确率: ").append(String.format("%.1f%%", characterAccuracy * 100)).append("\n");
        sb.append("- 对话覆盖率: ").append(String.format("%.1f%%", dialogueCoverage * 100)).append("\n");
        sb.append("- 场景完整性: ").append(String.format("%.1f%%", sceneCompleteness * 100)).append("\n");
        sb.append("- 情绪准确率: ").append(String.format("%.1f%%", emotionAccuracy * 100)).append("\n");
        sb.append("- 格式有效性: ").append(String.format("%.1f%%", formatValidity * 100)).append("\n");
        
        if (!issues.isEmpty()) {
            sb.append("\n发现问题 (").append(issues.size()).append("个):\n");
            for (QualityIssue issue : issues) {
                sb.append("- [").append(issue.getSeverity()).append("] ")
                  .append(issue.getType()).append(": ")
                  .append(issue.getDetail()).append("\n");
            }
        }
        
        if (!suggestions.isEmpty()) {
            sb.append("\n建议:\n");
            for (String suggestion : suggestions) {
                sb.append("- ").append(suggestion).append("\n");
            }
        }
        
        if (needsReview) {
            sb.append("\n⚠️ 建议进行人工校对\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 质量问题
     */
    public static class QualityIssue {
        private String type;      // character, dialogue, scene, emotion, format
        private String severity;  // high, medium, low
        private String detail;
        private String targetId;  // 相关ID
        
        public QualityIssue() {
            this.severity = "medium";
        }
        
        public QualityIssue(String type, String detail) {
            this();
            this.type = type;
            this.detail = detail;
        }
        
        public QualityIssue(String type, String severity, String detail) {
            this.type = type;
            this.severity = severity;
            this.detail = detail;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getSeverity() {
            return severity;
        }
        
        public void setSeverity(String severity) {
            this.severity = severity;
        }
        
        public String getDetail() {
            return detail;
        }
        
        public void setDetail(String detail) {
            this.detail = detail;
        }
        
        public String getTargetId() {
            return targetId;
        }
        
        public void setTargetId(String targetId) {
            this.targetId = targetId;
        }
    }
}

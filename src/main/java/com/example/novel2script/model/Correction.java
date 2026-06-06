package com.example.novel2script.model;

import java.time.LocalDateTime;

/**
 * 校对修正模型
 */
public class Correction {

    private String correctionId;
    private String scriptId;
    private String reportId;
    
    // 修正类型
    private CorrectionType type;
    
    // 目标ID
    private String targetId;
    
    // 字段
    private String field;
    
    // 原值和修正值
    private String originalValue;
    private String correctedValue;
    
    // 原因
    private String reason;
    
    // 时间
    private LocalDateTime createdAt;
    private LocalDateTime appliedAt;
    
    // 状态
    private CorrectionStatus status;
    
    public Correction() {
        this.correctionId = "corr_" + System.currentTimeMillis();
        this.createdAt = LocalDateTime.now();
        this.status = CorrectionStatus.PENDING;
    }
    
    public String getCorrectionId() {
        return correctionId;
    }
    
    public void setCorrectionId(String correctionId) {
        this.correctionId = correctionId;
    }
    
    public String getScriptId() {
        return scriptId;
    }
    
    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }
    
    public String getReportId() {
        return reportId;
    }
    
    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
    
    public CorrectionType getType() {
        return type;
    }
    
    public void setType(CorrectionType type) {
        this.type = type;
    }
    
    public String getTargetId() {
        return targetId;
    }
    
    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
    
    public String getField() {
        return field;
    }
    
    public void setField(String field) {
        this.field = field;
    }
    
    public String getOriginalValue() {
        return originalValue;
    }
    
    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }
    
    public String getCorrectedValue() {
        return correctedValue;
    }
    
    public void setCorrectedValue(String correctedValue) {
        this.correctedValue = correctedValue;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }
    
    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }
    
    public CorrectionStatus getStatus() {
        return status;
    }
    
    public void setStatus(CorrectionStatus status) {
        this.status = status;
    }
    
    public void apply() {
        this.status = CorrectionStatus.APPLIED;
        this.appliedAt = LocalDateTime.now();
    }
    
    public void reject() {
        this.status = CorrectionStatus.REJECTED;
    }
    
    public boolean canApply() {
        return this.status == CorrectionStatus.PENDING;
    }
    
    public String toSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("修正记录: ").append(correctionId).append("\n");
        sb.append("类型: ").append(type).append("\n");
        sb.append("目标: ").append(targetId).append(".").append(field).append("\n");
        sb.append("原值: ").append(originalValue).append("\n");
        sb.append("修正值: ").append(correctedValue).append("\n");
        sb.append("原因: ").append(reason).append("\n");
        sb.append("状态: ").append(status).append("\n");
        return sb.toString();
    }
    
    public enum CorrectionType {
        CHARACTER,    // 人物修正
        DIALOGUE,     // 对话修正
        SCENE,        // 场景修正
        EMOTION,      // 情绪修正
        METADATA,     // 元数据修正
        FORMAT        // 格式修正
    }
    
    public enum CorrectionStatus {
        PENDING,     // 待处理
        APPLIED,     // 已应用
        REJECTED     // 已拒绝
    }
}

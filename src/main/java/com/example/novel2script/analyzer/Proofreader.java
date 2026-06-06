package com.example.novel2script.analyzer;

import com.example.novel2script.model.*;
import com.example.novel2script.model.Correction.CorrectionStatus;
import com.example.novel2script.model.Correction.CorrectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 人工校对管理器
 */
@Component
public class Proofreader {

    private static final Logger logger = LoggerFactory.getLogger(Proofreader.class);

    // 存储修正记录
    private Map<String, List<Correction>> correctionsByScript = new HashMap<>();

    /**
     * 创建修正记录
     */
    public Correction createCorrection(String scriptId, CorrectionType type, 
                                       String targetId, String field, 
                                       String originalValue, String correctedValue, String reason) {
        Correction correction = new Correction();
        correction.setScriptId(scriptId);
        correction.setType(type);
        correction.setTargetId(targetId);
        correction.setField(field);
        correction.setOriginalValue(originalValue);
        correction.setCorrectedValue(correctedValue);
        correction.setReason(reason);
        
        if (!correctionsByScript.containsKey(scriptId)) {
            correctionsByScript.put(scriptId, new ArrayList<>());
        }
        correctionsByScript.get(scriptId).add(correction);
        
        logger.info("创建修正记录: {}", correction.getCorrectionId());
        
        return correction;
    }

    /**
     * 应用单条修正
     */
    public void applyCorrection(Correction correction, Script script) {
        if (!correction.canApply()) {
            logger.warn("修正记录无法应用: {}", correction.getCorrectionId());
            return;
        }
        
        logger.info("应用修正: {}", correction.getCorrectionId());
        
        switch (correction.getType()) {
            case CHARACTER:
                applyCharacterCorrection(correction, script);
                break;
            case DIALOGUE:
                applyDialogueCorrection(correction, script);
                break;
            case SCENE:
                applySceneCorrection(correction, script);
                break;
            case EMOTION:
                applyEmotionCorrection(correction, script);
                break;
            case METADATA:
                applyMetadataCorrection(correction, script);
                break;
            default:
                logger.warn("不支持的修正类型: {}", correction.getType());
        }
        
        correction.apply();
    }

    /**
     * 批量应用所有待处理修正
     */
    public void applyAllCorrections(String scriptId, Script script) {
        List<Correction> corrections = correctionsByScript.get(scriptId);
        if (corrections == null || corrections.isEmpty()) {
            return;
        }
        
        int appliedCount = 0;
        for (Correction correction : corrections) {
            if (correction.canApply()) {
                applyCorrection(correction, script);
                appliedCount++;
            }
        }
        
        logger.info("批量应用了 {} 条修正记录", appliedCount);
    }

    /**
     * 应用人物修正
     */
    private void applyCharacterCorrection(Correction correction, Script script) {
        if (script.getCharacters() == null) {
            return;
        }
        
        for (com.example.novel2script.model.Character character : script.getCharacters()) {
            if (correction.getTargetId().equals(character.getId())) {
                switch (correction.getField()) {
                    case "name":
                        character.setName(correction.getCorrectedValue());
                        break;
                    case "role":
                        character.setRole(com.example.novel2script.model.Character.Role.valueOf(correction.getCorrectedValue()));
                        break;
                    case "description":
                        character.setDescription(correction.getCorrectedValue());
                        break;
                    case "firstAppear":
                        character.setFirstAppear(correction.getCorrectedValue());
                        break;
                    default:
                        logger.warn("不支持的字段: {}", correction.getField());
                }
                break;
            }
        }
    }

    /**
     * 应用对话修正
     */
    private void applyDialogueCorrection(Correction correction, Script script) {
        if (script.getScenes() == null) {
            return;
        }
        
        // 查找targetId对应的beat
        for (Scene scene : script.getScenes()) {
            if (scene.getBeats() != null) {
                for (Beat beat : scene.getBeats()) {
                    if (correction.getTargetId().equals(beat.getId())) {
                        switch (correction.getField()) {
                            case "speaker":
                                beat.setSpeaker(correction.getCorrectedValue());
                                break;
                            case "content":
                                beat.setContent(correction.getCorrectedValue());
                                break;
                            default:
                                logger.warn("不支持的字段: {}", correction.getField());
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * 应用场景修正
     */
    private void applySceneCorrection(Correction correction, Script script) {
        if (script.getScenes() == null) {
            return;
        }
        
        for (Scene scene : script.getScenes()) {
            if (correction.getTargetId().equals(scene.getId())) {
                switch (correction.getField()) {
                    case "location":
                        if (scene.getLocation() != null) {
                            scene.getLocation().setName(correction.getCorrectedValue());
                        }
                        break;
                    case "description":
                        scene.setDescription(correction.getCorrectedValue());
                        break;
                    case "atmosphere":
                        scene.setAtmosphere(correction.getCorrectedValue());
                        break;
                    default:
                        logger.warn("不支持的字段: {}", correction.getField());
                }
                break;
            }
        }
    }

    /**
     * 应用情绪修正
     */
    private void applyEmotionCorrection(Correction correction, Script script) {
        if (script.getScenes() == null) {
            return;
        }
        
        for (Scene scene : script.getScenes()) {
            if (scene.getBeats() != null) {
                for (Beat beat : scene.getBeats()) {
                    if (correction.getTargetId().equals(beat.getId())) {
                        switch (correction.getField()) {
                            case "emotion":
                                beat.setEmotion(Beat.Emotion.valueOf(correction.getCorrectedValue()));
                                break;
                            case "intensity":
                                beat.setIntensity(Beat.Intensity.valueOf(correction.getCorrectedValue()));
                                break;
                            default:
                                logger.warn("不支持的字段: {}", correction.getField());
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * 应用元数据修正
     */
    private void applyMetadataCorrection(Correction correction, Script script) {
        if (script.getMeta() == null) {
            return;
        }
        
        MetaInformation meta = script.getMeta();
        switch (correction.getField()) {
            case "title":
                meta.setTitle(correction.getCorrectedValue());
                break;
            case "author":
                meta.setAuthor(correction.getCorrectedValue());
                break;
            case "version":
                meta.setVersion(correction.getCorrectedValue());
                break;
            default:
                logger.warn("不支持的字段: {}", correction.getField());
        }
    }

    /**
     * 获取所有修正记录
     */
    public List<Correction> getCorrections(String scriptId) {
        return correctionsByScript.getOrDefault(scriptId, new ArrayList<>());
    }

    /**
     * 获取待处理修正记录
     */
    public List<Correction> getPendingCorrections(String scriptId) {
        List<Correction> pending = new ArrayList<>();
        List<Correction> corrections = correctionsByScript.get(scriptId);
        if (corrections != null) {
            for (Correction correction : corrections) {
                if (correction.getStatus() == CorrectionStatus.PENDING) {
                    pending.add(correction);
                }
            }
        }
        return pending;
    }

    /**
     * 拒绝修正
     */
    public void rejectCorrection(String correctionId, String reason) {
        for (List<Correction> corrections : correctionsByScript.values()) {
            for (Correction correction : corrections) {
                if (correction.getCorrectionId().equals(correctionId)) {
                    correction.reject();
                    logger.info("拒绝修正: {}, 原因: {}", correctionId, reason);
                    return;
                }
            }
        }
    }

    /**
     * 基于质量报告生成建议修正
     */
    public List<Correction> generateSuggestedCorrections(Script script, QualityReport report) {
        List<Correction> suggestions = new ArrayList<>();
        
        for (QualityReport.QualityIssue issue : report.getIssues()) {
            Correction correction = new Correction();
            correction.setScriptId(report.getScriptId());
            correction.setReportId(report.getReportId());
            correction.setType(CorrectionType.valueOf(issue.getType().toUpperCase()));
            correction.setTargetId(issue.getTargetId());
            correction.setOriginalValue(issue.getDetail());
            correction.setReason("基于质量报告自动生成的建议");
            
            suggestions.add(correction);
        }
        
        return suggestions;
    }
}

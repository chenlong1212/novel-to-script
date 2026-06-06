package com.example.novel2script.analyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 对话提取器
 * 从小说文本中提取对话内容
 */
@Component
public class DialogueExtractor {

    private static final Logger logger = LoggerFactory.getLogger(DialogueExtractor.class);

    // 模式1：XX说："..." 或者 "..."XX说
    private static final Pattern DIALOGUE_PATTERN = Pattern.compile(
        "(?:([\\u4e00-\\u9fa5]{2,4})[说问道答叫喊问笑怒道惊]\\s*[：:]?\\s*)?[\"\"''「」]([^\"\"''「」]{2,100})[\"\"''「」](?:\\s*[,，。.！!？?]?\\s*([\\u4e00-\\u9fa5]{2,4})[说问道答叫喊问笑怒道惊]?)?",
        Pattern.DOTALL
    );

    /**
     * 提取对话列表
     */
    public List<DialogueInfo> extractDialogues(String text) {
        logger.info("开始提取对话信息...");
        
        List<DialogueInfo> dialogues = new ArrayList<>();
        
        // 按行处理文本，更容易识别
        String[] lines = text.split("\n");
        
        String lastSpeaker = "未知";
        
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue;
            }
            
            // 提取对话
            Matcher matcher = DIALOGUE_PATTERN.matcher(line);
            
            while (matcher.find()) {
                String speaker1 = matcher.group(1); // 前面的说话人
                String content = matcher.group(2);  // 对话内容
                String speaker2 = matcher.group(3); // 后面的说话人
                
                String speaker = speaker1 != null ? speaker1 : speaker2;
                
                if (speaker == null) {
                    speaker = lastSpeaker; // 继承上一个说话人
                } else {
                    lastSpeaker = speaker; // 更新最后说话人
                }
                
                if (content != null && content.trim().length() >= 2) {
                    DialogueInfo info = new DialogueInfo();
                    info.setSpeaker(speaker);
                    info.setContent(content.trim());
                    info.setEmotion(detectEmotion(content));
                    info.setType("dialogue");
                    dialogues.add(info);
                }
            }
        }
        
        logger.info("对话提取完成，共提取 {} 条对话", dialogues.size());
        
        return dialogues;
    }

    /**
     * 检测对话情绪
     */
    private String detectEmotion(String content) {
        if (content == null || content.isEmpty()) {
            return "neutral";
        }
        
        // 检测感叹句
        if (content.contains("！") || content.contains("!")) {
            if (content.contains("好") || content.contains("棒") || 
                content.contains("厉害") || content.contains("真") || content.contains("开心")) {
                return "happy";
            } else if (content.contains("啊") || content.contains("呀") || content.contains("哦")) {
                return "surprised";
            }
            return "angry";
        }
        
        // 检测疑问句
        if (content.contains("？") || content.contains("?") || 
            content.contains("吗") || content.contains("呢")) {
            return "curious";
        }
        
        // 检测语气词
        if (content.contains("哈哈") || content.contains("呵呵") || 
            content.contains("笑") || content.contains("开心")) {
            return "happy";
        }
        
        // 检测神秘/小声
        if (content.contains("神秘") || content.contains("小声") || content.contains("低声")) {
            return "mysterious";
        }
        
        return "neutral";
    }

    /**
     * 统计对话信息
     */
    public DialogueStatistics calculateStatistics(List<DialogueInfo> dialogues) {
        DialogueStatistics stats = new DialogueStatistics();
        
        stats.setTotalDialogues(dialogues.size());
        
        long identifiedSpeakers = dialogues.stream()
            .filter(d -> !d.getSpeaker().equals("未知"))
            .count();
        stats.setIdentifiedSpeakers((int) identifiedSpeakers);
        
        long happy = dialogues.stream()
            .filter(d -> d.getEmotion().equals("happy")).count();
        long neutral = dialogues.stream()
            .filter(d -> d.getEmotion().equals("neutral")).count();
        long angry = dialogues.stream()
            .filter(d -> d.getEmotion().equals("angry")).count();
        
        stats.setHappyDialogues((int) happy);
        stats.setNeutralDialogues((int) neutral);
        stats.setAngryDialogues((int) angry);
        
        return stats;
    }

    /**
     * 对话信息内部类
     */
    public static class DialogueInfo {
        private String speaker;
        private String content;
        private String emotion;
        private String type;
        private String direction;

        public String getSpeaker() {
            return speaker;
        }
        public void setSpeaker(String speaker) {
            this.speaker = speaker;
        }
        public String getContent() {
            return content;
        }
        public void setContent(String content) {
            this.content = content;
        }
        public String getEmotion() {
            return emotion;
        }
        public void setEmotion(String emotion) {
            this.emotion = emotion;
        }
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        public String getDirection() {
            return direction;
        }
        public void setDirection(String direction) {
            this.direction = direction;
        }
    }

    /**
     * 对话统计信息
     */
    public static class DialogueStatistics {
        private int totalDialogues;
        private int identifiedSpeakers;
        private int happyDialogues;
        private int neutralDialogues;
        private int angryDialogues;

        public int getTotalDialogues() {
            return totalDialogues;
        }
        public void setTotalDialogues(int totalDialogues) {
            this.totalDialogues = totalDialogues;
        }
        public int getIdentifiedSpeakers() {
            return identifiedSpeakers;
        }
        public void setIdentifiedSpeakers(int identifiedSpeakers) {
            this.identifiedSpeakers = identifiedSpeakers;
        }
        public int getHappyDialogues() {
            return happyDialogues;
        }
        public void setHappyDialogues(int happyDialogues) {
            this.happyDialogues = happyDialogues;
        }
        public int getNeutralDialogues() {
            return neutralDialogues;
        }
        public void setNeutralDialogues(int neutralDialogues) {
            this.neutralDialogues = neutralDialogues;
        }
        public int getAngryDialogues() {
            return angryDialogues;
        }
        public void setAngryDialogues(int angryDialogues) {
            this.angryDialogues = angryDialogues;
        }
    }
}

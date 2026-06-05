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

    // 对话模式1：XX说："..."
    private static final Pattern SAY_DIALOGUE_PATTERN = Pattern.compile(
        "([\\u4e00-\\u9fa5]{1,4})[说问道答叫喊念嘟囔叹称问怒喝道]\s*[\"\"''「」]?\\s*(.+?)\\s*[\"\"''」」]?(?=\\n|[\\u4e00-\\u9fa5]{1,4}[说问道答叫喊念嘟囔叹称]|$)",
        Pattern.DOTALL
    );

    // 对话模式2："..."
    private static final Pattern QUOTE_DIALOGUE_PATTERN = Pattern.compile(
        "[\"\"''「」](.+?)[\"\"''」」]",
        Pattern.DOTALL
    );

    // 情绪词映射
    private static final Pattern EXCLAMATION_PATTERN = Pattern.compile("[!！]{2,}");
    private static final Pattern QUESTION_PATTERN = Pattern.compile("[?？]{2,}");
    private static final Pattern ELLIPSIS_PATTERN = Pattern.compile("[.。]{3,}");

    /**
     * 提取对话列表
     *
     * @param text 小说文本
     * @return 对话信息列表
     */
    public List<DialogueInfo> extractDialogues(String text) {
        logger.info("开始提取对话信息...");
        
        List<DialogueInfo> dialogues = new ArrayList<>();
        
        // 提取 XX说："..." 格式的对话
        extractSayDialogues(text, dialogues);
        
        // 提取纯引号对话
        extractQuoteDialogues(text, dialogues);
        
        logger.info("对话提取完成，共提取 {} 条对话", dialogues.size());
        
        return dialogues;
    }

    /**
     * 提取 XX说："..." 格式的对话
     */
    private void extractSayDialogues(String text, List<DialogueInfo> dialogues) {
        Matcher matcher = SAY_DIALOGUE_PATTERN.matcher(text);
        
        while (matcher.find()) {
            String speaker = matcher.group(1).trim();
            String content = matcher.group(2).trim();
            
            // 过滤过短的对话
            if (content.length() < 2) {
                continue;
            }
            
            DialogueInfo info = new DialogueInfo();
            info.setSpeaker(speaker);
            info.setContent(content);
            info.setEmotion(detectEmotion(content));
            info.setType("direct");
            
            dialogues.add(info);
        }
    }

    /**
     * 提取纯引号对话
     */
    private void extractQuoteDialogues(String text, List<DialogueInfo> dialogues) {
        Matcher matcher = QUOTE_DIALOGUE_PATTERN.matcher(text);
        
        while (matcher.find()) {
            String content = matcher.group(1).trim();
            
            // 过滤过短的对话
            if (content.length() < 3) {
                continue;
            }
            
            DialogueInfo info = new DialogueInfo();
            info.setSpeaker("未知");  // 无法确定说话人
            info.setContent(content);
            info.setEmotion(detectEmotion(content));
            info.setType("quote");
            
            dialogues.add(info);
        }
    }

    /**
     * 检测对话情绪
     */
    private String detectEmotion(String content) {
        if (content == null || content.isEmpty()) {
            return "neutral";
        }
        
        // 检测感叹句
        if (EXCLAMATION_PATTERN.matcher(content).find()) {
            if (content.contains("好") || content.contains("棒") || content.contains("厉害")) {
                return "happy";
            } else if (content.contains("啊") || content.contains("呀") || content.contains("哦")) {
                return "surprised";
            }
            return "angry";
        }
        
        // 检测疑问句
        if (QUESTION_PATTERN.matcher(content).find() || 
            content.contains("吗") || content.contains("呢") || content.contains("？")) {
            return "curious";
        }
        
        // 检测省略号
        if (ELLIPSIS_PATTERN.matcher(content).find()) {
            if (content.contains("但是") || content.contains("不过")) {
                return "hesitant";
            }
            return "thoughtful";
        }
        
        // 检测语气词
        if (content.contains("啊") || content.contains("呀") || content.contains("呢")) {
            return "casual";
        }
        
        return "neutral";
    }

    /**
     * 统计对话信息
     */
    public DialogueStatistics calculateStatistics(List<DialogueInfo> dialogues) {
        DialogueStatistics stats = new DialogueStatistics();
        
        stats.setTotalDialogues(dialogues.size());
        
        // 统计说话人
        long identifiedSpeakers = dialogues.stream()
            .filter(d -> !d.getSpeaker().equals("未知"))
            .count();
        stats.setIdentifiedSpeakers((int) identifiedSpeakers);
        
        // 统计情绪分布
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

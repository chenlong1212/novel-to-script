package com.example.novel2script.analyzer;

import com.example.novel2script.model.Beat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 情绪标注器
 * 增强的情绪检测和标注功能
 */
@Component
public class EmotionAnnotator {

    private static final Logger logger = LoggerFactory.getLogger(EmotionAnnotator.class);

    // 情绪关键词映射
    private static final Map<Beat.Emotion, List<String>> EMOTION_KEYWORDS = new HashMap<>();

    static {
        // 开心/喜悦
        EMOTION_KEYWORDS.put(Beat.Emotion.happy, Arrays.asList(
            "笑", "开心", "高兴", "快乐", "愉快", "欢喜", "欢笑", "喜悦",
            "高兴地", "开心地", "快乐地", "愉快地", "笑了", "大笑", "微笑",
            "哈哈", "嘻嘻", "呵呵", "嘿嘿", "好开心", "好高兴", "太棒了",
            "太好了", "开心极了", "高兴极了", "哈哈哈", "嘻嘻嘻"
        ));

        // 悲伤/难过
        EMOTION_KEYWORDS.put(Beat.Emotion.sad, Arrays.asList(
            "哭", "悲伤", "难过", "伤心", "痛苦", "流泪", "哭泣", "伤心地",
            "难过地", "痛苦地", "悲伤地", "眼泪", "泪水", "痛哭", "痛哭流涕",
            "伤心欲绝", "悲痛", "哀伤", "忧伤", "伤心的", "难过的",
            "呜呜", "555", "哎"
        ));

        // 愤怒/生气
        EMOTION_KEYWORDS.put(Beat.Emotion.angry, Arrays.asList(
            "生气", "愤怒", "恼火", "怒", "气", "愤怒地", "生气地",
            "生气", "愤怒", "恼火", "大怒", "怒道", "喝道", "怒斥",
            "哼", "可恶", "可恨", "该死", "混账", "混蛋", "该死的",
            "!", "！", "怒", "愤怒"
        ));

        // 恐惧/害怕
        EMOTION_KEYWORDS.put(Beat.Emotion.fearful, Arrays.asList(
            "怕", "害怕", "恐惧", "惊恐", "惊慌", "恐惧", "害怕地",
            "恐惧地", "惊恐地", "惊慌地", "吓", "吓死", "害怕极了",
            "惊恐万分", "惊慌失措", "慌", "恐慌", "恐怖", "可怕",
            "战战兢兢", "颤抖", "发抖", "冷汗", "后怕"
        ));

        // 惊讶/意外
        EMOTION_KEYWORDS.put(Beat.Emotion.surprised, Arrays.asList(
            "啊", "什么", "怎么", "惊讶", "吃惊", "意外", "惊讶地",
            "吃惊地", "意外地", "啊！", "什么？", "怎么会", "不可能",
            "没想到", "意外", "惊", "惊讶", "震惊"
        ));

        // 厌恶/恶心
        EMOTION_KEYWORDS.put(Beat.Emotion.disgusted, Arrays.asList(
            "恶心", "讨厌", "厌恶", "恶", "脏", "恶心地", "讨厌地",
            "厌恶地", "恶心的", "讨厌的", "呸", "啐",
            "恶心", "讨厌", "真恶心", "太讨厌"
        ));

        // 恋爱/喜欢
        EMOTION_KEYWORDS.put(Beat.Emotion.loving, Arrays.asList(
            "爱", "喜欢", "心动", "喜欢", "爱", "喜欢上",
            "爱的", "喜欢的", "心动的", "喜欢地", "爱地",
            "喜欢", "爱", "喜欢你", "我爱你", "亲爱的"
        ));

        // 内疚/愧疚
        EMOTION_KEYWORDS.put(Beat.Emotion.guilty, Arrays.asList(
            "对不起", "抱歉", "内疚", "愧疚", "抱歉地", "内疚地",
            "愧疚地", "对不起", "抱歉", "不好意思", "对不起啊"
        ));

        // 骄傲/自豪
        EMOTION_KEYWORDS.put(Beat.Emotion.proud, Arrays.asList(
            "骄傲", "自豪", "得意", "骄傲地", "自豪地", "得意地",
            "厉害", "了不起", "棒", "太棒了", "牛"
        ));

        // 害羞/羞涩
        EMOTION_KEYWORDS.put(Beat.Emotion.shy, Arrays.asList(
            "害羞", "羞涩", "不好意思", "害羞地", "羞涩地",
            "脸", "红", "脸红", "脸红红的", "脸红红的",
            "害羞", "羞涩", "不好意思"
        ));

        // 讽刺/挖苦
        EMOTION_KEYWORDS.put(Beat.Emotion.sarcastic, Arrays.asList(
            "呵", "切", "哼", "呵呵", "冷笑", "讽刺", "挖苦",
            "嘲讽", "嘲", "笑", "冷笑", "嘲讽地", "挖苦地"
        ));
    }

    // 标点符号情绪模式
    private static final Pattern QUESTION_MARK = Pattern.compile("[?？]");
    private static final Pattern EXCLAMATION_MARK = Pattern.compile("[!！]");
    private static final Pattern MULTI_QUESTION = Pattern.compile("[?？]{2,}");
    private static final Pattern MULTI_EXCLAMATION = Pattern.compile("[!！]{2,}");
    private static final Pattern ELLIPSIS = Pattern.compile("[.。…]{2,}");

    /**
     * 分析文本的情绪
     *
     * @param text 文本内容
     * @return 情绪枚举
     */
    public Beat.Emotion analyzeEmotion(String text) {
        Map<Beat.Emotion, Integer> scores = new HashMap<>();

        // 1. 关键词匹配
        for (Map.Entry<Beat.Emotion, List<String>> entry : EMOTION_KEYWORDS.entrySet()) {
            int score = 0;
            for (String keyword : entry.getValue()) {
                score += countOccurrences(text, keyword);
            }
            scores.put(entry.getKey(), score);
        }

        // 2. 标点符号分析
        analyzePunctuation(text, scores);

        // 3. 找出得分最高的情绪
        return scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .filter(e -> e.getValue() > 0)
            .map(Map.Entry::getKey)
            .orElse(Beat.Emotion.neutral);
    }

    /**
     * 分析标点符号
     */
    private void analyzePunctuation(String text, Map<Beat.Emotion, Integer> scores) {
        // 问号 -> 好奇/惊讶
        if (QUESTION_MARK.matcher(text).find()) {
            int count = countOccurrences(text, "?") + countOccurrences(text, "？");
            scores.merge(Beat.Emotion.surprised, count, Integer::sum);
        }

        // 多个问号 -> 更惊讶
        if (MULTI_QUESTION.matcher(text).find()) {
            scores.merge(Beat.Emotion.surprised, 2, Integer::sum);
        }

        // 感叹号 -> 强烈情绪（愤怒/开心/惊讶）
        if (EXCLAMATION_MARK.matcher(text).find()) {
            int count = countOccurrences(text, "!") + countOccurrences(text, "！");
            // 根据上下文判断，但这里简化处理
            scores.merge(Beat.Emotion.angry, count, Integer::sum);
            scores.merge(Beat.Emotion.surprised, count, Integer::sum);
            scores.merge(Beat.Emotion.happy, count, Integer::sum);
        }

        // 多个感叹号 -> 更强烈
        if (MULTI_EXCLAMATION.matcher(text).find()) {
            scores.merge(Beat.Emotion.angry, 2, Integer::sum);
            scores.merge(Beat.Emotion.surprised, 2, Integer::sum);
            scores.merge(Beat.Emotion.happy, 2, Integer::sum);
        }

        // 省略号 -> 犹豫/思考
        if (ELLIPSIS.matcher(text).find()) {
            scores.merge(Beat.Emotion.neutral, 1, Integer::sum);
        }
    }

    /**
     * 分析情绪强度
     *
     * @param text 文本内容
     * @param emotion 情绪
     * @return 强度
     */
    public Beat.Intensity analyzeIntensity(String text, Beat.Emotion emotion) {
        int intensityScore = 0;

        // 1. 关键词数量
        List<String> keywords = EMOTION_KEYWORDS.getOrDefault(emotion, new ArrayList<>());
        for (String keyword : keywords) {
            intensityScore += countOccurrences(text, keyword);
        }

        // 2. 标点符号
        if (MULTI_EXCLAMATION.matcher(text).find()) {
            intensityScore += 2;
        }
        if (EXCLAMATION_MARK.matcher(text).find()) {
            intensityScore += 1;
        }

        // 3. 根据分数判断强度
        if (intensityScore <= 1) {
            return Beat.Intensity.low;
        } else if (intensityScore <= 3) {
            return Beat.Intensity.medium;
        } else {
            return Beat.Intensity.high;
        }
    }

    /**
     * 获得情绪描述
     *
     * @param emotion 情绪
     * @return 描述
     */
    public String getEmotionDescription(Beat.Emotion emotion) {
        switch (emotion) {
            case happy:
                return "开心、喜悦、快乐";
            case sad:
                return "悲伤、难过、痛苦";
            case angry:
                return "愤怒、生气、恼火";
            case fearful:
                return "恐惧、害怕、惊慌";
            case surprised:
                return "惊讶、意外、吃惊";
            case disgusted:
                return "厌恶、恶心、讨厌";
            case loving:
                return "喜欢、爱慕、温柔";
            case guilty:
                return "内疚、愧疚、抱歉";
            case proud:
                return "骄傲、自豪、得意";
            case shy:
                return "害羞、羞涩、腼腆";
            case sarcastic:
                return "讽刺、挖苦、冷笑";
            case neutral:
            default:
                return "中性、平静、无特别情绪";
        }
    }

    /**
     * 获得情绪表情符号
     *
     * @param emotion 情绪
     * @return 表情符号
     */
    public String getEmotionEmoji(Beat.Emotion emotion) {
        switch (emotion) {
            case happy:
                return "😊";
            case sad:
                return "😢";
            case angry:
                return "😠";
            case fearful:
                return "😨";
            case surprised:
                return "😲";
            case disgusted:
                return "🤢";
            case loving:
                return "😍";
            case guilty:
                return "😔";
            case proud:
                return "😎";
            case shy:
                return "😊";
            case sarcastic:
                return "😏";
            case neutral:
            default:
                return "😐";
        }
    }

    /**
     * 统计出现次数
     */
    private int countOccurrences(String text, String keyword) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(keyword, index)) != -1) {
            count++;
            index += keyword.length();
        }
        return count;
    }
}

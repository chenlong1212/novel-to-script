package com.example.novel2script.analyzer;

import com.example.novel2script.model.Character;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 人物识别器
 * 从小说文本中识别和提取人物信息
 */
@Component
public class CharacterAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(CharacterAnalyzer.class);

    // 常见姓氏列表
    private static final Set<String> COMMON_SURNAMES = new HashSet<>(Arrays.asList(
        "张", "王", "李", "赵", "刘", "陈", "杨", "黄", "周", "吴",
        "徐", "孙", "马", "朱", "胡", "郭", "何", "林", "高", "罗",
        "郑", "梁", "谢", "宋", "唐", "许", "韩", "冯", "邓", "曹",
        "彭", "曾", "萧", "蔡", "潘", "田", "董", "袁", "于", "余",
        "叶", "蒋", "杜", "苏", "魏", "程", "吕", "丁", "沈", "任"
    ));

    // 需要去掉的修饰词（人名后面常接的词）
    private static final Set<String> STOP_WORDS_AFTER = new HashSet<>(Arrays.asList(
        "说", "道", "问", "答", "喊", "叫", "笑", "哭", "小声", "大声",
        "轻轻", "慢慢", "高兴", "生气", "难过", "开心", "惊讶", "疑惑",
        "轻声", "温柔", "严厉", "温和", "愤怒", "突然", "忽然", "立刻", "马上", 
        "赶紧", "连忙", "急忙", "迅速", "笑着", "说道", "问道", "答道", "喊道", 
        "叫道", "感叹", "叹息", "的", "了", "着", "过", "吗", "呢", "吧", "啊", "呀", "啦",
        "想", "看", "听", "走", "站", "坐", "躺", "跑", "跳", "低", "高", "微", "悄", "低"
    ));

    // 绝对无效的词（绝对不可能是名字）
    private static final Set<String> INVALID_WORDS = new HashSet<>(Arrays.asList(
        // 人称代词
        "我", "你", "他", "她", "它", "我们", "你们", "他们", "她们", "它们",
        "咱", "咱们", "俺", "俺们", "您", "阁下",
        // 指示代词
        "这", "那", "这个", "那个", "这些", "那些", "这里", "那里", "这儿", "那儿",
        // 疑问代词
        "谁", "什么", "哪", "哪里", "哪个", "哪些", "怎么", "怎样", "为什么",
        // 章节相关
        "第", "章", "节", "卷", "部", "篇", "序", "前言", "后记", "第一章", "第二章", "第三章", "第四章", "第五章",
        // 性别/年龄/身份泛指
        "男", "女", "男人", "女人", "男孩", "女孩", "青年", "中年", "老年", "少年", "儿童",
        "先生", "女士", "小姐", "夫人", "太太", "同学", "朋友",
        // 方位词
        "上", "下", "左", "右", "前", "后", "里", "外", "内", "中", "东", "西", "南", "北",
        // 时间词
        "今天", "明天", "昨天", "前天", "后天", "早上", "上午", "中午", "下午", "晚上", "夜里",
        "现在", "刚才", "立刻", "马上", "突然", "忽然",
        // 副词/形容词
        "很", "非常", "特别", "十分", "极其", "比较", "更", "最", "太", "真", "好",
        "新", "旧", "大", "小", "多", "少", "高", "矮", "胖", "瘦", "长", "短", "快", "慢",
        // 其他
        "的", "了", "着", "过", "在", "是", "有", "和", "就", "都", "也", "还", "又", "再",
        "说", "道", "问", "答", "喊", "叫", "笑", "哭", "走", "跑", "看", "听", "想", "做",
        "带", "来", "去", "到", "往", "从", "向", "给", "把", "被", "比", "跟", "同", "与",
        "眼", "睛", "嘴", "脸", "手", "脚", "头", "身", "心",
        "服", "务", "员", "医", "生", "老", "师", "学", "生", "工", "人", "农", "民",
        "明", "着", "试", "探", "性", "地", "东", "西", "带", "来", "小", "心", "点",
        "低", "声", "微", "笑", "点", "头", "眼", "中", "闪", "过", "了", "一", "丝",
        "惊", "讶", "疑", "惑", "高", "兴", "难", "过", "生", "气", "愤", "怒",
        "温", "柔", "严", "厉", "和", "蔼", "可", "亲", "冷", "淡", "平", "静"
    ));

    // 已知的固定角色名（避免被清洗）
    private static final Set<String> KNOWN_CHARACTERS = new HashSet<>(Arrays.asList(
        "服务员", "风衣男人", "神秘女人", "老人", "小孩", "青年",
        "中年", "老板", "经理", "老师", "医生", "警察", "司机"
    ));

    // 对话模式：只识别 XX说/道/问/答/喊/叫 + 冒号/引号 这种明确的模式
    private static final Pattern DIALOGUE_PATTERN = Pattern.compile(
        "([\\u4e00-\\u9fa5]{2,4})(?:说|道|问|答|喊|叫|笑|哭)\\s*[：:\"「]",
        Pattern.MULTILINE
    );

    // 对话模式2：引号后接 XX说/道/问
    private static final Pattern DIALOGUE_PATTERN_2 = Pattern.compile(
        "[\"」]\\s*[，,。.！!？?]?\\s*([\\u4e00-\\u9fa5]{2,4})(?:说|道|问|答|喊|叫)",
        Pattern.MULTILINE
    );

    /**
     * 分析小说文本，提取人物信息
     */
    public List<CharacterInfo> analyzeCharacters(String text) {
        logger.info("开始分析人物信息...");

        // 1. 先提取所有可能的候选名字
        Set<String> candidateNames = extractCandidateNames(text);

        // 2. 清洗和去重
        Map<String, Integer> cleanedNames = cleanAndDeduplicate(candidateNames, text);

        // 3. 构建结果
        List<CharacterInfo> characters = buildCharacterList(cleanedNames);

        logger.info("人物分析完成，共识别 {} 个人物: {}", characters.size(),
                characters.stream().map(CharacterInfo::getName).toList());

        return characters;
    }

    /**
     * 提取候选名字
     */
    private Set<String> extractCandidateNames(String text) {
        Set<String> names = new LinkedHashSet<>();

        // 逐行处理
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            // 模式1：XX说："..." 或 XX说"..."
            Matcher m1 = DIALOGUE_PATTERN.matcher(line);
            while (m1.find()) {
                String name = m1.group(1);
                if (name != null && isValidCandidateName(name)) {
                    names.add(name);
                }
            }

            // 模式2："...",XX说 或 "..."XX道
            Matcher m2 = DIALOGUE_PATTERN_2.matcher(line);
            while (m2.find()) {
                String name = m2.group(1);
                if (name != null && isValidCandidateName(name)) {
                    names.add(name);
                }
            }
        }

        // 补充一些已知的固定角色
        for (String knownChar : KNOWN_CHARACTERS) {
            if (text.contains(knownChar)) {
                names.add(knownChar);
            }
        }

        return names;
    }

    /**
     * 验证候选名字的有效性
     */
    private boolean isValidCandidateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        name = name.trim();

        // 长度必须在2-4之间（2-4个字的名字比较合理）
        if (name.length() < 2 || name.length() > 4) {
            return false;
        }

        // 必须全是汉字
        if (!name.matches("[\u4e00-\u9fa5]+")) {
            return false;
        }

        // 检查是否是绝对无效词
        if (INVALID_WORDS.contains(name)) {
            return false;
        }

        // 如果是已知的角色，直接通过
        if (KNOWN_CHARACTERS.contains(name)) {
            return true;
        }

        // 检查是否包含无效词（比如"笑着说"被识别成"笑着"）
        for (String invalidWord : INVALID_WORDS) {
            if (name.equals(invalidWord) || name.startsWith(invalidWord) || name.endsWith(invalidWord)) {
                // 如果只是以无效词开头或结尾，但整体合理的话，还是可以接受
                // 但如果等于无效词，就直接拒绝
                if (name.equals(invalidWord)) {
                    return false;
                }
            }
        }

        // 对于老X或小X这种称呼，特殊处理
        if ((name.startsWith("老") || name.startsWith("小")) && name.length() == 2) {
            // 第二个字不能是无效词
            String secondChar = name.substring(1, 2);
            if (!INVALID_WORDS.contains(secondChar)) {
                return true;
            }
        }

        // 第一个字最好是常见姓氏（可以适当放宽，但必须至少合理）
        String firstChar = name.substring(0, 1);
        if (COMMON_SURNAMES.contains(firstChar)) {
            return true;
        }

        // 即使不是常见姓氏，如果长度是2-3个字，也接受（可能是名字）
        // 但需要再次确认不包含无效词
        for (String invalidWord : INVALID_WORDS) {
            if (name.contains(invalidWord)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 清洗名字，去掉后面的修饰词
     */
    private String cleanName(String name) {
        if (name == null) return null;

        name = name.trim();

        // 如果是已知角色，直接返回
        if (KNOWN_CHARACTERS.contains(name)) {
            return name;
        }

        // 尝试从后往前去掉停用词，直到得到一个看起来像名字的
        String cleaned = name;
        
        // 从最长的可能开始，逐步缩短
        for (int i = name.length(); i >= 2; i--) {
            String candidate = name.substring(0, i);
            if (isValidCandidateName(candidate)) {
                // 检查候选名字后面是否跟着停用词
                String suffix = name.length() > i ? name.substring(i) : "";
                boolean endsWithStopWord = false;
                for (String stopWord : STOP_WORDS_AFTER) {
                    if (suffix.startsWith(stopWord)) {
                        endsWithStopWord = true;
                        break;
                    }
                }
                if (endsWithStopWord || i == name.length()) {
                    cleaned = candidate;
                    break;
                }
            }
        }

        return cleaned;
    }

    /**
     * 清洗和去重，统计频率
     */
    private Map<String, Integer> cleanAndDeduplicate(Set<String> candidates, String text) {
        Map<String, Integer> nameCount = new LinkedHashMap<>();

        // 先清洗所有候选
        Set<String> validNames = new LinkedHashSet<>();
        for (String candidate : candidates) {
            String cleaned = cleanName(candidate);
            if (isValidCandidateName(cleaned)) {
                validNames.add(cleaned);
            }
        }

        // 统计每个清洗后名字的出现频率
        for (String name : validNames) {
            int count = 0;
            // 使用更精确的匹配模式，确保匹配的是完整的名字
            // 比如要匹配"张三"，不能匹配"张三说"里的"张三"
            // 使用单词边界的概念（但中文没有空格，所以用上下文判断）
            Pattern p = Pattern.compile("(?<![\\u4e00-\\u9fa5])" + Pattern.quote(name) + "(?![\\u4e00-\\u9fa5])");
            Matcher m = p.matcher(text);
            while (m.find()) count++;
            
            // 另外也统计一下"XXX说"这种模式
            Pattern p2 = Pattern.compile(Pattern.quote(name) + "(?:说|道|问|答|喊|叫)");
            Matcher m2 = p2.matcher(text);
            while (m2.find()) count++;

            // 至少出现2次才认为是角色（提高准确率）
            if (count >= 2) {
                nameCount.put(name, count);
            }
        }

        return nameCount;
    }

    /**
     * 构建人物列表
     */
    private List<CharacterInfo> buildCharacterList(Map<String, Integer> nameCount) {
        List<CharacterInfo> characters = new ArrayList<>();

        // 按频率排序
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(nameCount.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        int index = 1;
        for (Map.Entry<String, Integer> entry : sorted) {
            CharacterInfo info = new CharacterInfo();
            info.setId("char_" + String.format("%03d", index++));
            info.setName(entry.getKey());
            info.setFrequency(entry.getValue());
            info.setRole(determineRole(entry.getValue(), index - 1));
            characters.add(info);
        }

        return characters;
    }

    /**
     * 判断人物角色类型
     */
    private Character.Role determineRole(int frequency, int rank) {
        if (rank == 1 && frequency >= 5) {
            return Character.Role.protagonist;
        } else if (frequency >= 3) {
            return Character.Role.supporting;
        } else {
            return Character.Role.minor;
        }
    }

    /**
     * 人物信息内部类
     */
    public static class CharacterInfo {
        private String id;
        private String name;
        private int frequency;
        private Character.Role role;
        private String description;
        private String firstAppear;

        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public int getFrequency() {
            return frequency;
        }
        public void setFrequency(int frequency) {
            this.frequency = frequency;
        }
        public Character.Role getRole() {
            return role;
        }
        public void setRole(Character.Role role) {
            this.role = role;
        }
        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }
        public String getFirstAppear() {
            return firstAppear;
        }
        public void setFirstAppear(String firstAppear) {
            this.firstAppear = firstAppear;
        }
    }
}

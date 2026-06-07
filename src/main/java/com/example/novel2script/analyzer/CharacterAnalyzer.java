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
        "轻声", "大声", "温柔", "严厉", "温和", "愤怒", "开心", "难过",
        "突然", "忽然", "立刻", "马上", "赶紧", "连忙", "急忙", "迅速",
        "笑着", "说道", "问道", "答道", "喊道", "叫道", "感叹", "叹息",
        "的", "了", "着", "过", "吗", "呢", "吧", "啊", "呀", "啦"
    ));

    // 单个汉字的姓列表（用于验证）
    private static final Set<String> SINGLE_SURNAMES = new HashSet<>(Arrays.asList(
        "张", "王", "李", "赵", "刘", "陈", "杨", "黄", "周", "吴",
        "徐", "孙", "马", "朱", "胡", "郭", "何", "林", "高", "罗",
        "郑", "梁", "谢", "宋", "唐", "许", "韩", "冯", "邓", "曹"
    ));

    // 已知的固定角色名（避免被清洗）
    private static final Set<String> KNOWN_CHARACTERS = new HashSet<>(Arrays.asList(
        "服务员", "风衣男人", "神秘女人", "老人", "小孩", "青年",
        "中年", "老板", "经理", "老师", "医生", "警察", "司机"
    ));

    // 提取名字的正则表达式
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "([\u4e00-\u9fa5]{2,5})[说道问道喊问笑哭道惊]|[\u4e00-\u9fa5\"\"''「」](.+?)[\"\"''「」]\\s*[，,。.！!？?\\s]*([\u4e00-\u9fa5]{2,5})[说道问道喊问笑哭]|([\u4e00-\u9fa5]{2,5})[：:]"
    );

    // 简单的名字出现匹配模式（用于统计频率）
    private static final Pattern SIMPLE_NAME_PATTERN = Pattern.compile(
        "([\u4e00-\u9fa5]{2,5})"
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

        // 逐行处理，更准确
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            // 模式1：XX说/道/问/答/喊
            Matcher m1 = NAME_PATTERN.matcher(line);
            while (m1.find()) {
                for (int i = 1; i <= m1.groupCount(); i++) {
                    String name = m1.group(i);
                    if (name != null && isValidCandidateName(name)) {
                        names.add(cleanName(name));
                    }
                }
            }
        }

        // 补充一些常见角色
        if (text.contains("服务员")) {
            names.add("服务员");
        }
        if (text.contains("风衣男人")) {
            names.add("风衣男人");
        }
        if (text.contains("神秘女人")) {
            names.add("神秘女人");
        }

        return names;
    }

    /**
     * 验证候选名字的有效性
     */
    private boolean isValidCandidateName(String name) {
        if (name == null || name.trim().isEmpty()) return false;

        name = name.trim();

        // 长度必须在2-5之间
        if (name.length() < 2 || name.length() > 5) return false;

        // 必须全是汉字
        if (!name.matches("[\u4e00-\u9fa5]+")) return false;

        // 如果是已知的角色，直接通过
        if (KNOWN_CHARACTERS.contains(name)) return true;

        // 第一个字必须是常见姓氏（除非是已知角色）
        String firstChar = name.substring(0, 1);
        if (!SINGLE_SURNAMES.contains(firstChar)) {
            return false;
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
        for (int i = name.length(); i >= 2; i--) {
            String candidate = name.substring(0, i);
            if (isValidCandidateName(candidate)) {
                // 检查候选名字是否后面跟着停用词
                String suffix = name.length() > i ? name.substring(i) : "";
                boolean endsWithStopWord = false;
                for (String stopWord : STOP_WORDS_AFTER) {
                    if (suffix.startsWith(stopWord)) {
                        endsWithStopWord = true;
                        break;
                    }
                }
                if (endsWithStopWord) {
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

        // 先把候选名字按清洗后分组
        Map<String, Set<String>> nameGroups = new HashMap<>();
        for (String candidate : candidates) {
            String cleaned = cleanName(candidate);
            nameGroups.computeIfAbsent(cleaned, k -> new LinkedHashSet<>()).add(candidate);
        }

        // 统计每个清洗后名字的出现频率
        for (String cleanedName : nameGroups.keySet()) {
            int count = 0;
            Pattern p = Pattern.compile(Pattern.quote(cleanedName));
            Matcher m = p.matcher(text);
            while (m.find()) count++;

            if (count >= 2) { // 至少出现2次才认为是角色
                nameCount.put(cleanedName, count);
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

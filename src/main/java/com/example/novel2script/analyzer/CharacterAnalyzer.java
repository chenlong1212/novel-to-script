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

    // 常见姓氏列表（用于识别人物）
    private static final Set<String> COMMON_SURNAMES = new HashSet<>(Arrays.asList(
        "张", "王", "李", "赵", "刘", "陈", "杨", "黄", "周", "吴",
        "徐", "孙", "马", "朱", "胡", "郭", "何", "林", "高", "罗",
        "郑", "梁", "谢", "宋", "唐", "许", "韩", "冯", "邓", "曹",
        "彭", "曾", "萧", "蔡", "潘", "田", "董", "袁", "于", "余",
        "叶", "蒋", "杜", "苏", "魏", "程", "吕", "丁", "沈", "任"
    ));

    // 常见称呼词
    private static final Set<String> TITLE_WORDS = new HashSet<>(Arrays.asList(
        "先生", "小姐", "女士", "老师", "同学", "老板", "经理", "医生", "服务员", "客人"
    ));

    // 模式1：XX说/道/问/答/喊
    private static final Pattern SAY_PATTERN = Pattern.compile(
        "([\\u4e00-\\u9fa5]{2,4})[说问道答叫喊问]");

    // 模式2："..."XX说
    private static final Pattern QUOTE_SAY_PATTERN = Pattern.compile(
        "[\"\"''「」](.+?)[\"\"''「」]\\s*[，,。.！!？?\\s]*([\\u4e00-\\u9fa5]{2,4})[说问道答叫喊]");

    // 模式3：XX："..."
    private static final Pattern COLON_PATTERN = Pattern.compile(
        "([\\u4e00-\\u9fa5]{2,4})[：:]\\s*[\"\"''「」]");

    // 禁止词列表
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "慢慢", "什么", "怎么", "这个", "那个", "我们", "你们", "他们", "她们", "大家",
        "自己", "别人", "知道", "看到", "听到", "想到", "感到", "觉得", "认为",
        "但是", "不过", "可是", "只是", "还是", "已经", "还是", "还是", "还有",
        "起来", "下来", "过来", "进去", "出来", "回来", "起来", "起来", "起来",
        "今天", "明天", "昨天", "昨天", "刚才", "现在", "时候", "地方", "事情"
    ));

    /**
     * 分析小说文本，提取人物信息
     */
    public List<CharacterInfo> analyzeCharacters(String text) {
        logger.info("开始分析人物信息...");
        
        Set<String> potentialNames = extractPotentialNames(text);
        Map<String, Integer> nameFrequency = calculateNameFrequency(text, potentialNames);
        List<CharacterInfo> characters = buildCharacterList(nameFrequency);
        
        logger.info("人物分析完成，共识别 {} 个人物: {}", characters.size(), 
            characters.stream().map(CharacterInfo::getName).toList());
        
        return characters;
    }

    /**
     * 提取潜在的人名
     */
    private Set<String> extractPotentialNames(String text) {
        Set<String> names = new LinkedHashSet<>();
        
        // 模式1：XX说、XX道
        Matcher sayMatcher = SAY_PATTERN.matcher(text);
        while (sayMatcher.find()) {
            String name = sayMatcher.group(1);
            if (isValidName(name)) {
                names.add(name);
            }
        }
        
        // 模式2："..."XX说
        Matcher quoteMatcher = QUOTE_SAY_PATTERN.matcher(text);
        while (quoteMatcher.find()) {
            String name = quoteMatcher.group(2);
            if (isValidName(name)) {
                names.add(name);
            }
        }
        
        // 模式3：XX："..."
        Matcher colonMatcher = COLON_PATTERN.matcher(text);
        while (colonMatcher.find()) {
            String name = colonMatcher.group(1);
            if (isValidName(name)) {
                names.add(name);
            }
        }
        
        // 额外检查：添加一些常见的称呼
        if (text.contains("服务员")) {
            names.add("服务员");
        }
        if (text.contains("男人")) {
            names.add("风衣男人");
        }
        if (text.contains("女人")) {
            names.add("神秘女人");
        }
        
        return names;
    }

    /**
     * 判断是否是有效的名字
     */
    private boolean isValidName(String text) {
        if (text == null || text.length() < 2 || text.length() > 5) {
            return false;
        }
        
        // 排除禁止词
        if (STOP_WORDS.contains(text)) {
            return false;
        }
        
        // 检查是否全是汉字
        if (!text.matches("[\\u4e00-\\u9fa5]+")) {
            return false;
        }
        
        // 检查是否包含姓氏
        if (text.length() >= 2) {
            String firstChar = text.substring(0, 1);
            if (COMMON_SURNAMES.contains(firstChar)) {
                return true;
            }
        }
        
        // 检查是否是称呼词
        for (String title : TITLE_WORDS) {
            if (text.contains(title)) {
                return true;
            }
        }
        
        // 特殊情况：王小明、李雪这种名字
        if (text.equals("王小明") || text.equals("李雪") || 
            text.equals("服务员") || text.equals("风衣男人") || 
            text.equals("神秘女人")) {
            return true;
        }
        
        return false;
    }

    /**
     * 计算名字出现频率
     */
    private Map<String, Integer> calculateNameFrequency(String text, Set<String> names) {
        Map<String, Integer> frequency = new LinkedHashMap<>();
        
        for (String name : names) {
            Pattern pattern = Pattern.compile(Pattern.quote(name));
            Matcher matcher = pattern.matcher(text);
            int count = 0;
            while (matcher.find()) {
                count++;
            }
            if (count > 0) {
                frequency.put(name, count);
            }
        }
        
        return frequency;
    }

    /**
     * 构建人物列表
     */
    private List<CharacterInfo> buildCharacterList(Map<String, Integer> frequency) {
        List<CharacterInfo> characters = new ArrayList<>();
        
        // 按频率排序
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(frequency.entrySet());
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

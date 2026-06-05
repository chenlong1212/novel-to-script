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

    // 人称代词模式
    private static final Pattern PRONOUN_PATTERN = Pattern.compile(
        "(他|她|他们|她们)的"
    );

    // 引号对话模式（中文）
    private static final Pattern DIALOGUE_PATTERN = Pattern.compile(
        "[「\"'](.+?)[」\"']\\s*[,，]\\s*([^\"\"''「」\\n]+)",
        Pattern.DOTALL
    );

    // 对话标签模式
    private static final Pattern DIALOGUE_TAG_PATTERN = Pattern.compile(
        "([^\"\"''「」\\n：:：\\s]{2,4})[：:：]\\s*[\"\"''「「\"'](.+?)[\"\"''」」\"']",
        Pattern.DOTALL
    );

    /**
     * 分析小说文本，提取人物信息
     *
     * @param text 小说文本
     * @return 人物列表
     */
    public List<CharacterInfo> analyzeCharacters(String text) {
        logger.info("开始分析人物信息...");
        
        Set<String> potentialNames = extractPotentialNames(text);
        Map<String, Integer> nameFrequency = calculateNameFrequency(text, potentialNames);
        List<CharacterInfo> characters = buildCharacterList(nameFrequency);
        
        logger.info("人物分析完成，共识别 {} 个人物", characters.size());
        
        return characters;
    }

    /**
     * 提取潜在的人名
     */
    private Set<String> extractPotentialNames(String text) {
        Set<String> names = new LinkedHashSet<>();
        
        // 模式1：XX说、XX道、XX问道等
        Pattern sayPattern = Pattern.compile("([\\u4e00-\\u9fa5]{2,4})[说问道答叫喊念嘟囔叹称]");  
        Matcher sayMatcher = sayPattern.matcher(text);
        while (sayMatcher.find()) {
            String name = sayMatcher.group(1);
            if (isLikelyName(name)) {
                names.add(name);
            }
        }
        
        // 模式2："XXX" 或 'XXX' 开头
        Pattern quotePattern = Pattern.compile("[\"\"''「」]([\\u4e00-\\u9fa5]{2,4})[\"\"''「」]");
        Matcher quoteMatcher = quotePattern.matcher(text);
        while (quoteMatcher.find()) {
            String name = quoteMatcher.group(1);
            if (isLikelyName(name)) {
                names.add(name);
            }
        }
        
        return names;
    }

    /**
     * 计算名字出现频率
     */
    private Map<String, Integer> calculateNameFrequency(String text, Set<String> names) {
        Map<String, Integer> frequency = new LinkedHashMap<>();
        
        for (String name : names) {
            Pattern pattern = Pattern.compile(name);
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
        if (rank == 1 && frequency > 50) {
            return Character.Role.protagonist;  // 主角
        } else if (frequency > 20) {
            return Character.Role.supporting;   // 配角
        } else {
            return Character.Role.minor;        // 路人
        }
    }

    /**
     * 判断是否是可能的姓名
     */
    private boolean isLikelyName(String text) {
        if (text == null || text.length() < 2 || text.length() > 4) {
            return false;
        }
        
        // 检查是否包含姓氏
        if (text.length() >= 2) {
            String firstChar = text.substring(0, 1);
            if (COMMON_SURNAMES.contains(firstChar)) {
                return true;
            }
        }
        
        // 检查是否全是汉字
        return text.matches("[\\u4e00-\\u9fa5]+");
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

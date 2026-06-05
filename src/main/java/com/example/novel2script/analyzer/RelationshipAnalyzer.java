package com.example.novel2script.analyzer;

import com.example.novel2script.model.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 关系分析器
 * 分析人物之间的关系
 */
@Component
public class RelationshipAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(RelationshipAnalyzer.class);

    // 关系词映射
    private static final Map<String, RelationshipType> RELATION_WORDS = new HashMap<>();

    static {
        // 亲人关系
        RELATION_WORDS.put("父亲", RelationshipType.family);
        RELATION_WORDS.put("母亲", RelationshipType.family);
        RELATION_WORDS.put("爸爸", RelationshipType.family);
        RELATION_WORDS.put("妈妈", RelationshipType.family);
        RELATION_WORDS.put("儿子", RelationshipType.family);
        RELATION_WORDS.put("女儿", RelationshipType.family);
        RELATION_WORDS.put("兄弟", RelationshipType.family);
        RELATION_WORDS.put("兄妹", RelationshipType.family);
        RELATION_WORDS.put("姐弟", RelationshipType.family);
        RELATION_WORDS.put("夫妻", RelationshipType.family);
        RELATION_WORDS.put("丈夫", RelationshipType.family);
        RELATION_WORDS.put("妻子", RelationshipType.family);
        
        // 朋友关系
        RELATION_WORDS.put("朋友", RelationshipType.friend);
        RELATION_WORDS.put("好友", RelationshipType.friend);
        RELATION_WORDS.put("同学", RelationshipType.friend);
        RELATION_WORDS.put("同事", RelationshipType.friend);
        RELATION_WORDS.put("伙伴", RelationshipType.friend);
        RELATION_WORDS.put("搭档", RelationshipType.friend);
        
        // 敌人关系
        RELATION_WORDS.put("敌人", RelationshipType.enemy);
        RELATION_WORDS.put("对手", RelationshipType.enemy);
        RELATION_WORDS.put("仇人", RelationshipType.enemy);
        RELATION_WORDS.put("死对头", RelationshipType.enemy);
        
        // 恋人关系
        RELATION_WORDS.put("恋人", RelationshipType.lover);
        RELATION_WORDS.put("情侣", RelationshipType.lover);
        RELATION_WORDS.put("女朋友", RelationshipType.lover);
        RELATION_WORDS.put("男朋友", RelationshipType.lover);
        RELATION_WORDS.put("情人", RelationshipType.lover);
    }

    // 关系描述模式
    private static final Pattern RELATION_PATTERN = Pattern.compile(
        "([\\u4e00-\\u9fa5]{1,4})和([\\u4e00-\\u9fa5]{1,4})是(.+?)[。\\n]",
        Pattern.DOTALL
    );

    /**
     * 分析人物关系
     *
     * @param text              小说文本
     * @param characterNames    人物名称列表
     * @return 关系列表
     */
    public List<RelationshipInfo> analyzeRelationships(String text, List<String> characterNames) {
        logger.info("开始分析人物关系...");
        
        List<RelationshipInfo> relationships = new ArrayList<>();
        
        // 提取关系描述
        extractRelationDescriptions(text, relationships);
        
        // 基于共现分析关系
        analyzeCooccurrence(text, characterNames, relationships);
        
        logger.info("关系分析完成，共识别 {} 个关系", relationships.size());
        
        return relationships;
    }

    /**
     * 提取关系描述
     */
    private void extractRelationDescriptions(String text, List<RelationshipInfo> relationships) {
        Matcher matcher = RELATION_PATTERN.matcher(text);
        
        while (matcher.find()) {
            String person1 = matcher.group(1);
            String person2 = matcher.group(2);
            String relationDesc = matcher.group(3);
            
            RelationshipType type = determineRelationType(relationDesc);
            
            if (type != null) {
                RelationshipInfo info = new RelationshipInfo();
                info.setFrom(person1);
                info.setTo(person2);
                info.setType(type);
                info.setDescription(relationDesc);
                
                relationships.add(info);
            }
        }
    }

    /**
     * 基于共现分析关系
     */
    private void analyzeCooccurrence(String text, List<String> characterNames, 
                                     List<RelationshipInfo> relationships) {
        if (characterNames.size() < 2) {
            return;
        }
        
        // 计算人物共现次数
        Map<String, Map<String, Integer>> cooccurrence = new HashMap<>();
        
        for (int i = 0; i < characterNames.size(); i++) {
            for (int j = i + 1; j < characterNames.size(); j++) {
                String name1 = characterNames.get(i);
                String name2 = characterNames.get(j);
                
                int count = countCooccurrence(text, name1, name2);
                
                if (count > 5) {  // 超过5次共现，认为有关系
                    RelationshipInfo info = new RelationshipInfo();
                    info.setFrom(name1);
                    info.setTo(name2);
                    info.setType(RelationshipType.friend);  // 默认为朋友关系
                    info.setDescription("基于共现分析推断的关系");
                    info.setStrength(count);
                    
                    relationships.add(info);
                }
            }
        }
    }

    /**
     * 计算两个名字在文本中的共现次数
     */
    private int countCooccurrence(String text, String name1, String name2) {
        int count = 0;
        int index = 0;
        
        while (index < text.length()) {
            int found1 = text.indexOf(name1, index);
            int found2 = text.indexOf(name2, index);
            
            if (found1 == -1 && found2 == -1) {
                break;
            }
            
            int nextIndex = Math.min(
                found1 == -1 ? Integer.MAX_VALUE : found1,
                found2 == -1 ? Integer.MAX_VALUE : found2
            );
            
            // 检查在一定范围内是否有另一个名字
            String window = text.substring(
                Math.max(0, nextIndex - 200),
                Math.min(text.length(), nextIndex + 200)
            );
            
            if ((found1 != -1 && window.contains(name2)) ||
                (found2 != -1 && window.contains(name1))) {
                count++;
            }
            
            index = nextIndex + 1;
        }
        
        return count;
    }

    /**
     * 判断关系类型
     */
    private RelationshipType determineRelationType(String description) {
        for (Map.Entry<String, RelationshipType> entry : RELATION_WORDS.entrySet()) {
            if (description.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 关系信息内部类
     */
    public static class RelationshipInfo {
        private String from;
        private String to;
        private RelationshipType type;
        private String description;
        private int strength;

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public RelationshipType getType() {
            return type;
        }

        public void setType(RelationshipType type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getStrength() {
            return strength;
        }

        public void setStrength(int strength) {
            this.strength = strength;
        }
    }

    /**
     * 关系类型枚举
     */
    public enum RelationshipType {
        family,   // 家人
        friend,   // 朋友
        lover,    // 恋人
        enemy,    // 敌人
        colleague // 同事
    }
}

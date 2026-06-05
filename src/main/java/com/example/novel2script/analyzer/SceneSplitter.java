package com.example.novel2script.analyzer;

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
 * 场景分割器
 * 自动识别小说中的场景切换
 */
@Component
public class SceneSplitter {

    private static final Logger logger = LoggerFactory.getLogger(SceneSplitter.class);

    // 地点关键词
    private static final List<String> LOCATION_KEYWORDS = Arrays.asList(
        "来到", "走到", "进入", "到了", "来到", "进入", "走", "来到", 
        "回到", "前往", "来到", "到达", "站在", "坐在", "在", "房间", 
        "客厅", "厨房", "卧室", "书房", "餐厅", "客厅", "办公室", 
        "会议室", "教室", "医院", "学校", "公园", "咖啡厅", "餐厅", 
        "酒店", "旅馆", "机场", "火车站", "地铁站", "路上", "车里",
        "楼下", "楼上", "门外", "门内", "窗边", "阳台", "花园", "院子",
        "商场", "超市", "市场", "广场", "街道", "巷子里", "胡同里"
    );

    // 时间关键词
    private static final List<String> TIME_KEYWORDS = Arrays.asList(
        "第二天", "第三天", "一个月后", "一年后", "几年后",
        "早上", "上午", "中午", "下午", "傍晚", "晚上", "深夜",
        "春天", "夏天", "秋天", "冬天", "今年", "明年", "去年",
        "小时候", "长大后", "不久", "一会儿", "突然", "这时",
        "转眼", "转眼间", "不知不觉", "忽然间", "片刻后",
        "几小时后", "几天前", "几个月后", "几年前"
    );

    // 场景分隔符（常见小说章节标记）
    private static final Pattern SCENE_SEPARATOR_PATTERN = Pattern.compile(
        "^\\s*[=-]{3,}\\s*$|^\\s*[#*]{3,}\\s*$|^\\s*第.*章\\s*$|^\\s*[一二三四五六七八九十]+\\s*$",
        Pattern.MULTILINE
    );

    // 地点描述模式
    private static final Pattern LOCATION_PATTERN = Pattern.compile(
        "在[\\u4e00-\\u9fa5a-zA-Z0-9]{1,10}[里中内旁]"
    );

    // 时间描述模式
    private static final Pattern TIME_PATTERN = Pattern.compile(
        "(早上|上午|中午|下午|傍晚|晚上|深夜|凌晨|黎明|日出|日落|夜晚)"
    );

    // 常见场景类型
    private static final List<String> SCENE_TYPES = Arrays.asList(
        "室内", "室外", "办公室", "家庭", "公共场所", "交通工具",
        "回忆", "梦境", "现实", "闪回", "幻想"
    );

    // 氛围词库
    private static final Map<String, List<String>> ATMOSPHERE_KEYWORDS = new HashMap<>();

    static {
        ATMOSPHERE_KEYWORDS.put("紧张", Arrays.asList(
            "紧张", "害怕", "恐惧", "焦虑", "不安", "担心", "心跳",
            "颤抖", "冷汗", "发抖", "恐慌", "惊恐"
        ));
        ATMOSPHERE_KEYWORDS.put("温馨", Arrays.asList(
            "温馨", "温暖", "幸福", "甜蜜", "快乐", "开心", "温馨",
            "温暖", "亲切", "和蔼", "温暖的", "幸福的"
        ));
        ATMOSPHERE_KEYWORDS.put("悲伤", Arrays.asList(
            "悲伤", "难过", "伤心", "痛苦", "泪水", "哭泣", "眼泪",
            "痛苦", "绝望", "心碎", "悲痛", "哀伤"
        ));
        ATMOSPHERE_KEYWORDS.put("欢乐", Arrays.asList(
            "欢乐", "开心", "高兴", "快乐", "愉快", "欢快", "欢笑",
            "笑声", "喜悦", "快乐地", "开心地"
        ));
        ATMOSPHERE_KEYWORDS.put("神秘", Arrays.asList(
            "神秘", "奇怪", "诡异", "离奇", "神秘的", "奇怪的",
            "神秘地", "神秘地", "奇怪地", "奇怪地"
        ));
    }

    /**
     * 分割小说为场景
     *
     * @param text 小说文本
     * @return 场景列表
     */
    public List<SceneInfo> splitScenes(String text) {
        logger.info("开始分割场景...");
        
        List<SceneInfo> scenes = new ArrayList<>();
        List<Integer> splitPoints = new ArrayList<>();
        
        // 1. 查找分隔符
        findSeparators(text, splitPoints);
        
        // 2. 查找地点变化
        findLocationChanges(text, splitPoints);
        
        // 3. 查找时间变化
        findTimeChanges(text, splitPoints);
        
        // 4. 排序并去重分割点
        splitPoints = deduplicateAndSort(splitPoints, text.length());
        
        // 5. 根据分割点创建场景
        int start = 0;
        for (int i = 0; i < splitPoints.size(); i++) {
            int end = splitPoints.get(i);
            String sceneText = text.substring(start, end).trim();
            
            if (sceneText.length() > 50) { // 过滤太短的场景
                SceneInfo scene = analyzeScene(sceneText, i + 1);
                scenes.add(scene);
            }
            
            start = end;
        }
        
        // 6. 处理最后一段
        if (start < text.length()) {
            String sceneText = text.substring(start).trim();
            if (sceneText.length() > 50) {
                SceneInfo scene = analyzeScene(sceneText, scenes.size() + 1);
                scenes.add(scene);
            }
        }
        
        logger.info("场景分割完成，共识别 {} 个场景", scenes.size());
        
        return scenes;
    }

    /**
     * 查找分隔符
     */
    private void findSeparators(String text, List<Integer> splitPoints) {
        Matcher matcher = SCENE_SEPARATOR_PATTERN.matcher(text);
        while (matcher.find()) {
            splitPoints.add(matcher.start());
        }
    }

    /**
     * 查找地点变化
     */
    private void findLocationChanges(String text, List<Integer> splitPoints) {
        for (String keyword : LOCATION_KEYWORDS) {
            int index = 0;
            while ((index = text.indexOf(keyword, index)) != -1) {
                // 只在段落开始附近添加分割点
                int lineStart = text.lastIndexOf('\n', index) + 1;
                if (index - lineStart < 20) {
                    splitPoints.add(lineStart);
                }
                index += keyword.length();
            }
        }
    }

    /**
     * 查找时间变化
     */
    private void findTimeChanges(String text, List<Integer> splitPoints) {
        for (String keyword : TIME_KEYWORDS) {
            int index = 0;
            while ((index = text.indexOf(keyword, index)) != -1) {
                int lineStart = text.lastIndexOf('\n', index) + 1;
                if (index - lineStart < 20) {
                    splitPoints.add(lineStart);
                }
                index += keyword.length();
            }
        }
    }

    /**
     * 去重并排序分割点
     */
    private List<Integer> deduplicateAndSort(List<Integer> points, int maxLength) {
        List<Integer> sorted = new ArrayList<>(new java.util.TreeSet<>(points));
        List<Integer> filtered = new ArrayList<>();
        
        int last = 0;
        for (int point : sorted) {
            // 过滤太近的分割点（至少间隔500字）
            if (point - last >= 500 && point < maxLength) {
                filtered.add(point);
                last = point;
            }
        }
        
        return filtered;
    }

    /**
     * 分析单个场景
     */
    private SceneInfo analyzeScene(String text, int number) {
        SceneInfo info = new SceneInfo();
        info.setNumber(number);
        info.setRawText(text);
        
        // 提取地点
        info.setLocation(extractLocation(text));
        
        // 提取时间
        info.setTime(extractTime(text));
        
        // 提取描述
        info.setDescription(extractDescription(text));
        
        // 分析氛围
        info.setAtmosphere(analyzeAtmosphere(text));
        
        // 提取舞台指示
        info.setStageDirections(extractStageDirections(text));
        
        return info;
    }

    /**
     * 提取地点
     */
    private String extractLocation(String text) {
        Matcher matcher = LOCATION_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        
        // 尝试关键词匹配
        for (String keyword : LOCATION_KEYWORDS) {
            if (text.contains(keyword)) {
                return keyword;
            }
        }
        
        return "未知地点";
    }

    /**
     * 提取时间
     */
    private String extractTime(String text) {
        Matcher matcher = TIME_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        
        for (String keyword : TIME_KEYWORDS) {
            if (text.contains(keyword)) {
                return keyword;
            }
        }
        
        return "未知时间";
    }

    /**
     * 提取场景描述
     */
    private String extractDescription(String text) {
        // 取第一段或前200字
        int end = text.indexOf('\n');
        if (end == -1 || end > 200) {
            end = Math.min(200, text.length());
        }
        return text.substring(0, end).trim();
    }

    /**
     * 分析氛围
     */
    private String analyzeAtmosphere(String text) {
        Map<String, Integer> scores = new HashMap<>();
        
        for (Map.Entry<String, List<String>> entry : ATMOSPHERE_KEYWORDS.entrySet()) {
            int score = 0;
            for (String keyword : entry.getValue()) {
                int count = countOccurrences(text, keyword);
                score += count;
            }
            scores.put(entry.getKey(), score);
        }
        
        // 找得分最高的
        return scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .filter(e -> e.getValue() > 0)
            .map(Map.Entry::getKey)
            .orElse("中性");
    }

    /**
     * 提取舞台指示
     */
    private List<String> extractStageDirections(String text) {
        List<String> directions = new ArrayList<>();
        
        // 简单实现：提取括号内的内容或描述性文字
        Pattern parentheses = Pattern.compile("[(（](.*?)[)）]", Pattern.DOTALL);
        Matcher matcher = parentheses.matcher(text);
        
        while (matcher.find()) {
            String content = matcher.group(1).trim();
            if (content.length() > 5 && content.length() < 100) {
                directions.add(content);
            }
        }
        
        return directions;
    }

    /**
     * 计数关键词出现次数
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

    /**
     * 场景信息类
     */
    public static class SceneInfo {
        private int number;
        private String location;
        private String time;
        private String description;
        private String atmosphere;
        private List<String> stageDirections;
        private String rawText;

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getAtmosphere() {
            return atmosphere;
        }

        public void setAtmosphere(String atmosphere) {
            this.atmosphere = atmosphere;
        }

        public List<String> getStageDirections() {
            return stageDirections;
        }

        public void setStageDirections(List<String> stageDirections) {
            this.stageDirections = stageDirections;
        }

        public String getRawText() {
            return rawText;
        }

        public void setRawText(String rawText) {
            this.rawText = rawText;
        }
    }
}

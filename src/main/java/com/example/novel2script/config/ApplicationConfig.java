package com.example.novel2script.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "novel2script")
public class ApplicationConfig {
    private SchemaConfig schema = new SchemaConfig();
    private AiConfig ai = new AiConfig();
    private ConversionConfig conversion = new ConversionConfig();
    private OutputConfig output = new OutputConfig();

    public SchemaConfig getSchema() {
        return schema;
    }

    public void setSchema(SchemaConfig schema) {
        this.schema = schema;
    }

    public AiConfig getAi() {
        return ai;
    }

    public void setAi(AiConfig ai) {
        this.ai = ai;
    }

    public ConversionConfig getConversion() {
        return conversion;
    }

    public void setConversion(ConversionConfig conversion) {
        this.conversion = conversion;
    }

    public OutputConfig getOutput() {
        return output;
    }

    public void setOutput(OutputConfig output) {
        this.output = output;
    }

    public static class SchemaConfig {
        private String version = "1.0";
        private String characterPrefix = "char_";
        private String scenePrefix = "scene_";

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getCharacterPrefix() {
            return characterPrefix;
        }

        public void setCharacterPrefix(String characterPrefix) {
            this.characterPrefix = characterPrefix;
        }

        public String getScenePrefix() {
            return scenePrefix;
        }

        public void setScenePrefix(String scenePrefix) {
            this.scenePrefix = scenePrefix;
        }
    }

    public static class AiConfig {
        private String provider = "deepseek";
        private String model = "deepseek-chat";
        private String apiKey;
        private String apiUrl = "https://api.deepseek.com/v1/chat/completions";
        private double temperature = 0.7;
        private int maxTokens = 4096;

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        public int getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
        }
    }

    public static class ConversionConfig {
        private SceneConfig scene = new SceneConfig();
        private CharacterConfig character = new CharacterConfig();
        private EmotionConfig emotion = new EmotionConfig();

        public SceneConfig getScene() {
            return scene;
        }

        public void setScene(SceneConfig scene) {
            this.scene = scene;
        }

        public CharacterConfig getCharacter() {
            return character;
        }

        public void setCharacter(CharacterConfig character) {
            this.character = character;
        }

        public EmotionConfig getEmotion() {
            return emotion;
        }

        public void setEmotion(EmotionConfig emotion) {
            this.emotion = emotion;
        }
    }

    public static class SceneConfig {
        private int minLength = 100;
        private List<String> locationKeywords;
        private List<String> timeKeywords;

        public int getMinLength() {
            return minLength;
        }

        public void setMinLength(int minLength) {
            this.minLength = minLength;
        }

        public List<String> getLocationKeywords() {
            return locationKeywords;
        }

        public void setLocationKeywords(List<String> locationKeywords) {
            this.locationKeywords = locationKeywords;
        }

        public List<String> getTimeKeywords() {
            return timeKeywords;
        }

        public void setTimeKeywords(List<String> timeKeywords) {
            this.timeKeywords = timeKeywords;
        }
    }

    public static class CharacterConfig {
        private int protagonistThreshold = 10;
        private int supportingThreshold = 3;

        public int getProtagonistThreshold() {
            return protagonistThreshold;
        }

        public void setProtagonistThreshold(int protagonistThreshold) {
            this.protagonistThreshold = protagonistThreshold;
        }

        public int getSupportingThreshold() {
            return supportingThreshold;
        }

        public void setSupportingThreshold(int supportingThreshold) {
            this.supportingThreshold = supportingThreshold;
        }
    }

    public static class EmotionConfig {
        private boolean enabled = true;
        private Map<String, List<String>> keywords;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Map<String, List<String>> getKeywords() {
            return keywords;
        }

        public void setKeywords(Map<String, List<String>> keywords) {
            this.keywords = keywords;
        }
    }

    public static class OutputConfig {
        private String format = "yaml";
        private String encoding = "UTF-8";
        private boolean includeSource = false;
        private boolean generateReport = true;

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getEncoding() {
            return encoding;
        }

        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        public boolean isIncludeSource() {
            return includeSource;
        }

        public void setIncludeSource(boolean includeSource) {
            this.includeSource = includeSource;
        }

        public boolean isGenerateReport() {
            return generateReport;
        }

        public void setGenerateReport(boolean generateReport) {
            this.generateReport = generateReport;
        }
    }
}

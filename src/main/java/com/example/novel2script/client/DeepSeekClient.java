package com.example.novel2script.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * DeepSeek API 客户端
 * 用于调用 DeepSeek 大语言模型进行小说转剧本转换
 */
@Component
public class DeepSeekClient {

    private static final Logger logger = LoggerFactory.getLogger(DeepSeekClient.class);

    private final HttpClient httpClient;
    private final String apiKey;
    private final String apiUrl;
    private final String model;
    private final double temperature;
    private final int maxTokens;

    @Autowired
    public DeepSeekClient(com.example.novel2script.config.ApplicationConfig config) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        
        this.apiKey = config.getAi().getApiKey();
        this.apiUrl = config.getAi().getApiUrl();
        this.model = config.getAi().getModel();
        this.temperature = config.getAi().getTemperature();
        this.maxTokens = config.getAi().getMaxTokens();
        
        logger.info("DeepSeekClient 初始化完成");
        logger.info("API URL: {}", apiUrl);
        logger.info("Model: {}", model);
    }

    /**
     * 调用 DeepSeek API 进行对话
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户输入
     * @return AI 响应
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public String chat(String systemPrompt, String userPrompt) throws IOException, InterruptedException {
        logger.info("开始调用 DeepSeek API...");
        
        // 构建请求体
        String requestBody = String.format("""
            {
                "model": "%s",
                "messages": [
                    {
                        "role": "system",
                        "content": "%s"
                    },
                    {
                        "role": "user",
                        "content": "%s"
                    }
                ],
                "temperature": %.1f,
                "max_tokens": %d
            }
            """, 
            model,
            escapeJson(systemPrompt),
            escapeJson(userPrompt),
            temperature,
            maxTokens
        );

        // 构建 HTTP 请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(120))
                .build();

        // 发送请求
        logger.debug("发送请求到: {}", apiUrl);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // 处理响应
        if (response.statusCode() == 200) {
            String responseBody = response.body();
            logger.info("API 调用成功");
            
            // 解析 JSON 响应（简化处理）
            return extractContentFromResponse(responseBody);
        } else {
            logger.error("API 调用失败，状态码: {}", response.statusCode());
            logger.error("响应内容: {}", response.body());
            throw new IOException("API 调用失败: " + response.statusCode());
        }
    }

    /**
     * 提取响应中的内容
     */
    private String extractContentFromResponse(String responseBody) {
        // 简单的 JSON 解析（提取 content 字段）
        // 实际生产环境建议使用 Jackson 或 Gson
        int contentIndex = responseBody.indexOf("\"content\":\"");
        if (contentIndex != -1) {
            int start = contentIndex + 10;
            int end = responseBody.indexOf("\"", start);
            if (end != -1) {
                String content = responseBody.substring(start, end);
                // 处理转义字符
                content = content.replace("\\n", "\n")
                               .replace("\\\"", "\"")
                               .replace("\\\\", "\\");
                return content;
            }
        }
        
        logger.warn("无法从响应中提取内容");
        return "";
    }

    /**
     * JSON 字符串转义
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * 测试 API 连接
     */
    public boolean testConnection() {
        try {
            String response = chat(
                "你是一个简单的助手，只回答'连接成功'四个字。",
                "你好"
            );
            boolean success = response.contains("连接成功") || response.contains("成功");
            logger.info("API 连接测试: {}", success ? "成功" : "失败");
            return success;
        } catch (Exception e) {
            logger.error("API 连接测试失败: {}", e.getMessage());
            return false;
        }
    }
}

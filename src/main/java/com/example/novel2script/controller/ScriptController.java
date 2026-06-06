package com.example.novel2script.controller;

import com.example.novel2script.analyzer.AnalysisManager;
import com.example.novel2script.model.Script;
import com.example.novel2script.reader.NovelReader;
import com.example.novel2script.util.YamlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST API 控制器 - v1.2 版本
 * 返回符合YAML Schema规范的结果
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class ScriptController {

    private static final Logger logger = LoggerFactory.getLogger(ScriptController.class);
    
    private static final String UPLOAD_DIR = "uploads/";

    @Autowired
    private NovelReader novelReader;
    
    @Autowired
    private AnalysisManager analysisManager;
    
    @Autowired
    private YamlSerializer yamlSerializer;

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("version", "1.2.0");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * 上传小说并转换为剧本，直接返回YAML
     */
    @PostMapping(value = "/convert/yaml", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> convertToYaml(@RequestParam("file") MultipartFile file) {
        try {
            logger.info("收到转换请求: {}", file.getOriginalFilename());
            
            // 读取文件内容
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            
            // 执行完整分析
            AnalysisManager.AnalysisResult result = analysisManager.analyze(content);
            Script script = result.getScript();
            
            // 设置一些默认信息
            if (script.getMeta() != null) {
                script.getMeta().setSource(file.getOriginalFilename());
                script.getMeta().setAuthor("未知");
                script.getMeta().setAdapter("AI小说转剧本工具 v1.2");
            }
            
            // 序列化为YAML
            String yamlContent = yamlSerializer.serialize(script);
            
            logger.info("YAML转换成功，长度: {} 字符", yamlContent.length());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/x-yaml; charset=UTF-8"))
                    .body(yamlContent);
            
        } catch (Exception e) {
            logger.error("转换失败", e);
            return ResponseEntity.status(500)
                    .body("转换失败: " + e.getMessage());
        }
    }

    /**
     * 上传小说并转换为剧本，提供YAML下载
     */
    @PostMapping(value = "/convert/yaml/download", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> downloadYaml(@RequestParam("file") MultipartFile file) {
        try {
            logger.info("收到下载请求: {}", file.getOriginalFilename());
            
            // 读取文件内容
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            
            // 执行完整分析
            AnalysisManager.AnalysisResult result = analysisManager.analyze(content);
            Script script = result.getScript();
            
            // 设置一些默认信息
            if (script.getMeta() != null) {
                script.getMeta().setSource(file.getOriginalFilename());
                script.getMeta().setAuthor("未知");
                script.getMeta().setAdapter("AI小说转剧本工具 v1.2");
            }
            
            // 序列化为YAML
            String yamlContent = yamlSerializer.serialize(script);
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String baseName = originalFilename != null 
                ? originalFilename.replace(".txt", "") 
                : "script";
            String outputFileName = baseName + "_converted.yaml";
            
            logger.info("YAML下载准备完成: {}", outputFileName);
            
            // 返回可下载的响应
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + outputFileName + "\"")
                    .contentType(MediaType.parseMediaType("application/x-yaml; charset=UTF-8"))
                    .body(yamlContent.getBytes(StandardCharsets.UTF_8));
            
        } catch (Exception e) {
            logger.error("下载失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 使用文本内容转换为YAML（不是文件上传）
     */
    @PostMapping(value = "/convert/yaml/text", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> convertTextToYaml(@RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("小说内容不能为空");
            }
            
            logger.info("收到文本转换请求，内容长度: {}", content.length());
            
            // 执行完整分析
            AnalysisManager.AnalysisResult result = analysisManager.analyze(content);
            Script script = result.getScript();
            
            // 设置一些默认信息
            if (script.getMeta() != null) {
                script.getMeta().setSource("直接输入");
                script.getMeta().setAuthor("未知");
                script.getMeta().setAdapter("AI小说转剧本工具 v1.2");
            }
            
            // 序列化为YAML
            String yamlContent = yamlSerializer.serialize(script);
            
            logger.info("YAML转换成功，长度: {} 字符", yamlContent.length());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/x-yaml; charset=UTF-8"))
                    .body(yamlContent);
            
        } catch (Exception e) {
            logger.error("转换失败", e);
            return ResponseEntity.status(500)
                    .body("转换失败: " + e.getMessage());
        }
    }

    /**
     * 上传小说文件 - 保持兼容
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadNovel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            String originalFilename = file.getOriginalFilename();
            String fileId = UUID.randomUUID().toString();
            String fileName = fileId + "_" + originalFilename;
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.write(filePath, file.getBytes());
            
            logger.info("文件上传成功: {}", fileName);
            
            response.put("success", true);
            response.put("fileId", fileId);
            response.put("fileName", originalFilename);
            response.put("filePath", filePath.toString());
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            logger.error("文件上传失败", e);
            response.put("success", false);
            response.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}

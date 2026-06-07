package com.example.novel2script.controller;

import com.example.novel2script.analyzer.AnalysisManager;
import com.example.novel2script.model.Script;
import com.example.novel2script.util.YamlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST API 控制器 - v1.3 版本
 * 返回结构化对象和 YAML 格式
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class ScriptController {

    private static final Logger logger = LoggerFactory.getLogger(ScriptController.class);
    
    private static final String UPLOAD_DIR = "uploads/";

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
        response.put("version", "1.3.0");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * 上传小说并转换为剧本，返回完整响应
     */
    @PostMapping(value = "/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> convertNovel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
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
                script.getMeta().setAdapter("AI小说转剧本工具 v1.3");
            }
            
            // 序列化为 YAML
            String yamlContent = yamlSerializer.serialize(script);
            
            // 构建响应
            response.put("success", true);
            response.put("script", script);         // 结构化对象
            response.put("yaml", yamlContent);     // YAML 字符串
            response.put("fileName", file.getOriginalFilename());
            
            logger.info("转换成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("转换失败", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 下载 YAML 文件
     */
    @PostMapping(value = "/convert/download", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
                script.getMeta().setAdapter("AI小说转剧本工具 v1.3");
            }
            
            // 序列化为 YAML
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
                    .header("Content-Disposition", "attachment; filename=\"" + outputFileName + "\"")
                    .contentType(MediaType.parseMediaType("application/x-yaml; charset=UTF-8"))
                    .body(yamlContent.getBytes(StandardCharsets.UTF_8));
            
        } catch (Exception e) {
            logger.error("下载失败", e);
            return ResponseEntity.status(500).build();
        }
    }
}

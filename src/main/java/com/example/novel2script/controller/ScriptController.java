package com.example.novel2script.controller;

import com.example.novel2script.analyzer.AnalysisManager;
import com.example.novel2script.analyzer.Proofreader;
import com.example.novel2script.analyzer.QualityEvaluator;
import com.example.novel2script.model.*;
import com.example.novel2script.reader.NovelReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API 控制器
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class ScriptController {

    private static final Logger logger = LoggerFactory.getLogger(ScriptController.class);
    
    private static final String UPLOAD_DIR = "uploads/";
    private static final String OUTPUT_DIR = "output/";

    @Autowired
    private NovelReader novelReader;
    
    @Autowired
    private AnalysisManager analysisManager;
    
    @Autowired
    private QualityEvaluator qualityEvaluator;
    
    @Autowired
    private Proofreader proofreader;

    /**
     * 上传小说文件
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadNovel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 创建上传目录
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // 保存文件
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

    /**
     * 读取小说内容
     */
    @GetMapping("/novel/{fileId}")
    public ResponseEntity<Map<String, Object>> getNovelContent(@PathVariable String fileId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 查找对应的文件
            File uploadDir = new File(UPLOAD_DIR);
            File[] files = uploadDir.listFiles((dir, name) -> name.startsWith(fileId));
            
            if (files == null || files.length == 0) {
                response.put("success", false);
                response.put("message", "文件不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            String content = novelReader.readNovel(files[0].getAbsolutePath());
            
            response.put("success", true);
            response.put("content", content);
            response.put("fileName", files[0].getName());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("读取小说内容失败", e);
            response.put("success", false);
            response.put("message", "读取失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 执行转换
     */
    @PostMapping("/convert")
    public ResponseEntity<Map<String, Object>> convertNovel(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String content = request.get("content");
            if (content == null || content.isEmpty()) {
                response.put("success", false);
                response.put("message", "小说内容不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            logger.info("开始转换小说...");
            
            // 执行分析
            AnalysisManager.AnalysisResult result = analysisManager.analyze(content);
            Script script = result.getScript();
            
            // 执行质量评估
            QualityReport qualityReport = qualityEvaluator.evaluate(script, content);
            
            logger.info("转换完成，评分: {}", qualityReport.getOverallScore());
            
            response.put("success", true);
            response.put("script", script);
            response.put("qualityReport", qualityReport);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("转换失败", e);
            response.put("success", false);
            response.put("message", "转换失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取质量报告
     */
    @PostMapping("/quality")
    public ResponseEntity<Map<String, Object>> evaluateQuality(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String content = request.get("content");
            String scriptJson = request.get("script");
            
            if (content == null || content.isEmpty()) {
                response.put("success", false);
                response.put("message", "小说内容不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 这里应该从scriptJson反序列化Script对象
            // 简化处理，假设script已经在request中
            response.put("success", true);
            response.put("message", "质量评估需要完整的Script对象");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("质量评估失败", e);
            response.put("success", false);
            response.put("message", "评估失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 创建修正
     */
    @PostMapping("/corrections")
    public ResponseEntity<Map<String, Object>> createCorrection(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String scriptId = request.get("scriptId");
            String type = request.get("type");
            String targetId = request.get("targetId");
            String field = request.get("field");
            String originalValue = request.get("originalValue");
            String correctedValue = request.get("correctedValue");
            String reason = request.get("reason");
            
            if (scriptId == null || type == null || targetId == null) {
                response.put("success", false);
                response.put("message", "缺少必要参数");
                return ResponseEntity.badRequest().body(response);
            }
            
            Correction.CorrectionType correctionType = Correction.CorrectionType.valueOf(type.toUpperCase());
            
            Correction correction = proofreader.createCorrection(
                scriptId, correctionType, targetId, field,
                originalValue, correctedValue, reason
            );
            
            response.put("success", true);
            response.put("correction", correction);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("创建修正失败", e);
            response.put("success", false);
            response.put("message", "创建失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取修正列表
     */
    @GetMapping("/corrections/{scriptId}")
    public ResponseEntity<Map<String, Object>> getCorrections(@PathVariable String scriptId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Correction> corrections = proofreader.getCorrections(scriptId);
            List<Correction> pendingCorrections = proofreader.getPendingCorrections(scriptId);
            
            response.put("success", true);
            response.put("corrections", corrections);
            response.put("pendingCount", pendingCorrections.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取修正列表失败", e);
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 应用修正
     */
    @PostMapping("/corrections/{correctionId}/apply")
    public ResponseEntity<Map<String, Object>> applyCorrection(
            @PathVariable String correctionId,
            @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Script script = (Script) request.get("script");
            if (script == null) {
                response.put("success", false);
                response.put("message", "剧本对象不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 查找并应用修正
            List<Correction> corrections = proofreader.getCorrections(
                script.getMeta() != null ? script.getMeta().getTitle() : "unknown"
            );
            
            Correction targetCorrection = null;
            for (Correction c : corrections) {
                if (c.getCorrectionId().equals(correctionId)) {
                    targetCorrection = c;
                    break;
                }
            }
            
            if (targetCorrection == null) {
                response.put("success", false);
                response.put("message", "修正记录不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            proofreader.applyCorrection(targetCorrection, script);
            
            response.put("success", true);
            response.put("script", script);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("应用修正失败", e);
            response.put("success", false);
            response.put("message", "应用失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 导出剧本
     */
    @GetMapping("/export/{scriptId}")
    public ResponseEntity<Map<String, Object>> exportScript(@PathVariable String scriptId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 创建输出目录
            File outputDir = new File(OUTPUT_DIR);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            response.put("success", true);
            response.put("message", "导出功能需要完整的剧本对象");
            response.put("outputPath", OUTPUT_DIR + scriptId + ".yaml");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("导出失败", e);
            response.put("success", false);
            response.put("message", "导出失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("version", "1.0.0");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}

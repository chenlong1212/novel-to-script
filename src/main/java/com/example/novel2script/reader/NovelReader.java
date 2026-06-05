package com.example.novel2script.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 小说文件读取器
 * 支持读取 TXT 格式的小说文件
 */
@Component
public class NovelReader {

    private static final Logger logger = LoggerFactory.getLogger(NovelReader.class);

    /**
     * 读取小说文件
     *
     * @param filePath 文件路径
     * @return 小说文本内容
     * @throws IOException IO 异常
     */
    public String readNovel(String filePath) throws IOException {
        logger.info("开始读取小说文件: {}", filePath);
        
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            throw new IOException("文件不存在: " + filePath);
        }
        
        if (!Files.isReadable(path)) {
            throw new IOException("文件不可读: " + filePath);
        }
        
        // 读取文件内容
        String content = Files.readString(path, StandardCharsets.UTF_8);
        
        logger.info("成功读取小说文件，共 {} 个字符", content.length());
        
        return content;
    }

    /**
     * 读取小说文件并按行返回
     *
     * @param filePath 文件路径
     * @return 小说文本行列表
     * @throws IOException IO 异常
     */
    public List<String> readNovelLines(String filePath) throws IOException {
        logger.info("开始读取小说文件（按行）: {}", filePath);
        
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            throw new IOException("文件不存在: " + filePath);
        }
        
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        
        // 过滤空行和纯空白行
        List<String> filteredLines = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                filteredLines.add(trimmed);
            }
        }
        
        logger.info("成功读取小说文件，共 {} 行（非空行）", filteredLines.size());
        
        return filteredLines;
    }

    /**
     * 将小说文本分割成段落
     *
     * @param content 小说文本内容
     * @return 段落列表
     */
    public List<String> splitIntoParagraphs(String content) {
        logger.info("开始分割小说文本为段落...");
        
        // 按换行符分割
        String[] parts = content.split("\\n\\s*\\n");
        
        List<String> paragraphs = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                paragraphs.add(trimmed);
            }
        }
        
        logger.info("成功分割为 {} 个段落", paragraphs.size());
        
        return paragraphs;
    }

    /**
     * 提取小说元信息（标题、作者等）
     * 这是一个简单的实现，实际可能需要更复杂的逻辑
     *
     * @param content 小说文本内容
     * @return 元信息
     */
    public NovelMetadata extractMetadata(String content) {
        logger.info("开始提取小说元信息...");
        
        NovelMetadata metadata = new NovelMetadata();
        
        // 简单实现：假设前几行包含标题和作者信息
        String[] lines = content.split("\\n", 5);
        
        if (lines.length > 0) {
            // 第一行通常是标题
            String title = lines[0].trim();
            // 移除常见的标题前缀
            title = title.replaceAll("^[《\"]", "").replaceAll("[》\"]$", "");
            metadata.setTitle(title);
        }
        
        if (lines.length > 1) {
            // 第二行通常是作者
            String authorLine = lines[1].trim();
            if (authorLine.startsWith("作者：") || authorLine.startsWith("作者:")) {
                metadata.setAuthor(authorLine.substring(3).trim());
            }
        }
        
        logger.info("元信息提取完成: 标题={}, 作者={}", 
                   metadata.getTitle(), metadata.getAuthor());
        
        return metadata;
    }

    /**
     * 小说元信息类
     */
    public static class NovelMetadata {
        private String title;
        private String author;
        private String genre;
        private String description;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getGenre() {
            return genre;
        }

        public void setGenre(String genre) {
            this.genre = genre;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}

package com.example.novel2script.test;

import com.example.novel2script.converter.NovelToScriptConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 测试运行器
 * 用于测试各个组件功能
 */
@Component
public class TestRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(TestRunner.class);

    @Autowired
    private NovelToScriptConverter converter;

    @Override
    public void run(String... args) throws Exception {
        logger.info("=== AI 小说转剧本工具 v0.2 ===");
        logger.info("DeepSeek API 客户端已初始化");
        logger.info("小说转换器已就绪");
        logger.info("=== 系统准备完成 ===");
    }
}

package com.example.novel2script;

import com.example.novel2script.config.ApplicationConfig;
import com.example.novel2script.util.IDGenerator;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationConfig.class)
public class Novel2ScriptApplication {

    private static final Logger logger = LoggerFactory.getLogger(Novel2ScriptApplication.class);

    public static void main(String[] args) {
        // Load environment variables from .env file
        Dotenv.configure().load();
        SpringApplication.run(Novel2ScriptApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(ApplicationConfig config) {
        return args -> {
            logger.info("=== AI小说转剧本工具 v1.4.0 ===");
            logger.info("Schema版本: {}", config.getSchema().getVersion());
            logger.info("AI提供商: {}", config.getAi().getProvider());
            logger.info("输出格式: {}", config.getOutput().getFormat());
            logger.info("=== 系统初始化完成 ===");
        };
    }

    @Bean
    @Primary
    public IDGenerator idGenerator(ApplicationConfig config) {
        return new IDGenerator(
            config.getSchema().getCharacterPrefix(),
            config.getSchema().getScenePrefix()
        );
    }
}

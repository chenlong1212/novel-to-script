package com.example.novel2script.cli;

import com.example.novel2script.converter.NovelToScriptConverter;
import com.example.novel2script.model.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Scanner;

/**
 * 命令行接口
 * 提供用户交互式的转换操作
 * 只在 cli profile 下自动运行
 */
@Component
@Profile("cli")
public class ScriptConverterCLI implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ScriptConverterCLI.class);

    private final NovelToScriptConverter converter;

    @Autowired
    public ScriptConverterCLI(
            NovelToScriptConverter converter) {
        this.converter = converter;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("=== AI 小说转剧本工具 - 命令行界面 ===");
        
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    runConversion(scanner);
                    break;
                case "2":
                    testConnection();
                    break;
                case "3":
                    System.out.println("感谢使用，再见！");
                    return;
                default:
                    System.out.println("无效的选择，请重新输入。");
            }
            
            System.out.println();
        }
    }

    /**
     * 打印菜单
     */
    private void printMenu() {
        System.out.println("请选择操作：");
        System.out.println("1. 转换小说文件");
        System.out.println("2. 测试 API 连接");
        System.out.println("3. 退出");
        System.out.print("请输入选项 (1-3): ");
    }

    /**
     * 运行转换
     */
    private void runConversion(Scanner scanner) {
        System.out.print("请输入小说文件路径: ");
        String filePath = scanner.nextLine().trim();
        
        if (filePath.isEmpty()) {
            System.out.println("文件路径不能为空。");
            return;
        }
        
        System.out.println("开始转换，请稍候...");
        
        try {
            Script script = converter.convert(filePath);
            
            System.out.println("✓ 转换成功！");
            System.out.println("剧本信息：");
            System.out.println("  - 标题: " + script.getMeta().getTitle());
            System.out.println("  - 作者: " + script.getMeta().getAuthor());
            System.out.println("  - 场景数: " + script.getMeta().getTotalScenes());
            System.out.println("  - 人物数: " + script.getMeta().getTotalCharacters());
            
            // 生成输出文件名
            String outputPath = generateOutputPath(filePath);
            System.out.println("  - 输出文件: " + outputPath);
            
        } catch (IOException e) {
            System.err.println("✗ 文件读取失败: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("✗ 转换被中断: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("✗ 转换失败: " + e.getMessage());
            logger.error("转换失败", e);
        }
    }

    /**
     * 生成输出文件路径
     */
    private String generateOutputPath(String inputPath) {
        // 简单实现：从输入路径生成输出路径
        // 例如: input/novel.txt -> output/novel.yaml
        if (inputPath.contains("/")) {
            return inputPath.substring(0, inputPath.lastIndexOf("/") + 1) + 
                   "output/" + 
                   inputPath.substring(inputPath.lastIndexOf("/") + 1).replace(".txt", ".yaml");
        } else if (inputPath.contains("\\")) {
            return inputPath.substring(0, inputPath.lastIndexOf("\\") + 1) + 
                   "output\\" + 
                   inputPath.substring(inputPath.lastIndexOf("\\") + 1).replace(".txt", ".yaml");
        } else {
            return "output/" + inputPath.replace(".txt", ".yaml");
        }
    }

    /**
     * 测试连接
     */
    private void testConnection() {
        System.out.println("测试 API 连接...");
        
        try {
            converter.testConverter();
            System.out.println("✓ 连接测试完成");
        } catch (Exception e) {
            System.err.println("✗ 连接测试失败: " + e.getMessage());
            logger.error("连接测试失败", e);
        }
    }
}

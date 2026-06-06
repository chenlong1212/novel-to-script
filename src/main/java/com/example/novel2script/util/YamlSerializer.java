package com.example.novel2script.util;

import com.example.novel2script.model.Script;
import com.example.novel2script.model.ScriptWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;

/**
 * YAML 序列化工具类
 * 用于将 Script 对象转换为符合规范的 YAML 格式
 */
@Component
public class YamlSerializer {
    
    private static final Logger logger = LoggerFactory.getLogger(YamlSerializer.class);
    
    private final ObjectMapper yamlMapper;

    public YamlSerializer() {
        // 配置 YAML 生成器
        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        
        this.yamlMapper = new ObjectMapper(yamlFactory);
        // 配置日期格式
        this.yamlMapper.findAndRegisterModules();
    }

    /**
     * 将 Script 对象序列化为 YAML 字符串
     * 输出格式符合 Schema 规范:
     * script:
     *   schema_version: "1.0"
     *   meta: ...
     * 
     * @param script 剧本对象
     * @return YAML 字符串
     */
    public String serialize(Script script) throws IOException {
        logger.info("开始序列化剧本为YAML格式");
        
        // 包装在 script 根节点下
        ScriptWrapper wrapper = new ScriptWrapper(script);
        
        StringWriter writer = new StringWriter();
        yamlMapper.writeValue(writer, wrapper);
        
        String yamlString = writer.toString();
        logger.info("YAML序列化完成，长度: {} 字符", yamlString.length());
        
        return yamlString;
    }

    /**
     * 直接获取包装后的对象
     */
    public ScriptWrapper wrap(Script script) {
        return new ScriptWrapper(script);
    }
}

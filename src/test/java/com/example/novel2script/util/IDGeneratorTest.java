package com.example.novel2script.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IDGeneratorTest {

    private IDGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new IDGenerator();
    }

    @Test
    @DisplayName("测试生成人物ID")
    void testGenerateCharacterId() {
        String id1 = generator.generateCharacterId();
        String id2 = generator.generateCharacterId();
        
        assertEquals("char_001", id1);
        assertEquals("char_002", id2);
        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("测试生成场景ID")
    void testGenerateSceneId() {
        String id1 = generator.generateSceneId();
        String id2 = generator.generateSceneId();
        
        assertEquals("scene_001", id1);
        assertEquals("scene_002", id2);
        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("测试注册已存在的ID")
    void testRegisterExistingId() {
        generator.registerCharacterId("char_005");
        
        String newId = generator.generateCharacterId();
        assertEquals("char_006", newId);
    }

    @Test
    @DisplayName("测试重置功能")
    void testReset() {
        generator.generateCharacterId();
        generator.generateCharacterId();
        generator.generateSceneId();
        
        generator.reset();
        
        String charId = generator.generateCharacterId();
        String sceneId = generator.generateSceneId();
        
        assertEquals("char_001", charId);
        assertEquals("scene_001", sceneId);
    }

    @Test
    @DisplayName("测试统计功能")
    void testGetCharacterCount() {
        generator.generateCharacterId();
        generator.generateCharacterId();
        generator.generateSceneId();
        
        assertEquals(2, generator.getCharacterCount());
        assertEquals(1, generator.getSceneCount());
    }

    @Test
    @DisplayName("测试自定义前缀")
    void testCustomPrefix() {
        IDGenerator customGenerator = new IDGenerator("character_", "scene_");
        
        String charId = customGenerator.generateCharacterId();
        String sceneId = customGenerator.generateSceneId();
        
        assertEquals("character_001", charId);
        assertEquals("scene_001", sceneId);
    }
}

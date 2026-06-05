package com.example.novel2script.util;

import java.util.HashSet;
import java.util.Set;

public class IDGenerator {
    private final String characterPrefix;
    private final String scenePrefix;
    private int characterCounter = 0;
    private int sceneCounter = 0;
    private final Set<String> usedCharacterIds = new HashSet<>();
    private final Set<String> usedSceneIds = new HashSet<>();

    public IDGenerator() {
        this("char_", "scene_");
    }

    public IDGenerator(String characterPrefix, String scenePrefix) {
        this.characterPrefix = characterPrefix;
        this.scenePrefix = scenePrefix;
    }

    public String generateCharacterId() {
        characterCounter++;
        String charId = String.format("%s%03d", characterPrefix, characterCounter);
        
        while (usedCharacterIds.contains(charId)) {
            characterCounter++;
            charId = String.format("%s%03d", characterPrefix, characterCounter);
        }
        
        usedCharacterIds.add(charId);
        return charId;
    }

    public String generateSceneId() {
        sceneCounter++;
        String sceneId = String.format("%s%03d", scenePrefix, sceneCounter);
        
        while (usedSceneIds.contains(sceneId)) {
            sceneCounter++;
            sceneId = String.format("%s%03d", scenePrefix, sceneCounter);
        }
        
        usedSceneIds.add(sceneId);
        return sceneId;
    }

    public void registerCharacterId(String charId) {
        usedCharacterIds.add(charId);
        try {
            int num = Integer.parseInt(charId.replace(characterPrefix, ""));
            if (num > characterCounter) {
                characterCounter = num;
            }
        } catch (NumberFormatException e) {
            // Ignore invalid format
        }
    }

    public void registerSceneId(String sceneId) {
        usedSceneIds.add(sceneId);
        try {
            int num = Integer.parseInt(sceneId.replace(scenePrefix, ""));
            if (num > sceneCounter) {
                sceneCounter = num;
            }
        } catch (NumberFormatException e) {
            // Ignore invalid format
        }
    }

    public void reset() {
        characterCounter = 0;
        sceneCounter = 0;
        usedCharacterIds.clear();
        usedSceneIds.clear();
    }

    public int getCharacterCount() {
        return usedCharacterIds.size();
    }

    public int getSceneCount() {
        return usedSceneIds.size();
    }
}

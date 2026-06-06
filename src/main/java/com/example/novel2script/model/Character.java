package com.example.novel2script.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class Character {
    private String id;
    private String name;
    private Role role;
    private String description;
    
    @JsonProperty("first_appear")
    private String firstAppear;
    
    @JsonProperty("last_appear")
    private String lastAppear;
    
    private List<String> aliases;
    private Map<String, Object> attributes;

    public Character() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFirstAppear() {
        return firstAppear;
    }

    public void setFirstAppear(String firstAppear) {
        this.firstAppear = firstAppear;
    }

    public String getLastAppear() {
        return lastAppear;
    }

    public void setLastAppear(String lastAppear) {
        this.lastAppear = lastAppear;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public enum Role {
        protagonist, antagonist, supporting, minor, cameo
    }
}

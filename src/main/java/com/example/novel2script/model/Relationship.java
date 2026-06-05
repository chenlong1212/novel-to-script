package com.example.novel2script.model;

public class Relationship {
    private String from;
    private String to;
    private RelationshipType type;
    private String description;

    public Relationship() {
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public RelationshipType getType() {
        return type;
    }

    public void setType(RelationshipType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public enum RelationshipType {
        family, friend, colleague, lover, enemy, superior, subordinate, acquaintance, stranger
    }
}

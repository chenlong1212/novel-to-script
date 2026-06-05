package com.example.novel2script.model;

public class Location {
    private String name;
    private LocationType type;
    private Boolean interior;
    private String description;

    public Location() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocationType getType() {
        return type;
    }

    public void setType(LocationType type) {
        this.type = type;
    }

    public Boolean getInterior() {
        return interior;
    }

    public void setInterior(Boolean interior) {
        this.interior = interior;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public enum LocationType {
        fixed, public_space, private_space, outdoor, virtual
    }
}

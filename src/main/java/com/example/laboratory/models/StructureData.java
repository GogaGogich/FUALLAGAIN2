package com.example.laboratory.models;

import org.bukkit.Location;

public class StructureData {
    
    private final String id;
    private final String type;
    private final Location location;
    
    public StructureData(String id, String type, Location location) {
        this.id = id;
        this.type = type;
        this.location = location;
    }
    
    public String getId() {
        return id;
    }
    
    public String getType() {
        return type;
    }
    
    public Location getLocation() {
        return location;
    }
}
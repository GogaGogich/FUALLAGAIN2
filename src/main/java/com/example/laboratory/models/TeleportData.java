package com.example.laboratory.models;

import org.bukkit.Location;

public class TeleportData {
    
    private final String id;
    private final Location location;
    
    public TeleportData(String id, Location location) {
        this.id = id;
        this.location = location;
    }
    
    public String getId() {
        return id;
    }
    
    public Location getLocation() {
        return location;
    }
}
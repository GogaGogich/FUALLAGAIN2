package com.example.laboratory.models;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Research {
    
    private final String id;
    private final String name;
    private final int timeSeconds;
    private final List<ItemStack> materials;
    private final List<String> prerequisites;
    
    public Research(String id, String name, int timeSeconds, List<ItemStack> materials, List<String> prerequisites) {
        this.id = id;
        this.name = name;
        this.timeSeconds = timeSeconds;
        this.materials = materials;
        this.prerequisites = prerequisites;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public int getTimeSeconds() {
        return timeSeconds;
    }
    
    public List<ItemStack> getMaterials() {
        return materials;
    }
    
    public List<String> getPrerequisites() {
        return prerequisites;
    }
}
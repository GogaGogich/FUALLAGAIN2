package com.example.laboratory.managers;

import com.example.laboratory.LaboratoryPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    
    private final LaboratoryPlugin plugin;
    private FileConfiguration config;
    private File configFile;
    
    public ConfigManager(LaboratoryPlugin plugin) {
        this.plugin = plugin;
        createConfig();
    }
    
    private void createConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        setDefaults();
    }
    
    private void setDefaults() {
        config.addDefault("radiation.check-interval", 20);
        config.addDefault("radiation.effects.low-threshold", 20);
        config.addDefault("radiation.effects.medium-threshold", 50);
        config.addDefault("radiation.effects.high-threshold", 100);
        config.addDefault("radiation.effects.critical-threshold", 200);
        
        config.addDefault("centrifuge.process-time", 300);
        config.addDefault("centrifuge.min-uranium", 1);
        config.addDefault("centrifuge.max-uranium", 5);
        
        config.addDefault("research.auto-save-interval", 600);
        
        config.addDefault("tablet.max-structures", 50);
        config.addDefault("tablet.update-interval", 40);
        
        config.options().copyDefaults(true);
        saveConfig();
    }
    
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("Configuration reloaded!");
    }
    
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config: " + e.getMessage());
        }
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
    
    public int getRadiationCheckInterval() {
        return config.getInt("radiation.check-interval", 20);
    }
    
    public int getCentrifugeProcessTime() {
        return config.getInt("centrifuge.process-time", 300);
    }
    
    public int getTabletMaxStructures() {
        return config.getInt("tablet.max-structures", 50);
    }
}
package com.example.laboratory;

import com.example.laboratory.commands.RadiationCommand;
import com.example.laboratory.commands.TeleportCommand;
import com.example.laboratory.commands.LaboratoryCommand;
import com.example.laboratory.listeners.BlockListener;
import com.example.laboratory.listeners.PlayerListener;
import com.example.laboratory.managers.CentrifugeManager;
import com.example.laboratory.managers.RadiationManager;
import com.example.laboratory.managers.ResearchManager;
import com.example.laboratory.managers.TeleportManager;
import com.example.laboratory.managers.TabletManager;
import com.example.laboratory.managers.ConfigManager;
import com.example.laboratory.managers.DataManager;
import org.bukkit.plugin.java.JavaPlugin;

public class LaboratoryPlugin extends JavaPlugin {
    
    private static LaboratoryPlugin instance;
    private RadiationManager radiationManager;
    private ResearchManager researchManager;
    private TeleportManager teleportManager;
    private CentrifugeManager centrifugeManager;
    private TabletManager tabletManager;
    private ConfigManager configManager;
    private DataManager dataManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize config first
        configManager = new ConfigManager(this);
        
        // Check if Nexo is available
        if (!getServer().getPluginManager().isPluginEnabled("Nexo")) {
            getLogger().severe("Nexo plugin not found! This plugin requires Nexo to function.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Initialize managers
        dataManager = new DataManager(this);
        dataManager.loadData();
        radiationManager = new RadiationManager(this);
        researchManager = new ResearchManager(this);
        teleportManager = new TeleportManager(this);
        centrifugeManager = new CentrifugeManager(this);
        tabletManager = new TabletManager(this);
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        // Register commands
        getCommand("teleport").setExecutor(new TeleportCommand(teleportManager));
        getCommand("radiation").setExecutor(new RadiationCommand(radiationManager));
        getCommand("laboratory").setExecutor(new LaboratoryCommand(this));
        
        getLogger().info("Laboratory Plugin enabled successfully!");
        }
        if (radiationManager != null) {
            radiationManager.shutdown();
        }
        getLogger().info("Laboratory Plugin disabled!");
    }
    
    public static LaboratoryPlugin getInstance() {
        return instance;
    }
    
    public RadiationManager getRadiationManager() {
        return radiationManager;
    }
    
    public ResearchManager getResearchManager() {
        return researchManager;
    }
    
    public TeleportManager getTeleportManager() {
        return teleportManager;
    }
    
    public CentrifugeManager getCentrifugeManager() {
        return centrifugeManager;
    }
    
    public TabletManager getTabletManager() {
        return tabletManager;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
}
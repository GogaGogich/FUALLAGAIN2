package com.example.laboratory;

import com.example.laboratory.commands.RadiationCommand;
import com.example.laboratory.commands.TeleportCommand;
import com.example.laboratory.commands.LaboratoryCommand;
import com.example.laboratory.listeners.BlockListener;
import com.example.laboratory.listeners.PlayerListener;
import com.example.laboratory.managers.*;
import com.example.laboratory.utils.MessageUtils;
import org.bukkit.plugin.Plugin;
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
    private RecipeManager recipeManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize config first
        configManager = new ConfigManager(this);
        
        // Check if Nexo is available
        if (!getServer().getPluginManager().isPluginEnabled("Nexo")) {
            getLogger().severe("Nexo plugin not found! This plugin requires Nexo to function.");
            getLogger().severe("Please install Nexo plugin and restart the server.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Verify Nexo version compatibility
        Plugin nexoPlugin = getServer().getPluginManager().getPlugin("Nexo");
        String nexoVersion = nexoPlugin.getDescription().getVersion();
        getLogger().info("Found Nexo version: " + nexoVersion);
        
        if (!isNexoVersionCompatible(nexoVersion)) {
            getLogger().warning("Nexo version " + nexoVersion + " may not be fully compatible. Recommended: 1.9.0+");
        }
        
        // Initialize message utils
        MessageUtils.init(this);
        
        // Initialize managers
        dataManager = new DataManager(this);
        dataManager.loadData();
        radiationManager = new RadiationManager(this);
        researchManager = new ResearchManager(this);
        teleportManager = new TeleportManager(this);
        centrifugeManager = new CentrifugeManager(this);
        tabletManager = new TabletManager(this);
        recipeManager = new RecipeManager(this);
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        // Register commands
        getCommand("teleport").setExecutor(new TeleportCommand(teleportManager));
        getCommand("radiation").setExecutor(new RadiationCommand(radiationManager));
        getCommand("laboratory").setExecutor(new LaboratoryCommand(this));
        
        // Register recipes
        recipeManager.registerRecipes();
        
        // Auto-save task
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            dataManager.saveData();
        }, 0L, configManager.getConfig().getInt("research.auto-save-interval", 600) * 20L);
        
        getLogger().info("Laboratory Plugin enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        if (radiationManager != null) {
            radiationManager.shutdown();
        }
        if (dataManager != null) {
            dataManager.saveData();
        }
        getLogger().info("Laboratory Plugin disabled!");
    }
    
    private boolean isNexoVersionCompatible(String version) {
        try {
            String[] parts = version.split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            return major > 1 || (major == 1 && minor >= 9);
        } catch (Exception e) {
            return false;
        }
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
    
    public RecipeManager getRecipeManager() {
        return recipeManager;
    }
}
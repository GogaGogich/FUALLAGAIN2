package com.example.laboratory.utils;

import com.example.laboratory.LaboratoryPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageUtils {
    
    private static LaboratoryPlugin plugin;
    
    public static void init(LaboratoryPlugin pluginInstance) {
        plugin = pluginInstance;
    }
    
    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public static void sendMessage(Player player, String key) {
        String prefix = plugin.getConfigManager().getConfig().getString("messages.prefix", "§8[§6Laboratory§8]§r ");
        String message = plugin.getConfigManager().getConfig().getString("messages." + key, "Message not found: " + key);
        player.sendMessage(colorize(prefix + message));
    }
    
    public static void sendMessage(Player player, String key, String... replacements) {
        String prefix = plugin.getConfigManager().getConfig().getString("messages.prefix", "§8[§6Laboratory§8]§r ");
        String message = plugin.getConfigManager().getConfig().getString("messages." + key, "Message not found: " + key);
        
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        
        player.sendMessage(colorize(prefix + message));
    }
    
    public static String getMessage(String key) {
        return plugin.getConfigManager().getConfig().getString("messages." + key, "Message not found: " + key);
    }
}
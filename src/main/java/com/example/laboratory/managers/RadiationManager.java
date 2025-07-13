package com.example.laboratory.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.example.laboratory.LaboratoryPlugin;
import com.nexomc.nexo.api.NexoItems;

import java.util.HashMap;
import java.util.Map;

public class RadiationManager {
    
    private final LaboratoryPlugin plugin;
    private final Map<Player, Integer> playerRadiation;
    private BukkitTask radiationTask;
    
    public RadiationManager(LaboratoryPlugin plugin) {
        this.plugin = plugin;
        this.playerRadiation = new HashMap<>();
        startRadiationTask();
    }
    
    private void startRadiationTask() {
        radiationTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updatePlayerRadiation(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }
    
    private void updatePlayerRadiation(Player player) {
        if (!player.isOnline()) {
            return;
        }
        
        int radiationLevel = calculateRadiationLevel(player);
        playerRadiation.put(player, radiationLevel);
        
        // Check for protection
        if (hasRadiationProtection(player)) {
            return;
        }
        
        // Apply radiation effects based on level
        if (radiationLevel > 0) {
            applyRadiationEffects(player, radiationLevel);
        }
    }
    
    private int calculateRadiationLevel(Player player) {
        int totalRadiation = 0;
        
        try {
            // Check inventory for uranium items
            for (ItemStack item : player.getInventory().getContents()) {
                if (item == null) continue;
                
                String itemId = NexoItems.idFromItem(item);
                if (itemId == null) continue;
                
                switch (itemId) {
                    case "uranium_dust":
                        totalRadiation += item.getAmount() * 1; // 1 radiation per dust
                        break;
                    case "uranium_ingot":
                        totalRadiation += item.getAmount() * 9; // 9 radiation per ingot (9 dust)
                        break;
                    case "uranium_block":
                        totalRadiation += item.getAmount() * 81; // 81 radiation per block (81 dust)
                        break;
                }
            }
        } catch (Exception e) {
            // Fallback if Nexo API fails
            return 0;
        }
        
        return totalRadiation;
    }
    
    private boolean hasRadiationProtection(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chestplate = player.getInventory().getChestplate();
        ItemStack leggings = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();
        
        // Check for chemical protection suit
        if (isChemProtectionSuit(helmet, chestplate, leggings, boots)) {
            return true;
        }
        
        // Check for power armor
        if (isPowerArmor(helmet, chestplate, leggings, boots)) {
            return true;
        }
        
        return false;
    }
    
    private boolean isChemProtectionSuit(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
        return isNexoItem(helmet, "chem_protection_helmet") &&
               isNexoItem(chestplate, "chem_protection_chestplate") &&
               isNexoItem(leggings, "chem_protection_leggings") &&
               isNexoItem(boots, "chem_protection_boots");
    }
    
    private boolean isPowerArmor(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
        return isNexoItem(helmet, "power_armor_helmet") &&
               isNexoItem(chestplate, "power_armor_chestplate") &&
               isNexoItem(leggings, "power_armor_leggings") &&
               isNexoItem(boots, "power_armor_boots");
    }
    
    private boolean isNexoItem(ItemStack item, String itemId) {
        if (item == null) return false;
        try {
            String id = NexoItems.idFromItem(item);
            return itemId.equals(id);
        } catch (Exception e) {
            return false;
        }
    }
    
    private void applyRadiationEffects(Player player, int radiationLevel) {
        if (radiationLevel >= 100) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 40, 0));
        } else if (radiationLevel >= 50) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0));
        } else if (radiationLevel >= 20) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0));
        }
    }
    
    public int getPlayerRadiation(Player player) {
        return playerRadiation.getOrDefault(player, 0);
    }
    
    public void shutdown() {
        if (radiationTask != null) {
            radiationTask.cancel();
        }
    }
    
    public void clearPlayerRadiation(Player player) {
        playerRadiation.remove(player);
        // Remove all radiation effects
        player.removePotionEffect(PotionEffectType.POISON);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.removePotionEffect(PotionEffectType.NAUSEA);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
    }
}
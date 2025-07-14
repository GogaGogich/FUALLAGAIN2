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
import java.util.UUID;

public class RadiationManager {
    
    private final LaboratoryPlugin plugin;
    private final Map<UUID, Integer> playerRadiation;
    private final Map<UUID, Long> lastRadiationCheck;
    private BukkitTask radiationTask;
    
    public RadiationManager(LaboratoryPlugin plugin) {
        this.plugin = plugin;
        this.playerRadiation = new HashMap<>();
        this.lastRadiationCheck = new HashMap<>();
        startRadiationTask();
    }
    
    private void startRadiationTask() {
        int checkInterval = plugin.getConfigManager().getRadiationCheckInterval();
        
        radiationTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updatePlayerRadiation(player);
                }
            }
        }.runTaskTimer(plugin, 0L, checkInterval);
    }
    
    private void updatePlayerRadiation(Player player) {
        if (!player.isOnline()) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // Throttle radiation checks per player
        if (lastRadiationCheck.containsKey(playerId) && 
            currentTime - lastRadiationCheck.get(playerId) < 1000) {
            return;
        }
        
        lastRadiationCheck.put(playerId, currentTime);
        
        int radiationLevel = calculateRadiationLevel(player);
        int previousLevel = playerRadiation.getOrDefault(playerId, 0);
        playerRadiation.put(playerId, radiationLevel);
        
        // Check for protection
        if (hasRadiationProtection(player)) {
            // Clear radiation effects if protected
            if (previousLevel > 0) {
                clearRadiationEffects(player);
            }
            return;
        }
        
        // Apply radiation effects based on level
        if (radiationLevel > 0) {
            applyRadiationEffects(player, radiationLevel);
            
            // Notify player of radiation level changes
            if (radiationLevel != previousLevel) {
                notifyRadiationChange(player, radiationLevel, previousLevel);
            }
        } else if (previousLevel > 0) {
            // Clear effects when radiation drops to 0
            clearRadiationEffects(player);
        }
    }
    
    private int calculateRadiationLevel(Player player) {
        int totalRadiation = 0;
        
        // Check inventory for uranium items
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || !NexoItems.exists(item)) continue;
            
            String itemId = NexoItems.idFromItem(item);
            if (itemId == null) continue;
            
            switch (itemId) {
                case "uranium_dust":
                    totalRadiation += item.getAmount() * 1; // 1 radiation per dust
                    break;
                case "uranium_ingot":
                    totalRadiation += item.getAmount() * 9; // 9 radiation per ingot
                    break;
                case "uranium_block":
                    totalRadiation += item.getAmount() * 81; // 81 radiation per block
                    break;
                case "uranium_capsule":
                    // Capsules provide protection, reduce radiation from stored uranium
                    int storedUranium = getStoredUraniumFromCapsule(item);
                    totalRadiation += (storedUranium / 10); // 10x less radiation when in capsule
                    break;
            }
        }
        
        // Add environmental radiation sources
        totalRadiation += getEnvironmentalRadiation(player.getLocation());
        
        return totalRadiation;
    }
    
    private int getStoredUraniumFromCapsule(ItemStack capsule) {
        if (capsule.getItemMeta() == null || capsule.getItemMeta().getLore() == null) {
            return 0;
        }
        
        for (String line : capsule.getItemMeta().getLore()) {
            if (line.contains("Уран:")) {
                try {
                    String[] parts = line.split("/");
                    if (parts.length > 0) {
                        String numberPart = parts[0].replaceAll("[^0-9]", "");
                        if (!numberPart.isEmpty()) {
                            return Integer.parseInt(numberPart);
                        }
                    }
                } catch (Exception e) {
                    return 0;
                }
            }
        }
        return 0;
    }
    
    private int getEnvironmentalRadiation(Location location) {
        // Add radiation from nearby uranium blocks in the world
        int environmentalRadiation = 0;
        
        // Check blocks in a 5x5x5 area around player
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    Location checkLoc = location.clone().add(x, y, z);
                    
                    // Check if block is uranium block using simple API
                    String blockId = com.nexomc.nexo.api.NexoBlocks.idFromBlock(checkLoc.getBlock());
                    
                    if ("uranium_block".equals(blockId)) {
                        double distance = location.distance(checkLoc);
                        environmentalRadiation += (int) Math.max(1, 50 / (distance + 1));
                    }
                }
            }
        }
        
        return environmentalRadiation;
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
        
        // Check for power armor (better protection)
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
        if (item == null || !NexoItems.exists(item)) return false;
        return itemId.equals(NexoItems.idFromItem(item));
    }
    
    private void applyRadiationEffects(Player player, int radiationLevel) {
        // Enhanced radiation effects with gradual intensity
        if (radiationLevel >= 200) {
            // Critical radiation
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 3));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
            
            // Damage over time for critical levels
            if (Math.random() < 0.1) { // 10% chance per check
                player.damage(1.0);
            }
            
        } else if (radiationLevel >= 100) {
            // High radiation
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 40, 0));
            
        } else if (radiationLevel >= 50) {
            // Medium radiation
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0));
            
        } else if (radiationLevel >= 20) {
            // Low radiation
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0));
        }
    }
    
    private void clearRadiationEffects(Player player) {
        player.removePotionEffect(PotionEffectType.POISON);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.removePotionEffect(PotionEffectType.NAUSEA);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
    }
    
    private void notifyRadiationChange(Player player, int newLevel, int oldLevel) {
        if (newLevel > oldLevel && newLevel >= 50) {
            player.sendMessage("§c⚠ Уровень радиации повысился до " + newLevel + " рад/с!");
        } else if (newLevel < oldLevel && oldLevel >= 50 && newLevel < 50) {
            player.sendMessage("§aУровень радиации снизился до безопасного уровня");
        }
    }
    
    public int getPlayerRadiation(Player player) {
        return playerRadiation.getOrDefault(player.getUniqueId(), 0);
    }
    
    public String getRadiationStatus(Player player) {
        int radiation = getPlayerRadiation(player);
        
        if (radiation == 0) return "§aБезопасно";
        if (radiation < 20) return "§eНизкий";
        if (radiation < 50) return "§6Средний";
        if (radiation < 100) return "§cВысокий";
        if (radiation < 200) return "§4Критический";
        return "§4СМЕРТЕЛЬНЫЙ";
    }
    
    public void shutdown() {
        if (radiationTask != null) {
            radiationTask.cancel();
        }
    }
    
    public void clearPlayerRadiation(Player player) {
        UUID playerId = player.getUniqueId();
        playerRadiation.remove(playerId);
        lastRadiationCheck.remove(playerId);
        clearRadiationEffects(player);
    }
}
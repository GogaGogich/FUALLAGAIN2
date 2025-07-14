package com.example.laboratory.managers;

import com.example.laboratory.LaboratoryPlugin;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.NexoBlocks;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CentrifugeManager {
    
    private final LaboratoryPlugin plugin;
    private final Map<Location, Long> activeCentrifuges;
    private final Map<Location, Integer> centrifugeProgress;
    private final Random random;
    
    public CentrifugeManager(LaboratoryPlugin plugin) {
        this.plugin = plugin;
        this.activeCentrifuges = new HashMap<>();
        this.centrifugeProgress = new HashMap<>();
        this.random = new Random();
        startCentrifugeTask();
    }
    
    private void startCentrifugeTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                int processTime = plugin.getConfigManager().getCentrifugeProcessTime() * 1000;
                
                activeCentrifuges.entrySet().removeIf(entry -> {
                    Location location = entry.getKey();
                    long startTime = entry.getValue();
                    long elapsed = currentTime - startTime;
                    
                    // Update progress
                    int progress = (int) ((elapsed * 100) / processTime);
                    centrifugeProgress.put(location, Math.min(progress, 100));
                    
                    // Add particle effects during processing
                    if (elapsed < processTime) {
                        addProcessingEffects(location);
                        return false;
                    } else {
                        completeCentrifuge(location);
                        centrifugeProgress.remove(location);
                        return true;
                    }
                });
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    private void addProcessingEffects(Location location) {
        // Add smoke particles (MC 1.21 compatible)
        location.getWorld().spawnParticle(
            Particle.SMOKE, 
            location.clone().add(0.5, 1.2, 0.5), 
            3, 0.1, 0.1, 0.1, 0.01
        );
        
        // Add occasional spark effects
        if (random.nextInt(10) == 0) {
            location.getWorld().spawnParticle(
                Particle.CRIT, 
                location.clone().add(0.5, 1, 0.5), 
                5, 0.3, 0.3, 0.3, 0.1
            );
        }
        
        // Add processing sound every 5 seconds
        if (random.nextInt(100) == 0) {
            location.getWorld().playSound(location, Sound.BLOCK_FURNACE_FIRE_CRACKLE, 0.5f, 1.0f);
        }
    }
    
    public boolean isCentrifugeStructure(Location centerLocation) {
        Block center = centerLocation.getBlock();
        
        // Check if center is centrifuge block
        String blockId = NexoBlocks.idFromBlock(center);
        if (!"centrifuge_block".equals(blockId)) {
            return false;
        }
        
        // Check 3x3 structure around center
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue; // Skip center
                
                Block block = center.getRelative(x, 0, z);
                
                // Corner blocks should be iron
                if ((Math.abs(x) == 1 && Math.abs(z) == 1)) {
                    if (block.getType() != Material.IRON_BLOCK) {
                        return false;
                    }
                }
                // Side blocks should be cauldrons
                else {
                    if (block.getType() != Material.CAULDRON) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    public boolean startCentrifuge(Location location) {
        if (!isCentrifugeStructure(location)) {
            return false;
        }
        
        if (activeCentrifuges.containsKey(location)) {
            return false; // Already running
        }
        
        activeCentrifuges.put(location, System.currentTimeMillis());
        centrifugeProgress.put(location, 0);
        
        // Add startup sound
        location.getWorld().playSound(location, Sound.BLOCK_FURNACE_FIRE_CRACKLE, 1.0f, 0.8f);
        
        return true;
    }
    
    private void completeCentrifuge(Location location) {
        // Generate uranium dust based on config
        int minUranium = plugin.getConfigManager().getConfig().getInt("centrifuge.min-uranium", 1);
        int maxUranium = plugin.getConfigManager().getConfig().getInt("centrifuge.max-uranium", 5);
        int amount = random.nextInt(maxUranium - minUranium + 1) + minUranium;
        
        ItemStack uraniumDust = NexoItems.itemFromId("uranium_dust").build();
        if (uraniumDust != null) {
            uraniumDust.setAmount(amount);
            
            // Drop the items at the centrifuge location
            Location dropLocation = location.clone().add(0.5, 1, 0.5);
            location.getWorld().dropItemNaturally(dropLocation, uraniumDust);
            
            // Add completion effects (MC 1.21 compatible)
            location.getWorld().spawnParticle(
                Particle.EXPLOSION, 
                dropLocation, 
                10, 0.5, 0.5, 0.5, 0.1
            );
            
            location.getWorld().playSound(location, Sound.BLOCK_ANVIL_USE, 1.0f, 1.5f);
            
            // Notify nearby players
            location.getWorld().getNearbyPlayers(location, 10).forEach(player -> {
                player.sendMessage("§aЦентрифуга завершила работу! Получено: " + amount + " урановой пыли");
            });
        }
    }
    
    public boolean isCentrifugeActive(Location location) {
        return activeCentrifuges.containsKey(location);
    }
    
    public long getRemainingTime(Location location) {
        if (!activeCentrifuges.containsKey(location)) {
            return 0;
        }
        
        long startTime = activeCentrifuges.get(location);
        long elapsed = System.currentTimeMillis() - startTime;
        int processTime = plugin.getConfigManager().getCentrifugeProcessTime() * 1000;
        
        return Math.max(0, processTime - elapsed);
    }
    
    public int getProgress(Location location) {
        return centrifugeProgress.getOrDefault(location, 0);
    }
    
    public String getProgressBar(Location location) {
        int progress = getProgress(location);
        int barLength = 20;
        int filled = (progress * barLength) / 100;
        
        StringBuilder bar = new StringBuilder("§8[");
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append("§a█");
            } else {
                bar.append("§7█");
            }
        }
        bar.append("§8] §e").append(progress).append("%");
        
        return bar.toString();
    }
}
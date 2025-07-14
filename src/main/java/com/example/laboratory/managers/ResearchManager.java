package com.example.laboratory.managers;

import com.example.laboratory.LaboratoryPlugin;
import com.example.laboratory.models.Research;
import com.nexomc.nexo.api.NexoItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ResearchManager {
    
    private final LaboratoryPlugin plugin;
    private final Map<String, Research> researches;
    private final Map<UUID, Set<String>> playerResearches;
    private final Map<UUID, Research> activeResearches;
    private final Map<UUID, Long> researchStartTimes;
    
    public ResearchManager(LaboratoryPlugin plugin) {
        this.plugin = plugin;
        this.researches = new HashMap<>();
        this.playerResearches = new HashMap<>();
        this.activeResearches = new HashMap<>();
        this.researchStartTimes = new HashMap<>();
        initializeResearches();
    }
    
    private void initializeResearches() {
        // Tier 1: Basic researches
        addResearch("quantum_core", "Квантовое ядро", 300, // 5 minutes
            Arrays.asList(
                new ItemStack(Material.DIAMOND, 4),
                new ItemStack(Material.REDSTONE, 16),
                new ItemStack(Material.GOLD_INGOT, 8)
            ), Collections.emptyList());
        
        // Tier 2: Intermediate researches
        addResearch("uranium_capsule", "Урановая капсула", 600, // 10 minutes
            Arrays.asList(
                new ItemStack(Material.IRON_INGOT, 8),
                new ItemStack(Material.GLASS, 4),
                NexoItems.itemFromId("quantum_core")
            ), Arrays.asList("quantum_core"));
        
        addResearch("centrifuge_block", "Блок центрифуги", 900, // 15 minutes
            Arrays.asList(
                new ItemStack(Material.IRON_BLOCK, 2),
                new ItemStack(Material.REDSTONE_BLOCK, 1),
                NexoItems.itemFromId("quantum_core")
            ), Arrays.asList("quantum_core"));
        
        addResearch("tablet", "Планшет", 450, // 7.5 minutes
            Arrays.asList(
                new ItemStack(Material.GLASS_PANE, 6),
                new ItemStack(Material.REDSTONE, 8),
                NexoItems.itemFromId("quantum_core")
            ), Arrays.asList("quantum_core"));
        
        addResearch("geiger_counter", "Счётчик Гейгера", 360, // 6 minutes
            Arrays.asList(
                new ItemStack(Material.IRON_INGOT, 4),
                new ItemStack(Material.REDSTONE, 8),
                new ItemStack(Material.CLOCK, 1)
            ), Collections.emptyList());
        
        // Tier 3: Advanced researches
        addResearch("chem_protection_helmet", "Шлем хим защиты", 800, // 13.3 minutes
            Arrays.asList(
                new ItemStack(Material.LEATHER, 8),
                new ItemStack(Material.IRON_INGOT, 4),
                NexoItems.itemFromId("quantum_core")
            ), Arrays.asList("quantum_core", "uranium_capsule"));
        
        addResearch("chem_protection_chestplate", "Нагрудник хим защиты", 900,
            Arrays.asList(
                new ItemStack(Material.LEATHER, 12),
                new ItemStack(Material.IRON_INGOT, 6),
                NexoItems.itemFromId("quantum_core")
            ), Arrays.asList("chem_protection_helmet"));
        
        addResearch("chem_protection_leggings", "Поножи хим защиты", 800,
            Arrays.asList(
                new ItemStack(Material.LEATHER, 10),
                new ItemStack(Material.IRON_INGOT, 5),
                NexoItems.itemFromId("quantum_core")
            ), Arrays.asList("chem_protection_chestplate"));
        
        addResearch("chem_protection_boots", "Ботинки хим защиты", 700,
            Arrays.asList(
                new ItemStack(Material.LEATHER, 6),
                new ItemStack(Material.IRON_INGOT, 3),
                NexoItems.itemFromId("quantum_core")
            ), Arrays.asList("chem_protection_leggings"));
        
        // Tier 4: High-tech researches
        addResearch("power_armor_helmet", "Шлем силовой брони", 1500, // 25 minutes
            Arrays.asList(
                new ItemStack(Material.NETHERITE_INGOT, 2),
                new ItemStack(Material.DIAMOND, 4),
                NexoItems.itemFromId("quantum_core"),
                NexoItems.itemFromId("chem_protection_helmet")
            ), Arrays.asList("chem_protection_boots"));
        
        addResearch("power_armor_chestplate", "Нагрудник силовой брони", 1800,
            Arrays.asList(
                new ItemStack(Material.NETHERITE_INGOT, 3),
                new ItemStack(Material.DIAMOND, 6),
                NexoItems.itemFromId("quantum_core"),
                NexoItems.itemFromId("chem_protection_chestplate")
            ), Arrays.asList("power_armor_helmet"));
        
        addResearch("power_armor_leggings", "Поножи силовой брони", 1600,
            Arrays.asList(
                new ItemStack(Material.NETHERITE_INGOT, 2),
                new ItemStack(Material.DIAMOND, 5),
                NexoItems.itemFromId("quantum_core"),
                NexoItems.itemFromId("chem_protection_leggings")
            ), Arrays.asList("power_armor_chestplate"));
        
        addResearch("power_armor_boots", "Ботинки силовой брони", 1400,
            Arrays.asList(
                new ItemStack(Material.NETHERITE_INGOT, 1),
                new ItemStack(Material.DIAMOND, 3),
                NexoItems.itemFromId("quantum_core"),
                NexoItems.itemFromId("chem_protection_boots")
            ), Arrays.asList("power_armor_leggings"));
        
        // Tier 5: Ultimate researches
        addResearch("railgun", "Рельсатрон", 2100, // 35 minutes
            Arrays.asList(
                new ItemStack(Material.IRON_BLOCK, 4),
                new ItemStack(Material.REDSTONE_BLOCK, 2),
                new ItemStack(Material.NETHERITE_INGOT, 1),
                NexoItems.itemFromId("quantum_core")
            ), Arrays.asList("power_armor_boots"));
        
        addResearch("teleporter", "Телепорт", 2400, // 40 minutes
            Arrays.asList(
                new ItemStack(Material.OBSIDIAN, 8),
                new ItemStack(Material.ENDER_PEARL, 4),
                new ItemStack(Material.NETHERITE_INGOT, 2),
                NexoItems.itemFromId("quantum_core")
            ), Arrays.asList("railgun"));
    }
    
    private void addResearch(String id, String name, int timeSeconds, List<ItemStack> materials, List<String> prerequisites) {
        List<ItemStack> validatedMaterials = new ArrayList<>();
        for (ItemStack material : materials) {
            if (material != null) {
                validatedMaterials.add(material.clone());
            }
        }
        researches.put(id, new Research(id, name, timeSeconds, validatedMaterials, prerequisites));
    }
    
    public boolean canResearch(Player player, String researchId) {
        if (hasResearched(player, researchId)) {
            return false;
        }
        
        if (hasActiveResearch(player)) {
            return false; // Can't start new research while one is active
        }
        
        Research research = researches.get(researchId);
        if (research == null) {
            return false;
        }
        
        // Check prerequisites
        Set<String> playerResearchSet = playerResearches.getOrDefault(player.getUniqueId(), new HashSet<>());
        for (String prerequisite : research.getPrerequisites()) {
            if (!playerResearchSet.contains(prerequisite)) {
                return false;
            }
        }
        
        // Check if player has required materials
        return hasRequiredMaterials(player, research);
    }
    
    private boolean hasRequiredMaterials(Player player, Research research) {
        for (ItemStack required : research.getMaterials()) {
            if (!hasEnoughItems(player, required)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean hasEnoughItems(Player player, ItemStack required) {
        // Check if it's a Nexo item
        if (NexoItems.exists(required)) {
            String nexoId = NexoItems.idFromItem(required);
            int totalAmount = 0;
            
            for (ItemStack item : player.getInventory().getContents()) {
                if (item == null || !NexoItems.exists(item)) continue;
                
                if (nexoId.equals(NexoItems.idFromItem(item))) {
                    totalAmount += item.getAmount();
                }
            }
            
            return totalAmount >= required.getAmount();
        } else {
            // Vanilla item
            return player.getInventory().containsAtLeast(required, required.getAmount());
        }
    }
    
    public boolean hasResearched(Player player, String researchId) {
        return playerResearches.getOrDefault(player.getUniqueId(), new HashSet<>()).contains(researchId);
    }
    
    public boolean hasActiveResearch(Player player) {
        return activeResearches.containsKey(player.getUniqueId());
    }
    
    public boolean startResearch(Player player, String researchId) {
        if (!canResearch(player, researchId)) {
            return false;
        }
        
        Research research = researches.get(researchId);
        if (research == null) {
            return false;
        }
        
        // Consume materials
        if (!consumeMaterials(player, research)) {
            return false;
        }
        
        activeResearches.put(player.getUniqueId(), research);
        researchStartTimes.put(player.getUniqueId(), System.currentTimeMillis());
        
        return true;
    }
    
    private boolean consumeMaterials(Player player, Research research) {
        // First check if player has all materials
        if (!hasRequiredMaterials(player, research)) {
            return false;
        }
        
        // Then consume them
        for (ItemStack required : research.getMaterials()) {
            consumeItems(player, required);
        }
        
        return true;
    }
    
    private void consumeItems(Player player, ItemStack required) {
        if (NexoItems.exists(required)) {
            String nexoId = NexoItems.idFromItem(required);
            int remainingToConsume = required.getAmount();
            
            for (ItemStack item : player.getInventory().getContents()) {
                if (item == null || !NexoItems.exists(item) || remainingToConsume <= 0) continue;
                
                if (nexoId.equals(NexoItems.idFromItem(item))) {
                    int toRemove = Math.min(remainingToConsume, item.getAmount());
                    item.setAmount(item.getAmount() - toRemove);
                    remainingToConsume -= toRemove;
                }
            }
        } else {
            // Vanilla item
            player.getInventory().removeItem(required);
        }
    }
    
    public boolean isResearchComplete(Player player) {
        UUID playerId = player.getUniqueId();
        if (!activeResearches.containsKey(playerId)) {
            return false;
        }
        
        Research research = activeResearches.get(playerId);
        long startTime = researchStartTimes.get(playerId);
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        
        return elapsed >= research.getTimeSeconds();
    }
    
    public void completeResearch(Player player) {
        UUID playerId = player.getUniqueId();
        Research research = activeResearches.get(playerId);
        
        if (research != null && isResearchComplete(player)) {
            playerResearches.computeIfAbsent(playerId, k -> new HashSet<>()).add(research.getId());
            activeResearches.remove(playerId);
            researchStartTimes.remove(playerId);
            
            // Notify player
            player.sendMessage("§a✓ Исследование '" + research.getName() + "' завершено!");
            player.sendMessage("§eТеперь вы можете собрать этот предмет в сборщике.");
        }
    }
    
    public Research getActiveResearch(Player player) {
        return activeResearches.get(player.getUniqueId());
    }
    
    public long getRemainingTime(Player player) {
        UUID playerId = player.getUniqueId();
        if (!activeResearches.containsKey(playerId)) {
            return 0;
        }
        
        Research research = activeResearches.get(playerId);
        long startTime = researchStartTimes.get(playerId);
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        
        return Math.max(0, research.getTimeSeconds() - elapsed);
    }
    
    public int getResearchProgress(Player player) {
        UUID playerId = player.getUniqueId();
        if (!activeResearches.containsKey(playerId)) {
            return 0;
        }
        
        Research research = activeResearches.get(playerId);
        long startTime = researchStartTimes.get(playerId);
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        
        return (int) Math.min(100, (elapsed * 100) / research.getTimeSeconds());
    }
    
    public String getProgressBar(Player player) {
        int progress = getResearchProgress(player);
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
    
    public Map<String, Research> getAllResearches() {
        return new HashMap<>(researches);
    }
    
    public Set<String> getPlayerResearches(Player player) {
        return new HashSet<>(playerResearches.getOrDefault(player.getUniqueId(), new HashSet<>()));
    }
    
    public void clearPlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        playerResearches.remove(playerId);
        activeResearches.remove(playerId);
        researchStartTimes.remove(playerId);
    }
    
    public List<String> getAvailableResearches(Player player) {
        List<String> available = new ArrayList<>();
        
        for (Research research : researches.values()) {
            if (canResearch(player, research.getId())) {
                available.add(research.getId());
            }
        }
        
        return available;
    }
    
    public int getResearchTier(String researchId) {
        Research research = researches.get(researchId);
        if (research == null) return 0;
        
        // Calculate tier based on prerequisites depth
        return calculateTierDepth(research, new HashSet<>());
    }
    
    private int calculateTierDepth(Research research, Set<String> visited) {
        if (visited.contains(research.getId())) {
            return 0; // Prevent infinite recursion
        }
        
        visited.add(research.getId());
        
        if (research.getPrerequisites().isEmpty()) {
            return 1;
        }
        
        int maxDepth = 0;
        for (String prereq : research.getPrerequisites()) {
            Research prereqResearch = researches.get(prereq);
            if (prereqResearch != null) {
                maxDepth = Math.max(maxDepth, calculateTierDepth(prereqResearch, new HashSet<>(visited)));
            }
        }
        
        return maxDepth + 1;
    }
}
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
        // Basic researches
        addResearch("quantum_core", "Квантовое ядро", 300, // 5 minutes
            Arrays.asList(
                new ItemStack(Material.DIAMOND, 4),
                new ItemStack(Material.REDSTONE, 16),
                new ItemStack(Material.GOLD_INGOT, 8)
            ), Collections.emptyList());
        
        addResearch("uranium_capsule", "Урановая капсула", 600, // 10 minutes
            Arrays.asList(
                new ItemStack(Material.IRON_INGOT, 8),
                new ItemStack(Material.GLASS, 4),
                NexoItems.itemFromId("quantum_core").build()
            ), Arrays.asList("quantum_core"));
        
        addResearch("centrifuge_block", "Блок центрифуги", 900, // 15 minutes
            Arrays.asList(
                new ItemStack(Material.IRON_BLOCK, 2),
                new ItemStack(Material.REDSTONE_BLOCK, 1),
                NexoItems.itemFromId("quantum_core").build()
            ), Arrays.asList("quantum_core"));
        
        addResearch("tablet", "Планшет", 450, // 7.5 minutes
            Arrays.asList(
                new ItemStack(Material.GLASS_PANE, 6),
                new ItemStack(Material.REDSTONE, 8),
                NexoItems.itemFromId("quantum_core").build()
            ), Arrays.asList("quantum_core"));
        
        addResearch("chem_protection_suit", "Костюм хим защиты", 1200, // 20 minutes
            Arrays.asList(
                new ItemStack(Material.LEATHER, 16),
                new ItemStack(Material.IRON_INGOT, 12),
                NexoItems.itemFromId("quantum_core").build()
            ), Arrays.asList("quantum_core"));
        
        addResearch("power_armor", "Силовая броня", 1800, // 30 minutes
            Arrays.asList(
                new ItemStack(Material.DIAMOND, 8),
                new ItemStack(Material.NETHERITE_INGOT, 4),
                NexoItems.itemFromId("quantum_core").build()
            ), Arrays.asList("quantum_core", "chem_protection_suit"));
        
        addResearch("railgun", "Рельсатрон", 1500, // 25 minutes
            Arrays.asList(
                new ItemStack(Material.IRON_BLOCK, 4),
                new ItemStack(Material.REDSTONE_BLOCK, 2),
                NexoItems.itemFromId("quantum_core").build()
            ), Arrays.asList("quantum_core"));
        
        addResearch("teleporter", "Телепорт", 2100, // 35 minutes
            Arrays.asList(
                new ItemStack(Material.OBSIDIAN, 8),
                new ItemStack(Material.ENDER_PEARL, 4),
                NexoItems.itemFromId("quantum_core").build()
            ), Arrays.asList("quantum_core"));
    }
    
    private void addResearch(String id, String name, int timeSeconds, List<ItemStack> materials, List<String> prerequisites) {
        // Validate materials before adding research
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
        
        return true;
    }
    
    public boolean hasResearched(Player player, String researchId) {
        return playerResearches.getOrDefault(player.getUniqueId(), new HashSet<>()).contains(researchId);
    }
    
    public void startResearch(Player player, String researchId) {
        if (!canResearch(player, researchId)) {
            return;
        }
        
        Research research = researches.get(researchId);
        if (research == null) {
            return;
        }
        
        activeResearches.put(player.getUniqueId(), research);
        researchStartTimes.put(player.getUniqueId(), System.currentTimeMillis());
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
}
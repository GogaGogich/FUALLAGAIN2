package com.example.laboratory.managers;

import com.example.laboratory.LaboratoryPlugin;
import com.example.laboratory.models.StructureData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class TabletManager {
    
    private final LaboratoryPlugin plugin;
    private final Map<UUID, List<StructureData>> tabletBindings;
    private final Set<String> blockedResearches;
    
    public TabletManager(LaboratoryPlugin plugin) {
        this.plugin = plugin;
        this.tabletBindings = new HashMap<>();
        this.blockedResearches = new HashSet<>();
    }
    
    public void bindStructure(Player player, Location location, String structureType) {
        UUID playerId = player.getUniqueId();
        List<StructureData> structures = tabletBindings.computeIfAbsent(playerId, k -> new ArrayList<>());
        
        // Check max structures limit
        int maxStructures = plugin.getConfigManager().getTabletMaxStructures();
        if (structures.size() >= maxStructures) {
            player.sendMessage("§cДостигнут лимит структур на планшете! Максимум: " + maxStructures);
            return;
        }
        
        // Check if already bound
        for (StructureData structure : structures) {
            if (structure.getLocation().equals(location)) {
                player.sendMessage("§cЭта структура уже привязана к планшету!");
                return;
            }
        }
        
        // Add new structure
        String structureId = generateStructureId();
        StructureData structureData = new StructureData(structureId, structureType, location);
        structures.add(structureData);
        
        player.sendMessage("§aСтруктура привязана к планшету! ID: " + structureId);
    }
    
    public void unbindStructure(Player player, String structureId) {
        UUID playerId = player.getUniqueId();
        List<StructureData> structures = tabletBindings.get(playerId);
        
        if (structures == null) {
            player.sendMessage("§cУ вас нет привязанных структур!");
            return;
        }
        
        boolean removed = structures.removeIf(structure -> structure.getId().equals(structureId));
        
        if (removed) {
            player.sendMessage("§aСтруктура отвязана от планшета!");
        } else {
            player.sendMessage("§cСтруктура с таким ID не найдена!");
        }
    }
    
    public List<StructureData> getPlayerStructures(Player player) {
        return new ArrayList<>(tabletBindings.getOrDefault(player.getUniqueId(), new ArrayList<>()));
    }
    
    public StructureData getStructure(Player player, String structureId) {
        List<StructureData> structures = tabletBindings.get(player.getUniqueId());
        if (structures == null) return null;
        
        return structures.stream()
                .filter(structure -> structure.getId().equals(structureId))
                .findFirst()
                .orElse(null);
    }
    
    public String getStructureStatus(StructureData structure) {
        Location location = structure.getLocation();
        String type = structure.getType();
        
        switch (type) {
            case "BLAST_FURNACE":
                if (plugin.getCentrifugeManager().isCentrifugeActive(location)) {
                    long remaining = plugin.getCentrifugeManager().getRemainingTime(location);
                    return "§eРаботает (осталось: " + (remaining / 1000) + "с)";
                } else {
                    return "§7Неактивна";
                }
                
            case "CRAFTING_TABLE":
                // Check if any player has active research
                return "§aГотова к работе";
                
            case "END_PORTAL_FRAME":
                return "§aАктивна";
                
            default:
                return "§7Неизвестно";
        }
    }
    
    private String generateStructureId() {
        return "STR-" + String.format("%04d", new Random().nextInt(10000));
    }
    
    public boolean isResearchBlocked(String researchId) {
        return blockedResearches.contains(researchId);
    }
    
    public void blockResearch(String researchId) {
        blockedResearches.add(researchId);
    }
    
    public void unblockResearch(String researchId) {
        blockedResearches.remove(researchId);
    }
    
    public void clearPlayerData(Player player) {
        tabletBindings.remove(player.getUniqueId());
    }
}
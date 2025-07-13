package com.example.laboratory.managers;

import com.example.laboratory.LaboratoryPlugin;
import com.example.laboratory.models.TeleportData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class TeleportManager {
    
    private final LaboratoryPlugin plugin;
    private final Map<String, TeleportData> teleporters;
    private final Map<UUID, List<String>> recentTeleports;
    
    public TeleportManager(LaboratoryPlugin plugin) {
        this.plugin = plugin;
        this.teleporters = new HashMap<>();
        this.recentTeleports = new HashMap<>();
    }
    
    public void registerTeleporter(String id, Location location) {
        teleporters.put(id, new TeleportData(id, location));
    }
    
    public void removeTeleporter(String id) {
        teleporters.remove(id);
    }
    
    public boolean teleportPlayer(Player player, String teleporterId) {
        TeleportData teleporter = teleporters.get(teleporterId);
        if (teleporter == null) {
            return false;
        }
        
        player.teleport(teleporter.getLocation());
        addRecentTeleport(player, teleporterId);
        return true;
    }
    
    private void addRecentTeleport(Player player, String teleporterId) {
        UUID playerId = player.getUniqueId();
        List<String> recent = recentTeleports.computeIfAbsent(playerId, k -> new ArrayList<>());
        
        // Remove if already exists
        recent.remove(teleporterId);
        
        // Add to front
        recent.add(0, teleporterId);
        
        // Keep only last 5
        if (recent.size() > 5) {
            recent.remove(recent.size() - 1);
        }
    }
    
    public List<String> getRecentTeleports(Player player) {
        return new ArrayList<>(recentTeleports.getOrDefault(player.getUniqueId(), new ArrayList<>()));
    }
    
    public TeleportData getTeleporter(String id) {
        return teleporters.get(id);
    }
    
    public Collection<TeleportData> getAllTeleporters() {
        return new ArrayList<>(teleporters.values());
    }
    
    public String generateTeleporterId() {
        String id;
        do {
            id = "TP-" + String.format("%04d", new Random().nextInt(10000));
        } while (teleporters.containsKey(id));
        return id;
    }
}
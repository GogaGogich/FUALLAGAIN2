package com.example.laboratory.gui;

import com.example.laboratory.LaboratoryPlugin;
import com.example.laboratory.models.TeleportData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TeleporterGUI implements Listener {
    
    private final LaboratoryPlugin plugin;
    private final Player player;
    private final Location teleporterLocation;
    private final Inventory inventory;
    private String teleporterId;
    
    public TeleporterGUI(LaboratoryPlugin plugin, Player player, Location teleporterLocation) {
        this.plugin = plugin;
        this.player = player;
        this.teleporterLocation = teleporterLocation;
        this.inventory = Bukkit.createInventory(null, 27, "§8Телепорт");
        
        // Register or get teleporter ID
        this.teleporterId = findOrCreateTeleporterId();
        setupGUI();
    }
    
    private String findOrCreateTeleporterId() {
        // Check if teleporter already exists at this location
        for (TeleportData teleporter : plugin.getTeleportManager().getAllTeleporters()) {
            if (teleporter.getLocation().equals(teleporterLocation)) {
                return teleporter.getId();
            }
        }
        
        // Create new teleporter
        String newId = plugin.getTeleportManager().generateTeleporterId();
        plugin.getTeleportManager().registerTeleporter(newId, teleporterLocation);
        return newId;
    }
    
    private void setupGUI() {
        // Teleporter ID display
        ItemStack idItem = new ItemStack(Material.NAME_TAG);
        ItemMeta idMeta = idItem.getItemMeta();
        idMeta.setDisplayName("§6ID Телепорта");
        List<String> idLore = new ArrayList<>();
        idLore.add("§f" + teleporterId);
        idMeta.setLore(idLore);
        idItem.setItemMeta(idMeta);
        inventory.setItem(4, idItem);
        
        // Teleport button
        ItemStack teleportButton = new ItemStack(Material.ENDER_PEARL);
        ItemMeta teleportMeta = teleportButton.getItemMeta();
        teleportMeta.setDisplayName("§aTелепортироваться");
        List<String> teleportLore = new ArrayList<>();
        teleportLore.add("§7Встаньте на телепорт и используйте");
        teleportLore.add("§7команду §e/teleport <ID>");
        teleportMeta.setLore(teleportLore);
        teleportButton.setItemMeta(teleportMeta);
        inventory.setItem(11, teleportButton);
        
        // Recent teleports
        List<String> recentTeleports = plugin.getTeleportManager().getRecentTeleports(player);
        int slot = 15;
        
        for (String recentId : recentTeleports) {
            if (slot >= 24) break;
            
            TeleportData teleporter = plugin.getTeleportManager().getTeleporter(recentId);
            if (teleporter != null) {
                ItemStack recentItem = new ItemStack(Material.COMPASS);
                ItemMeta recentMeta = recentItem.getItemMeta();
                recentMeta.setDisplayName("§e" + recentId);
                List<String> recentLore = new ArrayList<>();
                recentLore.add("§7Нажмите для телепортации");
                Location loc = teleporter.getLocation();
                recentLore.add("§7Координаты: §f" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                recentMeta.setLore(recentLore);
                recentItem.setItemMeta(recentMeta);
                inventory.setItem(slot, recentItem);
                slot++;
            }
        }
        
        // Close button
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.setDisplayName("§cЗакрыть");
        closeButton.setItemMeta(closeMeta);
        inventory.setItem(26, closeButton);
    }
    
    public void open() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player clicker = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        
        if (clicked.getType() == Material.BARRIER) {
            clicker.closeInventory();
            return;
        }
        
        if (clicked.getType() == Material.COMPASS) {
            // Recent teleport clicked
            ItemMeta meta = clicked.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String targetId = meta.getDisplayName().substring(2); // Remove "§e"
                
                // Check if player is standing on teleporter
                if (isPlayerOnTeleporter(clicker)) {
                    if (plugin.getTeleportManager().teleportPlayer(clicker, targetId)) {
                        clicker.sendMessage("§aТелепортация выполнена!");
                    } else {
                        clicker.sendMessage("§cОшибка телепортации!");
                    }
                } else {
                    clicker.sendMessage("§cВы должны стоять на телепорте!");
                }
                clicker.closeInventory();
            }
        }
    }
    
    private boolean isPlayerOnTeleporter(Player player) {
        Location playerLoc = player.getLocation();
        return playerLoc.getBlock().getLocation().equals(teleporterLocation) ||
               playerLoc.subtract(0, 1, 0).getBlock().getLocation().equals(teleporterLocation);
    }
    
    public void close() {
        HandlerList.unregisterAll(this);
    }
}
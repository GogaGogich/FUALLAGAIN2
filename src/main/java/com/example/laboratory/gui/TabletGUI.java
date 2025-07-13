package com.example.laboratory.gui;

import com.example.laboratory.LaboratoryPlugin;
import com.example.laboratory.models.StructureData;
import org.bukkit.Bukkit;
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

public class TabletGUI implements Listener {
    
    private final LaboratoryPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    
    public TabletGUI(LaboratoryPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, "§8Планшет - Мониторинг");
        setupGUI();
    }
    
    private void setupGUI() {
        List<StructureData> structures = plugin.getTabletManager().getPlayerStructures(player);
        
        if (structures.isEmpty()) {
            ItemStack noStructures = new ItemStack(Material.BARRIER);
            ItemMeta meta = noStructures.getItemMeta();
            meta.setDisplayName("§cНет привязанных структур");
            List<String> lore = new ArrayList<>();
            lore.add("§7Используйте Shift + ПКМ по блоку");
            lore.add("§7чтобы привязать структуру к планшету");
            meta.setLore(lore);
            noStructures.setItemMeta(meta);
            inventory.setItem(22, noStructures);
        } else {
            int slot = 0;
            for (StructureData structure : structures) {
                if (slot >= 45) break; // Leave space for navigation
                
                ItemStack item = createStructureItem(structure);
                inventory.setItem(slot, item);
                slot++;
            }
        }
        
        // Add close button
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.setDisplayName("§cЗакрыть");
        closeButton.setItemMeta(closeMeta);
        inventory.setItem(53, closeButton);
        
        // Add refresh button
        ItemStack refreshButton = new ItemStack(Material.CLOCK);
        ItemMeta refreshMeta = refreshButton.getItemMeta();
        refreshMeta.setDisplayName("§aОбновить");
        List<String> refreshLore = new ArrayList<>();
        refreshLore.add("§7Обновить статус структур");
        refreshMeta.setLore(refreshLore);
        refreshButton.setItemMeta(refreshMeta);
        inventory.setItem(49, refreshButton);
    }
    
    private ItemStack createStructureItem(StructureData structure) {
        Material material;
        switch (structure.getType()) {
            case "centrifuge_block":
                material = Material.BLAST_FURNACE;
                break;
            case "laboratory_terminal":
                material = Material.CRAFTING_TABLE;
                break;
            case "teleporter":
                material = Material.END_PORTAL_FRAME;
                break;
            case "assembler":
                material = Material.SMITHING_TABLE;
                break;
            default:
                material = Material.STONE;
                break;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§f" + structure.getType());
        
        List<String> lore = new ArrayList<>();
        lore.add("§7ID: §e" + structure.getId());
        lore.add("§7Координаты: §f" + structure.getLocation().getBlockX() + 
                ", " + structure.getLocation().getBlockY() + 
                ", " + structure.getLocation().getBlockZ());
        lore.add("§7Статус: " + plugin.getTabletManager().getStructureStatus(structure));
        lore.add("");
        lore.add("§eЛКМ - Телепортироваться");
        lore.add("§cПКМ - Отвязать структуру");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
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
            if (clicked.getItemMeta().getDisplayName().equals("§cЗакрыть")) {
                clicker.closeInventory();
            }
            return;
        }
        
        if (clicked.getType() == Material.CLOCK) {
            // Refresh button
            setupGUI();
            clicker.sendMessage("§aСтатус структур обновлен!");
            return;
        }
        
        // Handle structure interaction
        ItemMeta meta = clicked.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            String structureId = null;
            
            for (String line : lore) {
                if (line.startsWith("§7ID: §e")) {
                    structureId = line.substring(8);
                    break;
                }
            }
            
            if (structureId != null) {
                StructureData structure = plugin.getTabletManager().getStructure(clicker, structureId);
                if (structure != null) {
                    if (event.isLeftClick()) {
                        // Teleport to structure
                        clicker.teleport(structure.getLocation());
                        clicker.sendMessage("§aТелепортация к структуре выполнена!");
                        clicker.closeInventory();
                    } else if (event.isRightClick()) {
                        // Unbind structure
                        plugin.getTabletManager().unbindStructure(clicker, structureId);
                        setupGUI(); // Refresh GUI
                    }
                }
            }
        }
    }
    
    public void close() {
        HandlerList.unregisterAll(this);
    }
}
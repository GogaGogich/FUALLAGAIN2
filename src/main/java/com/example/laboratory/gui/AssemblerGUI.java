package com.example.laboratory.gui;

import com.example.laboratory.LaboratoryPlugin;
import com.example.laboratory.models.Research;
import com.nexomc.nexo.api.NexoItems;
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
import java.util.Set;

public class AssemblerGUI implements Listener {
    
    private final LaboratoryPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    
    public AssemblerGUI(LaboratoryPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, "§8Сборщик");
        setupGUI();
    }
    
    private void setupGUI() {
        Set<String> researchedItems = plugin.getResearchManager().getPlayerResearches(player);
        int slot = 0;
        
        for (String researchId : researchedItems) {
            if (slot >= 45) break;
            
            Research research = plugin.getResearchManager().getAllResearches().get(researchId);
            if (research != null) {
                ItemStack item = createAssemblyItem(research);
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
    }
    
    private ItemStack createAssemblyItem(Research research) {
        ItemStack nexoItem = NexoItems.itemFromId(research.getId()).build();
        if (nexoItem == null) {
            nexoItem = new ItemStack(Material.CRAFTING_TABLE);
        }
        
        ItemMeta meta = nexoItem.getItemMeta();
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(nexoItem.getType());
        }
        
        meta.setDisplayName("§f" + research.getName());
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Материалы для сборки:");
        
        for (ItemStack material : research.getMaterials()) {
            String materialName = material.getType().name();
            String nexoId = NexoItems.idFromItem(material);
            if (nexoId != null) {
                materialName = nexoId;
            }
            lore.add("§8- §f" + material.getAmount() + "x " + materialName);
        }
        
        lore.add("");
        lore.add("§eНажмите для сборки");
        
        meta.setLore(lore);
        nexoItem.setItemMeta(meta);
        return nexoItem;
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
        
        // Handle assembly
        ItemMeta meta = clicked.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            String itemName = meta.getDisplayName();
            
            // Find corresponding research
            for (Research research : plugin.getResearchManager().getAllResearches().values()) {
                if (("§f" + research.getName()).equals(itemName)) {
                    if (hasRequiredMaterials(clicker, research)) {
                        consumeMaterials(clicker, research);
                        giveAssembledItem(clicker, research);
                        clicker.sendMessage("§a" + research.getName() + " успешно собран!");
                        clicker.closeInventory();
                    } else {
                        clicker.sendMessage("§cНедостаточно материалов для сборки!");
                    }
                    return;
                }
            }
        }
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
        String nexoId = NexoItems.idFromItem(required);
        int totalAmount = 0;
        
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            
            if (nexoId != null) {
                String itemNexoId = NexoItems.idFromItem(item);
                if (nexoId.equals(itemNexoId)) {
                    totalAmount += item.getAmount();
                }
            } else {
                if (item.getType() == required.getType()) {
                    totalAmount += item.getAmount();
                }
            }
        }
        
        return totalAmount >= required.getAmount();
    }
    
    private void consumeMaterials(Player player, Research research) {
        for (ItemStack required : research.getMaterials()) {
            consumeItems(player, required);
        }
    }
    
    private void consumeItems(Player player, ItemStack required) {
        String nexoId = NexoItems.idFromItem(required);
        int remainingToConsume = required.getAmount();
        
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || remainingToConsume <= 0) continue;
            
            boolean matches = false;
            if (nexoId != null) {
                String itemNexoId = NexoItems.idFromItem(item);
                matches = nexoId.equals(itemNexoId);
            } else {
                matches = item.getType() == required.getType();
            }
            
            if (matches) {
                int toRemove = Math.min(remainingToConsume, item.getAmount());
                item.setAmount(item.getAmount() - toRemove);
                remainingToConsume -= toRemove;
            }
        }
    }
    
    private void giveAssembledItem(Player player, Research research) {
        ItemStack assembledItem = NexoItems.itemFromId(research.getId());
        if (assembledItem != null) {
            player.getInventory().addItem(assembledItem);
        }
    }
    
    public void close() {
        HandlerList.unregisterAll(this);
    }
}
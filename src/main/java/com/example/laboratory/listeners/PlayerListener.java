package com.example.laboratory.listeners;

import com.example.laboratory.LaboratoryPlugin;
import com.nexomc.nexo.api.NexoItems;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PlayerListener implements Listener {
    
    private final LaboratoryPlugin plugin;
    
    public PlayerListener(LaboratoryPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null) return;
        
        String itemId = null;
        try {
        String itemId = NexoItems.idFromItem(item);
        } catch (Exception e) {
            return; // Nexo API not available
        }
        
        if (itemId == null) return;
        
        switch (itemId) {
            case "geiger_counter":
                handleGeigerCounter(player);
                event.setCancelled(true);
                break;
                
            case "uranium_capsule":
                handleUraniumCapsule(player, item, event);
                break;
                
            case "tablet":
                handleTablet(player, item, event);
                break;
                
            case "railgun":
                handleRailgun(player, item, event);
                break;
        }
    }
    
    private void handleGeigerCounter(Player player) {
        int radiation = plugin.getRadiationManager().getPlayerRadiation(player);
        player.sendMessage("§eУровень радиации: §c" + radiation + " §eрад/с");
        
        // Add sound effect
        player.getWorld().playSound(player.getLocation(), 
            org.bukkit.Sound.BLOCK_NOTE_BLOCK_CLICK, 1.0f, 1.0f);
    }
    
    private void handleUraniumCapsule(Player player, ItemStack capsule, PlayerInteractEvent event) {
        if (player.isSneaking()) {
            // Extract uranium from capsule
            extractUraniumFromCapsule(player, capsule);
        } else {
            // Store uranium in capsule
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (isUraniumDust(offHand)) {
                storeUraniumInCapsule(player, capsule, offHand);
            }
        }
        event.setCancelled(true);
    }
    
    private void handleTablet(Player player, ItemStack tablet, PlayerInteractEvent event) {
        if (player.isSneaking() && event.getClickedBlock() != null) {
            // Bind tablet to structure
            bindTabletToStructure(player, tablet, event.getClickedBlock().getLocation());
        } else {
            // Show tablet interface
            showTabletInterface(player, tablet);
        }
        event.setCancelled(true);
    }
    
    private void handleRailgun(Player player, ItemStack railgun, PlayerInteractEvent event) {
        // Handle railgun shooting modes
        player.sendMessage("§aРельсатрон активирован!");
        event.setCancelled(true);
    }
    
    private boolean isUraniumDust(ItemStack item) {
        if (item == null) return false;
        try {
            String itemId = NexoItems.idFromItem(item);
            return "uranium_dust".equals(itemId);
        } catch (Exception e) {
            return false;
        }
    }
    
    private void extractUraniumFromCapsule(Player player, ItemStack capsule) {
        ItemMeta meta = capsule.getItemMeta();
        if (meta == null) return;
        
        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();
        
        // Get stored uranium amount from lore
        int storedAmount = getStoredUranium(lore);
        if (storedAmount <= 0) {
            player.sendMessage("§cКапсула пуста!");
            return;
        }
        
        // Extract uranium dust
        ItemStack uraniumDust = NexoItems.itemFromId("uranium_dust").build();
        int extractAmount = Math.min(storedAmount, 64);
        uraniumDust.setAmount(extractAmount);
        
        // Give to player
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(uraniumDust);
            updateCapsuleLore(capsule, storedAmount - extractAmount);
            player.sendMessage("§aИзвлечено " + extractAmount + " урановой пыли!");
        } else {
            player.sendMessage("§cИнвентарь полон!");
        }
    }
    
    private void storeUraniumInCapsule(Player player, ItemStack capsule, ItemStack uraniumDust) {
        ItemMeta meta = capsule.getItemMeta();
        if (meta == null) return;
        
        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();
        
        int storedAmount = getStoredUranium(lore);
        int dustAmount = uraniumDust.getAmount();
        
        if (storedAmount + dustAmount > 500) {
            player.sendMessage("§cКапсула не может вместить столько урана! (Максимум: 500)");
            return;
        }
        
        // Store uranium
        updateCapsuleLore(capsule, storedAmount + dustAmount);
        uraniumDust.setAmount(0);
        player.sendMessage("§aУран сохранен в капсуле! Всего: " + (storedAmount + dustAmount));
    }
    
    private int getStoredUranium(List<String> lore) {
        for (String line : lore) {
            if (line.contains("Уран:")) {
                try {
                    return Integer.parseInt(line.replaceAll("[^0-9]", ""));
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }
    
    private void updateCapsuleLore(ItemStack capsule, int amount) {
        ItemMeta meta = capsule.getItemMeta();
        if (meta == null) return;
        
        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();
        
        // Remove old uranium line
        lore.removeIf(line -> line.contains("Уран:"));
        
        // Add new uranium line
        lore.add("§7Уран: §e" + amount + "§7/§e500");
        
        meta.setLore(lore);
        capsule.setItemMeta(meta);
    }
    
    private void bindTabletToStructure(Player player, ItemStack tablet, org.bukkit.Location location) {
        // Implementation for binding tablet to structures
        player.sendMessage("§aПланшет привязан к структуре!");
    }
    
    private void showTabletInterface(Player player, ItemStack tablet) {
        // Implementation for showing tablet interface
        player.sendMessage("§aИнтерфейс планшета открыт!");
    }
}
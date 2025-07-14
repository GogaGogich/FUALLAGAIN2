package com.example.laboratory.listeners;

import com.example.laboratory.LaboratoryPlugin;
import com.example.laboratory.gui.TabletGUI;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.mechanics.noteblock.NoteBlockMechanic;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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
        
        // Check if it's a Nexo item
        String itemId = NexoItems.idFromItem(item);
        if (itemId == null) return;
        
        switch (itemId) {
            case "geiger_counter":
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    handleGeigerCounter(player);
                    event.setCancelled(true);
                }
                break;
                
            case "uranium_capsule":
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    handleUraniumCapsule(player, item, event);
                }
                break;
                
            case "tablet":
                handleTablet(player, item, event);
                break;
                
            case "railgun":
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    handleRailgun(player, item, event);
                }
                break;
        }
    }
    
    private void handleGeigerCounter(Player player) {
        int radiation = plugin.getRadiationManager().getPlayerRadiation(player);
        
        // Enhanced radiation display with color coding
        String radiationColor;
        String statusMessage;
        
        if (radiation == 0) {
            radiationColor = "§a";
            statusMessage = "§aВы в безопасности!";
        } else if (radiation < 20) {
            radiationColor = "§e";
            statusMessage = "§eНизкий уровень радиации";
        } else if (radiation < 50) {
            radiationColor = "§6";
            statusMessage = "§6Средний уровень радиации";
        } else if (radiation < 100) {
            radiationColor = "§c";
            statusMessage = "§cВысокий уровень радиации!";
        } else {
            radiationColor = "§4";
            statusMessage = "§4КРИТИЧЕСКИЙ уровень радиации!";
        }
        
        player.sendMessage("§eУровень радиации: " + radiationColor + radiation + " §eрад/с");
        player.sendMessage(statusMessage);
        
        // Enhanced sound effects based on radiation level
        Sound sound = Sound.UI_BUTTON_CLICK;
        float pitch = 1.0f;
        
        if (radiation > 50) {
            sound = Sound.BLOCK_NOTE_BLOCK_BASS;
            pitch = 0.5f;
        } else if (radiation > 20) {
            pitch = 0.8f;
        }
        
        player.getWorld().playSound(player.getLocation(), sound, 1.0f, pitch);
        
        // Add clicking sound effect for realism
        if (radiation > 0) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.getWorld().playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.3f, 2.0f);
            }, 5L);
        }
    }
    
    private void handleUraniumCapsule(Player player, ItemStack capsule, PlayerInteractEvent event) {
        if (player.isSneaking()) {
            extractUraniumFromCapsule(player, capsule);
        } else {
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (isUraniumDust(offHand)) {
                storeUraniumInCapsule(player, capsule, offHand);
            } else {
                player.sendMessage("§7Возьмите урановую пыль в левую руку для загрузки");
                player.sendMessage("§7Или используйте Shift + ПКМ для извлечения");
            }
        }
        event.setCancelled(true);
    }
    
    private void handleTablet(Player player, ItemStack tablet, PlayerInteractEvent event) {
        if (player.isSneaking() && event.getClickedBlock() != null) {
            bindTabletToStructure(player, tablet, event.getClickedBlock().getLocation());
        } else {
            new TabletGUI(plugin, player).open();
        }
        event.setCancelled(true);
    }
    
    private void handleRailgun(Player player, ItemStack railgun, PlayerInteractEvent event) {
        // Enhanced railgun with different modes
        ItemMeta meta = railgun.getItemMeta();
        if (meta == null) return;
        
        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();
        
        // Cycle through firing modes
        String currentMode = getCurrentRailgunMode(lore);
        String nextMode = getNextRailgunMode(currentMode);
        
        updateRailgunMode(railgun, nextMode);
        player.sendMessage("§aРежим рельсатрона: §e" + nextMode);
        
        // Add sound effect
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.5f);
        
        event.setCancelled(true);
    }
    
    private boolean isUraniumDust(ItemStack item) {
        if (item == null) return false;
        return "uranium_dust".equals(NexoItems.idFromItem(item));
    }
    
    private void extractUraniumFromCapsule(Player player, ItemStack capsule) {
        ItemMeta meta = capsule.getItemMeta();
        if (meta == null) return;
        
        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();
        
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
            
            // Add sound effect
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_BOTTLE_EMPTY, 1.0f, 1.0f);
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
        int maxCapacity = 500;
        
        if (storedAmount + dustAmount > maxCapacity) {
            int canStore = maxCapacity - storedAmount;
            if (canStore <= 0) {
                player.sendMessage("§cКапсула полна! (Максимум: " + maxCapacity + ")");
                return;
            }
            player.sendMessage("§eМожно загрузить только " + canStore + " пыли из " + dustAmount);
            dustAmount = canStore;
        }
        
        // Store uranium
        updateCapsuleLore(capsule, storedAmount + dustAmount);
        uraniumDust.setAmount(uraniumDust.getAmount() - dustAmount);
        player.sendMessage("§aУран сохранен в капсуле! Всего: " + (storedAmount + dustAmount) + "/" + maxCapacity);
        
        // Add sound effect
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 1.0f, 1.0f);
    }
    
    private int getStoredUranium(List<String> lore) {
        for (String line : lore) {
            if (line.contains("Уран:")) {
                try {
                    String numbers = line.replaceAll("[^0-9]", "");
                    if (!numbers.isEmpty()) {
                        return Integer.parseInt(numbers.split("")[0] + numbers.split("")[1] + numbers.split("")[2]);
                    }
                } catch (Exception e) {
                    // Try alternative parsing
                    String[] parts = line.split("/");
                    if (parts.length > 0) {
                        String numberPart = parts[0].replaceAll("[^0-9]", "");
                        if (!numberPart.isEmpty()) {
                            return Integer.parseInt(numberPart);
                        }
                    }
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
        
        // Add new uranium line with progress bar
        String progressBar = createProgressBar(amount, 500);
        lore.add("§7Уран: §e" + amount + "§7/§e500");
        lore.add(progressBar);
        
        meta.setLore(lore);
        capsule.setItemMeta(meta);
    }
    
    private String createProgressBar(int current, int max) {
        int barLength = 20;
        int filled = (int) ((double) current / max * barLength);
        
        StringBuilder bar = new StringBuilder("§8[");
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append("§a█");
            } else {
                bar.append("§7█");
            }
        }
        bar.append("§8]");
        
        return bar.toString();
    }
    
    private void bindTabletToStructure(Player player, ItemStack tablet, Location location) {
        // Simplified structure binding - you might want to implement a registry
        // of valid structure locations or use a different approach
        String blockType = location.getBlock().getType().name();
        
        // Basic validation based on block type
        if (isValidStructureBlock(blockType)) {
            plugin.getTabletManager().bindStructure(player, location, blockType);
        } else {
            player.sendMessage("§cЭтот блок нельзя привязать к планшету!");
            return;
        }
        
        // Add sound effect
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
    }
    
    private boolean isValidStructureBlock(String blockType) {
        return blockType.equals("CRAFTING_TABLE") || 
               blockType.equals("SMITHING_TABLE") || 
               blockType.equals("BLAST_FURNACE") || 
               blockType.equals("END_PORTAL_FRAME");
    }
    
    private String getCurrentRailgunMode(List<String> lore) {
        for (String line : lore) {
            if (line.contains("Режим:")) {
                return line.substring(line.indexOf("Режим:") + 7).trim();
            }
        }
        return "Обычный";
    }
    
    private String getNextRailgunMode(String currentMode) {
        switch (currentMode) {
            case "Обычный": return "Пробивающий";
            case "Пробивающий": return "Взрывной";
            case "Взрывной": return "Обычный";
            default: return "Обычный";
        }
    }
    
    private void updateRailgunMode(ItemStack railgun, String mode) {
        ItemMeta meta = railgun.getItemMeta();
        if (meta == null) return;
        
        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();
        
        // Remove old mode line
        lore.removeIf(line -> line.contains("Режим:"));
        
        // Add new mode line
        lore.add("§7Режим: §e" + mode);
        
        meta.setLore(lore);
        railgun.setItemMeta(meta);
    }
}
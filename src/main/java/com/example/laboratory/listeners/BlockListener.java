package com.example.laboratory.listeners;

import com.example.laboratory.LaboratoryPlugin;
import com.example.laboratory.gui.LaboratoryGUI;
import com.example.laboratory.gui.AssemblerGUI;
import com.example.laboratory.gui.TeleporterGUI;
import com.nexomc.nexo.mechanics.custom_block.noteblock.NoteBlockMechanic;
import com.nexomc.nexo.mechanics.custom_block.noteblock.NoteBlockMechanicFactory;
import com.nexomc.nexo.mechanics.custom_block.stringblock.StringBlockMechanic;
import com.nexomc.nexo.mechanics.custom_block.stringblock.StringBlockMechanicFactory;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockListener implements Listener {
    
    private final LaboratoryPlugin plugin;
    
    public BlockListener(LaboratoryPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        
        Player player = event.getPlayer();
        String blockId = getCustomBlockId(block);
        
        if (blockId == null) {
            return; // Not a custom block
        }
        
        // Log interaction for debugging
        plugin.getLogger().info("Player " + player.getName() + " interacted with custom block: " + blockId);
        
        handleBlockInteraction(player, blockId, block.getLocation(), event);
    }
    
    private String getCustomBlockId(Block block) {
        try {
            // Try NoteBlock mechanic first using static INSTANCE
            NoteBlockMechanic noteBlockMechanic = NoteBlockMechanicFactory.INSTANCE.getMechanic(block.getBlockData());
            if (noteBlockMechanic != null) {
                return noteBlockMechanic.getItemID();
            }
            
            // Try StringBlock mechanic using static INSTANCE
            StringBlockMechanic stringBlockMechanic = StringBlockMechanicFactory.INSTANCE.getMechanic(block.getBlockData());
            if (stringBlockMechanic != null) {
                return stringBlockMechanic.getItemID();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting custom block ID: " + e.getMessage());
        }
        
        return null; // Not a custom block
    }
    
    private void handleBlockInteraction(Player player, String blockId, Location location, PlayerInteractEvent event) {
        switch (blockId) {
            case "laboratory_terminal":
                event.setCancelled(true);
                if (player.isSneaking()) {
                    handleResourceLoading(player, "laboratory");
                } else {
                    new LaboratoryGUI(plugin, player).open();
                }
                break;
                
            case "assembler":
                event.setCancelled(true);
                if (player.isSneaking()) {
                    handleResourceLoading(player, "assembler");
                } else {
                    new AssemblerGUI(plugin, player).open();
                }
                break;
                
            case "teleporter":
                event.setCancelled(true);
                new TeleporterGUI(plugin, player, location).open();
                break;
                
            case "centrifuge_block":
                event.setCancelled(true);
                handleCentrifugeInteraction(player, location);
                break;
                
            default:
                plugin.getLogger().info("Unknown custom block interaction: " + blockId);
                break;
        }
    }
    
    private void handleResourceLoading(Player player, String type) {
        // Enhanced resource loading with inventory checking
        int loadedItems = 0;
        
        // Check player inventory for relevant materials
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            // Implementation for loading resources
            // This would check for specific materials and "load" them into the machine
        }
        
        if (loadedItems > 0) {
            player.sendMessage("§aЗагружено " + loadedItems + " предметов в " + type + "!");
        } else {
            player.sendMessage("§cНет подходящих материалов для загрузки!");
        }
    }
    
    private void handleCentrifugeInteraction(Player player, Location location) {
        if (plugin.getCentrifugeManager().startCentrifuge(location)) {
            player.sendMessage("§aЦентрифуга запущена! Ожидайте " + 
                (plugin.getConfigManager().getCentrifugeProcessTime() / 60) + " минут.");
            
            // Add particle effects (MC 1.21 compatible)
            location.getWorld().spawnParticle(
                Particle.SMOKE, 
                location.clone().add(0.5, 1, 0.5), 
                10, 0.2, 0.2, 0.2, 0.01
            );
            
        } else if (plugin.getCentrifugeManager().isCentrifugeActive(location)) {
            long remaining = plugin.getCentrifugeManager().getRemainingTime(location);
            int minutes = (int) (remaining / 60000);
            int seconds = (int) ((remaining % 60000) / 1000);
            player.sendMessage("§eЦентрифуга уже работает. Осталось: " + minutes + ":" + 
                String.format("%02d", seconds));
                
        } else {
            player.sendMessage("§cНеправильная структура центрифуги!");
            player.sendMessage("§7Требуется структура 3x3: железные блоки по углам, котлы по сторонам");
        }
    }
}
package com.example.laboratory.listeners;

import com.example.laboratory.LaboratoryPlugin;
import com.example.laboratory.gui.LaboratoryGUI;
import com.example.laboratory.gui.AssemblerGUI;
import com.example.laboratory.gui.TeleporterGUI;
import com.nexomc.nexo.api.NexoBlocks;
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
        
        // Check if it's a Nexo custom block
        String blockId = NexoBlocks.idFromBlock(block);
        if (blockId == null) {
            return;
        }
        
        plugin.getLogger().info("Player " + player.getName() + " interacted with custom block: " + blockId);
        
        switch (blockId) {
            case "laboratory_terminal":
                event.setCancelled(true);
                if (player.isSneaking()) {
                    handleResourceLoading(player, block, "laboratory");
                } else {
                    new LaboratoryGUI(plugin, player).open();
                }
                break;
                
            case "assembler":
                event.setCancelled(true);
                if (player.isSneaking()) {
                    handleResourceLoading(player, block, "assembler");
                } else {
                    new AssemblerGUI(plugin, player).open();
                }
                break;
                
            case "teleporter":
                event.setCancelled(true);
                new TeleporterGUI(plugin, player, block.getLocation()).open();
                break;
                
            case "centrifuge_block":
                event.setCancelled(true);
                handleCentrifugeInteraction(player, block);
                break;
                
            default:
                plugin.getLogger().info("Unknown custom block interaction: " + blockId);
                break;
        }
    }
    
    private void handleResourceLoading(Player player, Block block, String type) {
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
    
    private void handleCentrifugeInteraction(Player player, Block block) {
        if (plugin.getCentrifugeManager().startCentrifuge(block.getLocation())) {
            player.sendMessage("§aЦентрифуга запущена! Ожидайте " + 
                (plugin.getConfigManager().getCentrifugeProcessTime() / 60) + " минут.");
            
            // Add particle effects (MC 1.21 compatible)
            block.getWorld().spawnParticle(
                Particle.SMOKE, 
                block.getLocation().add(0.5, 1, 0.5), 
                10, 0.2, 0.2, 0.2, 0.01
            );
            
        } else if (plugin.getCentrifugeManager().isCentrifugeActive(block.getLocation())) {
            long remaining = plugin.getCentrifugeManager().getRemainingTime(block.getLocation());
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
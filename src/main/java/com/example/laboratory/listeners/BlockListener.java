package com.example.laboratory.listeners;

import com.example.laboratory.LaboratoryPlugin;
import com.example.laboratory.gui.LaboratoryGUI;
import com.example.laboratory.gui.AssemblerGUI;
import com.example.laboratory.gui.TeleporterGUI;
import com.nexomc.nexo.api.events.custom_block.NexoStringBlockInteractEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BlockListener implements Listener {
    
    private final LaboratoryPlugin plugin;
    
    public BlockListener(LaboratoryPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onNexoBlockInteract(NexoStringBlockInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String blockId = event.getCustomBlock().getId();
        
        plugin.getLogger().info("Player " + player.getName() + " interacted with custom block: " + blockId);
        
        switch (blockId) {
            case "laboratory_terminal":
                event.setCancelled(true);
                if (player.isSneaking()) {
                    // Handle resource loading
                    handleResourceLoading(player, block);
                } else {
                    // Open laboratory GUI
                    new LaboratoryGUI(plugin, player).open();
                }
                break;
                
            case "assembler":
                event.setCancelled(true);
                if (player.isSneaking()) {
                    // Handle resource loading
                    handleAssemblerLoading(player, block);
                } else {
                    // Open assembler GUI
                    new AssemblerGUI(plugin, player).open();
                }
                break;
                
            case "teleporter":
                event.setCancelled(true);
                new TeleporterGUI(plugin, player, block.getLocation()).open();
                break;
                
            case "centrifuge_block":
                event.setCancelled(true);
                if (plugin.getCentrifugeManager().startCentrifuge(block.getLocation())) {
                    player.sendMessage("§aЦентрифуга запущена! Ожидайте 5 минут.");
                } else if (plugin.getCentrifugeManager().isCentrifugeActive(block.getLocation())) {
                    long remaining = plugin.getCentrifugeManager().getRemainingTime(block.getLocation());
                    player.sendMessage("§eЦентрифуга уже работает. Осталось: " + (remaining / 1000) + " секунд.");
                } else {
                    player.sendMessage("§cНеправильная структура центрифуги!");
                }
                break;
                
            default:
                // Log unknown block interactions for debugging
                plugin.getLogger().info("Unknown custom block interaction: " + blockId);
                break;
        }
    }
    
    private void handleResourceLoading(Player player, Block block) {
        // Implementation for loading resources into laboratory
        player.sendMessage("§aРесурсы загружены в лабораторию!");
    }
    
    private void handleAssemblerLoading(Player player, Block block) {
        // Implementation for loading resources into assembler
        player.sendMessage("§aРесурсы загружены в сборщик!");
    }
}
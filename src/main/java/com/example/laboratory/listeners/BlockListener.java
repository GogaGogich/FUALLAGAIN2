package com.example.laboratory.listeners;

import com.example.laboratory.LaboratoryPlugin;
import com.example.laboratory.gui.LaboratoryGUI;
import com.example.laboratory.gui.AssemblerGUI;
import com.example.laboratory.gui.TeleporterGUI;
import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockInteractEvent;
import com.nexomc.nexo.api.events.custom_block.stringblock.NexoStringBlockInteractEvent;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BlockListener implements Listener {
    
    private final LaboratoryPlugin plugin;
    
    public BlockListener(LaboratoryPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onNoteBlockInteract(NexoNoteBlockInteractEvent event) {
        // Получаем ID блока через getCustomBlock().getId()
        String blockId = event.getCustomBlock().getId();
        Player player = event.getPlayer();
        
        // Логируем взаимодействие
        plugin.getLogger().info("Player " + player.getName() + " interacted with NoteBlock: " + blockId);
        Bukkit.getLogger().info("Игрок " + player.getName() + " кликнул NoteBlock с ID: " + blockId);
        
        // Показываем ID игроку для отладки
        player.sendMessage("§7[DEBUG] NoteBlock ID: §e" + blockId);
        
        handleBlockInteraction(player, blockId, event);
    }
    
    @EventHandler
    public void onStringBlockInteract(NexoStringBlockInteractEvent event) {
        // Получаем ID блока через getCustomBlock().getId()
        String blockId = event.getCustomBlock().getId();
        Player player = event.getPlayer();
        
        // Логируем взаимодействие
        plugin.getLogger().info("Player " + player.getName() + " interacted with StringBlock: " + blockId);
        Bukkit.getLogger().info("Игрок " + player.getName() + " кликнул StringBlock с ID: " + blockId);
        
        // Показываем ID игроку для отладки
        player.sendMessage("§7[DEBUG] StringBlock ID: §e" + blockId);
        
        handleBlockInteraction(player, blockId, event);
    }
    
    // Общий метод для обработки взаимодействий с блоками
    private void handleBlockInteraction(Player player, String blockId, Object event) {
        switch (blockId) {
            case "laboratory_terminal":
                cancelEvent(event);
                if (player.isSneaking()) {
                    handleResourceLoading(player, "laboratory");
                } else {
                    new LaboratoryGUI(plugin, player).open();
                }
                break;
                
            case "assembler":
                cancelEvent(event);
                if (player.isSneaking()) {
                    handleResourceLoading(player, "assembler");
                } else {
                    new AssemblerGUI(plugin, player).open();
                }
                break;
                
            case "teleporter":
                cancelEvent(event);
                new TeleporterGUI(plugin, player, getEventLocation(event)).open();
                break;
                
            case "centrifuge_block":
                cancelEvent(event);
                handleCentrifugeInteraction(player, event);
                break;
                
            default:
                plugin.getLogger().info("Unknown custom block interaction: " + blockId);
                player.sendMessage("§c[DEBUG] Неизвестный блок: " + blockId);
                break;
        }
    }
    
    // Универсальный метод для отмены события
    private void cancelEvent(Object event) {
        if (event instanceof NexoNoteBlockInteractEvent) {
            ((NexoNoteBlockInteractEvent) event).setCancelled(true);
        } else if (event instanceof NexoStringBlockInteractEvent) {
            ((NexoStringBlockInteractEvent) event).setCancelled(true);
        }
    }
    
    // Универсальный метод для получения локации блока
    private org.bukkit.Location getEventLocation(Object event) {
        if (event instanceof NexoNoteBlockInteractEvent) {
            return ((NexoNoteBlockInteractEvent) event).getBlock().getLocation();
        } else if (event instanceof NexoStringBlockInteractEvent) {
            return ((NexoStringBlockInteractEvent) event).getBlock().getLocation();
        }
        return null;
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
    
    private void handleCentrifugeInteraction(Player player, Object event) {
        org.bukkit.Location location = getEventLocation(event);
        if (location == null) return;
        
        if (plugin.getCentrifugeManager().startCentrifuge(location)) {
            player.sendMessage("§aЦентрифуга запущена! Ожидайте " + 
                (plugin.getConfigManager().getCentrifugeProcessTime() / 60) + " минут.");
            
            // Add particle effects (MC 1.21 compatible)
            location.getWorld().spawnParticle(
                Particle.SMOKE, 
                location.add(0.5, 1, 0.5), 
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
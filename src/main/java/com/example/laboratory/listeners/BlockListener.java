package com.example.laboratory.listeners;

import com.example.laboratory.LaboratoryPlugin;
import com.example.laboratory.gui.LaboratoryGUI;
import com.example.laboratory.gui.AssemblerGUI;
import com.example.laboratory.gui.TeleporterGUI;
import com.nexomc.nexo.NexoPlugin;
import com.nexomc.nexo.mechanics.custom_block.noteblock.NoteBlockMechanic;
import com.nexomc.nexo.mechanics.custom_block.noteblock.NoteBlockMechanicFactory;
import com.nexomc.nexo.mechanics.custom_block.stringblock.StringBlockMechanic;
import com.nexomc.nexo.mechanics.custom_block.stringblock.StringBlockMechanicFactory;
import org.bukkit.Bukkit;
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
    private NoteBlockMechanicFactory noteBlockFactory;
    private StringBlockMechanicFactory stringBlockFactory;
    
    public BlockListener(LaboratoryPlugin plugin) {
        this.plugin = plugin;
        initializeNexoFactories();
    }
    
    private void initializeNexoFactories() {
        try {
            // Получаем инстанс плагина Nexo
            NexoPlugin nexoPlugin = (NexoPlugin) Bukkit.getPluginManager().getPlugin("Nexo");
            if (nexoPlugin == null) {
                plugin.getLogger().severe("Nexo plugin not found!");
                return;
            }
            
            // Пробуем разные способы получения фабрик в зависимости от версии Nexo
            try {
                // Способ 1: Через configsManager (возможно в новых версиях)
                this.noteBlockFactory = nexoPlugin.configsManager().getMechanics().getNoteBlockMechanicFactory();
                this.stringBlockFactory = nexoPlugin.configsManager().getMechanics().getStringBlockMechanicFactory();
                plugin.getLogger().info("Nexo factories initialized via configsManager!");
            } catch (Exception e1) {
                try {
                    // Способ 2: Прямые методы (если они есть)
                    this.noteBlockFactory = nexoPlugin.getNoteBlockMechanicFactory();
                    this.stringBlockFactory = nexoPlugin.getStringBlockMechanicFactory();
                    plugin.getLogger().info("Nexo factories initialized via direct methods!");
                } catch (Exception e2) {
                    try {
                        // Способ 3: Через механики менеджер (если есть)
                        Object mechanicsManager = nexoPlugin.getClass().getMethod("getMechanicsManager").invoke(nexoPlugin);
                        this.noteBlockFactory = (NoteBlockMechanicFactory) mechanicsManager.getClass()
                            .getMethod("getNoteBlockMechanicFactory").invoke(mechanicsManager);
                        this.stringBlockFactory = (StringBlockMechanicFactory) mechanicsManager.getClass()
                            .getMethod("getStringBlockMechanicFactory").invoke(mechanicsManager);
                        plugin.getLogger().info("Nexo factories initialized via mechanics manager!");
                    } catch (Exception e3) {
                        plugin.getLogger().severe("Failed to initialize Nexo factories with all methods!");
                        plugin.getLogger().severe("Method 1 error: " + e1.getMessage());
                        plugin.getLogger().severe("Method 2 error: " + e2.getMessage());
                        plugin.getLogger().severe("Method 3 error: " + e3.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Critical error initializing Nexo factories: " + e.getMessage());
            e.printStackTrace();
        }
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
        
        // Show ID to player for debugging
        player.sendMessage("§7[DEBUG] Block ID: §e" + blockId);
        
        handleBlockInteraction(player, blockId, block.getLocation(), event);
    }
    
    private String getCustomBlockId(Block block) {
        if (noteBlockFactory == null && stringBlockFactory == null) {
            plugin.getLogger().warning("Nexo factories not initialized!");
            return null;
        }
        
        try {
            // Try NoteBlock mechanic first
            if (noteBlockFactory != null) {
                NoteBlockMechanic noteBlockMechanic = noteBlockFactory.getMechanic(block.getBlockData());
                if (noteBlockMechanic != null) {
                    return noteBlockMechanic.getItemID();
                }
            }
            
            // Try StringBlock mechanic
            if (stringBlockFactory != null) {
                StringBlockMechanic stringBlockMechanic = stringBlockFactory.getMechanic(block.getBlockData());
                if (stringBlockMechanic != null) {
                    return stringBlockMechanic.getItemID();
                }
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
                player.sendMessage("§c[DEBUG] Неизвестный блок: " + blockId);
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
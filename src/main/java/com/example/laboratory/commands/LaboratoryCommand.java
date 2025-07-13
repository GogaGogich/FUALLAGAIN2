package com.example.laboratory.commands;

import com.example.laboratory.LaboratoryPlugin;
import com.nexomc.nexo.api.NexoItems;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LaboratoryCommand implements CommandExecutor, TabCompleter {
    
    private final LaboratoryPlugin plugin;
    
    public LaboratoryCommand(LaboratoryPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("laboratory.admin")) {
            sender.sendMessage("§cУ вас нет прав для использования этой команды!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.getConfigManager().reloadConfig();
                sender.sendMessage("§aКонфигурация плагина перезагружена!");
                break;
                
            case "give":
                if (args.length < 3) {
                    sender.sendMessage("§cИспользование: /laboratory give <игрок> <предмет> [количество]");
                    return true;
                }
                
                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cИгрок не найден!");
                    return true;
                }
                
                String itemId = args[2];
                int amount = 1;
                
                if (args.length > 3) {
                    try {
                        amount = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cНеверное количество!");
                        return true;
                    }
                }
                
                try {
                    ItemStack item = NexoItems.itemFromId(itemId).build();
                    if (item != null) {
                        item.setAmount(amount);
                        target.getInventory().addItem(item);
                        sender.sendMessage("§aВыдан предмет " + itemId + " x" + amount + " игроку " + target.getName());
                        target.sendMessage("§aВы получили " + itemId + " x" + amount);
                    } else {
                        sender.sendMessage("§cПредмет с ID '" + itemId + "' не найден!");
                    }
                } catch (Exception e) {
                    sender.sendMessage("§cОшибка при выдаче предмета: " + e.getMessage());
                }
                break;
                
            case "clear":
                if (args.length < 2) {
                    sender.sendMessage("§cИспользование: /laboratory clear <игрок>");
                    return true;
                }
                
                Player clearTarget = plugin.getServer().getPlayer(args[1]);
                if (clearTarget == null) {
                    sender.sendMessage("§cИгрок не найден!");
                    return true;
                }
                
                plugin.getRadiationManager().clearPlayerRadiation(clearTarget);
                plugin.getResearchManager().clearPlayerData(clearTarget);
                sender.sendMessage("§aДанные игрока " + clearTarget.getName() + " очищены!");
                break;
                
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== Laboratory Plugin Commands ===");
        sender.sendMessage("§e/laboratory reload §7- Перезагрузить конфигурацию");
        sender.sendMessage("§e/laboratory give <игрок> <предмет> [количество] §7- Выдать предмет");
        sender.sendMessage("§e/laboratory clear <игрок> §7- Очистить данные игрока");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("reload", "give", "clear"));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("clear"))) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                completions.add(player.getName());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            completions.addAll(Arrays.asList(
                "uranium_dust", "uranium_ingot", "uranium_block", "uranium_capsule",
                "quantum_core", "geiger_counter", "tablet", "railgun",
                "chem_protection_helmet", "power_armor_helmet",
                "laboratory_terminal", "assembler", "centrifuge_block", "teleporter"
            ));
        }
        
        return completions;
    }
}
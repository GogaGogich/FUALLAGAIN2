package com.example.laboratory.commands;

import com.example.laboratory.managers.TeleportManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportCommand implements CommandExecutor {
    
    private final TeleportManager teleportManager;
    
    public TeleportCommand(TeleportManager teleportManager) {
        this.teleportManager = teleportManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length != 1) {
            player.sendMessage("§cИспользование: /teleport <ID>");
            return true;
        }
        
        String teleporterId = args[0];
        
        if (teleportManager.teleportPlayer(player, teleporterId)) {
            player.sendMessage("§aТелепортация выполнена!");
        } else {
            player.sendMessage("§cТелепорт с ID '" + teleporterId + "' не найден!");
        }
        
        return true;
    }
}
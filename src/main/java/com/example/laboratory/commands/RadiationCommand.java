package com.example.laboratory.commands;

import com.example.laboratory.managers.RadiationManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RadiationCommand implements CommandExecutor {
    
    private final RadiationManager radiationManager;
    
    public RadiationCommand(RadiationManager radiationManager) {
        this.radiationManager = radiationManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        int radiation = radiationManager.getPlayerRadiation(player);
        
        player.sendMessage("§eВаш уровень радиации: §c" + radiation + " §eрад/с");
        
        if (radiation == 0) {
            player.sendMessage("§aВы в безопасности!");
        } else if (radiation < 20) {
            player.sendMessage("§eНизкий уровень радиации");
        } else if (radiation < 50) {
            player.sendMessage("§6Средний уровень радиации");
        } else if (radiation < 100) {
            player.sendMessage("§cВысокий уровень радиации!");
        } else {
            player.sendMessage("§4КРИТИЧЕСКИЙ уровень радиации!");
        }
        
        return true;
    }
}
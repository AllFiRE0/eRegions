package com.allfire.eregions.commands.subcommands;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Cancel command handler
 * 
 * @author AllF1RE
 */
public class CancelCommand extends SubCommand {
    
    public CancelCommand(ERegions plugin) {
        super(plugin, "cancel", "eregions.create", "Отменить создание региона", "/eregion cancel");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эта команда доступна только игрокам!");
            return;
        }
        
        Player player = (Player) sender;
        
        // Check if player has active selection or is waiting for name
        boolean hasActiveSelection = plugin.getSelectionManager().hasActiveSelection(player);
        boolean isWaitingForName = plugin.getSelectionManager().isWaitingForName(player);
        
        if (!hasActiveSelection && !isWaitingForName) {
            sendError(player, "У вас нет активного процесса создания региона для отмены!");
            return;
        }
        
        // Clear selection completely - this will remove from both activeSelections and waitingForName
        plugin.getSelectionManager().clearSelection(player);
        sendSuccess(player, "Создание региона отменено!");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}

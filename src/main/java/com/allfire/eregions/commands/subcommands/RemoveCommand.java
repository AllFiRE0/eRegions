package com.allfire.eregions.commands.subcommands;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.commands.SubCommand;
import com.allfire.eregions.managers.CommandTriggerManager;
import com.allfire.eregions.utils.WorldGuardUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Remove region subcommand
 * 
 * Handles /eregion remove <region> command
 * Removes a region (only if player is owner)
 * 
 * @author AllF1RE
 */
public class RemoveCommand extends SubCommand {
    
    private final CommandTriggerManager commandTriggerManager;
    private final WorldGuardUtils worldGuardUtils;
    
    public RemoveCommand(ERegions plugin) {
        super(plugin, "remove", "eregions.region.remove", 
              "Удалить регион", "/eregion remove <region>");
        
        this.commandTriggerManager = plugin.getCommandTriggerManager();
        this.worldGuardUtils = plugin.getWorldGuardUtils();
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendError(sender, "Эта команда может быть выполнена только игроком!");
            return;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 1) {
            sendUsage(player);
            return;
        }
        
        String regionName = args[0];
        
        // Check if region exists
        if (!worldGuardUtils.regionExists(player.getWorld(), regionName)) {
            sendError(player, "Регион &e" + regionName + " &cне найден!");
            return;
        }
        
        // Check if player is owner of the region
        if (!worldGuardUtils.isRegionOwner(player, regionName)) {
            sendError(player, "Вы не являетесь владельцем региона &e" + regionName + "&c!");
            return;
        }
        
        // Remove region
        try {
            boolean success = worldGuardUtils.removeRegion(player.getWorld(), regionName);
            
            if (success) {
                // Trigger region removed commands
                commandTriggerManager.executeTrigger("region-removed", player, regionName);
                
                sendSuccess(player, "Регион &e" + regionName + " &aуспешно удален!");
            } else {
                sendError(player, "Не удалось удалить регион &e" + regionName + "&c!");
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при удалении региона: " + e.getMessage());
            sendError(player, "Произошла ошибка при удалении региона!");
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1 && sender instanceof Player) {
            // Tab complete region names - ONLY OWNED REGIONS
            Player player = (Player) sender;
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] RemoveCommand.onTabComplete: Getting owned regions for player " + player.getName());
            }
            
            List<String> ownedRegions = worldGuardUtils.getPlayerOwnedRegions(player);
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] RemoveCommand.onTabComplete: Found " + ownedRegions.size() + " owned regions");
            }
            
            for (String region : ownedRegions) {
                if (region.toLowerCase().startsWith(args[0].toLowerCase())) {
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] RemoveCommand.onTabComplete: Adding region to completions: " + region);
                    }
                    completions.add(region);
                }
            }
        }
        
        return completions;
    }
}

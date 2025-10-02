package com.allfire.eregions.commands.subcommands;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.commands.SubCommand;
import com.allfire.eregions.managers.CommandTriggerManager;
import com.allfire.eregions.utils.WorldGuardUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Owner management subcommand
 * 
 * Handles /eregion owner add/remove <region> <player> commands
 * Allows region owners to manage region owners
 * 
 * @author AllF1RE
 */
public class OwnerCommand extends SubCommand {
    
    private final CommandTriggerManager commandTriggerManager;
    private final WorldGuardUtils worldGuardUtils;
    
    public OwnerCommand(ERegions plugin) {
        super(plugin, "owner", "eregions.region.owner", 
              "Управление владельцами региона", "/eregion owner <add/remove> <region> <player>");
        
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
        
        if (args.length < 3) {
            sendUsage(player);
            return;
        }
        
        String action = args[0].toLowerCase();
        String regionName = args[1];
        String targetPlayerName = args[2];
        
        // Validate action
        if (!action.equals("add") && !action.equals("remove")) {
            sendError(player, "Действие должно быть &eadd &cили &eremove&c!");
            return;
        }
        
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
        
        // Check if target player exists
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer == null) {
            sendError(player, "Игрок &e" + targetPlayerName + " &cне найден!");
            return;
        }
        
        // Perform action
        try {
            boolean success = false;
            
            if (action.equals("add")) {
                success = worldGuardUtils.addRegionOwner(player.getWorld(), regionName, targetPlayer);
                
                if (success) {
                    commandTriggerManager.executeTrigger("owner-added", player, regionName, targetPlayerName);
                    sendSuccess(player, "Игрок &e" + targetPlayerName + " &aстал владельцем региона &e" + regionName + "&a!");
                } else {
                    sendError(player, "Не удалось добавить владельца в регион!");
                }
            } else {
                success = worldGuardUtils.removeRegionOwner(player.getWorld(), regionName, targetPlayer);
                
                if (success) {
                    commandTriggerManager.executeTrigger("owner-removed", player, regionName, targetPlayerName);
                    sendSuccess(player, "Игрок &e" + targetPlayerName + " &aбольше не владелец региона &e" + regionName + "&a!");
                } else {
                    sendError(player, "Не удалось удалить владельца из региона!");
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при управлении владельцами: " + e.getMessage());
            sendError(player, "Произошла ошибка при управлении владельцами!");
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Tab complete add/remove
            if ("add".startsWith(args[0].toLowerCase())) {
                completions.add("add");
            }
            if ("remove".startsWith(args[0].toLowerCase())) {
                completions.add("remove");
            }
        } else if (args.length == 2 && sender instanceof Player) {
            // Tab complete region names - ONLY OWNED REGIONS
            Player player = (Player) sender;
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] OwnerCommand.onTabComplete: Getting owned regions for player " + player.getName());
            }
            
            List<String> ownedRegions = worldGuardUtils.getPlayerOwnedRegions(player);
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] OwnerCommand.onTabComplete: Found " + ownedRegions.size() + " owned regions");
            }
            
            for (String region : ownedRegions) {
                if (region.toLowerCase().startsWith(args[1].toLowerCase())) {
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] OwnerCommand.onTabComplete: Adding region to completions: " + region);
                    }
                    completions.add(region);
                }
            }
        } else if (args.length == 3) {
            // Tab complete player names
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                    completions.add(onlinePlayer.getName());
                }
            }
        }
        
        return completions;
    }
}

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
 * Member management subcommand
 * 
 * Handles /eregion member add/remove <region> <player> commands
 * Allows region owners to manage region members
 * 
 * @author AllF1RE
 */
public class MemberCommand extends SubCommand {
    
    private final CommandTriggerManager commandTriggerManager;
    private final WorldGuardUtils worldGuardUtils;
    
    public MemberCommand(ERegions plugin) {
        super(plugin, "member", "eregions.region.members", 
              "Управление участниками региона", "/eregion member <add/remove> <region> <player>");
        
        this.commandTriggerManager = plugin.getCommandTriggerManager();
        this.worldGuardUtils = plugin.getWorldGuardUtils();
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageUtils().sendMessage(sender, "player-only-command");
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
            plugin.getMessageUtils().sendMessage(player, "invalid-action-add-remove");
            return;
        }
        
        // Check if region exists
        if (!worldGuardUtils.regionExists(player.getWorld(), regionName)) {
            plugin.getMessageUtils().sendMessage(player, "region-not-found", "region_name", regionName);
            return;
        }
        
        // Check if player is owner of the region
        if (!worldGuardUtils.isRegionOwner(player, regionName)) {
            plugin.getMessageUtils().sendMessage(player, "not-region-owner", "region_name", regionName);
            return;
        }
        
        // Check if target player exists
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer == null) {
            plugin.getMessageUtils().sendMessage(player, "player-not-found", "player_name", targetPlayerName);
            return;
        }
        
        // Perform action
        try {
            boolean success = false;
            
            if (action.equals("add")) {
                success = worldGuardUtils.addRegionMember(player.getWorld(), regionName, targetPlayer);
                
                if (success) {
                    commandTriggerManager.executeTrigger("member-added", player, regionName, targetPlayerName);
                    plugin.getMessageUtils().sendMessage(player, "member-added-success", "player_name", targetPlayerName, "region_name", regionName);
                } else {
                    plugin.getMessageUtils().sendMessage(player, "member-add-failed");
                }
            } else {
                success = worldGuardUtils.removeRegionMember(player.getWorld(), regionName, targetPlayer);
                
                if (success) {
                    commandTriggerManager.executeTrigger("member-removed", player, regionName, targetPlayerName);
                    plugin.getMessageUtils().sendMessage(player, "member-removed-success", "player_name", targetPlayerName, "region_name", regionName);
                } else {
                    plugin.getMessageUtils().sendMessage(player, "member-remove-failed");
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при управлении участниками: " + e.getMessage());
            plugin.getMessageUtils().sendMessage(player, "member-management-error");
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
                plugin.getLogger().info("[DEBUG] MemberCommand.onTabComplete: Getting owned regions for player " + player.getName());
            }
            
            List<String> ownedRegions = worldGuardUtils.getPlayerOwnedRegions(player);
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] MemberCommand.onTabComplete: Found " + ownedRegions.size() + " owned regions");
            }
            
            for (String region : ownedRegions) {
                if (region.toLowerCase().startsWith(args[1].toLowerCase())) {
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] MemberCommand.onTabComplete: Adding region to completions: " + region);
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

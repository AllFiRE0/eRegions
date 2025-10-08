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
            plugin.getMessageUtils().sendMessage(sender, "player-only-command");
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
            plugin.getMessageUtils().sendMessage(player, "region-not-found", "region_name", regionName);
            return;
        }
        
        // Check if player is owner of the region
        if (!worldGuardUtils.isRegionOwner(player, regionName)) {
            plugin.getMessageUtils().sendMessage(player, "not-region-owner", "region_name", regionName);
            return;
        }
        
        // Remove region
        try {
            boolean success = worldGuardUtils.removeRegion(player.getWorld(), regionName);
            
            if (success) {
                // Trigger region removed commands
                commandTriggerManager.executeTrigger("region-removed", player, regionName);
                
                plugin.getMessageUtils().sendMessage(player, "region-removed-success", "region_name", regionName);
            } else {
                plugin.getMessageUtils().sendMessage(player, "region-remove-failed", "region_name", regionName);
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при удалении региона: " + e.getMessage());
            plugin.getMessageUtils().sendMessage(player, "region-remove-error");
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

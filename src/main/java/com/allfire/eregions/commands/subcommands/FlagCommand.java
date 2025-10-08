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
 * Flag management subcommand
 * 
 * Handles /eregion flag add/remove <region> <flag> commands
 * Allows region owners to manage region flags
 * 
 * @author AllF1RE
 */
public class FlagCommand extends SubCommand {
    
    private final CommandTriggerManager commandTriggerManager;
    private final WorldGuardUtils worldGuardUtils;
    
    public FlagCommand(ERegions plugin) {
        super(plugin, "flag", "eregions.region.flag", 
              "Управление флагами региона", "/eregion flag <add/remove> <region> <flag>");
        
        this.commandTriggerManager = plugin.getCommandTriggerManager();
        this.worldGuardUtils = plugin.getWorldGuardUtils();
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] ========== FlagCommand.execute ==========");
            plugin.getLogger().info("[DEBUG] FlagCommand.execute called with " + args.length + " arguments: " + String.join(" ", args));
        }
        
        if (!(sender instanceof Player)) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] FlagCommand: Sender is not a player");
            }
            plugin.getMessageUtils().sendMessage(sender, "player-only-command");
            return;
        }
        
        Player player = (Player) sender;
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] FlagCommand: Player " + player.getName() + " executing flag command");
        }
        
        if (args.length < 2) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] FlagCommand: Not enough arguments. Expected at least 2, got " + args.length);
            }
            sendUsage(player);
            return;
        }
        
        // Check for too many arguments
        if (args.length > 3) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] FlagCommand: Too many arguments. Expected max 3, got " + args.length);
            }
            plugin.getMessageUtils().sendMessage(player, "too-many-arguments-flag");
            sendUsage(player);
            return;
        }
        
        // Fix argument parsing - ERegionCommand removes the subcommand name
        // So the actual arguments are: args[0] = action, args[1] = region, args[2] = flag
        String action = args[0].toLowerCase().trim();
        String regionName = args[1].trim();
        String flagName = args.length > 2 ? args[2].trim() : null;
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] FlagCommand: Raw arguments - action: '" + action + "', region: '" + regionName + "', flag: '" + flagName + "'");
        }
        
        // Check for invalid action values
        if (action.isEmpty() || action.equals("пустая_строка") || action.equals("empty_string")) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] FlagCommand: Invalid action detected: '" + action + "'");
            }
            plugin.getMessageUtils().sendMessage(player, "invalid-action-add-remove");
            sendUsage(player);
            return;
        }
        
        // If flagName is null, we need to ask for it
        if (flagName == null || flagName.isEmpty()) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] FlagCommand: Flag name not provided, asking player");
            }
            plugin.getMessageUtils().sendMessage(player, "flag-name-required", "action", action, "region_name", regionName);
            return;
        }
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] FlagCommand: Parsed arguments - action: " + action + ", region: " + regionName + ", flag: " + flagName);
        }
        
        // Validate action
        if (!action.equals("add") && !action.equals("remove")) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] FlagCommand: Invalid action: " + action);
            }
            plugin.getMessageUtils().sendMessage(player, "invalid-action-add-remove");
            return;
        }
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] FlagCommand: Action validation passed");
        }
        
        // Check if region exists
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] FlagCommand: Checking if region exists: " + regionName + " in world: " + player.getWorld().getName());
        }
        if (!worldGuardUtils.regionExists(player.getWorld(), regionName)) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] FlagCommand: Region " + regionName + " not found");
            }
            plugin.getMessageUtils().sendMessage(player, "region-not-found", "region_name", regionName);
            return;
        }
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] FlagCommand: Region exists check passed");
        }
        
        // Check if player is owner of the region
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] FlagCommand: Checking if player " + player.getName() + " is owner of region " + regionName);
        }
        if (!worldGuardUtils.isRegionOwner(player, regionName)) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] FlagCommand: Player " + player.getName() + " is not owner of region " + regionName);
            }
            plugin.getMessageUtils().sendMessage(player, "not-region-owner", "region_name", regionName);
            return;
        }
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] FlagCommand: Owner check passed");
        }
        
        // Check if player has permission for this flag
        String permission = "worldguard.region.flag.flags." + flagName + ".*";
        String customPermission = "eregions.region.flag.flags." + flagName + ".*";
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] FlagCommand: Checking permissions: " + permission + " or " + customPermission);
        }
        
        boolean hasPermission = plugin.getPermissionUtils().hasPermission(player, permission) || 
                              plugin.getPermissionUtils().hasPermission(player, customPermission);
        
        if (!hasPermission) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] FlagCommand: Player " + player.getName() + " does not have permission for flag: " + flagName);
            }
            plugin.getMessageUtils().sendMessage(player, "no-flag-permission", "flag_name", flagName);
            return;
        }
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] FlagCommand: Permission check passed");
        }
        
        // Perform action
        try {
            boolean success = false;
            
            if (action.equals("add")) {
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] FlagCommand: Adding flag " + flagName + " to region " + regionName);
                }
                success = worldGuardUtils.addRegionFlag(player.getWorld(), regionName, flagName, "allow");
                
                if (success) {
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] FlagCommand: Flag added successfully, executing trigger");
                    }
                    commandTriggerManager.executeTrigger("flag-added", player, regionName, null, flagName);
                    
                    // Execute trigger with flag information
                    commandTriggerManager.executeTrigger("flag-changed", player, regionName, null, null, null, flagName, "allow");
                } else {
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] FlagCommand: Failed to add flag");
                    }
                    plugin.getMessageUtils().sendMessage(player, "flag-add-failed");
                }
            } else {
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] FlagCommand: Removing flag " + flagName + " from region " + regionName);
                }
                success = worldGuardUtils.removeRegionFlag(player.getWorld(), regionName, flagName);
                
                if (success) {
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] FlagCommand: Flag removed successfully, executing trigger");
                    }
                    commandTriggerManager.executeTrigger("flag-removed", player, regionName, null, flagName);
                    
                    // Execute trigger with flag information
                    commandTriggerManager.executeTrigger("flag-changed", player, regionName, null, null, null, flagName, "deny");
                } else {
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] FlagCommand: Failed to remove flag");
                    }
                    plugin.getMessageUtils().sendMessage(player, "flag-remove-failed");
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("[DEBUG] FlagCommand: Exception during flag operation: " + e.getMessage());
            e.printStackTrace();
            plugin.getMessageUtils().sendMessage(player, "flag-management-error");
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] ========== FlagCommand.onTabComplete ==========");
            plugin.getLogger().info("[DEBUG] FlagCommand.onTabComplete: Tab completing with " + args.length + " arguments: " + String.join(" ", args));
        }
        
        if (args.length == 1) {
            // Tab complete add/remove
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] FlagCommand.onTabComplete: Tab completing action, current input: '" + args[0] + "'");
            }
            
            String input = args[0].toLowerCase().trim();
            
            if ("add".startsWith(input)) {
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] FlagCommand.onTabComplete: Adding 'add' to completions");
                }
                completions.add("add");
            }
            if ("remove".startsWith(input)) {
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] FlagCommand.onTabComplete: Adding 'remove' to completions");
                }
                completions.add("remove");
            }
            
        } else if (args.length == 2 && sender instanceof Player) {
            // Tab complete region names - ONLY OWNED REGIONS
            Player player = (Player) sender;
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] FlagCommand.onTabComplete: Getting owned regions for player " + player.getName());
            }
            
            List<String> ownedRegions = worldGuardUtils.getPlayerOwnedRegions(player);
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] FlagCommand.onTabComplete: Found " + ownedRegions.size() + " owned regions");
            }
            
            String input = args[1].toLowerCase().trim();
            for (String region : ownedRegions) {
                if (region.toLowerCase().startsWith(input)) {
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] FlagCommand.onTabComplete: Adding region to completions: " + region);
                    }
                    completions.add(region);
                }
            }
            
        } else if (args.length == 3 && sender instanceof Player) {
            // Tab complete flags based on player permissions
            Player player = (Player) sender;
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] FlagCommand.onTabComplete: Getting available flags for player " + player.getName());
            }
            
            String[] allFlags = {
                "pvp", "mob-damage", "creeper-explosion", "tnt", "fire-spread",
                "lava-fire", "lightning", "mob-spawning", "other-explosion",
                "enderman-grief", "ghast-fireball", "snow-fall", "snow-melt",
                "ice-form", "ice-melt", "frosted-ice-melt", "mushroom-growth",
                "leaf-decay", "grass-growth", "mycelium-spread", "vine-growth",
                "crop-growth", "soil-dry", "water-flow", "lava-flow", "chest-access",
                "use", "interact", "damage", "sleep", "item-drop", "item-pickup",
                "exp-drops", "regionborder-view"
            };
            
            String input = args[2].toLowerCase().trim();
            for (String flag : allFlags) {
                // Check if player has permission for this flag
                String permission = "worldguard.region.flag.flags." + flag + ".*";
                String customPermission = "eregions.region.flag.flags." + flag + ".*";
                
                boolean hasPermission = plugin.getPermissionUtils().hasPermission(player, permission) || 
                                      plugin.getPermissionUtils().hasPermission(player, customPermission);
                
                if (hasPermission && flag.toLowerCase().startsWith(input)) {
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] FlagCommand.onTabComplete: Adding flag to completions: " + flag + " (permission: " + permission + ")");
                    }
                    completions.add(flag);
                }
            }
        }
        
        // Filter out invalid suggestions
        completions.removeIf(suggestion -> 
            suggestion == null || 
            suggestion.trim().isEmpty() || 
            suggestion.equals("пустая_строка") || 
            suggestion.equals("empty_string")
        );
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] FlagCommand.onTabComplete: Returning " + completions.size() + " completions: " + completions);
            plugin.getLogger().info("[DEBUG] ========== FlagCommand.onTabComplete END ==========");
        }
        return completions;
    }
}

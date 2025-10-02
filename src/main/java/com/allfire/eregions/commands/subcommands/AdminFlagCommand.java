package com.allfire.eregions.commands.subcommands;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.commands.SubCommand;
import com.allfire.eregions.utils.WorldGuardUtils;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.DoubleFlag;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Admin flag management subcommand
 * 
 * Handles /eregion admin flag commands for advanced flag management
 * Supports multiple groups, console execution, and silent mode
 * 
 * @author AllF1RE
 */
public class AdminFlagCommand extends SubCommand {
    
    private final WorldGuardUtils worldGuardUtils;
    
    public AdminFlagCommand(ERegions plugin) {
        super(plugin, "admin flag", "eregions.admin.flag", 
              "Управление флагами регионов (админ)", "/eregion admin flag <region> <flag> <value> [groups] [silent]");
        
        this.worldGuardUtils = plugin.getWorldGuardUtils();
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            if (sender instanceof Player) {
                sendUsage((Player) sender);
            } else {
                sender.sendMessage("Использование: " + getUsage());
            }
            return;
        }
        
        String regionName = args[0];
        String flagName = args[1];
        String value = args[2];
        
        // Parse optional parameters
        List<String> groups = new ArrayList<>();
        boolean silent = false;
        
        if (args.length > 3) {
            // Check for silent flag and collect groups
            for (int i = 3; i < args.length; i++) {
                String arg = args[i];
                if (arg.equalsIgnoreCase("silent")) {
                    silent = true;
                } else if (!arg.trim().isEmpty()) {
                    groups.add(arg);
                }
                // Skip empty arguments (double spaces)
            }
        }
        
        // Default groups if none specified
        if (groups.isEmpty()) {
            groups.add("all");
        }
        
        // Remove duplicates while preserving order
        List<String> uniqueGroups = new ArrayList<>();
        for (String group : groups) {
            if (!uniqueGroups.contains(group.toLowerCase())) {
                uniqueGroups.add(group);
            }
        }
        groups = uniqueGroups;
        
        // Validate groups
        List<String> validGroups = Arrays.asList("all", "members", "owners", "nonmembers", "nonowners");
        for (String group : groups) {
            if (!validGroups.contains(group.toLowerCase())) {
                sendError(sender, "Неверная группа: " + group + ". Доступные: " + String.join(", ", validGroups));
                return;
            }
        }
        
        // Get world (use sender's world if player, or default world if console)
        World world;
        if (sender instanceof Player) {
            world = ((Player) sender).getWorld();
        } else {
            world = Bukkit.getWorlds().get(0); // Default world
        }
        
        // Check if region exists
        if (!worldGuardUtils.regionExists(world, regionName)) {
            sendError(sender, "Регион " + regionName + " не найден в мире " + world.getName() + "!");
            return;
        }
        
        // Note: We don't validate flags here to allow custom flags from other plugins
        // WorldGuard will handle the validation when executing the command
        
        // Apply flag to all specified groups
        boolean success = true;
        for (String group : groups) {
            if (!setFlagForGroup(world, regionName, flagName, value, group, silent)) {
                success = false;
            }
        }
        
        if (success) {
            if (!silent) {
                sendSuccess(sender, "Флаг &e" + flagName + " &aустановлен в значение &e" + value + " &aдля групп &e" + String.join(", ", groups) + " &aв регионе &e" + regionName + "&a!");
            }
        } else {
            sendError(sender, "Ошибка при установке флага!");
        }
    }
    
    /**
     * Set flag for specific group using WorldGuard commands
     */
    private boolean setFlagForGroup(World world, String regionName, String flagName, String value, String group, boolean silent) {
        try {
            if (!worldGuardUtils.isWorldGuardAvailable()) {
                if (!silent) {
                    plugin.getLogger().warning("WorldGuard недоступен!");
                }
                return false;
            }
            
            // Note: We don't validate flags here anymore to allow custom flags from other plugins
            // WorldGuard will handle the validation when executing the command
            
            // Use WorldGuard commands with proper formatting
            String command;
            if (group.equalsIgnoreCase("all")) {
                command = String.format("region flag %s %s %s", regionName, flagName, value);
            } else {
                command = String.format("region flag %s -g %s %s %s", regionName, group, flagName, value);
            }
            
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] Executing WorldGuard command: " + command);
            }
            
            // Execute command directly without additional parameters
            boolean commandSuccess = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] Command execution result: " + commandSuccess);
            }
            
            return true;
            
        } catch (Exception e) {
            if (!silent) {
                plugin.getLogger().severe("Ошибка при установке флага: " + e.getMessage());
            }
            return false;
        }
    }
    
    /**
     * Parse flag value based on flag type
     */
    private Object parseFlagValue(Flag<?> flag, String value) {
        try {
            if (flag instanceof StateFlag) {
                if (value.equalsIgnoreCase("allow")) {
                    return StateFlag.State.ALLOW;
                } else if (value.equalsIgnoreCase("deny")) {
                    return StateFlag.State.DENY;
                } else {
                    return null;
                }
            } else if (flag instanceof StringFlag) {
                return value;
            } else if (flag instanceof IntegerFlag) {
                return Integer.parseInt(value);
            } else if (flag instanceof DoubleFlag) {
                return Double.parseDouble(value);
            } else if (flag instanceof BooleanFlag) {
                return Boolean.parseBoolean(value);
            } else {
                // For unknown flag types, try to parse as string
                return value;
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Check if flag is valid (including custom flags from other plugins)
     */
    private boolean isValidFlag(String flagName) {
        try {
            if (!worldGuardUtils.isWorldGuardAvailable()) {
                return false;
            }
            
            FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();
            Flag<?> flag = flagRegistry.get(flagName);
            
            // If flag exists in WorldGuard registry, it's valid
            if (flag != null) {
                return true;
            }
            
            // For custom flags from other plugins, we'll assume they're valid
            // and let WorldGuard handle the validation when executing the command
            // This allows plugins like ShopGuard, GriefPrevention, etc. to work
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Tab complete region names
            if (sender instanceof Player) {
                Player player = (Player) sender;
                List<String> regions = worldGuardUtils.getPlayerOwnedRegions(player);
                for (String region : regions) {
                    if (region.toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(region);
                    }
                }
            }
        } else if (args.length == 2) {
            // Tab complete flag names
            try {
                FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();
                List<Flag<?>> flags = flagRegistry.getAll();
                for (Flag<?> flag : flags) {
                    String flagName = flag.getName();
                    if (flagName.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(flagName);
                    }
                }
            } catch (Exception e) {
                // Ignore errors
            }
        } else if (args.length == 3) {
            // Tab complete flag values
            String flagName = args[1];
            try {
                FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();
                Flag<?> flag = flagRegistry.get(flagName);
                
                if (flag instanceof StateFlag) {
                    // For StateFlag, suggest allow/deny
                    completions.add("allow");
                    completions.add("deny");
                } else {
                    // For custom flags from other plugins, also suggest allow/deny
                    // as most custom flags follow the same pattern
                    completions.add("allow");
                    completions.add("deny");
                }
            } catch (Exception e) {
                // For unknown flags, suggest allow/deny as fallback
                completions.add("allow");
                completions.add("deny");
            }
        } else if (args.length >= 4) {
            // Tab complete groups
            List<String> groups = Arrays.asList("all", "members", "owners", "nonmembers", "nonowners", "silent");
            String lastArg = args[args.length - 1];
            
            // If last argument is empty (just spaces), show all groups
            if (lastArg.isEmpty()) {
                completions.addAll(groups);
            } else {
                // Filter groups based on last argument
                for (String group : groups) {
                    if (group.toLowerCase().startsWith(lastArg.toLowerCase())) {
                        completions.add(group);
                    }
                }
            }
        }
        
        return completions;
    }
}

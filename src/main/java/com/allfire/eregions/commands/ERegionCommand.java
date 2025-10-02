package com.allfire.eregions.commands;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.commands.subcommands.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main command handler for /eregion
 * 
 * @author AllF1RE
 */
public class ERegionCommand implements CommandExecutor, TabCompleter {
    
    private final ERegions plugin;
    private final Map<String, SubCommand> subcommands;
    
    public ERegionCommand(ERegions plugin) {
        this.plugin = plugin;
        this.subcommands = new HashMap<>();
        
        // Register subcommands
        registerSubcommands();
    }
    
    /**
     * Register all subcommands
     */
    private void registerSubcommands() {
        subcommands.put("create", new CreateCommand(plugin));
        subcommands.put("cancel", new CancelCommand(plugin));
        subcommands.put("remove", new RemoveCommand(plugin));
        subcommands.put("member", new MemberCommand(plugin));
        subcommands.put("owner", new OwnerCommand(plugin));
        subcommands.put("flag", new FlagCommand(plugin));
        subcommands.put("flags", new FlagsCommand(plugin));
        subcommands.put("move", new MoveCommand(plugin));
        subcommands.put("size", new SizeCommand(plugin));
        subcommands.put("reload", new ReloadCommand(plugin));
        subcommands.put("help", new HelpCommand(plugin));
        
        // Admin commands
        subcommands.put("admin", new AdminCommand(plugin));
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] ERegionCommand.onCommand called by " + sender.getName() + " with " + args.length + " arguments: " + String.join(" ", args));
        }
        
        if (args.length == 0) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] ERegionCommand: No arguments provided, showing help");
            }
            // Show help if no arguments
            SubCommand helpCommand = subcommands.get("help");
            if (helpCommand != null) {
                helpCommand.execute(sender, args);
            } else {
                plugin.getMessageUtils().sendMessage(sender, "&eИспользуйте /eregion help для получения справки");
            }
            return true;
        }
        
        String subcommandName = args[0].toLowerCase();
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] ERegionCommand: Looking for subcommand: " + subcommandName);
        }
        
        SubCommand subcommand = subcommands.get(subcommandName);
        
        if (subcommand == null) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] ERegionCommand: Subcommand not found: " + subcommandName);
            }
            plugin.getMessageUtils().sendMessage(sender, "&cНеизвестная подкоманда: " + subcommandName);
            return true;
        }
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] ERegionCommand: Subcommand found: " + subcommandName);
        }
        
        // Check permission
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] ERegionCommand: Checking permission for subcommand: " + subcommandName);
        }
        if (!subcommand.hasPermission(sender)) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] ERegionCommand: Permission denied for subcommand: " + subcommandName);
            }
            plugin.getMessageUtils().sendMessage(sender, "&cУ вас нет прав для выполнения этой команды!");
            return true;
        }
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] ERegionCommand: Permission granted, executing subcommand: " + subcommandName);
        }
        
        // Create new args array without the subcommand name for execution
        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, subArgs.length);
        
        // Log the exact arguments being passed to subcommand
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] ERegionCommand: Passing " + subArgs.length + " arguments to subcommand: " + String.join(" ", subArgs));
        }
        
        // Execute subcommand
        subcommand.execute(sender, subArgs);
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] ERegionCommand.onTabComplete: Tab completing with " + args.length + " arguments: " + String.join(" ", args));
        }
        
        if (args.length == 1) {
            // Tab complete subcommand names
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] ERegionCommand.onTabComplete: Tab completing subcommand names");
            }
            for (String subcommandName : subcommands.keySet()) {
                if (subcommandName.toLowerCase().startsWith(args[0].toLowerCase())) {
                    SubCommand subcommand = subcommands.get(subcommandName);
                    if (subcommand.hasPermission(sender)) {
                        if (plugin.getConfigManager().isDebugMode()) {
                            plugin.getLogger().info("[DEBUG] ERegionCommand.onTabComplete: Adding subcommand: " + subcommandName);
                        }
                        completions.add(subcommandName);
                    }
                }
            }
        } else if (args.length > 1) {
            // Tab complete subcommand arguments
            String subcommandName = args[0].toLowerCase();
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] ERegionCommand.onTabComplete: Tab completing arguments for subcommand: " + subcommandName);
            }
            
            SubCommand subcommand = subcommands.get(subcommandName);
            
            if (subcommand != null && subcommand.hasPermission(sender)) {
                // Create new args array without the subcommand name
                String[] subArgs = new String[args.length - 1];
                System.arraycopy(args, 1, subArgs, 0, subArgs.length);
                
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] ERegionCommand.onTabComplete: Passing " + subArgs.length + " arguments to subcommand: " + String.join(" ", subArgs));
                }
                
                List<String> subCompletions = subcommand.onTabComplete(sender, subArgs);
                if (subCompletions != null) {
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] ERegionCommand.onTabComplete: Got " + subCompletions.size() + " completions from subcommand");
                    }
                    completions.addAll(subCompletions);
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
            plugin.getLogger().info("[DEBUG] ERegionCommand.onTabComplete: Returning " + completions.size() + " completions: " + completions);
        }
        return completions;
    }
    
    /**
     * Get subcommand by name
     * 
     * @param name Subcommand name
     * @return Subcommand or null
     */
    public SubCommand getSubcommand(String name) {
        return subcommands.get(name.toLowerCase());
    }
    
    /**
     * Get all subcommands
     * 
     * @return Map of subcommands
     */
    public Map<String, SubCommand> getSubcommands() {
        return new HashMap<>(subcommands);
    }
}

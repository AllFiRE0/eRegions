package com.allfire.eregions.commands;

import com.allfire.eregions.ERegions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Base class for all subcommands
 * 
 * @author AllF1RE
 */
public abstract class SubCommand {
    
    protected final ERegions plugin;
    protected final String name;
    protected final String permission;
    protected final String description;
    protected final String usage;
    
    public SubCommand(ERegions plugin, String name, String permission, String description, String usage) {
        this.plugin = plugin;
        this.name = name;
        this.permission = permission;
        this.description = description;
        this.usage = usage;
    }
    
    /**
     * Execute the subcommand
     * 
     * @param sender Command sender
     * @param args Command arguments
     */
    public abstract void execute(CommandSender sender, String[] args);
    
    /**
     * Handle tab completion
     * 
     * @param sender Command sender
     * @param args Command arguments
     * @return List of completions
     */
    public abstract List<String> onTabComplete(CommandSender sender, String[] args);
    
    /**
     * Check if sender has permission
     * 
     * @param sender Command sender
     * @return True if has permission
     */
    public boolean hasPermission(CommandSender sender) {
        if (permission == null || permission.isEmpty()) {
            return true;
        }
        return sender.hasPermission(permission);
    }
    
    /**
     * Send error message to sender
     * 
     * @param sender Command sender
     * @param message Error message
     */
    protected void sendError(CommandSender sender, String message) {
        plugin.getMessageUtils().sendMessage(sender, "&c" + message);
    }
    
    /**
     * Send success message to sender
     * 
     * @param sender Command sender
     * @param message Success message
     */
    protected void sendSuccess(CommandSender sender, String message) {
        plugin.getMessageUtils().sendMessage(sender, "&a" + message);
    }
    
    /**
     * Send usage message to player
     * 
     * @param player Player
     */
    protected void sendUsage(Player player) {
        plugin.getMessageUtils().sendMessage(player, "usage-command", "usage", usage);
    }
    
    // Getters
    public String getName() {
        return name;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getUsage() {
        return usage;
    }
}


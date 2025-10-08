package com.allfire.eregions.commands.subcommands;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.commands.SubCommand;
import com.allfire.eregions.managers.SelectionManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Create region subcommand
 * 
 * Handles /eregion create command
 * Starts region creation process
 * 
 * @author AllF1RE
 */
public class CreateCommand extends SubCommand {
    
    private final SelectionManager selectionManager;
    
    public CreateCommand(ERegions plugin) {
        super(plugin, "create", "eregions.region.create", 
              "Создать новый регион", "/eregion create");
        
        this.selectionManager = plugin.getSelectionManager();
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] ========== CreateCommand.execute ==========");
            plugin.getLogger().info("[DEBUG] CreateCommand.execute called by " + sender.getName());
            plugin.getLogger().info("[DEBUG] CreateCommand args count: " + args.length);
            plugin.getLogger().info("[DEBUG] CreateCommand args: " + String.join(" ", args));
        }
        
        if (!(sender instanceof Player)) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] CreateCommand: Sender is not a player");
            }
            plugin.getMessageUtils().sendMessage(sender, "player-only-command");
            return;
        }
        
        Player player = (Player) sender;
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] CreateCommand: Player " + player.getName() + " starting region creation");
            plugin.getLogger().info("[DEBUG] CreateCommand: Player world: " + player.getWorld().getName());
            plugin.getLogger().info("[DEBUG] CreateCommand: Player location: " + player.getLocation().toString());
        }
        
        // Check if player already has active selection or is waiting for name
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] CreateCommand: Checking if player has active selection or is waiting for name");
        }
        boolean hasSelection = selectionManager.hasActiveSelection(player);
        boolean isWaitingForName = selectionManager.isWaitingForName(player);
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] CreateCommand: Player has active selection: " + hasSelection);
            plugin.getLogger().info("[DEBUG] CreateCommand: Player is waiting for name: " + isWaitingForName);
        }
        
        if (hasSelection || isWaitingForName) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] CreateCommand: Player already has active selection or is waiting for name, aborting");
            }
            plugin.getMessageUtils().sendMessage(player, "active-selection-exists");
            return;
        }
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] CreateCommand: No active selection found, proceeding");
        }
        
        // Start selection process
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] CreateCommand: Starting selection for player " + player.getName());
        }
        try {
            selectionManager.startSelection(player, "create");
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] CreateCommand: Selection started successfully");
            }
        } catch (Exception e) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().severe("[DEBUG] CreateCommand: Error starting selection: " + e.getMessage());
            }
            e.printStackTrace();
            plugin.getMessageUtils().sendMessage(player, "selection-start-error");
            return;
        }
        
        // Send messages to guide player
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] CreateCommand: Sending guidance messages to player");
        }
        try {
            // Send configurable messages
            plugin.getMessageUtils().sendConfigurableMessage(player, "selection-start");
            plugin.getMessageUtils().sendConfigurableMessage(player, "selection-instructions");
            plugin.getMessageUtils().sendConfigurableMessage(player, "selection-cancel");
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] CreateCommand: Messages sent successfully");
            }
        } catch (Exception e) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().severe("[DEBUG] CreateCommand: Error sending messages: " + e.getMessage());
            }
            e.printStackTrace();
        }
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] CreateCommand: Create command completed successfully");
            plugin.getLogger().info("[DEBUG] ========== CreateCommand.execute END ==========");
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        // No tab completion needed for create command
        return new ArrayList<>();
    }
}



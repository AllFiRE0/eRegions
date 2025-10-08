package com.allfire.eregions.listeners;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.managers.SelectionManager;
import com.allfire.eregions.managers.CommandTriggerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.logging.Level;

/**
 * Player Chat Listener
 *
 * Handles player chat events for region name input
 * Manages the region creation process
 *
 * @author AllF1RE
 */
public class PlayerChatListener implements Listener {

    private final ERegions plugin;
    private final SelectionManager selectionManager;
    private final CommandTriggerManager commandTriggerManager;

    public PlayerChatListener(ERegions plugin) {
        this.plugin = plugin;
        this.selectionManager = plugin.getSelectionManager();
        this.commandTriggerManager = plugin.getCommandTriggerManager();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        try {
            Player player = event.getPlayer();
            
            // Check if player is waiting for region name input
            if (!selectionManager.isWaitingForName(player)) {
                return;
            }
            
            SelectionManager.SelectionData selectionData = selectionManager.getWaitingForName(player);
            if (selectionData == null) {
                return;
            }
            
            // Check if player has create permission
            if (!plugin.getPermissionUtils().hasPermission(player, "eregions.region.create")) {
                return;
            }
            
            // Get region name from chat message
            String regionName = event.getMessage().trim();
            
            // Cancel the chat event to prevent the message from being sent
            event.setCancelled(true);
            
            // Check for cancellation
            if (regionName.equalsIgnoreCase("отмена") || regionName.equalsIgnoreCase("cancel") || 
                regionName.equalsIgnoreCase("нет") || regionName.equalsIgnoreCase("no")) {
                plugin.getMessageUtils().sendMessage(player, "region-creation-cancelled-chat");
                // Clear selection completely - this will remove from both activeSelections and waitingForName
                selectionManager.clearSelection(player);
                return;
            }
            
            // Validate region name
            if (regionName.isEmpty()) {
                plugin.getMessageUtils().sendMessage(player, "region-name-empty");
                return;
            }
            
            if (regionName.length() > 32) {
                plugin.getMessageUtils().sendMessage(player, "region-name-too-long");
                return;
            }
            
            // Check if region name contains only valid characters
            if (!regionName.matches("^[a-zA-Z0-9_-]+$")) {
                plugin.getMessageUtils().sendMessage(player, "region-name-invalid-chars");
                return;
            }
            
            // Create region
            boolean success = plugin.getRegionManager().createRegion(
                player,
                regionName,
                selectionData.getPos1(),
                selectionData.getPos2()
            );
            
            if (success) {
                // Trigger region created commands
                commandTriggerManager.executeTrigger("region-created", player, regionName);
                
                plugin.getMessageUtils().sendMessage(player, "region-created-success-direct", "region_name", regionName);
                
                // Remove from waiting for name but keep selection for size/move commands
                selectionManager.removeWaitingForName(player);
                
                // Automatically execute /eregion cancel command for the player in main thread
                // This will clear the selection state but keep WorldEdit selection for /svis
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    plugin.getServer().dispatchCommand(player, "eregion cancel");
                });
            } else {
                // Check if it's due to overlapping regions
                List<String> overlappingRegions = plugin.getWorldGuardUtils().getOverlappingRegions(
                    player.getWorld(), 
                    selectionData.getPos1(), 
                    selectionData.getPos2()
                );
                
                if (!overlappingRegions.isEmpty()) {
                    plugin.getMessageUtils().sendMessage(player, "region-overlapping-direct", "overlapping_regions", String.join(", ", overlappingRegions));
                } else {
                    plugin.getMessageUtils().sendMessage(player, "region-creation-failed-direct", "region_name", regionName);
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка в PlayerChatListener", e);
        }
    }
}

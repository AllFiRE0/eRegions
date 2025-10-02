package com.allfire.eregions.listeners;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.managers.SelectionManager;
import com.allfire.eregions.managers.CommandTriggerManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.logging.Level;

/**
 * Player Interact Listener
 * 
 * Handles player interaction events for region selection
 * Manages SHIFT+left/right click selection process
 * 
 * @author AllF1RE
 */
public class PlayerInteractListener implements Listener {
    
    private final ERegions plugin;
    private final SelectionManager selectionManager;
    private final CommandTriggerManager commandTriggerManager;
    
    public PlayerInteractListener(ERegions plugin) {
        this.plugin = plugin;
        this.selectionManager = plugin.getSelectionManager();
        this.commandTriggerManager = plugin.getCommandTriggerManager();
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        try {
            Player player = event.getPlayer();
            
            // Cancel block breaking when player has active selection
            if (selectionManager.hasActiveSelection(player) && player.isSneaking()) {
                event.setCancelled(true);
            }
            
            if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                plugin.getLogger().info("[DEBUG] PlayerInteractListener: Player " + player.getName() + " interacted with action: " + event.getAction() + ", sneaking: " + player.isSneaking());
            }
            
            // Check if player has active selection
            if (!selectionManager.hasActiveSelection(player)) {
                if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                    plugin.getLogger().info("[DEBUG] PlayerInteractListener: Player " + player.getName() + " has no active selection");
                }
                return;
            }
            
            if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                plugin.getLogger().info("[DEBUG] PlayerInteractListener: Player " + player.getName() + " has active selection");
            }
            
            // Check if player has view permission
            if (!plugin.getPermissionUtils().hasPermission(player, "eregions.view")) {
                if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                    plugin.getLogger().info("[DEBUG] PlayerInteractListener: Player " + player.getName() + " does not have eregions.view permission");
                }
                return;
            }
            
            if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                plugin.getLogger().info("[DEBUG] PlayerInteractListener: Player " + player.getName() + " has view permission");
            }
            
            // Only handle SHIFT+click events
            if (!player.isSneaking()) {
                if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                    plugin.getLogger().info("[DEBUG] PlayerInteractListener: Player " + player.getName() + " is not sneaking");
                }
                return;
            }
            
            if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                plugin.getLogger().info("[DEBUG] PlayerInteractListener: Player " + player.getName() + " is sneaking, handling click");
            }
            
            // Handle selection based on action - only LEFT_CLICK for both points
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                    plugin.getLogger().info("[DEBUG] PlayerInteractListener: Handling SHIFT+LEFT_CLICK_BLOCK");
                }
                handleShiftLeftClick(player, event);
            } else if (event.getAction() == Action.LEFT_CLICK_AIR) {
                if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                    plugin.getLogger().info("[DEBUG] PlayerInteractListener: Handling SHIFT+LEFT_CLICK_AIR");
                }
                handleShiftLeftClick(player, event);
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка в PlayerInteractListener", e);
        }
    }
    
    /**
     * Handle SHIFT+LEFT_CLICK selection
     * 
     * @param player Player
     * @param event Event
     */
    private void handleShiftLeftClick(Player player, PlayerInteractEvent event) {
        if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
            plugin.getLogger().info("[DEBUG] PlayerInteractListener.handleShiftLeftClick called for player " + player.getName());
        }
        
        try {
            // Check if player has active selection
            if (!selectionManager.hasActiveSelection(player)) {
                if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                    plugin.getLogger().info("[DEBUG] PlayerInteractListener: No active selection for player " + player.getName() + ", ignoring click");
                }
                return;
            }
            
            SelectionManager.SelectionData selectionData = selectionManager.getActiveSelection(player);
            if (selectionData.isCompleted()) {
                if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                    plugin.getLogger().info("[DEBUG] PlayerInteractListener: Selection already completed for player " + player.getName() + ", ignoring click");
                }
                return;
            }
            
            Location clickedLocation;
            if (event.getClickedBlock() != null) {
                // If clicked on a block, use that block's location
                clickedLocation = event.getClickedBlock().getLocation();
            } else {
                // If clicked in air, use raycast to find the block the player is looking at
                clickedLocation = getTargetBlockLocation(player);
            }
            
            if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                plugin.getLogger().info("[DEBUG] PlayerInteractListener: Clicked location: " + clickedLocation.getBlockX() + ", " + clickedLocation.getBlockY() + ", " + clickedLocation.getBlockZ());
            }
            
            // Set first position if not set
            if (selectionData.getPos1() == null) {
                if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                    plugin.getLogger().info("[DEBUG] PlayerInteractListener: Setting pos1 for player " + player.getName());
                }
                
                selectionData.setPos1(clickedLocation);
                
                // Update WorldEdit selection
                plugin.getWorldEditUtils().setSelection(player, clickedLocation, clickedLocation);
                
                if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                    plugin.getLogger().info("[DEBUG] WorldEdit pos1 set via API");
                }
                
                // Execute trigger
                commandTriggerManager.executeTrigger("first-point-selected", player, null);
                
                if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                    plugin.getLogger().info("[DEBUG] Player " + player.getName() + " selected pos1 at " + 
                        clickedLocation.getBlockX() + ", " + clickedLocation.getBlockY() + ", " + clickedLocation.getBlockZ());
                }
                
                if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                    plugin.getLogger().info("[DEBUG] PlayerInteractListener: Executing first-point-selected trigger");
                }
                
                // Message is sent by CommandTriggerManager, no need to send here
                
                if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                    plugin.getLogger().info("[DEBUG] PlayerInteractListener: Sending message to player");
                }
                
            } else {
                // Set second position and complete selection
                if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                    plugin.getLogger().info("[DEBUG] PlayerInteractListener: Setting pos2 and completing selection for player " + player.getName());
                }
                
                selectionData.setPos2(clickedLocation);
                
                // Update WorldEdit selection
                plugin.getWorldEditUtils().setSelection(player, selectionData.getPos1(), clickedLocation);
                
                if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                    plugin.getLogger().info("[DEBUG] WorldEdit pos2 set via API");
                }
                
                if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                    plugin.getLogger().info("[DEBUG] Player " + player.getName() + " selected pos2 at " + 
                        clickedLocation.getBlockX() + ", " + clickedLocation.getBlockY() + ", " + clickedLocation.getBlockZ());
                }
                
                // Complete selection
                SelectionManager.SelectionData completedSelection = selectionManager.completeSelection(player);
                
                if (completedSelection != null) {
                    if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                        plugin.getLogger().info("[DEBUG] Selection completed for player " + player.getName());
                    }
                    
                    // Execute triggers with delay
                    if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                        plugin.getLogger().info("[DEBUG] PlayerInteractListener: Executing triggers with delay");
                    }
                    
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                            plugin.getLogger().info("[DEBUG] PlayerInteractListener: Executing second-point-selected trigger");
                        }
                        commandTriggerManager.executeTrigger("second-point-selected", player, null);
                        
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                                plugin.getLogger().info("[DEBUG] PlayerInteractListener: Executing selection-completed trigger");
                            }
                            commandTriggerManager.executeTrigger("selection-completed", player, null);
                            
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                                    plugin.getLogger().info("[DEBUG] PlayerInteractListener: Sending completion message to player");
                                }
                                // Message is sent by CommandTriggerManager, no need to send here
                            }, plugin.getConfigManager().getMessageDelay());
                        }, plugin.getConfigManager().getMessageDelay());
                    }, plugin.getConfigManager().getMessageDelay());
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при обработке SHIFT+LEFT_CLICK для игрока " + player.getName(), e);
        }
    }
    
    /**
     * Get the block location that the player is looking at using raycast
     * 
     * @param player Player
     * @return Location of the block the player is looking at, or player location if none found
     */
    private Location getTargetBlockLocation(Player player) {
        try {
            // Get player's eye location and direction
            Location eyeLocation = player.getEyeLocation();
            Vector direction = eyeLocation.getDirection();
            
            // Perform raycast to find the block the player is looking at
            RayTraceResult rayTrace = player.getWorld().rayTraceBlocks(eyeLocation, direction, 100.0, 
                org.bukkit.FluidCollisionMode.NEVER, true);
            
            if (rayTrace != null && rayTrace.getHitBlock() != null) {
                Block hitBlock = rayTrace.getHitBlock();
                if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                    plugin.getLogger().info("[DEBUG] Raycast hit block at: " + hitBlock.getX() + ", " + hitBlock.getY() + ", " + hitBlock.getZ());
                }
                return hitBlock.getLocation();
            } else {
                // If no block found, use player's location as fallback
                if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                    plugin.getLogger().info("[DEBUG] Raycast found no block, using player location as fallback");
                }
                return player.getLocation();
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка при определении блока, на который смотрит игрок " + player.getName(), e);
            return player.getLocation();
        }
    }
}
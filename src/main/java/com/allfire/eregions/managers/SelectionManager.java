package com.allfire.eregions.managers;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.utils.WorldEditUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Selection Manager
 * 
 * Handles region selection process for players
 * Manages WorldEdit selection integration
 * 
 * @author AllF1RE
 */
public class SelectionManager {
    
    private final ERegions plugin;
    private WorldEditUtils worldEditUtils;
    private final Map<Player, SelectionData> activeSelections;
    private final Map<Player, SelectionData> waitingForName;
    
    public SelectionManager(ERegions plugin) {
        this.plugin = plugin;
        this.worldEditUtils = null; // Will be set later
        this.activeSelections = new HashMap<>();
        this.waitingForName = new HashMap<>();
    }
    
    /**
     * Set WorldEditUtils after initialization
     */
    public void setWorldEditUtils(WorldEditUtils worldEditUtils) {
        this.worldEditUtils = worldEditUtils;
    }
    
    /**
     * Start selection process for player
     * 
     * @param player Player starting selection
     * @param type Type of selection (create, etc.)
     */
    public void startSelection(Player player, String type) {
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] SelectionManager.startSelection called for player " + player.getName() + " with type: " + type);
        }
        
        try {
            // Check if player already has active selection
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] SelectionManager: Checking if player has active selection");
            }
            if (hasActiveSelection(player)) {
                plugin.getLogger().warning("[DEBUG] SelectionManager: Player " + player.getName() + " already has active selection!");
                return;
            }
            
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] SelectionManager: No active selection found, creating new selection data");
                }
            
            // Create selection data
            SelectionData selectionData = new SelectionData(player, type);
            activeSelections.put(player, selectionData);
            
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] SelectionManager: Selection data created and stored");
            }
            
            // Enable WorldEdit selection
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] SelectionManager: Enabling WorldEdit selection");
            }
            if (worldEditUtils != null) {
                worldEditUtils.enableSelection(player);
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] SelectionManager: WorldEdit selection enabled");
                }
            } else {
                plugin.getLogger().warning("[DEBUG] SelectionManager: WorldEditUtils is null!");
            }
            
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] Selection started for player " + player.getName() + " (type: " + type + ")");
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[DEBUG] SelectionManager: Error starting selection for player " + player.getName(), e);
            e.printStackTrace();
        }
    }
    
    /**
     * Complete selection process
     * 
     * @param player Player completing selection
     * @return Selection data or null if failed
     */
    public SelectionData completeSelection(Player player) {
        try {
            if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] SelectionManager.completeSelection called for player: " + player.getName());
        }
            
            SelectionData selectionData = activeSelections.get(player);
            if (selectionData == null) {
                plugin.getLogger().warning("Игрок " + player.getName() + " не имеет активного выделения!");
                return null;
            }
            
            // Check if we have both positions
            if (selectionData.getPos1() == null || selectionData.getPos2() == null) {
                plugin.getLogger().warning("Неполное выделение для игрока " + player.getName());
                return null;
            }
            
            // Update selection data
            selectionData.setCompleted(true);
            
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] Selection completed for player " + player.getName() + 
                    " from " + selectionData.getPos1().getBlockX() + "," + selectionData.getPos1().getBlockY() + "," + selectionData.getPos1().getBlockZ() +
                    " to " + selectionData.getPos2().getBlockX() + "," + selectionData.getPos2().getBlockY() + "," + selectionData.getPos2().getBlockZ());
            }
            
            // Move to waiting for name
            waitingForName.put(player, selectionData);
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] Player " + player.getName() + " moved to waiting for name");
            }
            
            // DON'T clear the active selection yet - keep it for WorldEdit/SelectionVisualizer
            // It will be cleared when region is created or cancelled
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] Selection marked as completed but kept in activeSelections for WorldEdit compatibility");
            }
            
            return selectionData;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при завершении выделения для игрока " + player.getName(), e);
            return null;
        }
    }
    
    /**
     * Cancel selection process
     * 
     * @param player Player canceling selection
     */
    public void cancelSelection(Player player) {
        try {
            SelectionData selectionData = activeSelections.remove(player);
            if (selectionData != null) {
                // Disable WorldEdit selection
                worldEditUtils.disableSelection(player);
                
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("Выделение отменено для игрока " + player.getName());
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при отмене выделения для игрока " + player.getName(), e);
        }
    }
    
    /**
     * Check if player has active selection
     * 
     * @param player Player to check
     * @return True if has active selection
     */
    public boolean hasActiveSelection(Player player) {
        return activeSelections.containsKey(player);
    }
    
    /**
     * Get active selection data
     * 
     * @param player Player
     * @return Selection data or null
     */
    public SelectionData getActiveSelection(Player player) {
        return activeSelections.get(player);
    }
    
    /**
     * Clear selection for player (removes from activeSelections and waitingForName)
     * Use this only when you want to completely remove the selection
     * 
     * @param player Player to clear selection for
     */
    public void clearSelection(Player player) {
        try {
            SelectionData selectionData = activeSelections.remove(player);
            if (selectionData != null) {
                // DON'T disable WorldEdit selection - keep it for /svis we and /svis wg
                // The WorldEdit selection should remain for SelectionVisualizer compatibility
                if (plugin.getConfigManager().isSelectionLoggingEnabled()) {
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] Selection cleared for player " + player.getName() + " (WorldEdit selection preserved)");
                    }
                }
            }
            
            // Also clear from waiting for name
            waitingForName.remove(player);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при очистке выделения для игрока " + player.getName(), e);
        }
    }
    
    /**
     * Force clear selection for player (use only when necessary)
     * This completely removes the selection and should be used sparingly
     * 
     * @param player Player to force clear selection for
     */
    public void forceClearSelection(Player player) {
        clearSelection(player);
    }
    
    /**
     * Check if player is waiting for region name input
     * 
     * @param player Player to check
     * @return true if player is waiting for name input
     */
    public boolean isWaitingForName(Player player) {
        return waitingForName.containsKey(player);
    }
    
    /**
     * Get selection data for player waiting for name
     * 
     * @param player Player
     * @return SelectionData or null
     */
    public SelectionData getWaitingForName(Player player) {
        return waitingForName.get(player);
    }
    
    /**
     * Remove player from waiting for name
     * 
     * @param player Player to remove
     */
    public void removeWaitingForName(Player player) {
        waitingForName.remove(player);
    }
    
    /**
     * Clean up inactive selections
     */
    public void cleanupInactiveSelections() {
        try {
            activeSelections.entrySet().removeIf(entry -> {
                Player player = entry.getKey();
                if (!player.isOnline()) {
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("Очищаем неактивное выделение для игрока " + player.getName());
                    }
                    return true;
                }
                return false;
            });
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при очистке неактивных выделений", e);
        }
    }
    
    /**
     * Selection data class
     */
    public static class SelectionData {
        private final Player player;
        private final String type;
        private final long startTime;
        private Location pos1;
        private Location pos2;
        private boolean completed;
        
        public SelectionData(Player player, String type) {
            this.player = player;
            this.type = type;
            this.startTime = System.currentTimeMillis();
            this.completed = false;
        }
        
        public Player getPlayer() {
            return player;
        }
        
        public String getType() {
            return type;
        }
        
        public long getStartTime() {
            return startTime;
        }
        
        public Location getPos1() {
            return pos1;
        }
        
        public void setPos1(Location pos1) {
            this.pos1 = pos1;
        }
        
        public Location getPos2() {
            return pos2;
        }
        
        public void setPos2(Location pos2) {
            this.pos2 = pos2;
        }
        
        public boolean isCompleted() {
            return completed;
        }
        
        public void setCompleted(boolean completed) {
            this.completed = completed;
        }
    }
}

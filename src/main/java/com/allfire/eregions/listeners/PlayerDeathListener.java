package com.allfire.eregions.listeners;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.managers.SelectionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.logging.Level;

/**
 * Player Death Listener
 *
 * Handles player death events to clean up active region creation processes
 * Resets selection state when player dies
 *
 * @author AllF1RE
 */
public class PlayerDeathListener implements Listener {

    private final ERegions plugin;
    private final SelectionManager selectionManager;

    public PlayerDeathListener(ERegions plugin) {
        this.plugin = plugin;
        this.selectionManager = plugin.getSelectionManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        try {
            Player player = event.getEntity();
            
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] PlayerDeathListener: Player " + player.getName() + " died");
            }
            
            // Check if player has active selection or is waiting for name
            boolean hasActiveSelection = selectionManager.hasActiveSelection(player);
            boolean isWaitingForName = selectionManager.isWaitingForName(player);
            
            if (hasActiveSelection || isWaitingForName) {
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] PlayerDeathListener: Cleaning up active region creation for player " + player.getName());
                }
                
                // Clear selection completely - this will remove from both activeSelections and waitingForName
                selectionManager.clearSelection(player);
                
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] PlayerDeathListener: Active region creation cleared for player " + player.getName());
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка в PlayerDeathListener", e);
        }
    }
}


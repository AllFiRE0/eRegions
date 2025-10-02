package com.allfire.eregions.listeners;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.managers.BoundaryDetectionManager;
import com.allfire.eregions.managers.CommandTriggerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.logging.Level;

/**
 * Boundary Detection Listener
 * 
 * Handles player movement events for boundary detection
 * Triggers commands when players approach region boundaries
 * 
 * @author AllF1RE
 */
public class BoundaryDetectionListener implements Listener {
    
    private final ERegions plugin;
    private final BoundaryDetectionManager boundaryDetectionManager;
    private final CommandTriggerManager commandTriggerManager;
    
    public BoundaryDetectionListener(ERegions plugin) {
        this.plugin = plugin;
        this.boundaryDetectionManager = plugin.getBoundaryDetectionManager();
        this.commandTriggerManager = plugin.getCommandTriggerManager();
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        try {
            Player player = event.getPlayer();
            
            // Check if boundary detection is enabled
            if (!plugin.getConfigManager().isBoundaryDetectionEnabled()) {
                return;
            }
            
            // Only check every 20 ticks (1 second) to reduce spam
            if (player.getTicksLived() % 20 != 0) {
                return;
            }
            
            // Check if player has view permission
            if (!player.hasPermission("eregions.region.view")) {
                return;
            }
            
            // Check for boundary proximity
            boundaryDetectionManager.checkBoundary(player);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка в BoundaryDetectionListener", e);
        }
    }
}

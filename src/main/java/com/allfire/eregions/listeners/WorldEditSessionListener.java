package com.allfire.eregions.listeners;

import com.allfire.eregions.ERegions;
import com.sk89q.worldedit.event.platform.PlayerInputEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.logging.Level;

/**
 * WorldEdit Session Listener
 * 
 * Listens for WorldEdit session changes to trigger SelectionVisualizer updates
 */
public class WorldEditSessionListener implements Listener {
    
    private final ERegions plugin;
    
    public WorldEditSessionListener(ERegions plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        try {
            String command = event.getMessage().toLowerCase();
            Player player = event.getPlayer();
            
            // Check if it's a WorldEdit selection command
            if (command.startsWith("//pos1") || command.startsWith("//pos2") || command.startsWith("//sel")) {
                plugin.getLogger().info("[DEBUG] WorldEditSessionListener: Detected WorldEdit selection command: " + command);
                
                // Schedule svis we command with delay
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    plugin.getLogger().info("[DEBUG] WorldEditSessionListener: Executing svis we for player: " + player.getName());
                    player.performCommand("svis we");
                }, 5L); // 5 ticks delay
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка в WorldEditSessionListener.onPlayerCommand", e);
        }
    }
}


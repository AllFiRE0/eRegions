package com.allfire.eregions.managers;

import com.allfire.eregions.ERegions;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Boundary Detection Manager
 * 
 * Handles region boundary detection
 * Manages player movement near region borders
 * 
 * @author AllF1RE
 */
public class BoundaryDetectionManager {
    
    private final ERegions plugin;
    private final Map<String, Long> permissionCheckCache;
    private final Map<String, String> lastTriggeredRegion; // Player -> Region mapping
    private final Map<String, Long> lastTriggerTime; // Player -> Last trigger time
    private final long CACHE_DURATION = 30000; // 30 seconds
    
    public BoundaryDetectionManager(ERegions plugin) {
        this.plugin = plugin;
        this.permissionCheckCache = new ConcurrentHashMap<>();
        this.lastTriggeredRegion = new ConcurrentHashMap<>();
        this.lastTriggerTime = new ConcurrentHashMap<>();
    }
    
    /**
     * Initialize boundary detection
     */
    public void initialize() {
        // Implementation for boundary detection
    }
    
    /**
     * Start tracking player for boundary detection
     * 
     * @param player Player to track
     */
    public void startTracking(Player player) {
        // Implementation for player tracking
    }
    
    /**
     * Stop tracking player for boundary detection
     * 
     * @param player Player to stop tracking
     */
    public void stopTracking(Player player) {
        // Implementation for stopping player tracking
    }
    
    /**
     * Check if player is near region boundary
     * 
     * @param player Player to check
     */
    public void checkBoundary(Player player) {
        try {
            // Check if player has view permission (without operator bypass)
            if (!player.hasPermission("eregions.region.view")) {
                return;
            }
            
            Location playerLocation = player.getLocation();
            
            // Get distance and cooldown from boundary-triggers config
            Map<String, Object> boundaryTrigger = plugin.getConfigManager().getBoundaryTrigger("boundary-enter");
            double detectionDistance = 5.0; // Default distance
            double cooldownSeconds = 3.0; // Default cooldown
            
            if (boundaryTrigger != null) {
                if (boundaryTrigger.containsKey("distance")) {
                    detectionDistance = ((Number) boundaryTrigger.get("distance")).doubleValue();
                }
                if (boundaryTrigger.containsKey("cooldown")) {
                    cooldownSeconds = ((Number) boundaryTrigger.get("cooldown")).doubleValue();
                }
            }
            
            // Check cooldown
            String playerName = player.getName();
            long currentTime = System.currentTimeMillis();
            Long lastTrigger = lastTriggerTime.get(playerName);
            if (lastTrigger != null && (currentTime - lastTrigger) < (cooldownSeconds * 1000)) {
                return; // Still in cooldown
            }
            
            // Get all regions in the world
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
            if (regions == null) {
                return;
            }
            
            // Check each region
            for (ProtectedRegion region : regions.getRegions().values()) {
                // Check if region has regionborder-view flag enabled
                var flagState = region.getFlag(plugin.getRegionBorderViewFlag());
                if (flagState == null || !flagState.equals(com.sk89q.worldguard.protection.flags.StateFlag.State.ALLOW)) {
                    continue; // Skip regions without the flag
                }
                
                // Check if player is within detection distance of region boundary
                String regionId = region.getId();
                if (isPlayerNearRegionBoundary(playerLocation, region, detectionDistance)) {
                    // Check if we already triggered for this region recently
                    String lastRegion = lastTriggeredRegion.get(playerName);
                    if (!regionId.equals(lastRegion)) {
                        // Trigger boundary enter command
                        plugin.getCommandTriggerManager().executeTrigger("boundary-enter", player, regionId);
                        lastTriggeredRegion.put(playerName, regionId);
                        lastTriggerTime.put(playerName, currentTime);
                    }
                    break; // Only trigger for the closest region
                } else {
                    // Player moved away from region, clear the cache
                    if (regionId.equals(lastTriggeredRegion.get(playerName))) {
                        lastTriggeredRegion.remove(playerName);
                    }
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при проверке границ для игрока " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Check if player is near region boundary
     * 
     * @param playerLocation Player location
     * @param region Region to check
     * @param detectionDistance Detection distance in blocks
     * @return True if player is near boundary
     */
    private boolean isPlayerNearRegionBoundary(Location playerLocation, ProtectedRegion region, double detectionDistance) {
        try {
            // Get region bounds
            var min = region.getMinimumPoint();
            var max = region.getMaximumPoint();
            
            // Calculate distance to region boundary
            double distanceToRegion = calculateDistanceToRegion(
                playerLocation.getX(), playerLocation.getY(), playerLocation.getZ(),
                min.x(), min.y(), min.z(),
                max.x(), max.y(), max.z()
            );
            
            return distanceToRegion <= detectionDistance;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка при расчете расстояния до региона: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Calculate distance from point to region boundary
     */
    private double calculateDistanceToRegion(double px, double py, double pz, 
                                           double minX, double minY, double minZ,
                                           double maxX, double maxY, double maxZ) {
        // Check if player is inside the region
        boolean insideX = px >= minX && px <= maxX;
        boolean insideY = py >= minY && py <= maxY;
        boolean insideZ = pz >= minZ && pz <= maxZ;
        
        if (insideX && insideY && insideZ) {
            // Player is inside region, calculate distance to nearest boundary
            double distToMinX = Math.abs(px - minX);
            double distToMaxX = Math.abs(px - maxX);
            double distToMinY = Math.abs(py - minY);
            double distToMaxY = Math.abs(py - maxY);
            double distToMinZ = Math.abs(pz - minZ);
            double distToMaxZ = Math.abs(pz - maxZ);
            
            // Return the minimum distance to any boundary
            return Math.min(Math.min(distToMinX, distToMaxX), 
                   Math.min(Math.min(distToMinY, distToMaxY), 
                   Math.min(distToMinZ, distToMaxZ)));
        } else {
            // Player is outside region, calculate distance to nearest point on boundary
            double dx = Math.max(0, Math.max(minX - px, px - maxX));
            double dy = Math.max(0, Math.max(minY - py, py - maxY));
            double dz = Math.max(0, Math.max(minZ - pz, pz - maxZ));
            
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
    }
    
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        // Implementation for cleanup
    }
}

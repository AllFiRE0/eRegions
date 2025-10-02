package com.allfire.eregions.managers;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.utils.WorldGuardUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * Region Manager
 * 
 * Handles region creation, deletion, and management
 * Integrates with WorldGuard for region operations
 * 
 * @author AllF1RE
 */
public class RegionManager {
    
    private final ERegions plugin;
    private WorldGuardUtils worldGuardUtils;
    private final Map<String, RegionData> regionCache;
    
    public RegionManager(ERegions plugin) {
        this.plugin = plugin;
        this.worldGuardUtils = null; // Will be set later
        this.regionCache = new HashMap<>();
    }
    
    /**
     * Set WorldGuardUtils after initialization
     */
    public void setWorldGuardUtils(WorldGuardUtils worldGuardUtils) {
        this.worldGuardUtils = worldGuardUtils;
    }
    
    /**
     * Create a new region
     * 
     * @param player Player creating the region
     * @param regionName Name of the region
     * @param pos1 First position
     * @param pos2 Second position
     * @return True if successful
     */
    public boolean createRegion(Player player, String regionName, Location pos1, Location pos2) {
        try {
            // Validate region name
            if (regionName == null || regionName.trim().isEmpty()) {
                plugin.getLogger().warning("Название региона не может быть пустым!");
                return false;
            }
            
            // Check if region already exists
            if (worldGuardUtils.regionExists(player.getWorld(), regionName)) {
                plugin.getLogger().warning("Регион '" + regionName + "' уже существует!");
                return false;
            }
            
            // Check for overlapping regions
            List<String> overlappingRegions = worldGuardUtils.getOverlappingRegions(player.getWorld(), pos1, pos2);
            if (!overlappingRegions.isEmpty()) {
                plugin.getLogger().warning("Выделенная область пересекается с существующими регионами: " + String.join(", ", overlappingRegions));
                return false;
            }
            
            // Create region in WorldGuard
            boolean success = worldGuardUtils.createRegion(player.getWorld(), regionName, pos1, pos2, player);
            
            if (success) {
                // Cache region data
                RegionData regionData = new RegionData(regionName, player.getName(), pos1, pos2);
                regionCache.put(regionName, regionData);
                
                // Log creation
                plugin.getLogger().info("Регион '" + regionName + "' создан игроком " + player.getName() + " в мире " + player.getWorld().getName());
                
                return true;
            } else {
                plugin.getLogger().warning("Не удалось создать регион '" + regionName + "'");
                return false;
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при создании региона '" + regionName + "'", e);
            return false;
        }
    }
    
    /**
     * Remove a region
     * 
     * @param player Player removing the region
     * @param regionName Name of the region
     * @return True if successful
     */
    public boolean removeRegion(Player player, String regionName) {
        try {
            // Check if region exists
            if (!worldGuardUtils.regionExists(player.getWorld(), regionName)) {
                plugin.getLogger().warning("Регион '" + regionName + "' не найден!");
                return false;
            }
            
            // Check if player is owner
            if (!worldGuardUtils.isRegionOwner(player, regionName)) {
                plugin.getLogger().warning("Игрок " + player.getName() + " не является владельцем региона '" + regionName + "'");
                return false;
            }
            
            // Remove region from WorldGuard
            boolean success = worldGuardUtils.removeRegion(player.getWorld(), regionName);
            
            if (success) {
                // Remove from cache
                regionCache.remove(regionName);
                
                // Log removal
                plugin.getLogger().info("Регион '" + regionName + "' удален игроком " + player.getName());
                
                return true;
            } else {
                plugin.getLogger().warning("Не удалось удалить регион '" + regionName + "'");
                return false;
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при удалении региона '" + regionName + "'", e);
            return false;
        }
    }
    
    /**
     * Get region data
     * 
     * @param regionName Name of the region
     * @return Region data or null if not found
     */
    public RegionData getRegionData(String regionName) {
        return regionCache.get(regionName);
    }
    
    /**
     * Get all regions for a player
     * 
     * @param player Player
     * @return List of region names
     */
    public List<String> getPlayerRegions(Player player) {
        return worldGuardUtils.getPlayerRegions(player);
    }
    
    /**
     * Check if player is owner of region
     * 
     * @param player Player
     * @param regionName Region name
     * @return True if owner
     */
    public boolean isRegionOwner(Player player, String regionName) {
        return worldGuardUtils.isRegionOwner(player, regionName);
    }
    
    /**
     * Check if player is member of region
     * 
     * @param player Player
     * @param regionName Region name
     * @return True if member
     */
    public boolean isRegionMember(Player player, String regionName) {
        return worldGuardUtils.isRegionMember(player, regionName);
    }
    
    /**
     * Save region data
     */
    public void saveData() {
        // Implementation for saving region data to file
        // This could be used for additional data not stored in WorldGuard
    }
    
    /**
     * Region data class
     */
    public static class RegionData {
        private final String name;
        private final String owner;
        private final Location pos1;
        private final Location pos2;
        private final long createdTime;
        
        public RegionData(String name, String owner, Location pos1, Location pos2) {
            this.name = name;
            this.owner = owner;
            this.pos1 = pos1;
            this.pos2 = pos2;
            this.createdTime = System.currentTimeMillis();
        }
        
        public String getName() {
            return name;
        }
        
        public String getOwner() {
            return owner;
        }
        
        public Location getPos1() {
            return pos1;
        }
        
        public Location getPos2() {
            return pos2;
        }
        
        public long getCreatedTime() {
            return createdTime;
        }
    }
}

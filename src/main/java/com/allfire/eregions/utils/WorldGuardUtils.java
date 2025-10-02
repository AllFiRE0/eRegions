package com.allfire.eregions.utils;

import com.allfire.eregions.ERegions;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * WorldGuard Utilities
 * 
 * Handles WorldGuard integration and operations
 * Manages region creation, deletion, and management
 * 
 * @author AllF1RE
 */
public class WorldGuardUtils {
    
    private final ERegions plugin;
    
    public WorldGuardUtils(ERegions plugin) {
        this.plugin = plugin;
    }
    
    
    /**
     * Check if region exists
     * 
     * @param world World
     * @param regionName Region name
     * @return True if exists
     */
    public boolean regionExists(World world, String regionName) {
        try {
            if (!isWorldGuardAvailable()) {
                return false;
            }
            
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(world));
            
            if (regions == null) {
                return false;
            }
            
            return regions.hasRegion(regionName);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при проверке существования региона: " + regionName, e);
            return false;
        }
    }
    
    /**
     * Create region
     * 
     * @param world World
     * @param regionName Region name
     * @param pos1 First position
     * @param pos2 Second position
     * @param owner Region owner
     * @return True if successful
     */
    public boolean createRegion(World world, String regionName, Location pos1, Location pos2, Player owner) {
        try {
            if (!isWorldGuardAvailable()) {
                plugin.getLogger().warning("WorldGuard недоступен для создания региона!");
                return false;
            }
            
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(world));
            
            if (regions == null) {
                plugin.getLogger().warning("Не удалось получить RegionManager для мира: " + world.getName());
                return false;
            }
            
            // Check if region already exists
            if (regions.hasRegion(regionName)) {
                plugin.getLogger().warning("Регион " + regionName + " уже существует!");
                return false;
            }
            
            // Convert Bukkit locations to WorldEdit BlockVector3
            BlockVector3 min = BlockVector3.at(
                Math.min(pos1.getBlockX(), pos2.getBlockX()),
                Math.min(pos1.getBlockY(), pos2.getBlockY()),
                Math.min(pos1.getBlockZ(), pos2.getBlockZ())
            );
            
            BlockVector3 max = BlockVector3.at(
                Math.max(pos1.getBlockX(), pos2.getBlockX()),
                Math.max(pos1.getBlockY(), pos2.getBlockY()),
                Math.max(pos1.getBlockZ(), pos2.getBlockZ())
            );
            
            // Create the region
            ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionName, min, max);
            
            // Set priority from config
            int priority = plugin.getConfigManager().getDefaultPriority();
            region.setPriority(priority);
            
            // Set owner
            if (owner != null) {
                region.getOwners().addPlayer(owner.getUniqueId());
            }
            
            // Apply default flags from config
            List<String> defaultFlags = plugin.getConfigManager().getDefaultFlags();
            for (String flagString : defaultFlags) {
                String[] parts = flagString.split("=");
                if (parts.length == 2) {
                    String flagName = parts[0].trim();
                    String flagValue = parts[1].trim();
                    
                    // Add the flag to region
                    addRegionFlag(world, regionName, flagName, flagValue);
                }
            }
            
            // Add our custom regionborder-view flag
            addRegionFlag(world, regionName, "regionborder-view", "allow");
            
            // Add region to manager
            regions.addRegion(region);
            
            // Set creator flag
            if (owner != null) {
                setRegionCreator(region, owner.getName());
            }
            
            // Save changes to WorldGuard
            regions.save();
            
            plugin.getLogger().info("Регион " + regionName + " успешно создан!");
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при создании региона: " + regionName, e);
            return false;
        }
    }
    
    /**
     * Remove region
     * 
     * @param world World
     * @param regionName Region name
     * @return True if successful
     */
    public boolean removeRegion(World world, String regionName) {
        try {
            if (!isWorldGuardAvailable()) {
                plugin.getLogger().warning("WorldGuard недоступен для удаления региона!");
                return false;
            }
            
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(world));
            
            if (regions == null) {
                plugin.getLogger().warning("Не удалось получить RegionManager для мира: " + world.getName());
                return false;
            }
            
            // Check if region exists
            if (!regions.hasRegion(regionName)) {
                plugin.getLogger().warning("Регион " + regionName + " не найден!");
                return false;
            }
            
            // Remove region
            regions.removeRegion(regionName);
            
            plugin.getLogger().info("Регион " + regionName + " успешно удален!");
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при удалении региона: " + regionName, e);
            return false;
        }
    }
    
    /**
     * Check if player is region owner
     * 
     * @param player Player
     * @param regionName Region name
     * @return True if owner
     */
    public boolean isRegionOwner(Player player, String regionName) {
        try {
            if (!isWorldGuardAvailable()) {
                return false;
            }
            
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
            
            if (regions == null) {
                return false;
            }
            
            ProtectedRegion region = regions.getRegion(regionName);
            if (region == null) {
                return false;
            }
            
            return region.getOwners().contains(player.getUniqueId());
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при проверке владения регионом: " + regionName, e);
            return false;
        }
    }
    
    /**
     * Check if player is region member
     * 
     * @param player Player
     * @param regionName Region name
     * @return True if member
     */
    public boolean isRegionMember(Player player, String regionName) {
        try {
            // Implementation for checking membership
            
            return false; // Placeholder
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при проверке членства в регионе: " + regionName, e);
            return false;
        }
    }
    
    /**
     * Get player's regions (both owned and member)
     * 
     * @param player Player
     * @return List of region names
     */
    public List<String> getPlayerRegions(Player player) {
        try {
            if (!isWorldGuardAvailable()) {
                return new ArrayList<>();
            }
            
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
            
            if (regions == null) {
                return new ArrayList<>();
            }
            
            List<String> playerRegions = new ArrayList<>();
            for (ProtectedRegion region : regions.getRegions().values()) {
                if (region.getOwners().contains(player.getUniqueId()) || region.getMembers().contains(player.getUniqueId())) {
                    playerRegions.add(region.getId());
                }
            }
            return playerRegions;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при получении регионов игрока: " + player.getName(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get player's owned regions only
     * 
     * @param player Player
     * @return List of region names where player is owner
     */
    public List<String> getPlayerOwnedRegions(Player player) {
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] ========== WorldGuardUtils.getPlayerOwnedRegions ==========");
            plugin.getLogger().info("[DEBUG] Player: " + player.getName());
            plugin.getLogger().info("[DEBUG] Player UUID: " + player.getUniqueId());
            plugin.getLogger().info("[DEBUG] Player world: " + player.getWorld().getName());
        }
        
        try {
            if (!isWorldGuardAvailable()) {
                plugin.getLogger().warning("[DEBUG] WorldGuard is not available!");
                return new ArrayList<>();
            }
            
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] WorldGuard is available, getting region container...");
            }
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] Region container: " + (container != null ? "found" : "null"));
            }
            
            RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] Region manager: " + (regions != null ? "found" : "null"));
            }
            
            if (regions == null) {
                plugin.getLogger().warning("[DEBUG] No region manager found for world: " + player.getWorld().getName());
                return new ArrayList<>();
            }
            
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] Getting all regions in world...");
            }
            int totalRegions = regions.getRegions().size();
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] Total regions in world: " + totalRegions);
            }
            
            List<String> ownedRegions = new ArrayList<>();
            int checkedRegions = 0;
            
            for (ProtectedRegion region : regions.getRegions().values()) {
                checkedRegions++;
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] Checking region " + checkedRegions + "/" + totalRegions + ": " + region.getId());
                    plugin.getLogger().info("[DEBUG] Region owners: " + region.getOwners().getPlayers());
                    plugin.getLogger().info("[DEBUG] Region members: " + region.getMembers().getPlayers());
                }
                
                boolean isOwner = region.getOwners().contains(player.getUniqueId());
                boolean isMember = region.getMembers().contains(player.getUniqueId());
                
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] Player is owner: " + isOwner);
                    plugin.getLogger().info("[DEBUG] Player is member: " + isMember);
                }
                
                if (isOwner) {
                    ownedRegions.add(region.getId());
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] Added region to owned list: " + region.getId());
                    }
                }
            }
            
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] Found " + ownedRegions.size() + " owned regions for player " + player.getName() + ": " + ownedRegions);
                plugin.getLogger().info("[DEBUG] ========== WorldGuardUtils.getPlayerOwnedRegions END ==========");
            }
            return ownedRegions;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[DEBUG] Error getting owned regions for player " + player.getName(), e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Add member to region
     * 
     * @param world World
     * @param regionName Region name
     * @param player Player to add
     * @return True if successful
     */
    public boolean addRegionMember(World world, String regionName, Player player) {
        try {
            if (!isWorldGuardAvailable()) {
                return false;
            }

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(world));

            if (regions == null) {
                return false;
            }

            ProtectedRegion region = regions.getRegion(regionName);
            if (region == null) {
                return false;
            }

            region.getMembers().addPlayer(player.getUniqueId());
            regions.save(); // Save changes
            return true;

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при добавлении участника в регион: " + regionName, e);
            return false;
        }
    }
    
    /**
     * Remove member from region
     * 
     * @param world World
     * @param regionName Region name
     * @param player Player to remove
     * @return True if successful
     */
    public boolean removeRegionMember(World world, String regionName, Player player) {
        try {
            if (!isWorldGuardAvailable()) {
                return false;
            }

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(world));

            if (regions == null) {
                return false;
            }

            ProtectedRegion region = regions.getRegion(regionName);
            if (region == null) {
                return false;
            }

            region.getMembers().removePlayer(player.getUniqueId());
            
            // Set expelled flag
            setRegionExpelled(region, player.getName());
            
            regions.save(); // Save changes
            return true;

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при удалении участника из региона: " + regionName, e);
            return false;
        }
    }
    
    /**
     * Add owner to region
     * 
     * @param world World
     * @param regionName Region name
     * @param player Player to add
     * @return True if successful
     */
    public boolean addRegionOwner(World world, String regionName, Player player) {
        try {
            if (!isWorldGuardAvailable()) {
                return false;
            }

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(world));

            if (regions == null) {
                return false;
            }

            ProtectedRegion region = regions.getRegion(regionName);
            if (region == null) {
                return false;
            }

            region.getOwners().addPlayer(player.getUniqueId());
            regions.save(); // Save changes
            return true;

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при добавлении владельца в регион: " + regionName, e);
            return false;
        }
    }
    
    /**
     * Remove owner from region
     * 
     * @param world World
     * @param regionName Region name
     * @param player Player to remove
     * @return True if successful
     */
    public boolean removeRegionOwner(World world, String regionName, Player player) {
        try {
            if (!isWorldGuardAvailable()) {
                return false;
            }

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(world));

            if (regions == null) {
                return false;
            }

            ProtectedRegion region = regions.getRegion(regionName);
            if (region == null) {
                return false;
            }

            region.getOwners().removePlayer(player.getUniqueId());
            
            // Set expelled flag
            setRegionExpelled(region, player.getName());
            
            regions.save(); // Save changes
            return true;

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при удалении владельца из региона: " + regionName, e);
            return false;
        }
    }
    
    /**
     * Add flag to region
     * 
     * @param world World
     * @param regionName Region name
     * @param flagName Flag name
     * @param value Flag value
     * @return True if successful
     */
    public boolean addRegionFlag(World world, String regionName, String flagName, String value) {
        try {
            if (!isWorldGuardAvailable()) {
                return false;
            }

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(world));

            if (regions == null) {
                return false;
            }

            ProtectedRegion region = regions.getRegion(regionName);
            if (region == null) {
                return false;
            }

            Flag<?> flag = WorldGuard.getInstance().getFlagRegistry().get(flagName);
            if (flag == null) {
                plugin.getLogger().warning("Флаг " + flagName + " не найден!");
                return false;
            }

            // Handle different flag types
            if (flag instanceof StateFlag) {
                StateFlag stateFlag = (StateFlag) flag;
                StateFlag.State state;
                
                // Parse the value to StateFlag.State
                if (value.equalsIgnoreCase("allow") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1")) {
                    state = StateFlag.State.ALLOW;
                } else if (value.equalsIgnoreCase("deny") || value.equalsIgnoreCase("false") || value.equalsIgnoreCase("0")) {
                    state = StateFlag.State.DENY;
                } else {
                    plugin.getLogger().warning("Неверное значение флага " + value + " для флага " + flagName + ". Используйте allow/deny");
                    return false;
                }
                
                region.setFlag(stateFlag, state);
            } else if (flagName.equals("regionborder-view")) {
                // Handle our custom regionborder-view flag
                plugin.getLogger().info("Устанавливаем кастомный флаг regionborder-view со значением: " + value);
                
                // Get our custom flag from the plugin
                com.sk89q.worldguard.protection.flags.StateFlag regionBorderViewFlag = plugin.getRegionBorderViewFlag();
                if (regionBorderViewFlag != null) {
                    com.sk89q.worldguard.protection.flags.StateFlag.State state;
                    if (value.equalsIgnoreCase("allow") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1")) {
                        state = com.sk89q.worldguard.protection.flags.StateFlag.State.ALLOW;
                    } else {
                        state = com.sk89q.worldguard.protection.flags.StateFlag.State.DENY;
                    }
                    region.setFlag(regionBorderViewFlag, state);
                    plugin.getLogger().info("Кастомный флаг regionborder-view установлен: " + state);
                } else {
                    plugin.getLogger().warning("Флаг regionborder-view не найден в плагине!");
                }
            } else {
                // Handle other flag types if necessary
                plugin.getLogger().warning("Неподдерживаемый тип флага: " + flagName);
                return false;
            }

            regions.save(); // Save changes
            return true;

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при добавлении флага в регион: " + regionName, e);
            return false;
        }
    }
    
    /**
     * Remove flag from region
     * 
     * @param world World
     * @param regionName Region name
     * @param flagName Flag name
     * @return True if successful
     */
    public boolean removeRegionFlag(World world, String regionName, String flagName) {
        try {
            if (!isWorldGuardAvailable()) {
                return false;
            }

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(world));

            if (regions == null) {
                return false;
            }

            ProtectedRegion region = regions.getRegion(regionName);
            if (region == null) {
                return false;
            }

            Flag<?> flag = WorldGuard.getInstance().getFlagRegistry().get(flagName);
            if (flag == null) {
                plugin.getLogger().warning("Флаг " + flagName + " не найден!");
                return false;
            }

            region.setFlag(flag, null); // Remove flag
            regions.save(); // Save changes
            return true;

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при удалении флага из региона: " + regionName, e);
            return false;
        }
    }
    
    /**
     * Move region
     * 
     * @param world World
     * @param regionName Region name
     * @param player Player (for direction)
     * @param distance Distance to move
     * @return True if successful
     */
    public boolean moveRegion(World world, String regionName, Player player, int distance) {
        try {
            // Implementation for moving region
            
            return false; // Placeholder
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при перемещении региона: " + regionName, e);
            return false;
        }
    }
    
    /**
     * Resize region
     * 
     * @param world World
     * @param regionName Region name
     * @param player Player (for direction)
     * @param direction Direction (up/down/face)
     * @param distance Distance to resize
     * @return True if successful
     */
    public boolean resizeRegion(World world, String regionName, Player player, String direction, int distance) {
        try {
            // Implementation for resizing region
            
            return false; // Placeholder
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при изменении размера региона: " + regionName, e);
            return false;
        }
    }
    
    /**
     * Get region flags
     * 
     * @param world World
     * @param regionName Region name
     * @return List of flags in format "flag=value"
     */
    public List<String> getRegionFlags(World world, String regionName) {
        List<String> flags = new ArrayList<>();
        
        try {
            if (!isWorldGuardAvailable()) {
                return flags;
            }

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(world));

            if (regions == null) {
                return flags;
            }

            ProtectedRegion region = regions.getRegion(regionName);
            if (region == null) {
                return flags;
            }

            // Get all flags from the region
            for (Flag<?> flag : WorldGuard.getInstance().getFlagRegistry()) {
                Object value = region.getFlag(flag);
                if (value != null) {
                    String flagName = flag.getName();
                    String flagValue = value.toString();
                    
                    // Format the flag
                    if (value instanceof com.sk89q.worldguard.protection.flags.StateFlag.State) {
                        com.sk89q.worldguard.protection.flags.StateFlag.State state = (com.sk89q.worldguard.protection.flags.StateFlag.State) value;
                        flagValue = state.name().toLowerCase();
                    }
                    
                    flags.add(flagName + "=" + flagValue);
                }
            }

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при получении флагов региона: " + regionName, e);
        }
        
        return flags;
    }

    /**
     * Check if selection overlaps with existing regions
     * 
     * @param world World
     * @param pos1 First position
     * @param pos2 Second position
     * @return List of overlapping region names
     */
    public List<String> getOverlappingRegions(World world, Location pos1, Location pos2) {
        List<String> overlappingRegions = new ArrayList<>();
        
        try {
            if (!isWorldGuardAvailable()) {
                return overlappingRegions;
            }
            
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(world));
            
            if (regions == null) {
                return overlappingRegions;
            }
            
            // Convert Bukkit locations to WorldEdit BlockVector3
            BlockVector3 min = BlockVector3.at(
                Math.min(pos1.getBlockX(), pos2.getBlockX()),
                Math.min(pos1.getBlockY(), pos2.getBlockY()),
                Math.min(pos1.getBlockZ(), pos2.getBlockZ())
            );
            
            BlockVector3 max = BlockVector3.at(
                Math.max(pos1.getBlockX(), pos2.getBlockX()),
                Math.max(pos1.getBlockY(), pos2.getBlockY()),
                Math.max(pos1.getBlockZ(), pos2.getBlockZ())
            );
            
            // Create temporary region for comparison
            ProtectedCuboidRegion tempRegion = new ProtectedCuboidRegion("temp", min, max);
            
            // Check all existing regions for overlap
            for (ProtectedRegion existingRegion : regions.getRegions().values()) {
                // Check if regions overlap by comparing their bounds
                if (regionsOverlap(tempRegion, existingRegion)) {
                    overlappingRegions.add(existingRegion.getId());
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при проверке пересечения регионов", e);
        }
        
        return overlappingRegions;
    }
    
    /**
     * Get region at specific location
     * 
     * @param world World
     * @param location Location to check
     * @return Region name if found, null otherwise
     */
    public String getRegionAtLocation(World world, Location location) {
        try {
            if (!isWorldGuardAvailable()) {
                return null;
            }
            
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(world));
            
            if (regions == null) {
                return null;
            }
            
            // Convert Bukkit location to WorldEdit BlockVector3
            BlockVector3 point = BukkitAdapter.asBlockVector(location);
            
            // Check all regions to find which one contains this point
            for (ProtectedRegion region : regions.getRegions().values()) {
                if (region.contains(point)) {
                    return region.getId();
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при поиске региона по координатам", e);
        }
        
        return null;
    }
    
    /**
     * Check if two regions overlap
     * 
     * @param region1 First region
     * @param region2 Second region
     * @return True if regions overlap
     */
    private boolean regionsOverlap(ProtectedCuboidRegion region1, ProtectedRegion region2) {
        try {
            // Get bounds of both regions
            BlockVector3 min1 = region1.getMinimumPoint();
            BlockVector3 max1 = region1.getMaximumPoint();
            
            BlockVector3 min2 = region2.getMinimumPoint();
            BlockVector3 max2 = region2.getMaximumPoint();
            
            // Check if regions overlap in 3D space
            // Two regions overlap if they overlap in all three dimensions (X, Y, Z)
            boolean xOverlap = (min1.getX() <= max2.getX()) && (max1.getX() >= min2.getX());
            boolean yOverlap = (min1.getY() <= max2.getY()) && (max1.getY() >= min2.getY());
            boolean zOverlap = (min1.getZ() <= max2.getZ()) && (max1.getZ() >= min2.getZ());
            
            return xOverlap && yOverlap && zOverlap;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при проверке пересечения регионов", e);
            return false;
        }
    }
    
    /**
     * Get region at specific location
     * 
     * @param location Location to check
     * @return ProtectedRegion at location or null
     */
    public ProtectedRegion getRegionAtLocation(Location location) {
        try {
            if (!isWorldGuardAvailable()) {
                return null;
            }
            
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(location.getWorld()));
            if (regions == null) {
                return null;
            }
            
            BlockVector3 point = BukkitAdapter.asBlockVector(location);
            for (ProtectedRegion region : regions.getRegions().values()) {
                if (region.contains(point)) {
                    return region;
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при поиске региона по координатам", e);
        }
        return null;
    }
    
    /**
     * Get region flags as string
     * 
     * @param region Region to get flags from
     * @return Flags as string
     */
    public String getRegionFlagsString(ProtectedRegion region) {
        try {
            StringBuilder flags = new StringBuilder();
            boolean first = true;
            
            for (Flag<?> flag : region.getFlags().keySet()) {
                if (!first) {
                    flags.append(", ");
                }
                flags.append(flag.getName()).append("=").append(region.getFlags().get(flag));
                first = false;
            }
            
            return flags.toString();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Get region flags as list
     * 
     * @param region Region to get flags from
     * @return List of flags
     */
    public List<String> getRegionFlagsList(ProtectedRegion region) {
        List<String> flags = new ArrayList<>();
        try {
            for (Flag<?> flag : region.getFlags().keySet()) {
                flags.add(flag.getName() + "=" + region.getFlags().get(flag));
            }
        } catch (Exception e) {
            // Ignore
        }
        return flags;
    }
    
    /**
     * Get region size
     * 
     * @param region Region to get size from
     * @return Size in blocks
     */
    public int getRegionSize(ProtectedRegion region) {
        try {
            BlockVector3 min = region.getMinimumPoint();
            BlockVector3 max = region.getMaximumPoint();
            
            int width = (int) (max.getX() - min.getX() + 1);
            int height = (int) (max.getY() - min.getY() + 1);
            int length = (int) (max.getZ() - min.getZ() + 1);
            
            return width * height * length;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Get region owners as list
     * 
     * @param region Region to get owners from
     * @return List of owner names
     */
    public List<String> getRegionOwnersList(ProtectedRegion region) {
        List<String> owners = new ArrayList<>();
        try {
            for (UUID uuid : region.getOwners().getUniqueIds()) {
                String playerName = Bukkit.getOfflinePlayer(uuid).getName();
                if (playerName != null) {
                    owners.add(playerName);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return owners;
    }
    
    /**
     * Get region members as list
     * 
     * @param region Region to get members from
     * @return List of member names
     */
    public List<String> getRegionMembersList(ProtectedRegion region) {
        List<String> members = new ArrayList<>();
        try {
            for (UUID uuid : region.getMembers().getUniqueIds()) {
                String playerName = Bukkit.getOfflinePlayer(uuid).getName();
                if (playerName != null) {
                    members.add(playerName);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return members;
    }
    
    /**
     * Get region creator
     * 
     * @param region Region to get creator from
     * @return Creator name or empty string
     */
    public String getRegionCreator(ProtectedRegion region) {
        try {
            // Try to get creator from region metadata
            if (region.getFlag(plugin.getCustomFlags().getCreatorFlag()) != null) {
                return region.getFlag(plugin.getCustomFlags().getCreatorFlag()).toString();
            }
            
            // Fallback: get first owner as creator
            List<String> owners = getRegionOwnersList(region);
            if (!owners.isEmpty()) {
                return owners.get(0);
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }
    
    /**
     * Get last expelled player
     * 
     * @param region Region to get expelled player from
     * @return Expelled player name or empty string
     */
    public String getRegionExpelled(ProtectedRegion region) {
        try {
            // Try to get expelled player from region metadata
            if (region.getFlag(plugin.getCustomFlags().getExpelledFlag()) != null) {
                return region.getFlag(plugin.getCustomFlags().getExpelledFlag()).toString();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }
    
    /**
     * Set region creator
     * 
     * @param region Region to set creator for
     * @param creatorName Creator name
     */
    public void setRegionCreator(ProtectedRegion region, String creatorName) {
        try {
            if (plugin.getCustomFlags().getCreatorFlag() != null) {
                region.setFlag(plugin.getCustomFlags().getCreatorFlag(), creatorName);
            }
        } catch (Exception e) {
            // Ignore
        }
    }
    
    /**
     * Set region expelled player
     * 
     * @param region Region to set expelled player for
     * @param expelledName Expelled player name
     */
    public void setRegionExpelled(ProtectedRegion region, String expelledName) {
        try {
            if (plugin.getCustomFlags().getExpelledFlag() != null) {
                region.setFlag(plugin.getCustomFlags().getExpelledFlag(), expelledName);
            }
        } catch (Exception e) {
            // Ignore
        }
    }
    
    /**
     * Check if WorldGuard is available
     * 
     * @return True if available
     */
    public boolean isWorldGuardAvailable() {
        try {
            return plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при проверке доступности WorldGuard", e);
            return false;
        }
    }
    
    /**
     * Get regions where player is member
     */
    public List<String> getPlayerMemberRegions(String playerName) {
        List<String> regions = new ArrayList<>();
        try {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] ========== WorldGuardUtils.getPlayerMemberRegions ==========");
                plugin.getLogger().info("[DEBUG] Player: " + playerName);
            }
            
            UUID playerUUID = Bukkit.getOfflinePlayer(playerName).getUniqueId();
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] Player UUID: " + playerUUID);
            }
            
            World world = Bukkit.getWorlds().get(0); // Get first world
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] Player world: " + world.getName());
            }
            
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
            
            if (regionManager != null) {
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] WorldGuard is available, getting region container...");
                    plugin.getLogger().info("[DEBUG] Region container: found");
                    plugin.getLogger().info("[DEBUG] Region manager: found");
                    plugin.getLogger().info("[DEBUG] Getting all regions in world...");
                    plugin.getLogger().info("[DEBUG] Total regions in world: " + regionManager.getRegions().size());
                }
                
                for (ProtectedRegion region : regionManager.getRegions().values()) {
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] Checking region " + regions.size() + "/" + regionManager.getRegions().size() + ": " + region.getId());
                    }
                    
                    if (region.getMembers() != null) {
                        if (plugin.getConfigManager().isDebugMode()) {
                            plugin.getLogger().info("[DEBUG] Region owners: " + region.getOwners().getPlayers());
                            plugin.getLogger().info("[DEBUG] Region members: " + region.getMembers().getPlayers());
                        }
                        
                        boolean isMember = region.getMembers().getPlayers().contains(playerName) || 
                                         region.getMembers().getUniqueIds().contains(playerUUID);
                        
                        if (plugin.getConfigManager().isDebugMode()) {
                            plugin.getLogger().info("[DEBUG] Player is member: " + isMember);
                        }
                        
                        if (isMember) {
                            regions.add(region.getId());
                        }
                    } else {
                        if (plugin.getConfigManager().isDebugMode()) {
                            plugin.getLogger().info("[DEBUG] Region owners: " + region.getOwners());
                            plugin.getLogger().info("[DEBUG] Region members: " + region.getMembers());
                            plugin.getLogger().info("[DEBUG] Player is member: false");
                        }
                    }
                }
            }
            
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] Found " + regions.size() + " member regions for player " + playerName + ": " + regions);
                plugin.getLogger().info("[DEBUG] ========== WorldGuardUtils.getPlayerMemberRegions END ==========");
            }
            
        } catch (Exception e) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().severe("[DEBUG] Error getting member regions: " + e.getMessage());
            }
        }
        return regions;
    }
}

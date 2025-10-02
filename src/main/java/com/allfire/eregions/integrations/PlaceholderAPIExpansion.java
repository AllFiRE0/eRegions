package com.allfire.eregions.integrations;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.utils.WorldGuardUtils;
import com.allfire.eregions.utils.WorldEditUtils;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ArrayList;

/**
 * PlaceholderAPI Expansion for eRegions
 * 
 * Provides placeholders for region information, selections, and permissions
 * 
 * @author AllF1RE
 */
public class PlaceholderAPIExpansion extends PlaceholderExpansion {
    
    private final ERegions plugin;
    private final WorldGuardUtils worldGuardUtils;
    private final WorldEditUtils worldEditUtils;
    
    public PlaceholderAPIExpansion(ERegions plugin) {
        this.plugin = plugin;
        this.worldGuardUtils = plugin.getWorldGuardUtils();
        this.worldEditUtils = plugin.getWorldEditUtils();
    }
    
    /**
     * Check if debug is enabled for specific type
     */
    private boolean isDebugEnabled(String debugType) {
        try {
            if (plugin.getConfigManager() != null) {
                // First check general debug
                if (!plugin.getConfigManager().isDebugMode()) {
                    return false;
                }
                // Then check specific debug type (for future use)
                return true; // For now, just use general debug
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Debug logging
     */
    private void debugLog(String message, String debugType) {
        if (isDebugEnabled(debugType)) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }
    
    @Override
    public @NotNull String getIdentifier() {
        return "eregions";
    }
    
    @Override
    public @NotNull String getAuthor() {
        return "AllF1RE";
    }
    
    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public List<String> getPlaceholders() {
        List<String> placeholders = new ArrayList<>();
        
        // Selection placeholders
        placeholders.add("%eregions_selection_pos1%");
        placeholders.add("%eregions_selection_pos2%");
        placeholders.add("%eregions_selection_size%");
        
        // Region placeholders
        placeholders.add("%eregions_region_flags%");
        placeholders.add("%eregions_region_flags_1%");
        placeholders.add("%eregions_region_flags_2%");
        placeholders.add("%eregions_region_flags_3%");
        placeholders.add("%eregions_region_flags_4%");
        placeholders.add("%eregions_region_flags_5%");
        placeholders.add("%eregions_region_flags_6%");
        placeholders.add("%eregions_region_flags_7%");
        placeholders.add("%eregions_region_flags_8%");
        placeholders.add("%eregions_region_flags_9%");
        placeholders.add("%eregions_region_flags_10%");
        
        // Region info placeholders
        placeholders.add("%eregions_region_viewing%");
        placeholders.add("%eregions_region_size%");
        placeholders.add("%eregions_region_creator%");
        placeholders.add("%eregions_region_expelled%");
        
        // Owners and members
        placeholders.add("%eregions_region_owners%");
        placeholders.add("%eregions_region_members%");
        
        // Individual owners (1-50)
        for (int i = 1; i <= 50; i++) {
            placeholders.add("%eregions_region_owners_" + i + "%");
        }
        
        // Individual members (1-50)
        for (int i = 1; i <= 50; i++) {
            placeholders.add("%eregions_region_members_" + i + "%");
        }
        
        // Region flags (1-50)
        for (int i = 1; i <= 50; i++) {
            placeholders.add("%eregions_region_flags_" + i + "%");
        }
        
        // Region owned by player
        placeholders.add("%eregions_region_owned%");
        for (int i = 1; i <= 50; i++) {
            placeholders.add("%eregions_region_owned_" + i + "%");
        }
        
        // Region membered by player
        placeholders.add("%eregions_region_membed%");
        for (int i = 1; i <= 50; i++) {
            placeholders.add("%eregions_region_membed_" + i + "%");
        }
        
        return placeholders;
    }
    
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "Player required";
        }
        
        try {
            debugLog("Processing placeholder: " + params + " for player: " + player.getName(), "placeholder-request");
            
            // Selection placeholders
            if (params.equals("selection_pos1")) {
                String result = getSelectionPos1(player);
                debugLog("selection_pos1 result: " + result, "selection");
                return result;
            } else if (params.equals("selection_pos2")) {
                String result = getSelectionPos2(player);
                debugLog("selection_pos2 result: " + result, "selection");
                return result;
            } else if (params.equals("selection_size")) {
                String result = getSelectionSize(player);
                debugLog("selection_size result: " + result, "selection");
                return result;
            }
            
            // Region placeholders
            else if (params.equals("region_flags")) {
                String result = getRegionFlags(player);
                debugLog("region_flags result: " + result, "region");
                return result;
            } else if (params.startsWith("region_flags_")) {
                String result = getRegionFlagsByIndex(player, params);
                debugLog("region_flags_" + params.substring("region_flags_".length()) + " result: " + result, "region");
                return result;
            } else if (params.equals("region_viewing")) {
                String result = getRegionViewing(player);
                debugLog("region_viewing result: " + result, "region");
                return result;
            } else if (params.equals("region_size")) {
                String result = getRegionSize(player);
                debugLog("region_size result: " + result, "region");
                return result;
            } else if (params.equals("region_owners")) {
                String result = getRegionOwners(player);
                debugLog("region_owners result: " + result, "region");
                return result;
            } else if (params.equals("region_members")) {
                String result = getRegionMembers(player);
                debugLog("region_members result: " + result, "region");
                return result;
            } else if (params.startsWith("region_owners_")) {
                String result = getRegionOwnersByIndex(player, params);
                debugLog("region_owners_" + params.substring("region_owners_".length()) + " result: " + result, "region");
                return result;
            } else if (params.startsWith("region_members_")) {
                String result = getRegionMembersByIndex(player, params);
                debugLog("region_members_" + params.substring("region_members_".length()) + " result: " + result, "region");
                return result;
            } else if (params.equals("region_creator")) {
                String result = getRegionCreator(player);
                debugLog("region_creator result: " + result, "region");
                return result;
            } else if (params.equals("region_expelled")) {
                String result = getRegionExpelled(player);
                debugLog("region_expelled result: " + result, "region");
                return result;
            } else if (params.equals("region_owned")) {
                String result = getRegionOwned(player);
                debugLog("region_owned result: " + result, "region");
                return result;
            } else if (params.startsWith("region_owned_")) {
                String result = getRegionOwnedByIndex(player, params);
                debugLog("region_owned_" + params.substring("region_owned_".length()) + " result: " + result, "region");
                return result;
            } else if (params.equals("region_membed")) {
                String result = getRegionMembed(player);
                debugLog("region_membed result: " + result, "region");
                return result;
            } else if (params.startsWith("region_membed_")) {
                String result = getRegionMembedByIndex(player, params);
                debugLog("region_membed_" + params.substring("region_membed_".length()) + " result: " + result, "region");
                return result;
            }
            
        } catch (Exception e) {
            debugLog("PlaceholderAPI error for " + params + ": " + e.getMessage(), "error");
        }
        
        return "";
    }
    
    /**
     * Get selection position 1
     */
    private String getSelectionPos1(Player player) {
        try {
            BlockVector3 pos1 = worldEditUtils.getSelectionPos1(player);
            if (pos1 != null) {
                return String.format("%.0f,%.0f,%.0f", pos1.getX(), pos1.getY(), pos1.getZ());
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }
    
    /**
     * Get selection position 2
     */
    private String getSelectionPos2(Player player) {
        try {
            BlockVector3 pos2 = worldEditUtils.getSelectionPos2(player);
            if (pos2 != null) {
                return String.format("%.0f,%.0f,%.0f", pos2.getX(), pos2.getY(), pos2.getZ());
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }
    
    /**
     * Get selection size
     */
    private String getSelectionSize(Player player) {
        try {
            int size = worldEditUtils.getSelectionSize(player);
            return String.valueOf(size);
        } catch (Exception e) {
            // Ignore
        }
        return "0";
    }
    
    /**
     * Get region flags
     */
    private String getRegionFlags(Player player) {
        try {
            Location location = player.getLocation();
            ProtectedRegion region = worldGuardUtils.getRegionAtLocation(location);
            if (region != null) {
                return worldGuardUtils.getRegionFlagsString(region);
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }
    
    /**
     * Get region flags by index
     */
    private String getRegionFlagsByIndex(Player player, String params) {
        try {
            String indexStr = params.substring("region_flags_".length());
            int index = Integer.parseInt(indexStr);
            if (index < 1 || index > 50) {
                return "";
            }
            
            Location location = player.getLocation();
            ProtectedRegion region = worldGuardUtils.getRegionAtLocation(location);
            if (region != null) {
                List<String> flags = worldGuardUtils.getRegionFlagsList(region);
                if (index <= flags.size()) {
                    return flags.get(index - 1); // Convert to 0-based index
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }
    
    /**
     * Get region viewing permission
     */
    private String getRegionViewing(Player player) {
        return player.hasPermission("eregions.view") ? "true" : "false";
    }
    
    /**
     * Get region size
     */
    private String getRegionSize(Player player) {
        try {
            Location location = player.getLocation();
            ProtectedRegion region = worldGuardUtils.getRegionAtLocation(location);
            if (region != null) {
                int size = worldGuardUtils.getRegionSize(region);
                return String.valueOf(size);
            }
        } catch (Exception e) {
            // Ignore
        }
        return "0";
    }
    
    /**
     * Get region owners
     */
    private String getRegionOwners(Player player) {
        try {
            Location location = player.getLocation();
            ProtectedRegion region = worldGuardUtils.getRegionAtLocation(location);
            if (region != null) {
                List<String> owners = worldGuardUtils.getRegionOwnersList(region);
                String separator = plugin.getConfig().getString("placeholders.owners-separator", ", ");
                return String.join(separator, owners);
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }
    
    /**
     * Get region members
     */
    private String getRegionMembers(Player player) {
        try {
            Location location = player.getLocation();
            ProtectedRegion region = worldGuardUtils.getRegionAtLocation(location);
            if (region != null) {
                List<String> members = worldGuardUtils.getRegionMembersList(region);
                String separator = plugin.getConfig().getString("placeholders.members-separator", ", ");
                return String.join(separator, members);
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }
    
    /**
     * Get region owners by index
     */
    private String getRegionOwnersByIndex(Player player, String params) {
        try {
            String indexStr = params.substring("region_owners_".length());
            int index = Integer.parseInt(indexStr);
            if (index < 1 || index > 50) {
                return "";
            }
            
            Location location = player.getLocation();
            ProtectedRegion region = worldGuardUtils.getRegionAtLocation(location);
            if (region != null) {
                List<String> owners = worldGuardUtils.getRegionOwnersList(region);
                if (index <= owners.size()) {
                    return owners.get(index - 1); // Convert to 0-based index
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }
    
    /**
     * Get region members by index
     */
    private String getRegionMembersByIndex(Player player, String params) {
        try {
            String indexStr = params.substring("region_members_".length());
            int index = Integer.parseInt(indexStr);
            if (index < 1 || index > 50) {
                return "";
            }
            
            Location location = player.getLocation();
            ProtectedRegion region = worldGuardUtils.getRegionAtLocation(location);
            if (region != null) {
                List<String> members = worldGuardUtils.getRegionMembersList(region);
                if (index <= members.size()) {
                    return members.get(index - 1); // Convert to 0-based index
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }
    
    /**
     * Get region creator
     */
    private String getRegionCreator(Player player) {
        try {
            Location location = player.getLocation();
            ProtectedRegion region = worldGuardUtils.getRegionAtLocation(location);
            if (region != null) {
                return worldGuardUtils.getRegionCreator(region);
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }
    
    /**
     * Get region expelled player
     */
    private String getRegionExpelled(Player player) {
        try {
            Location location = player.getLocation();
            ProtectedRegion region = worldGuardUtils.getRegionAtLocation(location);
            if (region != null) {
                return worldGuardUtils.getRegionExpelled(region);
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }
    
    /**
     * Get regions owned by player
     */
    private String getRegionOwned(Player player) {
        try {
            List<String> ownedRegions = worldGuardUtils.getPlayerOwnedRegions(player);
            String separator = plugin.getConfig().getString("placeholders.owned-separator", ", ");
            return String.join(separator, ownedRegions);
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }
    
    /**
     * Get regions owned by player by index
     */
    private String getRegionOwnedByIndex(Player player, String params) {
        try {
            String indexStr = params.substring("region_owned_".length());
            int index = Integer.parseInt(indexStr);
            if (index < 1 || index > 50) {
                return "";
            }
            
            List<String> ownedRegions = worldGuardUtils.getPlayerOwnedRegions(player);
            if (index <= ownedRegions.size()) {
                return ownedRegions.get(index - 1); // Convert to 0-based index
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }
    
    /**
     * Get regions where player is member
     */
    private String getRegionMembed(Player player) {
        try {
            List<String> memberRegions = worldGuardUtils.getPlayerMemberRegions(player.getName());
            String separator = plugin.getConfig().getString("placeholders.membered-separator", ", ");
            return String.join(separator, memberRegions);
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }
    
    /**
     * Get regions where player is member by index
     */
    private String getRegionMembedByIndex(Player player, String params) {
        try {
            String indexStr = params.substring("region_membed_".length());
            int index = Integer.parseInt(indexStr);
            if (index < 1 || index > 50) {
                return "";
            }
            
            List<String> memberRegions = worldGuardUtils.getPlayerMemberRegions(player.getName());
            if (index <= memberRegions.size()) {
                return memberRegions.get(index - 1); // Convert to 0-based index
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }
    
}

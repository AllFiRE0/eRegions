package com.allfire.eregions.utils;

import com.allfire.eregions.ERegions;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.logging.Level;

/**
 * Permission Utilities
 * 
 * Handles permission-related operations
 * Integrates with permission plugins like LuckPerms
 * 
 * @author AllF1RE
 */
public class PermissionUtils {
    
    private final ERegions plugin;
    
    public PermissionUtils(ERegions plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Check if player has permission
     * 
     * @param player Player
     * @param permission Permission string
     * @return True if has permission
     */
    public boolean hasPermission(Player player, String permission) {
        try {
            if (player == null || permission == null || permission.isEmpty()) {
                return false;
            }
            
            boolean hasPermission = player.hasPermission(permission);
            
            if (plugin.getConfigManager().isPermissionLoggingEnabled()) {
                plugin.getLogger().info("[DEBUG] Permission check for " + player.getName() + 
                                      " for permission '" + permission + "': " + hasPermission);
            }
            
            return hasPermission;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при проверке прав: " + permission, e);
            return false;
        }
    }
    
    /**
     * Check if player has permission with debug logging
     * 
     * @param player Player
     * @param permission Permission string
     * @param debugMessage Debug message
     * @return True if has permission
     */
    public boolean hasPermission(Player player, String permission, String debugMessage) {
        try {
            boolean hasPermission = hasPermission(player, permission);
            
            if (plugin.getConfigManager().isPermissionLoggingEnabled()) {
                plugin.getLogger().info("[DEBUG] " + debugMessage + " - Permission '" + permission + "': " + hasPermission);
            }
            
            return hasPermission;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при проверке прав: " + permission, e);
            return false;
        }
    }
    
    /**
     * Get permission value (for numeric permissions)
     * 
     * @param player Player
     * @param permission Permission string
     * @return Permission value or 0
     */
    public int getPermissionValue(Player player, String permission) {
        try {
            if (player == null || permission == null || permission.isEmpty()) {
                return 0;
            }
            
            int maxValue = 0;
            
            for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
                if (info.getPermission().startsWith(permission + ".")) {
                    try {
                        String valueStr = info.getPermission().substring(permission.length() + 1);
                        int value = Integer.parseInt(valueStr);
                        if (value > maxValue) {
                            maxValue = value;
                        }
                    } catch (NumberFormatException e) {
                        // Skip invalid numeric permissions
                    }
                } else if (info.getPermission().equals(permission)) {
                    // If player has the base permission, return 1
                    maxValue = Math.max(maxValue, 1);
                }
            }
            
            if (plugin.getConfigManager().isPermissionLoggingEnabled()) {
                plugin.getLogger().info("[DEBUG] Permission value for " + player.getName() + 
                                      " for permission '" + permission + "': " + maxValue);
            }
            
            return maxValue;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при получении значения права: " + permission, e);
            return 0;
        }
    }
    
    /**
     * Check if player has any of the specified permissions
     * 
     * @param player Player
     * @param permissions Array of permissions
     * @return True if has any permission
     */
    public boolean hasAnyPermission(Player player, String... permissions) {
        try {
            if (player == null || permissions == null || permissions.length == 0) {
                return false;
            }
            
            for (String permission : permissions) {
                if (hasPermission(player, permission)) {
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при проверке множественных прав", e);
            return false;
        }
    }
    
    /**
     * Check if player has all of the specified permissions
     * 
     * @param player Player
     * @param permissions Array of permissions
     * @return True if has all permissions
     */
    public boolean hasAllPermissions(Player player, String... permissions) {
        try {
            if (player == null || permissions == null || permissions.length == 0) {
                return false;
            }
            
            for (String permission : permissions) {
                if (!hasPermission(player, permission)) {
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при проверке множественных прав", e);
            return false;
        }
    }
    
    /**
     * Check if player is operator
     * 
     * @param player Player
     * @return True if operator
     */
    public boolean isOperator(Player player) {
        try {
            if (player == null) {
                return false;
            }
            
            return player.isOp();
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при проверке операторских прав", e);
            return false;
        }
    }
}

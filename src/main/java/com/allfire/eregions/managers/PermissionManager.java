package com.allfire.eregions.managers;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.utils.PermissionUtils;

/**
 * Permission Manager
 * 
 * Handles permission checking and management
 * Integrates with LuckPerms and Vault
 * 
 * @author AllF1RE
 */
public class PermissionManager {
    
    private final ERegions plugin;
    private final PermissionUtils permissionUtils;
    
    public PermissionManager(ERegions plugin) {
        this.plugin = plugin;
        this.permissionUtils = plugin.getPermissionUtils();
    }
}


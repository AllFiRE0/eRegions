package com.allfire.eregions.flags;

import com.allfire.eregions.ERegions;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

/**
 * Custom Flags for eRegions
 * 
 * Manages custom WorldGuard flags for tracking region metadata
 * 
 * @author AllF1RE
 */
public class CustomFlags {
    
    private final ERegions plugin;
    private StringFlag creatorFlag;
    private StringFlag expelledFlag;
    
    public CustomFlags(ERegions plugin) {
        this.plugin = plugin;
        registerFlags();
    }
    
    /**
     * Register custom flags
     */
    private void registerFlags() {
        try {
            FlagRegistry registry = com.sk89q.worldguard.WorldGuard.getInstance().getFlagRegistry();
            
            // Register creator flag
            try {
                creatorFlag = new StringFlag("eregions-creator", "Creator of the region");
                registry.register(creatorFlag);
                plugin.getLogger().info("Custom flag 'eregions-creator' registered!");
            } catch (FlagConflictException e) {
                creatorFlag = (StringFlag) registry.get("eregions-creator");
                plugin.getLogger().info("Custom flag 'eregions-creator' already exists!");
            }
            
            // Register expelled flag
            try {
                expelledFlag = new StringFlag("eregions-expelled", "Last expelled player from the region");
                registry.register(expelledFlag);
                plugin.getLogger().info("Custom flag 'eregions-expelled' registered!");
            } catch (FlagConflictException e) {
                expelledFlag = (StringFlag) registry.get("eregions-expelled");
                plugin.getLogger().info("Custom flag 'eregions-expelled' already exists!");
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error registering custom flags: " + e.getMessage());
        }
    }
    
    /**
     * Get creator flag
     * 
     * @return Creator flag
     */
    public StringFlag getCreatorFlag() {
        return creatorFlag;
    }
    
    /**
     * Get expelled flag
     * 
     * @return Expelled flag
     */
    public StringFlag getExpelledFlag() {
        return expelledFlag;
    }
}



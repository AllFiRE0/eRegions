package com.allfire.eregions.managers;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.utils.WorldGuardUtils;

import java.util.logging.Level;

/**
 * WorldGuard Integration Manager
 * 
 * Handles integration with WorldGuard plugin
 * Manages WorldGuard-related operations
 * 
 * @author AllF1RE
 */
public class WorldGuardIntegrationManager {
    
    private final ERegions plugin;
    private WorldGuardUtils worldGuardUtils;
    private boolean isEnabled;
    
    public WorldGuardIntegrationManager(ERegions plugin) {
        this.plugin = plugin;
        this.isEnabled = false;
    }
    
    /**
     * Initialize WorldGuard integration
     */
    public void initialize() {
        try {
            // Check if WorldGuard is available
            if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") == null) {
                plugin.getLogger().warning("WorldGuard не найден! Интеграция отключена.");
                return;
            }
            
            // Initialize WorldGuardUtils
            worldGuardUtils = new WorldGuardUtils(plugin);
            
            // Test integration
            if (worldGuardUtils.isWorldGuardAvailable()) {
                isEnabled = true;
                plugin.getLogger().info("Интеграция с WorldGuard успешно инициализирована!");
            } else {
                plugin.getLogger().warning("Не удалось инициализировать интеграцию с WorldGuard!");
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при инициализации интеграции с WorldGuard", e);
            isEnabled = false;
        }
    }
    
    /**
     * Check if WorldGuard integration is enabled
     * 
     * @return True if enabled
     */
    public boolean isEnabled() {
        return isEnabled;
    }
    
    /**
     * Get WorldGuardUtils instance
     * 
     * @return WorldGuardUtils or null
     */
    public WorldGuardUtils getWorldGuardUtils() {
        return worldGuardUtils;
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
     * Get WorldGuard version
     * 
     * @return Version string or null
     */
    public String getWorldGuardVersion() {
        try {
            if (isWorldGuardAvailable()) {
                return plugin.getServer().getPluginManager().getPlugin("WorldGuard").getDescription().getVersion();
            }
            return null;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при получении версии WorldGuard", e);
            return null;
        }
    }
    
    /**
     * Reload WorldGuard integration
     */
    public void reload() {
        try {
            initialize();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при перезагрузке интеграции с WorldGuard", e);
        }
    }
}


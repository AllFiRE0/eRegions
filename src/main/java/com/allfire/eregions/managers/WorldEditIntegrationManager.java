package com.allfire.eregions.managers;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.utils.WorldEditUtils;

import java.util.logging.Level;

/**
 * WorldEdit Integration Manager
 * 
 * Handles integration with WorldEdit plugin
 * Manages WorldEdit-related operations
 * 
 * @author AllF1RE
 */
public class WorldEditIntegrationManager {
    
    private final ERegions plugin;
    private WorldEditUtils worldEditUtils;
    private boolean isEnabled;
    
    public WorldEditIntegrationManager(ERegions plugin) {
        this.plugin = plugin;
        this.isEnabled = false;
    }
    
    /**
     * Initialize WorldEdit integration
     */
    public void initialize() {
        try {
            // Check if WorldEdit is available
            if (plugin.getServer().getPluginManager().getPlugin("WorldEdit") == null) {
                plugin.getLogger().warning("WorldEdit не найден! Интеграция отключена.");
                return;
            }
            
            // Initialize WorldEditUtils
            worldEditUtils = new WorldEditUtils(plugin);
            
            // Test integration
            if (worldEditUtils.isWorldEditAvailable()) {
                isEnabled = true;
                plugin.getLogger().info("Интеграция с WorldEdit успешно инициализирована!");
            } else {
                plugin.getLogger().warning("Не удалось инициализировать интеграцию с WorldEdit!");
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при инициализации интеграции с WorldEdit", e);
            isEnabled = false;
        }
    }
    
    /**
     * Check if WorldEdit integration is enabled
     * 
     * @return True if enabled
     */
    public boolean isEnabled() {
        return isEnabled;
    }
    
    /**
     * Get WorldEditUtils instance
     * 
     * @return WorldEditUtils or null
     */
    public WorldEditUtils getWorldEditUtils() {
        return worldEditUtils;
    }
    
    /**
     * Check if WorldEdit is available
     * 
     * @return True if available
     */
    public boolean isWorldEditAvailable() {
        try {
            return plugin.getServer().getPluginManager().getPlugin("WorldEdit") != null;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при проверке доступности WorldEdit", e);
            return false;
        }
    }
    
    /**
     * Get WorldEdit version
     * 
     * @return Version string or null
     */
    public String getWorldEditVersion() {
        try {
            if (isWorldEditAvailable()) {
                return plugin.getServer().getPluginManager().getPlugin("WorldEdit").getDescription().getVersion();
            }
            return null;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при получении версии WorldEdit", e);
            return null;
        }
    }
    
    /**
     * Reload WorldEdit integration
     */
    public void reload() {
        try {
            initialize();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при перезагрузке интеграции с WorldEdit", e);
        }
    }
}


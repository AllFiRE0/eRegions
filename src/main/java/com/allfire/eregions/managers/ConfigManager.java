package com.allfire.eregions.managers;

import com.allfire.eregions.ERegions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Configuration Manager
 * 
 * Handles loading and managing plugin configuration
 * Provides easy access to configuration values
 * 
 * @author AllF1RE
 */
public class ConfigManager {
    
    private final ERegions plugin;
    private FileConfiguration config;
    
    // Configuration sections
    private Map<String, Object> settings;
    private Map<String, List<String>> commandTriggers;
    private Map<String, Object> boundaryTriggers;
    private Map<String, Object> worldEditSettings;
    private Map<String, Object> worldGuardSettings;
    private Map<String, Object> economySettings;
    private Map<String, Object> permissionSettings;
    private Map<String, Object> placeholderSettings;
    private Map<String, Object> performanceSettings;
    private Map<String, Object> loggingSettings;
    
    public ConfigManager(ERegions plugin) {
        this.plugin = plugin;
        this.settings = new HashMap<>();
        this.commandTriggers = new HashMap<>();
        this.boundaryTriggers = new HashMap<>();
        this.worldEditSettings = new HashMap<>();
        this.worldGuardSettings = new HashMap<>();
        this.economySettings = new HashMap<>();
        this.permissionSettings = new HashMap<>();
        this.placeholderSettings = new HashMap<>();
        this.performanceSettings = new HashMap<>();
        this.loggingSettings = new HashMap<>();
    }
    
    /**
     * Load configuration from file
     */
    public void loadConfiguration() {
        try {
            config = plugin.getConfig();
            
            // Load settings
            loadSettings();
            
            // Load command triggers
            loadCommandTriggers();
            
            // Load boundary triggers
            loadBoundaryTriggers();
            
            // Load integration settings
            loadWorldEditSettings();
            loadWorldGuardSettings();
            loadEconomySettings();
            loadPermissionSettings();
            loadPlaceholderSettings();
            loadPerformanceSettings();
            loadLoggingSettings();
            
            plugin.getLogger().info("Конфигурация загружена успешно!");
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при загрузке конфигурации!", e);
        }
    }
    
    /**
     * Load basic settings
     */
    private void loadSettings() {
        ConfigurationSection settingsSection = config.getConfigurationSection("settings");
        if (settingsSection != null) {
            settings.put("max-selection-distance", settingsSection.getInt("max-selection-distance", 1000));
            settings.put("command-delay", settingsSection.getInt("command-delay", 100));
            settings.put("message-delay", settingsSection.getInt("message-delay", 50));
            settings.put("debug", settingsSection.getBoolean("debug", false));
            settings.put("enable-boundary-detection", settingsSection.getBoolean("enable-boundary-detection", true));
            settings.put("boundary-detection-distance", settingsSection.getDouble("boundary-detection-distance", 5.0));
        }
    }
    
    /**
     * Load command triggers
     */
    private void loadCommandTriggers() {
        ConfigurationSection triggersSection = config.getConfigurationSection("command-triggers");
        if (triggersSection != null) {
            for (String triggerName : triggersSection.getKeys(false)) {
                ConfigurationSection triggerSection = triggersSection.getConfigurationSection(triggerName);
                if (triggerSection != null) {
                    List<String> commands = triggerSection.getStringList("commands");
                    commandTriggers.put(triggerName, commands);
                }
            }
        }
    }
    
    /**
     * Load boundary triggers
     */
    private void loadBoundaryTriggers() {
        ConfigurationSection boundarySection = config.getConfigurationSection("boundary-triggers");
        if (boundarySection != null) {
            for (String triggerName : boundarySection.getKeys(false)) {
                ConfigurationSection triggerSection = boundarySection.getConfigurationSection(triggerName);
                if (triggerSection != null) {
                    Map<String, Object> triggerData = new HashMap<>();
                    triggerData.put("enabled", triggerSection.getBoolean("enabled", true));
                    triggerData.put("distance", triggerSection.getDouble("distance", 5.0));
                    triggerData.put("commands", triggerSection.getStringList("commands"));
                    boundaryTriggers.put(triggerName, triggerData);
                }
            }
        }
    }
    
    /**
     * Load WorldEdit settings
     */
    private void loadWorldEditSettings() {
        ConfigurationSection worldEditSection = config.getConfigurationSection("worldedit");
        if (worldEditSection != null) {
            worldEditSettings.put("enabled", worldEditSection.getBoolean("enabled", true));
            worldEditSettings.put("use-fawe", worldEditSection.getBoolean("use-fawe", true));
            
            ConfigurationSection selectionSection = worldEditSection.getConfigurationSection("selection-visualization");
            if (selectionSection != null) {
                Map<String, Object> selectionData = new HashMap<>();
                selectionData.put("enabled", selectionSection.getBoolean("enabled", true));
                selectionData.put("particle-type", selectionSection.getString("particle-type", "REDSTONE"));
                selectionData.put("particle-count", selectionSection.getInt("particle-count", 100));
                selectionData.put("particle-offset", selectionSection.getDouble("particle-offset", 0.1));
                worldEditSettings.put("selection-visualization", selectionData);
            }
        }
    }
    
    /**
     * Load WorldGuard settings
     */
    private void loadWorldGuardSettings() {
        ConfigurationSection worldGuardSection = config.getConfigurationSection("worldguard");
        if (worldGuardSection != null) {
            worldGuardSettings.put("enabled", worldGuardSection.getBoolean("enabled", true));
            worldGuardSettings.put("auto-create-regions", worldGuardSection.getBoolean("auto-create-regions", true));
            worldGuardSettings.put("default-priority", worldGuardSection.getInt("default-priority", 0));
            worldGuardSettings.put("default-flags", worldGuardSection.getStringList("default-flags"));
            
            // Load message settings
            worldGuardSettings.put("list-flags", worldGuardSection.getStringList("list-flags"));
            worldGuardSettings.put("flag-changed", worldGuardSection.getStringList("flag-changed"));
        }
    }
    
    /**
     * Load economy settings
     */
    private void loadEconomySettings() {
        ConfigurationSection economySection = config.getConfigurationSection("economy");
        if (economySection != null) {
            economySettings.put("enabled", economySection.getBoolean("enabled", true));
            economySettings.put("region-creation-cost", economySection.getInt("region-creation-cost", 1000));
            economySettings.put("region-removal-cost", economySection.getInt("region-removal-cost", 500));
            economySettings.put("region-resize-cost", economySection.getInt("region-resize-cost", 100));
            economySettings.put("region-move-cost", economySection.getInt("region-move-cost", 200));
        }
    }
    
    /**
     * Load permission settings
     */
    private void loadPermissionSettings() {
        ConfigurationSection permissionSection = config.getConfigurationSection("permissions");
        if (permissionSection != null) {
            permissionSettings.put("luckperms-enabled", permissionSection.getBoolean("luckperms-enabled", true));
            permissionSettings.put("vault-enabled", permissionSection.getBoolean("vault-enabled", true));
            permissionSettings.put("permission-check-delay", permissionSection.getInt("permission-check-delay", 100));
        }
    }
    
    /**
     * Load PlaceholderAPI settings
     */
    private void loadPlaceholderSettings() {
        ConfigurationSection placeholderSection = config.getConfigurationSection("placeholders");
        if (placeholderSection != null) {
            placeholderSettings.put("enabled", placeholderSection.getBoolean("enabled", true));
            placeholderSettings.put("custom-placeholders", placeholderSection.getStringList("custom-placeholders"));
        }
    }
    
    /**
     * Load performance settings
     */
    private void loadPerformanceSettings() {
        ConfigurationSection performanceSection = config.getConfigurationSection("performance");
        if (performanceSection != null) {
            performanceSettings.put("max-regions-per-tick", performanceSection.getInt("max-regions-per-tick", 10));
            performanceSettings.put("cache-regions", performanceSection.getBoolean("cache-regions", true));
            performanceSettings.put("cache-duration", performanceSection.getInt("cache-duration", 5));
            performanceSettings.put("async-operations", performanceSection.getBoolean("async-operations", true));
        }
    }
    
    /**
     * Load logging settings
     */
    private void loadLoggingSettings() {
        ConfigurationSection loggingSection = config.getConfigurationSection("logging");
        if (loggingSection != null) {
            loggingSettings.put("detailed-logging", loggingSection.getBoolean("detailed-logging", false));
            loggingSettings.put("log-commands", loggingSection.getBoolean("log-commands", true));
            loggingSettings.put("log-region-operations", loggingSection.getBoolean("log-region-operations", true));
            loggingSettings.put("log-permission-checks", loggingSection.getBoolean("log-permission-checks", false));
        }
    }
    
    // Getters for configuration values
    
    public int getMaxSelectionDistance() {
        return (Integer) settings.getOrDefault("max-selection-distance", 1000);
    }
    
    public int getCommandDelay() {
        return (Integer) settings.getOrDefault("command-delay", 100);
    }
    
    public int getMessageDelay() {
        return (Integer) settings.getOrDefault("message-delay", 50);
    }
    
    public boolean isDebugEnabled() {
        return (Boolean) settings.getOrDefault("debug", false);
    }
    
    public boolean isBoundaryDetectionEnabled() {
        return (Boolean) settings.getOrDefault("enable-boundary-detection", true);
    }
    
    public double getBoundaryDetectionDistance() {
        return (Double) settings.getOrDefault("boundary-detection-distance", 5.0);
    }
    
    public List<String> getCommandTrigger(String triggerName) {
        return commandTriggers.getOrDefault(triggerName, new ArrayList<>());
    }
    
    public Map<String, Object> getBoundaryTrigger(String triggerName) {
        return (Map<String, Object>) boundaryTriggers.getOrDefault(triggerName, new HashMap<>());
    }
    
    public boolean isWorldEditEnabled() {
        return (Boolean) worldEditSettings.getOrDefault("enabled", true);
    }
    
    public boolean isWorldGuardEnabled() {
        return (Boolean) worldGuardSettings.getOrDefault("enabled", true);
    }
    
    public boolean isEconomyEnabled() {
        return (Boolean) economySettings.getOrDefault("enabled", true);
    }
    
    public int getRegionCreationCost() {
        return (Integer) economySettings.getOrDefault("region-creation-cost", 1000);
    }
    
    public boolean isDebugMode() {
        return (Boolean) settings.getOrDefault("debug", false);
    }
    
    /**
     * Check if command logging is enabled
     * 
     * @return True if command logging is enabled
     */
    public boolean isCommandLoggingEnabled() {
        return isDebugMode() && (Boolean) settings.getOrDefault("debug-settings.log-commands", true);
    }
    
    /**
     * Check if trigger logging is enabled
     * 
     * @return True if trigger logging is enabled
     */
    public boolean isTriggerLoggingEnabled() {
        return isDebugMode() && (Boolean) settings.getOrDefault("debug-settings.log-triggers", true);
    }
    
    /**
     * Check if message logging is enabled
     * 
     * @return True if message logging is enabled
     */
    public boolean isMessageLoggingEnabled() {
        return isDebugMode() && (Boolean) settings.getOrDefault("debug-settings.log-messages", true);
    }
    
    /**
     * Check if selection logging is enabled
     * 
     * @return True if selection logging is enabled
     */
    public boolean isSelectionLoggingEnabled() {
        return isDebugMode() && (Boolean) settings.getOrDefault("debug-settings.log-selections", true);
    }
    
    /**
     * Check if region logging is enabled
     * 
     * @return True if region logging is enabled
     */
    public boolean isRegionLoggingEnabled() {
        return isDebugMode() && (Boolean) settings.getOrDefault("debug-settings.log-regions", true);
    }
    
    /**
     * Check if permission logging is enabled
     * 
     * @return True if permission logging is enabled
     */
    public boolean isPermissionLoggingEnabled() {
        return isDebugMode() && (Boolean) settings.getOrDefault("debug-settings.log-permissions", true);
    }
    
    /**
     * Check if WorldEdit logging is enabled
     * 
     * @return True if WorldEdit logging is enabled
     */
    public boolean isWorldEditLoggingEnabled() {
        return isDebugMode() && (Boolean) settings.getOrDefault("debug-settings.log-worldedit", true);
    }
    
    /**
     * Check if WorldGuard logging is enabled
     * 
     * @return True if WorldGuard logging is enabled
     */
    public boolean isWorldGuardLoggingEnabled() {
        return isDebugMode() && (Boolean) settings.getOrDefault("debug-settings.log-worldguard", true);
    }
    
    /**
     * Get default region priority
     * 
     * @return Default priority
     */
    public int getDefaultPriority() {
        return (Integer) worldGuardSettings.getOrDefault("default-priority", 0);
    }
    
    /**
     * Get default region flags
     * 
     * @return List of default flags
     */
    @SuppressWarnings("unchecked")
    public List<String> getDefaultFlags() {
        return (List<String>) worldGuardSettings.getOrDefault("default-flags", new ArrayList<>());
    }
    
    /**
     * Get message from config
     * 
     * @param key Message key
     * @return Message string
     */
    public String getMessage(String key) {
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] ConfigManager.getMessage called with key: " + key);
        }
        
        // Try to get from worldguard settings first
        if (key.startsWith("worldguard.")) {
            String subKey = key.substring("worldguard.".length());
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] Looking for subKey: " + subKey + " in worldGuardSettings");
                plugin.getLogger().info("[DEBUG] worldGuardSettings keys: " + worldGuardSettings.keySet());
            }
            
            if (worldGuardSettings.containsKey(subKey)) {
                Object value = worldGuardSettings.get(subKey);
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] Found value: " + value + " (type: " + value.getClass().getSimpleName() + ")");
                }
                
                if (value instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> messages = (List<String>) value;
                    String result = messages.isEmpty() ? "" : messages.get(0);
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] Returning message: " + result);
                    }
                    return result;
                }
                return value.toString();
            } else {
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().warning("[DEBUG] Key " + subKey + " not found in worldGuardSettings");
                }
            }
        }
        
        // Try to get from messages section
        if (key.startsWith("messages.")) {
            String subKey = key.substring("messages.".length());
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] Looking for message key: " + subKey);
            }
            
            // Get message from config directly
            String messagePath = "messages." + subKey + ".message";
            String message = config.getString(messagePath);
            if (message != null && !message.trim().isEmpty()) {
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] Found message: " + message);
                }
                return message;
            } else {
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().warning("[DEBUG] Message not found for path: " + messagePath);
                }
            }
        }
        
        // Try to get from command triggers
        if (commandTriggers.containsKey(key)) {
            List<String> messages = commandTriggers.get(key);
            return messages.isEmpty() ? "" : messages.get(0);
        }
        
        // Try to get from settings
        if (settings.containsKey(key)) {
            Object value = settings.get(key);
            if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> messages = (List<String>) value;
                return messages.isEmpty() ? "" : messages.get(0);
            }
            return value.toString();
        }
        
        return null;
    }
    
    /**
     * Reload configuration
     */
    public void reloadConfig() {
        try {
            plugin.reloadConfig();
            loadConfiguration();
            plugin.getLogger().info("Конфигурация перезагружена!");
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при перезагрузке конфигурации: " + e.getMessage());
        }
    }
}

package com.allfire.eregions;

import com.allfire.eregions.commands.ERegionCommand;
import com.allfire.eregions.listeners.BoundaryDetectionListener;
import com.allfire.eregions.listeners.PlayerInteractListener;
import com.allfire.eregions.listeners.PlayerChatListener;
import com.allfire.eregions.listeners.PlayerQuitListener;
import com.allfire.eregions.listeners.PlayerDeathListener;
import com.allfire.eregions.managers.*;
import com.allfire.eregions.utils.MessageUtils;
import com.allfire.eregions.utils.PermissionUtils;
import com.allfire.eregions.utils.WorldEditUtils;
import com.allfire.eregions.utils.WorldGuardUtils;
import com.allfire.eregions.integrations.PlaceholderAPIExpansion;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * eRegions - Advanced region management plugin
 * 
 * Features:
 * - Command-based region creation and management
 * - Visual effects through external plugins
 * - Boundary detection with customizable triggers
 * - Integration with WorldGuard, WorldEdit, and other plugins
 * - Comprehensive permission system
 * - Customizable messages and commands
 * 
 * @author AllF1RE
 * @version 1.0.0
 */
public class ERegions extends JavaPlugin {
    
    private static ERegions instance;
    
    // Managers
    private ConfigManager configManager;
    private CommandTriggerManager commandTriggerManager;
    private RegionManager regionManager;
    private SelectionManager selectionManager;
    private BoundaryDetectionManager boundaryDetectionManager;
    private MessageManager messageManager;
    private PermissionManager permissionManager;
    private WorldEditIntegrationManager worldEditIntegrationManager;
    private WorldGuardIntegrationManager worldGuardIntegrationManager;
    private com.allfire.eregions.flags.RegionBorderViewFlag regionBorderViewFlag;
    private com.allfire.eregions.flags.CustomFlags customFlags;
    
    // Utils
    private MessageUtils messageUtils;
    private PermissionUtils permissionUtils;
    private WorldEditUtils worldEditUtils;
    private WorldGuardUtils worldGuardUtils;
    
    @Override
    public void onLoad() {
        // Создаем и регистрируем флаги
        regionBorderViewFlag = new com.allfire.eregions.flags.RegionBorderViewFlag("regionborder-view");
        customFlags = new com.allfire.eregions.flags.CustomFlags(this);
        
        // Регистрируем флаги в WorldGuard
        try {
            com.sk89q.worldguard.WorldGuard.getInstance().getFlagRegistry().register(regionBorderViewFlag);
            getLogger().info("Флаг regionborder-view зарегистрирован в onLoad!");
        } catch (Exception e) {
            getLogger().warning("Ошибка при регистрации флага в onLoad: " + e.getMessage());
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        
        getLogger().info("eRegions запускается...");
        
        // Initialize configuration
        initializeConfiguration();
        
        // Initialize managers
        initializeManagers();
        
        // Initialize utilities
        initializeUtils();
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        // Initialize integrations
        initializeIntegrations();
        
        getLogger().info("eRegions успешно загружен!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("eRegions отключается...");
        
        // Save data
        if (regionManager != null) {
            regionManager.saveData();
        }
        
        // Cleanup
        if (boundaryDetectionManager != null) {
            boundaryDetectionManager.cleanup();
        }
        
        getLogger().info("eRegions отключен!");
    }
    
    /**
     * Initialize configuration
     */
    private void initializeConfiguration() {
        try {
            saveDefaultConfig();
            configManager = new ConfigManager(this);
            configManager.loadConfiguration();
            getLogger().info("Конфигурация загружена успешно!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Ошибка при загрузке конфигурации!", e);
        }
    }
    
    /**
     * Initialize all managers
     */
    private void initializeManagers() {
        try {
            // Initialize core managers
            messageManager = new MessageManager(this);
            permissionManager = new PermissionManager(this);
            // CommandTriggerManager will be initialized after MessageUtils
            selectionManager = new SelectionManager(this);
            regionManager = new RegionManager(this);
            boundaryDetectionManager = new BoundaryDetectionManager(this);
            
            // Initialize integration managers
            worldEditIntegrationManager = new WorldEditIntegrationManager(this);
            worldGuardIntegrationManager = new WorldGuardIntegrationManager(this);
            
            getLogger().info("Менеджеры инициализированы успешно!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Ошибка при инициализации менеджеров!", e);
        }
    }
    
    /**
     * Initialize utility classes
     */
    private void initializeUtils() {
        try {
            messageUtils = new MessageUtils(this);
            permissionUtils = new PermissionUtils(this);
            worldEditUtils = new WorldEditUtils(this);
            worldGuardUtils = new WorldGuardUtils(this);
            
            // Set WorldEditUtils in SelectionManager
            if (selectionManager != null) {
                selectionManager.setWorldEditUtils(worldEditUtils);
            }
            
            // Set WorldGuardUtils in RegionManager
            if (regionManager != null) {
                regionManager.setWorldGuardUtils(worldGuardUtils);
            }
            
            // Initialize CommandTriggerManager after MessageUtils
            commandTriggerManager = new CommandTriggerManager(this);
            
                // WorldGuard flags are now registered in onLoad()
            
            getLogger().info("Утилиты инициализированы успешно!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Ошибка при инициализации утилит!", e);
        }
    }
    
    /**
     * Register plugin commands
     */
    private void registerCommands() {
        try {
            ERegionCommand eregionCommand = new ERegionCommand(this);
            getCommand("eregion").setExecutor(eregionCommand);
            getCommand("eregion").setTabCompleter(eregionCommand);
            
            getLogger().info("Команды зарегистрированы успешно!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Ошибка при регистрации команд!", e);
        }
    }
    
    /**
     * Register event listeners
     */
    private void registerListeners() {
        try {
            getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
            getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
            getServer().getPluginManager().registerEvents(new BoundaryDetectionListener(this), this);
            getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
            getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
            
            getLogger().info("Слушатели зарегистрированы успешно!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Ошибка при регистрации слушателей!", e);
        }
    }
    
    /**
     * Initialize plugin integrations
     */
    private void initializeIntegrations() {
        try {
            // Initialize WorldEdit integration
            if (worldEditIntegrationManager != null) {
                worldEditIntegrationManager.initialize();
            }
            
            // Initialize WorldGuard integration
            if (worldGuardIntegrationManager != null) {
                worldGuardIntegrationManager.initialize();
            }
            
            // Initialize PlaceholderAPI integration
            if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new PlaceholderAPIExpansion(this).register();
                getLogger().info("PlaceholderAPI интеграция загружена!");
            }
            
            getLogger().info("Интеграции инициализированы успешно!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Ошибка при инициализации интеграций!", e);
        }
    }
    
    /**
     * Reload plugin configuration
     */
    public void reloadPlugin() {
        try {
            reloadConfig();
            configManager.loadConfiguration();
            messageManager.reloadMessages();
            getLogger().info("Конфигурация перезагружена!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Ошибка при перезагрузке конфигурации!", e);
        }
    }
    
    // Getters for managers and utilities
    public static ERegions getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public CommandTriggerManager getCommandTriggerManager() {
        return commandTriggerManager;
    }
    
    public RegionManager getRegionManager() {
        return regionManager;
    }
    
    public SelectionManager getSelectionManager() {
        return selectionManager;
    }
    
    public BoundaryDetectionManager getBoundaryDetectionManager() {
        return boundaryDetectionManager;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }
    
    public WorldEditIntegrationManager getWorldEditIntegrationManager() {
        return worldEditIntegrationManager;
    }
    
    public WorldGuardIntegrationManager getWorldGuardIntegrationManager() {
        return worldGuardIntegrationManager;
    }
    
    public MessageUtils getMessageUtils() {
        return messageUtils;
    }
    
    public PermissionUtils getPermissionUtils() {
        return permissionUtils;
    }
    
    public WorldEditUtils getWorldEditUtils() {
        return worldEditUtils;
    }
    
    public WorldGuardUtils getWorldGuardUtils() {
        return worldGuardUtils;
    }
    
    public com.allfire.eregions.flags.RegionBorderViewFlag getRegionBorderViewFlag() {
        return regionBorderViewFlag;
    }
    
    public com.allfire.eregions.flags.CustomFlags getCustomFlags() {
        return customFlags;
    }
}

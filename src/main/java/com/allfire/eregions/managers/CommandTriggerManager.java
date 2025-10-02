package com.allfire.eregions.managers;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * Command Trigger Manager
 * 
 * Handles execution of command triggers from configuration
 * Supports different command types: asPlayer, asConsole, with different message types
 * 
 * @author AllF1RE
 */
public class CommandTriggerManager {
    
    private final ERegions plugin;
    private final MessageUtils messageUtils;
    
    public CommandTriggerManager(ERegions plugin) {
        this.plugin = plugin;
        this.messageUtils = plugin.getMessageUtils();
        
        if (this.messageUtils == null) {
            plugin.getLogger().warning("[DEBUG] MessageUtils is null in CommandTriggerManager constructor!");
        }
    }
    
    /**
     * Execute command trigger
     * 
     * @param triggerName Name of the trigger
     * @param player Player to execute commands for
     * @param regionName Region name (optional)
     */
    public void executeTrigger(String triggerName, Player player, String regionName) {
        executeTrigger(triggerName, player, regionName, null, null);
    }
    
    /**
     * Execute command trigger with region name only
     * 
     * @param triggerName Name of the trigger
     * @param player Player to execute commands for
     * @param regionName Region name
     * @param additionalParam Additional parameter
     */
    public void executeTrigger(String triggerName, Player player, String regionName, String additionalParam) {
        executeTrigger(triggerName, player, regionName, additionalParam, null);
    }
    
    /**
     * Execute command trigger with additional parameters
     * 
     * @param triggerName Name of the trigger
     * @param player Player to execute commands for
     * @param regionName Region name (optional)
     * @param targetPlayer Target player name (optional)
     * @param flagName Flag name (optional)
     */
    public void executeTrigger(String triggerName, Player player, String regionName, String targetPlayer, String flagName) {
        executeTrigger(triggerName, player, regionName, targetPlayer, flagName, null, null, null);
    }
    
    /**
     * Execute command trigger with all parameters
     * 
     * @param triggerName Name of the trigger
     * @param player Player to execute commands for
     * @param regionName Region name (optional)
     * @param targetPlayer Target player name (optional)
     * @param flagName Flag name (optional)
     * @param size Size parameter (optional)
     * @param regionFlag Region flag name (optional)
     * @param stateFlag Flag state (optional)
     */
    public void executeTrigger(String triggerName, Player player, String regionName, String targetPlayer, String flagName, String size, String regionFlag, String stateFlag) {
        executeTrigger(triggerName, player, regionName, targetPlayer, flagName, size, regionFlag, stateFlag, null, null);
    }
    
    /**
     * Execute command trigger with all parameters including point coordinates
     * 
     * @param triggerName Name of the trigger
     * @param player Player to execute commands for
     * @param regionName Region name (optional)
     * @param targetPlayer Target player name (optional)
     * @param flagName Flag name (optional)
     * @param size Size parameter (optional)
     * @param regionFlag Region flag name (optional)
     * @param stateFlag Flag state (optional)
     * @param point1 First point coordinates (optional)
     * @param point2 Second point coordinates (optional)
     */
    public void executeTrigger(String triggerName, Player player, String regionName, String targetPlayer, String flagName, String size, String regionFlag, String stateFlag, String point1, String point2) {
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] ========== CommandTriggerManager.executeTrigger ==========");
            plugin.getLogger().info("[DEBUG] Trigger name: " + triggerName);
            plugin.getLogger().info("[DEBUG] Player: " + player.getName());
            plugin.getLogger().info("[DEBUG] Region: " + (regionName != null ? regionName : "null"));
            plugin.getLogger().info("[DEBUG] Target player: " + (targetPlayer != null ? targetPlayer : "null"));
            plugin.getLogger().info("[DEBUG] Flag: " + (flagName != null ? flagName : "null"));
        }
        
        try {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] Getting command trigger from config...");
            }
            List<String> commands = null;
            
            // Check if it's a boundary trigger first
            if (triggerName.startsWith("boundary-")) {
                Map<String, Object> boundaryTrigger = plugin.getConfigManager().getBoundaryTrigger(triggerName);
                if (boundaryTrigger != null && boundaryTrigger.containsKey("commands")) {
                    commands = (List<String>) boundaryTrigger.get("commands");
                }
            }
            
            // If not found in boundary triggers, try command triggers
            if (commands == null) {
                commands = plugin.getConfigManager().getCommandTrigger(triggerName);
            }
            
            if (commands == null) {
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().warning("[DEBUG] Trigger '" + triggerName + "' not found in config!");
                }
                return;
            }
            
            if (commands.isEmpty()) {
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().warning("[DEBUG] Trigger '" + triggerName + "' is empty!");
                }
                return;
            }
            
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] Found " + commands.size() + " commands for trigger '" + triggerName + "'");
                for (int i = 0; i < commands.size(); i++) {
                    plugin.getLogger().info("[DEBUG] Command " + (i + 1) + ": " + commands.get(i));
                }
            }
            
            // Execute commands with delay
            int delay = 0;
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] Starting command execution loop...");
            }
            
            for (int i = 0; i < commands.size(); i++) {
                String command = commands.get(i);
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] Processing command " + (i + 1) + ": '" + command + "'");
                }
                
                if (command.trim().isEmpty()) {
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] Skipping empty command " + (i + 1));
                    }
                    continue;
                }
                
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] Processing command with placeholders...");
                }
                final String processedCommand = processCommand(command, player, regionName, targetPlayer, flagName, size, regionFlag, stateFlag, point1, point2);
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] Processed command: '" + processedCommand + "'");
                }
                
                if (processedCommand != null && !processedCommand.trim().isEmpty()) {
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] Scheduling command execution: '" + processedCommand + "' with delay: " + delay + " ticks");
                    }
                    
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (plugin.getConfigManager().isDebugMode()) {
                            plugin.getLogger().info("[DEBUG] Executing scheduled command: '" + processedCommand + "'");
                        }
                        executeCommand(processedCommand, player);
                    }, delay);
                    
                    delay += plugin.getConfigManager().getCommandDelay() / 50; // Convert to ticks
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] Next command delay will be: " + delay + " ticks");
                    }
                } else {
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().warning("[DEBUG] Skipping empty processed command: '" + command + "'");
                    }
                }
            }
            
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] All commands scheduled for execution");
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при выполнении триггера '" + triggerName + "'", e);
        }
    }
    
    /**
     * Process command string with placeholders
     * 
     * @param command Original command
     * @param player Player
     * @param regionName Region name
     * @param targetPlayer Target player name
     * @param flagName Flag name
     * @param size Size parameter
     * @param regionFlag Region flag name
     * @param stateFlag Flag state
     * @return Processed command
     */
    private String processCommand(String command, Player player, String regionName, String targetPlayer, String flagName, String size, String regionFlag, String stateFlag, String point1, String point2) {
        String processed = command;
        
        // Replace placeholders
        processed = processed.replace("{player_name}", player.getName());
        processed = processed.replace("{player_displayname}", player.getDisplayName());
        processed = processed.replace("{player_world}", player.getWorld().getName());
        processed = processed.replace("{player_x}", String.valueOf(player.getLocation().getBlockX()));
        processed = processed.replace("{player_y}", String.valueOf(player.getLocation().getBlockY()));
        processed = processed.replace("{player_z}", String.valueOf(player.getLocation().getBlockZ()));
        
        if (regionName != null) {
            processed = processed.replace("{region_name}", regionName);
        }
        
        if (targetPlayer != null) {
            processed = processed.replace("{target_player}", targetPlayer);
        }
        
        if (flagName != null) {
            processed = processed.replace("{flag_name}", flagName);
        }
        
        if (size != null) {
            processed = processed.replace("{size}", size);
        }
        
        if (regionFlag != null) {
            processed = processed.replace("{region_flag}", regionFlag);
        }
        
        if (stateFlag != null) {
            processed = processed.replace("{state_flag}", stateFlag);
        }
        
        if (point1 != null) {
            processed = processed.replace("{point_1}", point1);
        }
        
        if (point2 != null) {
            processed = processed.replace("{point_2}", point2);
        }
        
        return processed;
    }
    
    /**
     * Execute processed command
     * 
     * @param command Processed command
     * @param player Player
     */
    private void executeCommand(String command, Player player) {
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] ========== CommandTriggerManager.executeCommand ==========");
            plugin.getLogger().info("[DEBUG] Command to execute: '" + command + "'");
            plugin.getLogger().info("[DEBUG] Player: " + player.getName());
        }
        
        try {
            if (command.startsWith("asPlayer!")) {
                // Execute as player
                String playerCommand = command.substring(9).trim();
                if (playerCommand.startsWith("/")) {
                    playerCommand = playerCommand.substring(1);
                }

                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] Executing as player '" + player.getName() + "': " + playerCommand);
                    plugin.getLogger().info("[DEBUG] Player is online: " + player.isOnline());
                }

                if (player.isOnline()) {
                    // Special handling for svis commands - add delay
                    if (playerCommand.startsWith("svis ")) {
                        if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] Detected svis command, adding delay");
                }
                        final String finalPlayerCommand = playerCommand;
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            boolean success = player.performCommand(finalPlayerCommand);
                            if (plugin.getConfigManager().isDebugMode()) {
                                plugin.getLogger().info("[DEBUG] Delayed svis command execution result: " + success);
                            }
                        }, 5L); // 5 ticks delay
                    } else {
                        // Use player.performCommand to execute the command as the player
                        boolean success = player.performCommand(playerCommand);
                        if (plugin.getConfigManager().isDebugMode()) {
                            plugin.getLogger().info("[DEBUG] Command execution result: " + success);
                        }
                    }
                } else {
                    plugin.getLogger().warning("[DEBUG] Player is not online, cannot execute command!");
                }
                
            } else if (command.startsWith("asConsole!")) {
                // Execute as console
                String consoleCommand = command.substring(10).trim();
                if (consoleCommand.startsWith("/")) {
                    consoleCommand = consoleCommand.substring(1);
                }
                
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] Executing as console: " + consoleCommand);
                }
                
                boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCommand);
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] Console command execution result: " + success);
                }
                
            } else if (command.startsWith("asPlayer! msg ")) {
                // Send message to player
                String message = command.substring(14).trim();
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] Sending message to player: " + message);
                }
                messageUtils.sendMessage(player, message);
                
            } else if (command.startsWith("asConsole! msg ")) {
                // Send message to console
                String message = command.substring(15).trim();
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] Sending message to console: " + message);
                }
                plugin.getLogger().info(message);
                
            } else if (command.startsWith("asConsole! say ")) {
                // Broadcast message
                String message = command.substring(15).trim();
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] Broadcasting message: " + message);
                }
                Bukkit.broadcastMessage(messageUtils.colorize(message));
                
            } else if (command.startsWith("chat! ")) {
                // Send chat message to player
                String message = command.substring(6).trim();
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] Sending chat message to player: " + message);
                }
                if (messageUtils != null) {
                    messageUtils.sendMessage(player, message);
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] Chat message sent successfully");
                    }
                } else {
                    plugin.getLogger().warning("[DEBUG] MessageUtils is null, cannot send chat message!");
                }
                
            } else if (command.startsWith("actionbar! ")) {
                // Send actionbar message to player
                String message = command.substring(11).trim();
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] Sending actionbar message to player: " + message);
                }
                if (messageUtils != null) {
                    messageUtils.sendActionBar(player, message);
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] Actionbar message sent successfully");
                    }
                } else {
                    plugin.getLogger().warning("[DEBUG] MessageUtils is null, cannot send actionbar message!");
                }
                
            } else if (command.startsWith("title! ")) {
                // Send title to player
                String message = command.substring(7).trim();
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] Sending title to player: " + message);
                }
                if (messageUtils != null) {
                    messageUtils.sendMessage(player, "title! " + message);
                }
                
            } else if (command.startsWith("subtitle! ")) {
                // Send subtitle to player
                String message = command.substring(10).trim();
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] Sending subtitle to player: " + message);
                }
                if (messageUtils != null) {
                    messageUtils.sendMessage(player, "subtitle! " + message);
                }
                
            } else {
                // Default: execute as console
                if (command.startsWith("/")) {
                    command = command.substring(1);
                }
                
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] Executing as console (default): " + command);
                }
                boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] Default console command execution result: " + success);
                }
            }
            
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] ========== CommandTriggerManager.executeCommand END ==========");
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка при выполнении команды: " + command, e);
        }
    }
}

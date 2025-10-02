package com.allfire.eregions.utils;

import com.allfire.eregions.ERegions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Message Utilities
 * 
 * Handles message formatting, colorization, and sending
 * Supports vanilla colors, HEX colors, and MiniMessage format
 * 
 * @author AllF1RE
 */
public class MessageUtils {
    
    private final ERegions plugin;
    private final Pattern hexPattern = Pattern.compile("\\{#([A-Fa-f0-9]{6})\\}");
    
    public MessageUtils(ERegions plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Send message to command sender
     * 
     * @param sender Command sender
     * @param message Message to send
     */
    public void sendMessage(CommandSender sender, String message) {
        if (message == null || message.trim().isEmpty()) {
            if (plugin.getConfigManager().isMessageLoggingEnabled()) {
                plugin.getLogger().info("[DEBUG] Skipping empty message for " + sender.getName());
            }
            return;
        }
        
        // Debug logging
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] sendMessage called:");
            plugin.getLogger().info("[DEBUG] Sender: " + sender.getName());
            plugin.getLogger().info("[DEBUG] Message: '" + message + "'");
        }
        
        // Check if message contains command prefix
        if (message.startsWith("chat!")) {
            // Remove chat! prefix and send as regular message
            message = message.substring(5);
        } else if (message.startsWith("actionbar;")) {
            // Send as action bar message with custom duration
            int semisemicolonIndex = message.indexOf("!");
            if (semisemicolonIndex > 0) {
                String durationStr = message.substring(10, semisemicolonIndex);
                message = message.substring(semisemicolonIndex + 1);
                try {
                    int durationSeconds = Integer.parseInt(durationStr);
                    if (sender instanceof Player) {
                        sendActionBar((Player) sender, message, durationSeconds);
                        return;
                    }
                } catch (NumberFormatException e) {
                    // Invalid duration, fall back to default
                }
            }
        } else if (message.startsWith("actionbar!")) {
            // Send as action bar message (default 1 second)
            message = message.substring(10);
            if (sender instanceof Player) {
                sendActionBar((Player) sender, message, 1);
                return;
            }
        } else if (message.startsWith("title;")) {
            // Send as title with custom duration
            int semicolonIndex = message.indexOf("!");
            if (semicolonIndex > 0) {
                String durationStr = message.substring(6, semicolonIndex);
                message = message.substring(semicolonIndex + 1);
                try {
                    int durationSeconds = Integer.parseInt(durationStr);
                    if (sender instanceof Player) {
                        sendTitle((Player) sender, message, "", durationSeconds);
                        return;
                    }
                } catch (NumberFormatException e) {
                    // Invalid duration, fall back to default
                }
            }
        } else if (message.startsWith("title!")) {
            // Send as title (default 1 second)
            message = message.substring(6);
            if (sender instanceof Player) {
                // Check for combined title+subtitle format (like CMI)
                if (message.contains("%subtitle%")) {
                    String[] parts = message.split("%subtitle%", 2);
                    String title = parts[0].trim();
                    String subtitle = parts.length > 1 ? parts[1].trim() : "";
                    sendTitle((Player) sender, title, subtitle, 1);
                } else {
                    sendTitle((Player) sender, message, "", 1);
                }
                return;
            }
        } else if (message.startsWith("subtitle;")) {
            // Send as subtitle with custom duration
            int semicolonIndex = message.indexOf("!");
            if (semicolonIndex > 0) {
                String durationStr = message.substring(9, semicolonIndex);
                message = message.substring(semicolonIndex + 1);
                try {
                    int durationSeconds = Integer.parseInt(durationStr);
                    if (sender instanceof Player) {
                        sendTitle((Player) sender, "", message, durationSeconds);
                        return;
                    }
                } catch (NumberFormatException e) {
                    // Invalid duration, fall back to default
                }
            }
        } else if (message.startsWith("subtitle!")) {
            // Send as subtitle (default 1 second)
            message = message.substring(9);
            if (sender instanceof Player) {
                sendTitle((Player) sender, "", message, 1);
                return;
            }
        }
        
        String formattedMessage = formatMessage(message);
        
        if (plugin.getConfigManager().isMessageLoggingEnabled()) {
            plugin.getLogger().info("[DEBUG] Sending message to " + sender.getName() + ": " + formattedMessage);
        }
        
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.sendMessage(formattedMessage);
        } else {
            sender.sendMessage(formattedMessage);
        }
    }
    
    /**
     * Send message to command sender with placeholders
     * 
     * @param sender Command sender
     * @param messageKey Message key from config
     * @param placeholders Placeholder pairs (key, value, key, value, ...)
     */
    public void sendMessage(CommandSender sender, String messageKey, String... placeholders) {
        if (messageKey == null || messageKey.trim().isEmpty()) {
            return;
        }
        
        // Get message from config
        String message = plugin.getConfigManager().getMessage(messageKey);
        if (message == null || message.trim().isEmpty()) {
            plugin.getLogger().warning("Message not found for key: " + messageKey);
            return;
        }
        
        // Apply placeholders
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                String placeholder = placeholders[i];
                String value = placeholders[i + 1];
                // Replace {placeholder} format
                message = message.replace("{" + placeholder + "}", value != null ? value : "");
            }
        }
        
        sendMessage(sender, message);
    }
    
    /**
     * Send action bar message to player
     * 
     * @param player Player to send message to
     * @param message Message to send
     */
    public void sendActionBar(Player player, String message) {
        sendActionBar(player, message, 1); // Default 1 second
    }
    
    /**
     * Send action bar message to player with custom duration
     * 
     * @param player Player to send message to
     * @param message Message to send
     * @param durationSeconds Duration in seconds
     */
    public void sendActionBar(Player player, String message, int durationSeconds) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        
        String formattedMessage = formatMessage(message);
        
        // Send action bar message
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
            TextComponent.fromLegacyText(formattedMessage));
        
        // Schedule removal after duration
        if (durationSeconds > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Send empty action bar to clear it
                player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
                    TextComponent.fromLegacyText(""));
            }, durationSeconds * 20L); // Convert seconds to ticks
        }
    }
    
    /**
     * Send title to player with custom duration in seconds
     * 
     * @param player Player to send title to
     * @param title Title text
     * @param subtitle Subtitle text
     * @param durationSeconds Duration in seconds
     */
    public void sendTitle(Player player, String title, String subtitle, int durationSeconds) {
        // Convert seconds to ticks (20 ticks = 1 second)
        int fadeIn = 10;  // 0.5 seconds fade in
        int stay = durationSeconds * 20;  // Duration in ticks
        int fadeOut = 10; // 0.5 seconds fade out
        
        sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
    }
    
    /**
     * Send title to player
     * 
     * @param player Player to send title to
     * @param title Title text
     * @param subtitle Subtitle text
     * @param fadeIn Fade in ticks
     * @param stay Stay ticks
     * @param fadeOut Fade out ticks
     */
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        String formattedTitle = (title != null && !title.trim().isEmpty()) ? formatMessage(title) : "";
        String formattedSubtitle = (subtitle != null && !subtitle.trim().isEmpty()) ? formatMessage(subtitle) : "";
        
        // Debug logging
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] sendTitle called:");
            plugin.getLogger().info("[DEBUG] Player: " + player.getName());
            plugin.getLogger().info("[DEBUG] Title: '" + formattedTitle + "'");
            plugin.getLogger().info("[DEBUG] Subtitle: '" + formattedSubtitle + "'");
            plugin.getLogger().info("[DEBUG] FadeIn: " + fadeIn + ", Stay: " + stay + ", FadeOut: " + fadeOut);
        }
        
        // Try modern Adventure API first (Paper 1.21+)
        try {
            // Create Adventure components
            Component titleComponent = formattedTitle.isEmpty() ? Component.empty() : 
                LegacyComponentSerializer.legacyAmpersand().deserialize(formattedTitle);
            Component subtitleComponent = formattedSubtitle.isEmpty() ? Component.empty() : 
                LegacyComponentSerializer.legacyAmpersand().deserialize(formattedSubtitle);
            
            // Create title with times
            Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeIn * 50L),    // Convert ticks to milliseconds
                Duration.ofMillis(stay * 50L),       // Convert ticks to milliseconds
                Duration.ofMillis(fadeOut * 50L)     // Convert ticks to milliseconds
            );
            
            Title adventureTitle = Title.title(titleComponent, subtitleComponent, times);
            
            // Try to use modern API through reflection or direct call
            // For now, we'll use the legacy API with Adventure components
            player.sendTitle(
                LegacyComponentSerializer.legacyAmpersand().serialize(titleComponent),
                LegacyComponentSerializer.legacyAmpersand().serialize(subtitleComponent),
                fadeIn, stay, fadeOut
            );
            
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] sendTitle executed successfully with Adventure API");
            }
            return;
            
        } catch (Exception e) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] Adventure API failed, falling back to legacy API: " + e.getMessage());
            }
        }
        
        // Fallback to legacy Bukkit API
        try {
            player.sendTitle(formattedTitle, formattedSubtitle, fadeIn, stay, fadeOut);
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[DEBUG] sendTitle executed successfully with legacy API");
            }
        } catch (Exception e) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().severe("[DEBUG] sendTitle error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Format message with colors and placeholders
     * 
     * @param message Original message
     * @return Formatted message
     */
    public String formatMessage(String message) {
        if (message == null) {
            return "";
        }
        
        // Replace newlines
        message = message.replace("\\n", "\n");
        
        // Apply HEX colors
        message = applyHexColors(message);
        
        // Apply vanilla colors
        message = colorize(message);
        
        return message;
    }
    
    /**
     * Apply HEX colors to message
     * 
     * @param message Message to colorize
     * @return Colorized message
     */
    private String applyHexColors(String message) {
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            try {
                // Try modern ChatColor.of first
                ChatColor color = ChatColor.of("#" + hexCode);
                matcher.appendReplacement(buffer, color.toString());
            } catch (Exception e) {
                try {
                    // Fallback to legacy color codes for common colors
                    String legacyColor = convertHexToLegacy(hexCode);
                    if (legacyColor != null) {
                        matcher.appendReplacement(buffer, legacyColor);
                    } else {
                        // Invalid hex color, keep original
                        matcher.appendReplacement(buffer, matcher.group(0));
                    }
                } catch (Exception e2) {
                    // Invalid hex color, keep original
                    matcher.appendReplacement(buffer, matcher.group(0));
                }
            }
        }
        
        matcher.appendTail(buffer);
        return buffer.toString();
    }
    
    /**
     * Convert hex color to legacy color code
     * 
     * @param hexCode Hex color code (without #)
     * @return Legacy color code or null if not supported
     */
    private String convertHexToLegacy(String hexCode) {
        switch (hexCode.toUpperCase()) {
            case "000000": return "§0"; // Black
            case "0000FF": return "§1"; // Dark Blue
            case "00AA00": return "§2"; // Dark Green
            case "00AAAA": return "§3"; // Dark Aqua
            case "AA0000": return "§4"; // Dark Red
            case "AA00AA": return "§5"; // Dark Purple
            case "FFAA00": return "§6"; // Gold
            case "AAAAAA": return "§7"; // Gray
            case "555555": return "§8"; // Dark Gray
            case "5555FF": return "§9"; // Blue
            case "55FF55": return "§a"; // Green
            case "55FFFF": return "§b"; // Aqua
            case "FF5555": return "§c"; // Red
            case "FF55FF": return "§d"; // Light Purple
            case "FFFF55": return "§e"; // Yellow
            case "FFFFFF": return "§f"; // White
            case "00FF00": return "§a"; // Green (for #00FF00)
            default: return null;
        }
    }
    
    /**
     * Apply vanilla color codes
     * 
     * @param message Message to colorize
     * @return Colorized message
     */
    public String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    /**
     * Send debug message if debug mode is enabled
     * 
     * @param message Debug message
     */
    public void debug(String message) {
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }
    
    /**
     * Send warning message
     * 
     * @param message Warning message
     */
    public void warning(String message) {
        plugin.getLogger().warning(message);
    }
    
    /**
     * Send error message
     * 
     * @param message Error message
     */
    public void error(String message) {
        plugin.getLogger().severe(message);
    }
    
    /**
     * Send configurable message from config
     * 
     * @param sender Command sender
     * @param messageKey Message key from config
     */
    public void sendConfigurableMessage(CommandSender sender, String messageKey) {
        try {
            // Get message from config
            String message = plugin.getConfig().getString("messages." + messageKey + ".message");
            boolean enabled = plugin.getConfig().getBoolean("messages." + messageKey + ".enabled", true);
            
            if (!enabled) {
                return; // Message is disabled
            }
            
            if (message == null || message.trim().isEmpty()) {
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] Message key '" + messageKey + "' not found in config");
                }
                return;
            }
            
            // Send the message
            sendMessage(sender, message);
            
        } catch (Exception e) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().severe("[DEBUG] Error sending configurable message '" + messageKey + "': " + e.getMessage());
            }
        }
    }
}

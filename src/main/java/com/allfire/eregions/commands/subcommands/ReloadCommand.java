package com.allfire.eregions.commands.subcommands;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Reload subcommand
 * 
 * Handles /eregion reload command
 * Reloads plugin configuration
 * 
 * @author AllF1RE
 */
public class ReloadCommand extends SubCommand {
    
    public ReloadCommand(ERegions plugin) {
        super(plugin, "reload", "eregions.reload", 
              "Перезагрузить конфигурацию плагина", "/eregion reload");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            // Reload plugin configuration
            plugin.reloadPlugin();
            
            plugin.getMessageUtils().sendMessage(sender, "config-reloaded-success");
            
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при перезагрузке конфигурации: " + e.getMessage());
            plugin.getMessageUtils().sendMessage(sender, "config-reload-error");
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        // No tab completion needed for reload command
        return new ArrayList<>();
    }
}


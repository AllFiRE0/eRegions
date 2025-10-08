package com.allfire.eregions.commands.subcommands;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Help subcommand
 * 
 * Handles /eregion help command
 * Shows help information
 * 
 * @author AllF1RE
 */
public class HelpCommand extends SubCommand {
    
    public HelpCommand(ERegions plugin) {
        super(plugin, "help", "eregions.help", 
              "Показать справку по командам", "/eregion help");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        // Send help messages
        plugin.getMessageUtils().sendMessage(sender, "help-title");
        plugin.getMessageUtils().sendMessage(sender, "help-empty-line");
        
        plugin.getMessageUtils().sendMessage(sender, "help-create");
        plugin.getMessageUtils().sendMessage(sender, "help-cancel");
        plugin.getMessageUtils().sendMessage(sender, "help-remove");
        plugin.getMessageUtils().sendMessage(sender, "help-member");
        plugin.getMessageUtils().sendMessage(sender, "help-owner");
        plugin.getMessageUtils().sendMessage(sender, "help-flag");
        plugin.getMessageUtils().sendMessage(sender, "help-flags");
        plugin.getMessageUtils().sendMessage(sender, "help-move");
        plugin.getMessageUtils().sendMessage(sender, "help-size");
        plugin.getMessageUtils().sendMessage(sender, "help-reload");
        plugin.getMessageUtils().sendMessage(sender, "help-help");
        plugin.getMessageUtils().sendMessage(sender, "help-empty-line");
        
        plugin.getMessageUtils().sendMessage(sender, "help-creation-title");
        plugin.getMessageUtils().sendMessage(sender, "help-creation-step1");
        plugin.getMessageUtils().sendMessage(sender, "help-creation-step2");
        plugin.getMessageUtils().sendMessage(sender, "help-creation-step3");
        plugin.getMessageUtils().sendMessage(sender, "help-creation-step4");
        plugin.getMessageUtils().sendMessage(sender, "help-empty-line");
        
        plugin.getMessageUtils().sendMessage(sender, "help-management-title");
        plugin.getMessageUtils().sendMessage(sender, "help-management-intro");
        plugin.getMessageUtils().sendMessage(sender, "help-management-move");
        plugin.getMessageUtils().sendMessage(sender, "help-management-size");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        // No tab completion needed for help command
        return new ArrayList<>();
    }
}

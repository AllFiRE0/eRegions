package com.allfire.eregions.commands.subcommands;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin command handler
 * 
 * Handles /eregion admin <subcommand> commands
 * 
 * @author AllF1RE
 */
public class AdminCommand extends SubCommand {
    
    private final Map<String, SubCommand> adminSubcommands;
    
    public AdminCommand(ERegions plugin) {
        super(plugin, "admin", "eregions.admin", 
              "Админские команды", "/eregion admin <subcommand>");
        
        this.adminSubcommands = new HashMap<>();
        
        // Register admin subcommands
        adminSubcommands.put("flag", new AdminFlagCommand(plugin));
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            if (sender instanceof org.bukkit.entity.Player) {
                sendUsage((org.bukkit.entity.Player) sender);
            } else {
                sender.sendMessage("Использование: " + getUsage());
            }
            return;
        }
        
        String subcommandName = args[0].toLowerCase();
        SubCommand subcommand = adminSubcommands.get(subcommandName);
        
        if (subcommand == null) {
            sendError(sender, "Неизвестная админская команда: " + subcommandName);
            sendError(sender, "Доступные команды: " + String.join(", ", adminSubcommands.keySet()));
            return;
        }
        
        // Check permission
        if (!sender.hasPermission(subcommand.getPermission())) {
            sendError(sender, "У вас нет прав для выполнения команды: " + subcommandName);
            return;
        }
        
        // Execute subcommand with remaining args
        String[] remainingArgs = new String[args.length - 1];
        System.arraycopy(args, 1, remainingArgs, 0, args.length - 1);
        subcommand.execute(sender, remainingArgs);
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Tab complete admin subcommands
            for (String subcommandName : adminSubcommands.keySet()) {
                if (subcommandName.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subcommandName);
                }
            }
        } else if (args.length > 1) {
            // Tab complete for specific admin subcommand
            String subcommandName = args[0].toLowerCase();
            SubCommand subcommand = adminSubcommands.get(subcommandName);
            
            if (subcommand != null) {
                String[] remainingArgs = new String[args.length - 1];
                System.arraycopy(args, 1, remainingArgs, 0, args.length - 1);
                return subcommand.onTabComplete(sender, remainingArgs);
            }
        }
        
        return completions;
    }
}

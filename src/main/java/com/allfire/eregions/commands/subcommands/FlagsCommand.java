package com.allfire.eregions.commands.subcommands;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.commands.SubCommand;
import com.allfire.eregions.utils.WorldGuardUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Flags command handler
 *
 * @author AllF1RE
 */
public class FlagsCommand extends SubCommand {

    public FlagsCommand(ERegions plugin) {
        super(plugin, "flags", "eregions.region.flags", "Показать флаги региона", "/eregion flags <region_name>");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эта команда доступна только игрокам!");
            return;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            sendError(player, "Использование: /eregion flags <region_name>");
            return;
        }

        String regionName = args[0];
        WorldGuardUtils worldGuardUtils = plugin.getWorldGuardUtils();

        // Check if player owns the region
        if (!worldGuardUtils.isRegionOwner(player, regionName)) {
            sendError(player, "Вы не владеете регионом " + regionName + "!");
            return;
        }

        // Get region flags
        List<String> flags = worldGuardUtils.getRegionFlags(player.getWorld(), regionName);
        
        if (flags.isEmpty()) {
            sendSuccess(player, "У региона " + regionName + " нет флагов.");
            return;
        }

        // Format flags as string
        String flagsString = String.join(", ", flags);
        
        // Send message with placeholders
        plugin.getMessageUtils().sendMessage(player, "worldguard.list-flags", 
            "region_name", regionName,
            "region_flags", flagsString);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            // Return player's owned regions
            return plugin.getWorldGuardUtils().getPlayerOwnedRegions(player);
        }

        return new ArrayList<>();
    }
}


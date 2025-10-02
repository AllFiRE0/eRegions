package com.allfire.eregions.commands.subcommands;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.commands.SubCommand;
import com.allfire.eregions.managers.CommandTriggerManager;
import com.allfire.eregions.managers.SelectionManager;
import com.allfire.eregions.utils.WorldEditUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Move region subcommand
 * 
 * Handles /eregion move <+/-distance> commands
 * Moves the currently selected region
 * 
 * @author AllF1RE
 */
public class MoveCommand extends SubCommand {
    
    private final CommandTriggerManager commandTriggerManager;
    private final SelectionManager selectionManager;
    private final WorldEditUtils worldEditUtils;
    private final Pattern distancePattern = Pattern.compile("^[+-]?\\d+$");
    
    public MoveCommand(ERegions plugin) {
        super(plugin, "move", "eregions.region.move", 
              "Переместить выделенную область", "/eregion move <+/-distance>");
        
        this.commandTriggerManager = plugin.getCommandTriggerManager();
        this.selectionManager = plugin.getSelectionManager();
        this.worldEditUtils = plugin.getWorldEditUtils();
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendError(sender, "Эта команда может быть выполнена только игроком!");
            return;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 1) {
            sendUsage(player);
            return;
        }
        
        String distanceStr = args[0];
        
        // Validate distance format
        if (!distancePattern.matcher(distanceStr).matches()) {
            sendError(player, "Неверный формат расстояния! Используйте &e+10 &cили &e-5&c!");
            return;
        }
        
        int distance = Integer.parseInt(distanceStr);
        
        // Check if player has a completed selection
        SelectionManager.SelectionData selectionData = selectionManager.getActiveSelection(player);
        if (selectionData == null || !selectionData.isCompleted()) {
            sendError(player, "У вас нет завершенного выделения! Сначала выделите область.");
            return;
        }
        
        // Move selection
        try {
            Location pos1 = selectionData.getPos1();
            Location pos2 = selectionData.getPos2();
            
            // Calculate direction based on player's facing direction
            float yaw = player.getLocation().getYaw();
            double radians = Math.toRadians(yaw);
            
            double deltaX = -Math.sin(radians) * distance;
            double deltaZ = Math.cos(radians) * distance;
            
            // Move both positions
            Location newPos1 = pos1.clone().add(deltaX, 0, deltaZ);
            Location newPos2 = pos2.clone().add(deltaX, 0, deltaZ);
            
            // Update selection data
            selectionData.setPos1(newPos1);
            selectionData.setPos2(newPos2);
            
            // Update WorldEdit selection
            worldEditUtils.setSelection(player, newPos1, newPos2);
            
            // Update SelectionVisualizer to show new boundaries
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Bukkit.dispatchCommand(player, "svis we");
            }, 5L);
            
            // Format coordinates for the trigger
            String point1 = String.format("%.0f,%.0f,%.0f", newPos1.getX(), newPos1.getY(), newPos1.getZ());
            String point2 = String.format("%.0f,%.0f,%.0f", newPos2.getX(), newPos2.getY(), newPos2.getZ());
            
            commandTriggerManager.executeTrigger("region-moved", player, null, null, null, String.valueOf(Math.abs(distance)), null, null, point1, point2);
            sendSuccess(player, "Выделенная область перемещена на &e" + Math.abs(distance) + " &aблоков!");
            
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при перемещении выделения: " + e.getMessage());
            sendError(player, "Произошла ошибка при перемещении выделения!");
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Tab complete distance examples
            completions.add("+10");
            completions.add("-5");
            completions.add("+1");
            completions.add("-1");
        }
        
        return completions;
    }
}


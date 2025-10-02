package com.allfire.eregions.commands.subcommands;

import com.allfire.eregions.ERegions;
import com.allfire.eregions.commands.SubCommand;
import com.allfire.eregions.managers.CommandTriggerManager;
import com.allfire.eregions.managers.SelectionManager;
import com.allfire.eregions.utils.WorldEditUtils;
import com.allfire.eregions.utils.WorldGuardUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Resize region subcommand
 * 
 * Handles /eregion size <up/down/face> <+/-distance> commands
 * Resizes the currently selected region
 * 
 * @author AllF1RE
 */
public class SizeCommand extends SubCommand {
    
    private final CommandTriggerManager commandTriggerManager;
    private final SelectionManager selectionManager;
    private final WorldEditUtils worldEditUtils;
    private final WorldGuardUtils worldGuardUtils;
    private final Pattern distancePattern = Pattern.compile("^[+-]?\\d+$");
    
    public SizeCommand(ERegions plugin) {
        super(plugin, "size", "eregions.region.size", 
              "Изменить размер выделенной области", "/eregion size <up/down/face> <+/-distance>");
        
        this.commandTriggerManager = plugin.getCommandTriggerManager();
        this.selectionManager = plugin.getSelectionManager();
        this.worldEditUtils = plugin.getWorldEditUtils();
        this.worldGuardUtils = plugin.getWorldGuardUtils();
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendError(sender, "Эта команда может быть выполнена только игроком!");
            return;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 2) {
            sendUsage(player);
            return;
        }
        
        String direction = args[0].toLowerCase();
        String distanceStr = args[1];
        
        // Validate direction
        if (!direction.equals("up") && !direction.equals("down") && !direction.equals("face")) {
            sendError(player, "Направление должно быть &eup&c, &edown &cили &eface&c!");
            return;
        }
        
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
        
        // Resize selection
        try {
            Location pos1 = selectionData.getPos1();
            Location pos2 = selectionData.getPos2();
            
            Location newPos1 = pos1.clone();
            Location newPos2 = pos2.clone();
            
            switch (direction) {
                case "up":
                    // Expand upward
                    if (distance > 0) {
                        // Положительное значение - расширяем вверх
                        newPos2.setY(Math.max(pos1.getY(), pos2.getY()) + distance);
                    } else {
                        // Отрицательное значение - сжимаем сверху (опускаем верхнюю границу)
                        newPos2.setY(Math.max(pos1.getY(), pos2.getY()) + distance);
                    }
                    break;
                    
                case "down":
                    // Expand downward
                    if (distance > 0) {
                        // Положительное значение - расширяем вниз
                        newPos1.setY(Math.min(pos1.getY(), pos2.getY()) - distance);
                    } else {
                        // Отрицательное значение - сжимаем снизу (поднимаем нижнюю границу)
                        newPos1.setY(Math.min(pos1.getY(), pos2.getY()) - distance);
                    }
                    break;
                    
                case "face":
                    // Expand in player's facing direction
                    float yaw = player.getLocation().getYaw();
                    double radians = Math.toRadians(yaw);
                    
                    double deltaX = -Math.sin(radians) * distance;
                    double deltaZ = Math.cos(radians) * distance;
                    
                    if (distance > 0) {
                        // Expand forward
                        if (deltaX > 0) {
                            newPos2.setX(Math.max(pos1.getX(), pos2.getX()) + deltaX);
                        } else {
                            newPos1.setX(Math.min(pos1.getX(), pos2.getX()) + deltaX);
                        }
                        
                        if (deltaZ > 0) {
                            newPos2.setZ(Math.max(pos1.getZ(), pos2.getZ()) + deltaZ);
                        } else {
                            newPos1.setZ(Math.min(pos1.getZ(), pos2.getZ()) + deltaZ);
                        }
                    } else {
                        // Shrink backward
                        if (deltaX > 0) {
                            newPos1.setX(Math.min(pos1.getX(), pos2.getX()) + deltaX);
                        } else {
                            newPos2.setX(Math.max(pos1.getX(), pos2.getX()) + deltaX);
                        }
                        
                        if (deltaZ > 0) {
                            newPos1.setZ(Math.min(pos1.getZ(), pos2.getZ()) + deltaZ);
                        } else {
                            newPos2.setZ(Math.max(pos1.getZ(), pos2.getZ()) + deltaZ);
                        }
                    }
                    break;
            }
            
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
            
            commandTriggerManager.executeTrigger("region-resized", player, null, null, null, String.valueOf(Math.abs(distance)), null, null, point1, point2);
            sendSuccess(player, "Размер выделенной области изменен на &e" + Math.abs(distance) + " &aблоков в направлении &e" + direction + "&a!");
            
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при изменении размера выделения: " + e.getMessage());
            sendError(player, "Произошла ошибка при изменении размера выделения!");
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Tab complete directions
            if ("up".startsWith(args[0].toLowerCase())) {
                completions.add("up");
            }
            if ("down".startsWith(args[0].toLowerCase())) {
                completions.add("down");
            }
            if ("face".startsWith(args[0].toLowerCase())) {
                completions.add("face");
            }
        } else if (args.length == 2) {
            // Tab complete distance examples
            completions.add("+10");
            completions.add("-5");
            completions.add("+1");
            completions.add("-1");
        }
        
        return completions;
    }
}

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
        plugin.getMessageUtils().sendMessage(sender, "&6=== eRegions - Справка по командам ===");
        plugin.getMessageUtils().sendMessage(sender, "");
        
        plugin.getMessageUtils().sendMessage(sender, "&e/eregion create &7- Создать новый регион");
        plugin.getMessageUtils().sendMessage(sender, "&e/eregion cancel &7- Отменить создание региона");
        plugin.getMessageUtils().sendMessage(sender, "&e/eregion remove <region> &7- Удалить регион");
        plugin.getMessageUtils().sendMessage(sender, "&e/eregion member <add/remove> <region> <player> &7- Управление участниками");
        plugin.getMessageUtils().sendMessage(sender, "&e/eregion owner <add/remove> <region> <player> &7- Управление владельцами");
        plugin.getMessageUtils().sendMessage(sender, "&e/eregion flag <add/remove> <region> <flag> &7- Управление флагами");
        plugin.getMessageUtils().sendMessage(sender, "&e/eregion flags <region> &7- Показать все флаги региона");
        plugin.getMessageUtils().sendMessage(sender, "&e/eregion move <+/-distance> &7- Переместить выделенную область");
        plugin.getMessageUtils().sendMessage(sender, "&e/eregion size <up/down/face> <+/-distance> &7- Изменить размер выделенной области");
        plugin.getMessageUtils().sendMessage(sender, "&e/eregion reload &7- Перезагрузить конфигурацию");
        plugin.getMessageUtils().sendMessage(sender, "&e/eregion help &7- Показать эту справку");
        plugin.getMessageUtils().sendMessage(sender, "");
        
        plugin.getMessageUtils().sendMessage(sender, "&6=== Процесс создания региона ===");
        plugin.getMessageUtils().sendMessage(sender, "&71. Используйте &e/eregion create");
        plugin.getMessageUtils().sendMessage(sender, "&72. Выберите две точки: &eSHIFT+ЛКМ &7и &eSHIFT+ПКМ");
        plugin.getMessageUtils().sendMessage(sender, "&73. Напишите название региона в чат");
        plugin.getMessageUtils().sendMessage(sender, "&74. Для отмены напишите &cотмена &7или используйте &e/eregion cancel");
        plugin.getMessageUtils().sendMessage(sender, "");
        
        plugin.getMessageUtils().sendMessage(sender, "&6=== Управление выделенной областью ===");
        plugin.getMessageUtils().sendMessage(sender, "&7После создания региона вы можете:");
        plugin.getMessageUtils().sendMessage(sender, "&7- Перемещать: &e/eregion move +10 &7(на 10 блоков вперед)");
        plugin.getMessageUtils().sendMessage(sender, "&7- Изменять размер: &e/eregion size up +5 &7(увеличить вверх на 5 блоков)");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        // No tab completion needed for help command
        return new ArrayList<>();
    }
}

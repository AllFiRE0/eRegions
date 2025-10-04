// Это пример простого Minecraft плагина
// Не волнуйтесь, если не понимаете - я объясню каждую строчку!

package com.example.simpleplugin; // Папка, где лежит наш код

// Импорты - это как сказать "я буду использовать эти готовые функции"
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

// Главный класс нашего плагина
public class SimplePluginExample extends JavaPlugin implements Listener {
    
    // Эта функция запускается, когда плагин включается
    @Override
    public void onEnable() {
        // Регистрируем наш плагин как слушатель событий
        getServer().getPluginManager().registerEvents(this, this);
        
        // Выводим сообщение в консоль сервера
        getLogger().info("Простой плагин включен!");
    }
    
    // Эта функция запускается, когда плагин выключается
    @Override
    public void onDisable() {
        getLogger().info("Простой плагин выключен!");
    }
    
    // Эта функция обрабатывает команды игроков
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Проверяем, что команда называется "hello"
        if (command.getName().equalsIgnoreCase("hello")) {
            // Проверяем, что команду ввел игрок (не консоль)
            if (sender instanceof Player) {
                Player player = (Player) sender;
                // Отправляем сообщение игроку
                player.sendMessage("Привет, " + player.getName() + "!");
                return true;
            }
        }
        return false;
    }
    
    // Эта функция срабатывает, когда игрок заходит на сервер
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Приветствуем игрока
        player.sendMessage("Добро пожаловать на сервер!");
    }
}
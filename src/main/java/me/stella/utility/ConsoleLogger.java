package me.stella.utility;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

public class ConsoleLogger {

    private final CommandSender console;

    public ConsoleLogger(Server server) {
        this.console = server.getConsoleSender();
    }

    public void log(Level level, String message) {
        ChatColor color;
        switch(level.intValue()) {
            case 300:
            case 400:
            case 500:
                color = ChatColor.DARK_GREEN;
                break;
            case 700:
                color = ChatColor.BLUE;
                break;
            case 800:
                color = ChatColor.GREEN;
                break;
            case 900:
                color = ChatColor.YELLOW;
                break;
            case 1000:
                color = ChatColor.DARK_RED;
                break;
            default:
                color = ChatColor.GRAY;
                break;
        }
        console.sendMessage(BukkitUtils.color(color + message));
    }

}

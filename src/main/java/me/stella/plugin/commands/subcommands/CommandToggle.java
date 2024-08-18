package me.stella.plugin.commands.subcommands;

import me.stella.HyperVariables;
import me.stella.plugin.HyperSettings;
import me.stella.plugin.commands.FarmSubCommand;
import me.stella.utility.BukkitUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandToggle implements FarmSubCommand {
    @Override
    public String getPermission() {
        return "farm.command.toggle";
    }

    @Override
    public String getUsage() {
        return "toggle";
    }

    @Override
    public boolean perform(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        HyperSettings config = HyperVariables.get(HyperSettings.class);
        boolean using = BukkitUtils.use.contains(player.getUniqueId());
        if(using) {
            player.sendMessage(BukkitUtils.color(config.getMessage("toggle-off")));
            BukkitUtils.use.remove(player.getUniqueId());
        } else {
            player.sendMessage(BukkitUtils.color(config.getMessage("toggle-on")));
            BukkitUtils.use.add(player.getUniqueId());
        }
        return true;
    }
}

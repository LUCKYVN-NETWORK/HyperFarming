package me.stella.plugin.commands.subcommands;

import me.stella.HyperFarming;
import me.stella.HyperVariables;
import me.stella.plugin.HyperSettings;
import me.stella.plugin.commands.FarmSubCommand;
import me.stella.utility.BukkitUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class CommandHelp implements FarmSubCommand {

    @Override
    public String getUsage() {
        return "help";
    }

    @Override
    public String getPermission() {
        return "farm.command.help";
    }

    @Override
    public boolean perform(CommandSender sender, String[] args) {
        (new BukkitRunnable() {
            @Override
            public void run() {
                HyperSettings config = HyperVariables.get(HyperSettings.class);
                List<String> basic = config.getMessageBlock("help");
                if(sender.hasPermission("farm.*"))
                    basic.addAll(config.getMessageBlock("help-admin"));
                basic.stream().map(line -> line.replace("{version}", HyperFarming.inst().getDescription().getVersion()))
                        .forEach(line -> sender.sendMessage(BukkitUtils.color(line)));
            }
        }).runTaskAsynchronously(HyperFarming.inst());
        return true;
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }
}

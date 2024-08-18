package me.stella.plugin.commands.subcommands;

import me.stella.HyperFarming;
import me.stella.HyperVariables;
import me.stella.gui.HyperGUIBuilder;
import me.stella.plugin.HyperSettings;
import me.stella.plugin.commands.FarmSubCommand;
import me.stella.utility.BukkitUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandReload implements FarmSubCommand {

    @Override
    public String getPermission() {
        return "farm.command.reload";
    }

    @Override
    public String getUsage() {
        return "reload";
    }

    @Override
    public boolean perform(CommandSender sender, String[] args) {
        final HyperSettings config = HyperVariables.get(HyperSettings.class);
        final HyperGUIBuilder builder = HyperVariables.get(HyperGUIBuilder.class);
        (new BukkitRunnable() {
            @Override
            public void run() {
                config.reload();
                builder.reload();
                BukkitUtils.initPricingTable();
                sender.sendMessage(BukkitUtils.color(config.getMessage("reload")
                        .replace("{version}", HyperFarming.inst().getDescription().getVersion())));
            }
        }).runTask(HyperFarming.inst());
        return false;
    }
}

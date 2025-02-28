package me.stella.plugin.commands.subcommands;

import me.stella.HyperVariables;
import me.stella.plugin.gui.HyperGUIBuilder;
import me.stella.plugin.HyperSettings;
import me.stella.plugin.commands.FarmSubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandStorage implements FarmSubCommand {

    private final String usage;

    public CommandStorage() {
        this.usage = HyperVariables.get(HyperSettings.class).getUsage("storage");
    }

    @Override
    public boolean perform(CommandSender sender, String[] args) {
        HyperVariables.get(HyperGUIBuilder.class).openSyncStorage((Player) sender);
        return true;
    }

    @Override
    public String getUsage() {
        return this.usage;
    }

}

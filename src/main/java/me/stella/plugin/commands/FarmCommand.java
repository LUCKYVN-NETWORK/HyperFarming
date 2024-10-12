package me.stella.plugin.commands;

import me.stella.HyperVariables;
import me.stella.plugin.HyperSettings;
import me.stella.plugin.commands.subcommands.*;
import me.stella.utility.BukkitUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class FarmCommand implements CommandExecutor {

    protected static Map<String, FarmSubCommand> subCommandMap = new HashMap<>();

    public FarmCommand() {
        subCommandMap.put("storage", new CommandStorage());
        subCommandMap.put("sell", new CommandSell());
        subCommandMap.put("take", new CommandTake());
        subCommandMap.put("deposit", new CommandDeposit());
        subCommandMap.put("send", new CommandSend());
        subCommandMap.put("replant", new CommandReplant());
        subCommandMap.put("multiplier", new CommandMultiplier());
        subCommandMap.put("help", new CommandHelp());
        subCommandMap.put("reload", new CommandReload());
        subCommandMap.put("toggle", new CommandToggle());
        subCommandMap.put("tool", new CommandTool());
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        final HyperSettings config = HyperVariables.get(HyperSettings.class);
        if(strings.length == 0)
            strings = new String[]{"storage"};
        FarmSubCommand subCommand = subCommandMap.get(strings[0]);
        if(subCommand.isPlayerOnly() && !(commandSender instanceof Player)) {
            commandSender.sendMessage(BukkitUtils.color(config.getMessage("only-player")));
            return true;
        }
        if(subCommand.isPlayerOnly()) {
            Player player = (Player) commandSender;
            if(!subCommand.getPermission().isEmpty() && !player.hasPermission(subCommand.getPermission())) {
                commandSender.sendMessage(BukkitUtils.color(config.getMessage("no-permissions")));
                return true;
            }
        }
        if(subCommand.getArguments() > (strings.length - 1)) {
            commandSender.sendMessage(BukkitUtils.color(
                    config.getMessage("no-args").replace("{syntax}",
                            "/farm " + subCommand.getUsage())));
            return true;
        }
        return subCommand.perform(commandSender, processParameters(strings));
    }

    private String[] processParameters(String[] defaultArray) {
        if(defaultArray.length <= 1)
            return new String[0];
        String[] output = new String[defaultArray.length - 1];
        System.arraycopy(defaultArray, 1, output,0, defaultArray.length - 1);
        return output;
    }
}

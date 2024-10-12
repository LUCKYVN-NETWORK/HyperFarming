package me.stella.plugin.commands.subcommands;

import me.stella.HyperVariables;
import me.stella.plugin.HyperSettings;
import me.stella.plugin.commands.FarmSubCommand;
import me.stella.utility.BukkitUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class CommandTool implements FarmSubCommand {

    private static final List<String> tools = Arrays.asList("melon");

    private final Map<Integer, List<String>> suggestions;
    private final String usage;

    public CommandTool() {
        this.suggestions = new HashMap<>();
        this.suggestions.put(2, new ArrayList<>(tools));
        this.usage = HyperVariables.get(HyperSettings.class).getUsage("tool");
    }

    @Override
    public int getArguments() {
        return 1;
    }

    @Override
    public String getPermission() {
        return "farm.command.tool";
    }

    @Override
    public String getUsage() {
        return this.usage;
    }

    @Override
    public boolean perform(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        HyperSettings config = HyperVariables.get(HyperSettings.class);
        switch(args[0]) {
            case "melon":
                UUID uid = player.getUniqueId();
                if(BukkitUtils.melonCompression.contains(uid)) {
                    BukkitUtils.melonCompression.remove(uid);
                    player.sendMessage(BukkitUtils.color(config.getMessage("melon-compression-off")));
                } else {
                    BukkitUtils.melonCompression.add(uid);
                    player.sendMessage(BukkitUtils.color(config.getMessage("melon-compression-on")));
                }
                break;
            default:
                player.sendMessage(BukkitUtils.color(config.getMessage("invalid-tool")));
                return true;
        }
        return true;
    }

    @Override
    public Map<Integer, List<String>> getSuggestions() {
        return this.suggestions;
    }
}

package me.stella.plugin.commands.subcommands;

import me.stella.HyperVariables;
import me.stella.utility.functions.FunctionSend;
import me.stella.plugin.HyperSettings;
import me.stella.plugin.commands.FarmSubCommand;
import me.stella.plugin.data.FarmerData;
import me.stella.utility.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class CommandSend implements FarmSubCommand {

    private final Map<Integer, List<String>> suggestions;
    private final String usage;

    public CommandSend() {
        this.suggestions = new HashMap<>();
        this.suggestions.put(2, Collections.emptyList());
        this.suggestions.put(3, FarmerData.getDataTypes().stream().map(String::toLowerCase).collect(Collectors.toList()));
        this.suggestions.put(4, Arrays.asList("1", "10", "64", "all"));
        this.usage = HyperVariables.get(HyperSettings.class).getUsage("send");
    }

    @Override
    public int getArguments() {
        return 3;
    }

    @Override
    public String getPermission() {
        return "farm.command.send";
    }

    @Override
    public String getUsage() {
        return this.usage;
    }

    @Override
    public boolean perform(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String[] responseSend = FunctionSend.send(player, args);
        final HyperSettings config = HyperVariables.get(HyperSettings.class);
        if(responseSend[0].equals("fail")) {
            switch(responseSend[1]) {
                case "param_none":
                    player.sendMessage(BukkitUtils.color(config.getMessage("no-args").replace("{syntax}", getUsage())));
                    break;
                case "not_online":
                    player.sendMessage(BukkitUtils.color(config.getMessage("player-offline").replace("{player}", responseSend[2])));
                    break;
                case "invalid_type":
                    player.sendMessage(BukkitUtils.color(config.getMessage("invalid-type")));
                    break;
                case "insufficient_balance":
                    player.sendMessage(BukkitUtils.color(config.getMessage("insufficient-balance")
                            .replace("{type}", config.getTypeName(responseSend[2]))));
                    break;
                case "receiver_full":
                    player.sendMessage(BukkitUtils.color(config.getMessage("receiver-full")
                            .replace("{player}", responseSend[3])
                            .replace("{type}", responseSend[2])));
                    break;
                case "invalid_int":
                    player.sendMessage(BukkitUtils.color(config.getMessage("invalid-int")
                            .replace("%input%", responseSend[2])));
                    break;
            }
            return true;
        }
        Player receiver = Bukkit.getPlayer(responseSend[1]);
        String type = responseSend[2];
        int amount = Integer.parseInt(responseSend[3]);
        player.sendMessage(BukkitUtils.color(config.getMessage("send"))
                .replace("{amount}", BukkitUtils.formatter.format(amount))
                .replace("{type}", config.getTypeName(type))
                .replace("{player}", receiver.getName()));
        receiver.sendMessage(BukkitUtils.color(config.getMessage("receive"))
                .replace("{amount}", BukkitUtils.formatter.format(amount))
                .replace("{type}", config.getTypeName(type))
                .replace("{player}", player.getName()));
        return true;
    }

    @Override
    public Map<Integer, List<String>> getSuggestions() {
        return this.suggestions;
    }
}

package me.stella.plugin.commands.subcommands;

import me.stella.HyperVariables;
import me.stella.utility.functions.FunctionDeposit;
import me.stella.plugin.HyperSettings;
import me.stella.plugin.commands.FarmSubCommand;
import me.stella.plugin.data.FarmerData;
import me.stella.utility.BukkitUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandDeposit implements FarmSubCommand {

    private final Map<Integer, List<String>> suggestions;
    private final String usage;

    public CommandDeposit() {
        this.suggestions = new HashMap<>();
        this.suggestions.put(2, FarmerData.getDataTypes().stream().map(String::toLowerCase).collect(Collectors.toList()));
        this.suggestions.put(3, Arrays.asList("1", "10", "64", "all"));
        this.usage = HyperVariables.get(HyperSettings.class).getUsage("deposit");
    }

    @Override
    public int getArguments() {
        return 2;
    }

    @Override
    public String getPermission() {
        return "farm.command.deposit";
    }

    @Override
    public String getUsage() {
        return this.usage;
    }

    @Override
    public boolean perform(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        final HyperSettings config = HyperVariables.get(HyperSettings.class);
        String[] responseDeposit = FunctionDeposit.deposit(player, args);
        if(responseDeposit[0].equals("fail")) {
            switch(responseDeposit[1]) {
                case "param_none":
                    player.sendMessage(BukkitUtils.color(config.getMessage("no-args").replace("{syntax}", getUsage())));
                    break;
                case "invalid_type":
                    player.sendMessage(BukkitUtils.color(config.getMessage("invalid-type")));
                    break;
                case "insufficient_balance":
                    player.sendMessage(BukkitUtils.color(config.getMessage("insufficient-supply")
                            .replace("{type}", config.getTypeName(responseDeposit[2]))));
                    break;
                case "storage_full":
                    player.sendMessage(BukkitUtils.color(config.getMessage("storage-full")));
                    break;
                case "invalid_int":
                    player.sendMessage(BukkitUtils.color(config.getMessage("invalid-int")
                            .replace("%input%", responseDeposit[2])));
                    break;
                default:
                    break;
            }
            return true;
        }
        int amount = Integer.parseInt(responseDeposit[2]);
        player.sendMessage(BukkitUtils.color(config.getMessage("deposit")
                .replace("{amount}", BukkitUtils.formatter.format(amount))
                .replace("{type}", config.getTypeName(responseDeposit[1]))));
        return true;
    }

    @Override
    public Map<Integer, List<String>> getSuggestions() {
        return this.suggestions;
    }
}

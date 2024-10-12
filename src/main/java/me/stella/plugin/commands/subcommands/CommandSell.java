package me.stella.plugin.commands.subcommands;

import me.stella.HyperVariables;
import me.stella.functions.FunctionSell;
import me.stella.objects.PricedCrop;
import me.stella.plugin.HyperSettings;
import me.stella.plugin.commands.FarmSubCommand;
import me.stella.plugin.data.FarmerData;
import me.stella.support.EconomyFramework;
import me.stella.utility.BukkitUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandSell implements FarmSubCommand {

    private final Map<Integer, List<String>> suggestions;
    private final String usage;

    public CommandSell() {
        this.suggestions = new HashMap<>();
        this.suggestions.put(2, FarmerData.getDataTypes().stream().map(String::toLowerCase).collect(Collectors.toList()));
        this.suggestions.put(3, Arrays.asList("1", "10", "64", "all"));
        this.usage = HyperVariables.get(HyperSettings.class).getUsage("sell");
    }

    @Override
    public int getArguments() {
        return 2;
    }

    @Override
    public String getPermission() {
        return "farm.command.sell";
    }

    @Override
    public String getUsage() {
        return this.usage;
    }

    @Override
    public boolean perform(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        final HyperSettings config = HyperVariables.get(HyperSettings.class);
        String[] responseSell = FunctionSell.sell(player, args);
        if(responseSell[0].equals("fail")) {
            switch(responseSell[1]) {
                case "param_none":
                    player.sendMessage(BukkitUtils.color(config.getMessage("no-args").replace("{syntax}", getUsage())));
                    break;
                case "invalid_type":
                    player.sendMessage(BukkitUtils.color(config.getMessage("invalid-type")));
                    break;
                case "insufficient_balance":
                    player.sendMessage(BukkitUtils.color(config.getMessage("insufficient-balance")
                            .replace("{type}", config.getTypeName(responseSell[2]))));
                    break;
                case "invalid_int":
                    player.sendMessage(BukkitUtils.color(config.getMessage("invalid-int")
                            .replace("%input%", responseSell[2])));
                    break;
                default:
                    break;
            }
            return true;
        }
        EconomyFramework serverEconomy = HyperVariables.get(EconomyFramework.class);
        PricedCrop pricing = BukkitUtils.pricingTable.get(responseSell[1]);
        int amount = Integer.parseInt(responseSell[2]);
        double earned = Math.floor(pricing.getPerValue() * ((double) amount / pricing.getPerCount()));
        serverEconomy.giveMoney(player, (long) earned);
        player.sendMessage(BukkitUtils.color(config.getMessage("sell")
                .replace("{amount}", BukkitUtils.formatter.format(amount))
                .replace("{type}", config.getTypeName(responseSell[1]))
                .replace("{money}", BukkitUtils.formatter.format(earned))));
        return true;
    }

    @Override
    public Map<Integer, List<String>> getSuggestions() {
        return this.suggestions;
    }
}

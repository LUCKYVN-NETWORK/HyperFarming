package me.stella.plugin.commands.subcommands;

import me.stella.HyperFarming;
import me.stella.HyperVariables;
import me.stella.functions.FunctionSell;
import me.stella.functions.FunctionTake;
import me.stella.plugin.HyperSettings;
import me.stella.plugin.commands.FarmSubCommand;
import me.stella.plugin.data.FarmerData;
import me.stella.utility.BukkitUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CommandTake implements FarmSubCommand {

    private final Map<Integer, List<String>> suggestions;
    private final String usage;

    public CommandTake() {
        this.suggestions = new HashMap<>();
        this.suggestions.put(2, FarmerData.getDataTypes().stream().map(String::toLowerCase).collect(Collectors.toList()));
        this.suggestions.put(3, Arrays.asList("1", "10", "64", "all"));
        this.usage = HyperVariables.get(HyperSettings.class).getUsage("take");
    }

    @Override
    public int getArguments() {
        return 2;
    }

    @Override
    public String getPermission() {
        return "farm.command.take";
    }

    @Override
    public String getUsage() {
        return this.usage;
    }

    @Override
    public boolean perform(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        final HyperSettings config = HyperVariables.get(HyperSettings.class);
        String[] responseTake = FunctionTake.take(player, args);
        if(responseTake[0].equals("fail")) {
            switch(responseTake[1]) {
                case "param_none":
                    player.sendMessage(BukkitUtils.color(config.getMessage("no-args").replace("{syntax}", getUsage())));
                    break;
                case "invalid_type":
                    player.sendMessage(BukkitUtils.color(config.getMessage("invalid-type")));
                    break;
                case "insufficient_balance":
                    player.sendMessage(BukkitUtils.color(config.getMessage("insufficient-balance")
                            .replace("{type}", config.getTypeName(responseTake[2]))));
                    break;
                case "invalid_int":
                    player.sendMessage(BukkitUtils.color(config.getMessage("invalid-int")
                            .replace("%input%", responseTake[2])));
                    break;
                default:
                    break;
            }
            return true;
        }
        int amount = Integer.parseInt(responseTake[2]);
        if(BukkitUtils.melonCompression.contains(player.getUniqueId()) && responseTake[1].equals("MELON")) {
            ItemStack melonBlock = new ItemStack(Objects.requireNonNull(BukkitUtils.returnOneOf("MELON_BLOCK", "MELON"))).clone();
            int amountBlock = amount / 9; int spare = amount % 9;
            for(ItemStack blockStack: BukkitUtils.buildItemStream(melonBlock, amountBlock))
                player.getInventory().addItem(blockStack);
            player.getInventory().addItem(new ItemStack(Material.MELON, spare));
            player.sendMessage(BukkitUtils.color(config.getMessage("take")
                    .replace("{amount}", BukkitUtils.formatter.format(amount))
                    .replace("{type}", config.getTypeName(responseTake[1]))));
            if(responseTake[1].equals("MELON"))
                player.sendMessage(BukkitUtils.color(config.getMessage("melon-reminder-on")));
        } else {
            ItemStack typeItem = BukkitUtils.idItemMap.get(responseTake[1]).clone();
            for (ItemStack itemStack : BukkitUtils.buildItemStream(typeItem, amount))
                player.getInventory().addItem(itemStack);
            player.sendMessage(BukkitUtils.color(config.getMessage("take")
                    .replace("{amount}", BukkitUtils.formatter.format(amount))
                    .replace("{type}", config.getTypeName(responseTake[1]))));
            if(responseTake[1].equals("MELON"))
                player.sendMessage(BukkitUtils.color(config.getMessage("melon-reminder-off")));
        }
        return true;
    }

    @Override
    public Map<Integer, List<String>> getSuggestions() {
        return this.suggestions;
    }
}

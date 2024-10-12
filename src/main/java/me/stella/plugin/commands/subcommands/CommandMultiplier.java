package me.stella.plugin.commands.subcommands;

import me.stella.HyperVariables;
import me.stella.functions.FunctionEnchant;
import me.stella.plugin.HyperSettings;
import me.stella.plugin.commands.FarmSubCommand;
import me.stella.plugin.data.FarmerData;
import me.stella.utility.BukkitUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class CommandMultiplier implements FarmSubCommand {

    private final Map<Integer, List<String>> suggestions;
    private final String usage;

    public CommandMultiplier() {
        this.suggestions = new HashMap<>();
        List<String> types = FarmerData.getDataTypes().stream().map(String::toLowerCase).collect(Collectors.toList());
        types.add("all");
        this.suggestions.put(2, types);
        this.suggestions.put(3, Arrays.asList("1.5", "2.0", "2.5", "3.0"));
        this.usage = HyperVariables.get(HyperSettings.class).getUsage("multiplier");
    }

    @Override
    public int getArguments() {
        return 2;
    }

    @Override
    public String getPermission() {
        return "farm.command.multiplier";
    }

    @Override
    public String getUsage() {
        return this.usage;
    }

    @Override
    public boolean perform(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        final HyperSettings config = HyperVariables.get(HyperSettings.class);
        String[] responseMultiplier= FunctionEnchant.enchant(player, inject("multiplier", args));
        if(responseMultiplier[0].equals("fail")) {
            switch(responseMultiplier[1]) {
                case "param_none":
                    player.sendMessage(BukkitUtils.color(config.getMessage("no-args").replace("{syntax}", getUsage())));
                    break;
                case "hand_empty":
                    player.sendMessage(BukkitUtils.color(config.getMessage("hand-empty")));
                    break;
                case "not_supported_tools":
                    player.sendMessage(BukkitUtils.color(config.getMessage("not-supported-tool")));
                    break;
                case "invalid_mode":
                    player.sendMessage(BukkitUtils.color(config.getMessage("invalid-mode")));
                    break;
                case "invalid_factor":
                    player.sendMessage(BukkitUtils.color(config.getMessage("invalid-factor")
                            .replace("{input}", responseMultiplier[2])));
                    break;
                case "factor_min":
                    player.sendMessage(BukkitUtils.color(config.getMessage("factor-min")));
                    break;
            }
            return true;
        }
        String scope = responseMultiplier[1].replace("multiplier_", "");
        double multiplier = ((double) Integer.parseInt(responseMultiplier[2])) / 100.0D;
        player.sendMessage(BukkitUtils.color(config.getMessage("enchant"))
                .replace("{key}", config.getTypeName(scope))
                .replace("{value}", BukkitUtils.formatter.format(multiplier)));
        ItemStack hand = player.getInventory().getItemInMainHand();
        ItemMeta meta = hand.getItemMeta();
        List<String> presentLore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        List<String> enchantLore = config.getEnchantmentLore("multiplier");
        enchantLore.stream().map(line -> line.replace("{multiplier}", BukkitUtils.formatter.format(multiplier))
                .replace("{type}", config.getTypeName(scope)))
                .forEach(line -> presentLore.add(BukkitUtils.color(line)));
        meta.setLore(presentLore); hand.setItemMeta(meta);
        return true;
    }

    @Override
    public Map<Integer, List<String>> getSuggestions() {
        return this.suggestions;
    }
}

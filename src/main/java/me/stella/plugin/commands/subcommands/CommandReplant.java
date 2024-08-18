package me.stella.plugin.commands.subcommands;

import me.stella.HyperVariables;
import me.stella.functions.FunctionEnchant;
import me.stella.plugin.HyperSettings;
import me.stella.plugin.commands.FarmSubCommand;
import me.stella.utility.BukkitUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandReplant implements FarmSubCommand {

    @Override
    public String getPermission() {
        return "farm.command.replant";
    }

    @Override
    public String getUsage() {
        return "replant";
    }

    @Override
    public boolean perform(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        final HyperSettings config = HyperVariables.get(HyperSettings.class);
        String[] responseReplant = FunctionEnchant.enchant(player, new String[] { "replant" });
        if(responseReplant[0].equals("fail")) {
            switch(responseReplant[1]) {
                case "param_none":
                    player.sendMessage(BukkitUtils.color(config.getMessage("no-args").replace("{syntax}", getUsage())));
                    break;
                case "hand_empty":
                    player.sendMessage(BukkitUtils.color(config.getMessage("hand-empty")));
                    break;
                case "not_supported_tools":
                    player.sendMessage(BukkitUtils.color(config.getMessage("not-supported-tool")));
                    break;
            }
            return true;
        }
        player.sendMessage(BukkitUtils.color(config.getMessage("enchant"))
                .replace("{key}", responseReplant[1])
                .replace("{value}", responseReplant[2]));
        ItemStack hand = player.getInventory().getItemInMainHand();
        ItemMeta meta = hand.getItemMeta();
        List<String> presentLore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        List<String> enchantLore = config.getEnchantmentLore("replant");
        presentLore.addAll(enchantLore.stream().map(BukkitUtils::color).collect(Collectors.toList()));
        meta.setLore(presentLore); hand.setItemMeta(meta);
        return true;
    }
}

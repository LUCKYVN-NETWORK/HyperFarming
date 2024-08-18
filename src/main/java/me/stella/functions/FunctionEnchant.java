package me.stella.functions;

import me.stella.HyperVariables;
import me.stella.nms.NMSProtocol;
import me.stella.plugin.data.FarmerData;
import me.stella.utility.BukkitUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class FunctionEnchant {

    private static final List<String> supported = Arrays.asList("multiplier", "replant");

    public static String[] enchant(Player player, String[] params) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if(hand == null || hand.getType().name().equals("AIR"))
            return new String[] { "fail", "hand_empty" };
        if(!BukkitUtils.tools.contains(hand.getType()))
            return new String[] { "fail", "not_supported_tools" };
        try {
            String enchant = params[0];
            if(!supported.contains(enchant))
                return new String[] { "fail", "invalid_enchant", enchant };
            String[] enchantProcessor = new String[2];
            switch(enchant) {
                case "multiplier":
                    String mode = params[1].toUpperCase();
                    if(!mode.equals("ALL")) {
                        if(!FarmerData.getDataTypes().contains(mode))
                            return new String[] { "fail", "invalid_mode" };
                    }
                    enchantProcessor[0] = "multiplier_" + mode;
                    try {
                        double d = Double.parseDouble(params[2]);
                        if(d <= 1.0D)
                            return new String[] { "fail", "factor_min" };
                        enchantProcessor[1] = String.valueOf((int)d*100);
                    } catch(Exception err2) {
                        return new String[] { "fail", "invalid_factor", params[2] };
                    }
                    break;
                case "replant":
                    enchantProcessor[0] = "replant";
                    enchantProcessor[1] = "1";
                    break;
            }
            player.getInventory().setItemInMainHand(HyperVariables.get(NMSProtocol.class).setNBTTag(
                    player.getInventory().getItemInMainHand(), enchantProcessor[0], Integer.parseInt(enchantProcessor[1])));
            return new String[]{ "success", enchantProcessor[0], enchantProcessor[1] };
        } catch(Exception err) { return new String[] { "fail", "param_none" }; }
    }

}

package me.stella.functions;

import me.stella.HyperFarming;
import me.stella.nms.MultiVerItems;
import me.stella.plugin.data.FarmerData;
import me.stella.utility.BukkitUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.logging.Level;

public class FunctionTake {

    public static String[] take(Player player, String[] params) {
        HyperFarming.console.log(Level.INFO, "[HyperFarming] " + player.getName() + " requested withdrawal with params: " + Arrays.toString(params));
        if (params.length != 2)
            return new String[]{"fail", "param_none"};
        String type = params[0].toUpperCase();
        if (type.contains("MELON"))
            return handleMelonWithdrawal(player, params, type);
        else
            return handleOtherCropWithdrawal(player, params, type);
    }

    private static String[] handleMelonWithdrawal(Player player, String[] params, String type) {
        String blockType = MultiVerItems.LEGACY ? "MELON_BLOCK" : "MELON";
        String itemType = MultiVerItems.LEGACY ? "MELON" : "MELON_SLICE";
        boolean melonBlocks = type.equals("MELON_BLOCK") && BukkitUtils.melonCompression.contains(player.getUniqueId());
        if (!FarmerData.getDataTypes().contains(type))
            return new String[]{"fail", "invalid_type"};
        FarmerData data = (FarmerData) player.getMetadata("farmerData").get(0).value();
        int balance = data.getData(type);
        if (balance == 0)
            return new String[]{"fail", "insufficient_balance", type};
        ItemStack typeItem = BukkitUtils.idItemMap.get(melonBlocks ? blockType : itemType).clone();
        int available = 0;
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null) {
                available += 64;
                continue;
            }
            if (BukkitUtils.isBasic(typeItem, item)) {
                available += (64 - item.getAmount());
            }
        }
        if (melonBlocks) {
            available--;
            available *= 9;
        }
        String amount = params[1];
        int amountInt = 0;
        if (amount.equals("all"))
            amountInt = balance;
        else {
            try {
                amountInt = Integer.parseInt(amount);
            } catch (Exception err) {
                err.printStackTrace();
                return new String[]{"fail", "invalid_int", amount};
            }
            amountInt = Math.min(amountInt, balance);
        }
        if (amountInt < 1)
            return new String[]{"fail", "invalid_int", amount};
        int withdrawalAmount = Math.min(amountInt, available);
        data.decrease(type, withdrawalAmount);
        return new String[]{"success", "MELON", String.valueOf(withdrawalAmount)};
    }

    private static String[] handleOtherCropWithdrawal(Player player, String[] params, String type) {
        if (!FarmerData.getDataTypes().contains(type))
            return new String[]{"fail", "invalid_type"};
        FarmerData data = (FarmerData) player.getMetadata("farmerData").get(0).value();
        int balance = data.getData(type);
        if (balance == 0)
            return new String[]{"fail", "insufficient_balance", type};
        ItemStack typeItem = BukkitUtils.idItemMap.get(type).clone();
        int available = 0;
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null) {
                available += 64;
                continue;
            }
            if (BukkitUtils.isBasic(typeItem, item)) {
                available += (64 - item.getAmount());
            }
        }
        String amount = params[1];
        int amountInt = 0;
        if (amount.equals("all"))
            amountInt = balance;
        else {
            try {
                amountInt = Integer.parseInt(amount);
            } catch (Exception err) {
                err.printStackTrace();
                return new String[]{"fail", "invalid_int", amount};
            }
            amountInt = Math.min(amountInt, balance);
        }
        if (amountInt < 1)
            return new String[]{"fail", "invalid_int", amount};
        int withdrawalAmount = Math.min(amountInt, available);
        data.decrease(type, withdrawalAmount);
        return new String[]{"success", type, String.valueOf(withdrawalAmount)};
    }
}

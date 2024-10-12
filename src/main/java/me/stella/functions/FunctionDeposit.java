package me.stella.functions;

import me.stella.HyperFarming;
import me.stella.plugin.data.FarmerData;
import me.stella.utility.BukkitUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class FunctionDeposit {

    public static String[] deposit(Player player, String[] params) {
        HyperFarming.console.log(Level.INFO, "[HyperFarming] " + player.getName() + " requested deposit with params: " + Arrays.toString(params));
        if(params.length != 2)
            return new String[]{"fail", "param_none"};
        String type = params[0].toUpperCase();
        String versionBlock = BukkitUtils.returnOneOf("MELON_BLOCK", "MELON").name();
        boolean melonBlocks = type.equals(versionBlock) && BukkitUtils.melonCompression.contains(player.getUniqueId());
        if(!melonBlocks && !FarmerData.getDataTypes().contains(type))
            return new String[]{"fail", "invalid_type"};
        ItemStack typeItem = BukkitUtils.idItemMap.get(type).clone();
        int available = 0;
        PlayerInventory inventory = player.getInventory();
        Map<Integer, Integer> depositable = new HashMap<>();
        for(int i = 0; i < 36;  i++) {
            ItemStack item = inventory.getItem(i);
            if(item == null)
                continue;
            if(BukkitUtils.isBasic(typeItem, item)) {
                available += item.getAmount();
                depositable.put(i, item.getAmount());
            }
        }
        if(available == 0)
            return new String[]{"fail", "insufficient_balance", type};
        if(melonBlocks)
            available *= 9;
        String paramAmount = params[1]; int amountInt;
        if(paramAmount.equals("all")) {
            amountInt = available;
        } else {
            try {
                amountInt = Integer.parseInt(paramAmount);
            } catch(Exception err) {
                err.printStackTrace();
                return new String[]{"fail", "invalid_int", paramAmount};
            }
        }
        if(amountInt < 1)
            return new String[] { "fail", "invalid_int", paramAmount };
        if(melonBlocks)
            amountInt *= 9;
        FarmerData data = (FarmerData) player.getMetadata("farmerData").get(0).value();
        amountInt = Math.min(data.getLimit() - data.getData(melonBlocks ? "MELON" : type), Math.min(amountInt, available));
        if(melonBlocks)
            amountInt /= 9;
        if(amountInt == 0)
            return new String[]{ "fail", "storage_full" };
        Map<Integer, Integer> result = new HashMap<>();
        AtomicInteger availableThreadSafe = new AtomicInteger(amountInt);
        depositable.forEach((slot, amount) -> {
            int availableCurrent = availableThreadSafe.get();
            if(availableCurrent <= 0)
                return;
            if(availableCurrent > amount) {
                result.put(slot, 0);
                availableCurrent -= amount;
            } else {
                result.put(slot, amount - availableCurrent);
                availableCurrent = 0;
            }
            availableThreadSafe.set(availableCurrent);
        });
        data.increase(melonBlocks ? "MELON" : type, amountInt * (melonBlocks ? 9 : 1));
        result.forEach((slot, amount) -> inventory.getItem(slot).setAmount(amount));
        return new String[]{"success", melonBlocks ? "MELON" : type, String.valueOf(amountInt * (melonBlocks ? 9 : 1))};
    }

}

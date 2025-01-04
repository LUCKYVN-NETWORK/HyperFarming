package me.stella.functions;

import javassist.I;
import me.stella.HyperFarming;
import me.stella.nms.MultiVerItems;
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
        if (params.length != 2)
            return new String[]{"fail", "param_none"};
        String type = params[0].toUpperCase();
        if (type.contains("MELON"))
            return handleMelonDeposit(player, params, type);
        else
            return handleOtherCropDeposit(player, params, type);
    }

    private static String[] handleMelonDeposit(Player player, String[] params, String type) {
        type = "MELON";
        if (!FarmerData.getDataTypes().contains(type))
            return new String[]{"fail", "invalid_type"};
        FarmerData data = (FarmerData) player.getMetadata("farmerData").get(0).value();
        ItemStack blockItem = BukkitUtils.idItemMap.get("MELON_BLOCK").clone();
        ItemStack sliceItem = BukkitUtils.idItemMap.get(type).clone();
        PlayerInventory inventory = player.getInventory();
        Map<Integer, Integer> depositableBlocks = new HashMap<>();
        Map<Integer, Integer> depositableSlices = new HashMap<>();
        int availableBlocks = 0;
        int availableSlices = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null) continue;
            if (BukkitUtils.isBasic(blockItem, item)) {
                availableBlocks += item.getAmount();
                depositableBlocks.put(i, item.getAmount());
            } else if (BukkitUtils.isBasic(sliceItem, item)) {
                availableSlices += item.getAmount();
                depositableSlices.put(i, item.getAmount());
            }
        }
        int totalAvailableSlices = (availableBlocks * 9) + availableSlices;
        if (totalAvailableSlices == 0)
            return new String[]{"fail", "insufficient_balance", type};
        String paramAmount = params[1];
        int amountInt;
        if (paramAmount.equals("all")) {
            amountInt = totalAvailableSlices;
        } else {
            try {
                amountInt = Integer.parseInt(paramAmount);
            } catch (Exception err) {
                err.printStackTrace();
                return new String[]{"fail", "invalid_int", paramAmount};
            }
        }
        if (amountInt < 1)
            return new String[]{"fail", "invalid_int", paramAmount};
        int depositAmount = Math.min(amountInt, totalAvailableSlices);
        depositAmount = Math.min(depositAmount, data.getLimit() - data.getData("MELON"));
        if (depositAmount == 0)
            return new String[]{"fail", "storage_full"};
        AtomicInteger remainingAmount = new AtomicInteger(depositAmount);
        depositableBlocks.forEach((slot, amount) -> {
            int needed = remainingAmount.get();
            if (needed <= 0) return;

            if (needed >= amount * 9) {
                inventory.clear(slot);
                remainingAmount.addAndGet(-amount * 9);
            } else {
                int blocksUsed = needed / 9;
                inventory.getItem(slot).setAmount(amount - blocksUsed);
                remainingAmount.addAndGet(-(blocksUsed * 9));
            }
        });
        depositableSlices.forEach((slot, amount) -> {
            int needed = remainingAmount.get();
            if (needed <= 0) return;

            if (needed >= amount) {
                inventory.clear(slot);
                remainingAmount.addAndGet(-amount);
            } else {
                inventory.getItem(slot).setAmount(amount - needed);
                remainingAmount.set(0);
            }
        });
        data.increase("MELON", depositAmount);
        return new String[]{"success", "MELON", String.valueOf(depositAmount)};
    }

    private static String[] handleOtherCropDeposit(Player player, String[] params, String type) {
        if (!FarmerData.getDataTypes().contains(type))
            return new String[]{"fail", "invalid_type"};
        FarmerData data = (FarmerData) player.getMetadata("farmerData").get(0).value();
        ItemStack itemType = BukkitUtils.idItemMap.get(type).clone();
        PlayerInventory inventory = player.getInventory();
        Map<Integer, Integer> depositable = new HashMap<>();
        int available = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null) continue;

            if (BukkitUtils.isBasic(itemType, item)) {
                available += item.getAmount();
                depositable.put(i, item.getAmount());
            }
        }
        if (available == 0)
            return new String[]{"fail", "insufficient_balance", type};
        String paramAmount = params[1];
        int amountInt;
        if (paramAmount.equals("all")) {
            amountInt = available;
        } else {
            try {
                amountInt = Integer.parseInt(paramAmount);
            } catch (Exception err) {
                err.printStackTrace();
                return new String[]{"fail", "invalid_int", paramAmount};
            }
        }
        if (amountInt < 1)
            return new String[]{"fail", "invalid_int", paramAmount};
        int depositAmount = Math.min(amountInt, available);
        depositAmount = Math.min(depositAmount, data.getLimit() - data.getData(type));
        if (depositAmount == 0)
            return new String[]{"fail", "storage_full"};
        AtomicInteger remainingAmount = new AtomicInteger(depositAmount);
        depositable.forEach((slot, amount) -> {
            int needed = remainingAmount.get();
            if (needed <= 0)
                return;
            if (needed >= amount) {
                inventory.clear(slot);
                remainingAmount.addAndGet(-amount);
            } else {
                inventory.getItem(slot).setAmount(amount - needed);
                remainingAmount.set(0);
            }
        });
        data.increase(type, depositAmount);
        return new String[]{"success", type, String.valueOf(depositAmount)};
    }

}

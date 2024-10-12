package me.stella.functions;

import me.stella.plugin.data.FarmerData;
import me.stella.utility.BukkitUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionSmartDeposit {

    public static List<String[]> deposit(Player player) {
        PlayerInventory inventory = player.getInventory();
        Map<String, Integer> data = new HashMap<>();
        for(int i = 0; i < 36; i++) {
            ItemStack item = inventory.getItem(i);
            if(item == null || item.getType().name().equals("AIR"))
                continue;
            ItemStack cloneLookup = item.clone();
            cloneLookup.setAmount(1);
            String key = BukkitUtils.idLookupMap.getOrDefault(cloneLookup.toString(), "#none");
            if(key.equals("#none"))
                continue;
            data.put(key, data.getOrDefault(key, 0) + item.getAmount());
        }
        List<String[]> responses = new ArrayList<>();
        data.forEach((type, count) -> responses.add(FunctionDeposit.deposit(player, new String[]{ type, String.valueOf(count) })));
        return responses;
    }

}

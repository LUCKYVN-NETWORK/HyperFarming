package me.stella.functions;

import me.stella.HyperFarming;
import me.stella.plugin.data.FarmerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.logging.Level;

public class FunctionSend {

    public static String[] send(Player player, String[] params) {
        HyperFarming.console.log(Level.INFO, "[HyperFarming] " + player.getName() + " requested sending with params: " + Arrays.toString(params));
        if(params.length != 3)
            return new String[]{ "fail", "param_none" };
        String name = params[0];
        if(Bukkit.getPlayer(name) == null)
            return new String[] { "fail", "not_online", name };
        Player toPlayer = Bukkit.getPlayer(name);
        String type = params[1].toUpperCase();
        if(!FarmerData.getDataTypes().contains(type))
            return new String[]{ "fail", "invalid_type" };
        FarmerData dataFrom = (FarmerData) player.getMetadata("farmerData").get(0).value();
        FarmerData dataTo = (FarmerData) toPlayer.getMetadata("farmerData").get(0).value();
        int balance = dataFrom.getData(type);
        if(balance == 0)
            return new String[]{ "fail", "insufficient_balance", type };
        if(dataTo.isFull(type))
            return new String[]{ "fail", "receiver_full", type, name };
        int cap = dataTo.getLimit() - dataTo.getData(type);
        String amount = params[2];
        int amountInt;
        if(amount.equals("all"))
            amountInt = balance;
        else {
            try {
                amountInt = Integer.parseInt(amount);
            } catch(Exception err) {
                return new String[]{ "fail", "invalid_int", amount };
            }
        }
        if(amountInt < 1)
            return new String[] { "fail", "invalid_int", amount };
        amountInt = Math.min(cap, Math.min(balance, amountInt));
        dataFrom.decrease(type, amountInt);
        dataTo.increase(type, amountInt);
        return new String[] { "success", name, type, String.valueOf(amountInt) };
    }

}

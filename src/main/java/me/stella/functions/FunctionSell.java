package me.stella.functions;

import me.stella.HyperFarming;
import me.stella.objects.PricedCrop;
import me.stella.plugin.data.FarmerData;
import me.stella.utility.BukkitUtils;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.logging.Level;

public class FunctionSell {

    public static String[] sell(Player player, String[] params) {
        HyperFarming.console.log(Level.INFO, "[HyperFarming] " + player.getName() + " requested selling with params: " + Arrays.toString(params));
        if(params.length != 2)
            return new String[]{"fail", "param_none"};
        String type = params[0].toUpperCase();
        if(!FarmerData.getDataTypes().contains(type))
            return new String[]{"fail", "invalid_type"  };
        String amount = params[1];
        FarmerData data = (FarmerData) player.getMetadata("farmerData").get(0).value();
        int balance = data.getData(type);
        PricedCrop cropPricing = BukkitUtils.pricingTable.get(type);
        if(balance == 0 || balance <= cropPricing.getPerCount())
            return new String[]{"fail", "insufficient_balance", type};
        int amountInt = 0;
        if(amount.equals("all"))
            amountInt = balance;
        else {
            try {
                amountInt = Integer.parseInt(amount);
            } catch(Exception err) {
                err.printStackTrace();
                return new String[]{"fail", "invalid_int", amount};
            }
            amountInt = Math.min(amountInt, balance);
        }
        if(amountInt <= cropPricing.getPerCount())
            return new String[]{"fail", "invalid_int", type};
        int selling = (amountInt / cropPricing.getPerCount()) * cropPricing.getPerCount();
        data.decrease(type, selling);
        return new String[]{"success", type, String.valueOf(selling)};
    }

}

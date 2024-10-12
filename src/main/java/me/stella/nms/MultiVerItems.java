package me.stella.nms;

import me.stella.HyperFarming;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MultiVerItems {

    public static boolean LEGACY = true;

    public static boolean isCocoaBeans(ItemStack itemStack) {
        if(LEGACY)
            return itemStack.getType().name().equals("INK_SACK") && itemStack.getDurability() == (short)3;
        else
            return itemStack.getType().name().equals("COCOA_BEANS");
    }

    public static Material getCocoaType() {
        try {
            Class<?> materialBukkit = Class.forName("org.bukkit.Material", true, HyperFarming.inst().getClass().getClassLoader());
            return (Material) materialBukkit.getMethod("getMaterial", String.class).invoke(null,
                    LEGACY ? "INK_SACK" : "COCOA_BEANS");
        } catch(Exception err) { err.printStackTrace(); }
        return Material.BEDROCK;
    }

    public static ItemStack buildCocoaStack() {
        return buildCocoaStack(1);
    }

    public static ItemStack buildCocoaStack(int amount) {
        Material type = getCocoaType();
        if(LEGACY)
            return new ItemStack(type, amount, (short)3);
        return new ItemStack(type, amount);
    }

}

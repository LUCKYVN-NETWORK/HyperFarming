package me.stella.utility.objects;

import org.bukkit.inventory.ItemStack;

public class CropsWrapper {

    private final String dataID;
    private final ItemStack dropStack;

    public String getDataID() {
        return dataID;
    }

    public ItemStack getDropStack() {
        return dropStack;
    }

    protected CropsWrapper(String dataID, ItemStack stack) {
        this.dataID = dataID;
        this.dropStack = stack;
    }

    public static CropsWrapper build(String paramId, ItemStack paramStack) {
        return new CropsWrapper(paramId, paramStack);
    }

}

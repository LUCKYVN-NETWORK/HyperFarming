package me.stella.plugin.gui;

import me.stella.utility.objects.ClickWrapper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class HyperGUIButton {

    private ItemStack itemStack;
    private ItemStack showStack;
    private int slot;

    public HyperGUIButton(ItemStack itemStack, int slot) {
        this.itemStack = itemStack;
        this.showStack = itemStack.clone();
        this.slot = slot;
    }

    public void setBase(ItemStack paramItem) {
        this.itemStack = paramItem;
        this.showStack = this.itemStack.clone();
    }

    public void setDisplay(ItemStack paramItem) {
        this.showStack = paramItem;
    }

    public ItemStack getBase() {
        return this.itemStack;
    }

    public ItemStack getDisplayStack() {
        return this.showStack;
    }

    public void setSlot(int paramInt) {
        this.slot = paramInt;
    }

    public int getSlot() {
        return this.slot;
    }

    public void showInInventory(Player toShow, Inventory inventory) {
        handleShow(toShow);
        inject(inventory);
    }

    public void inject(Inventory inventory) {
        inventory.setItem(getSlot(), getDisplayStack().clone());
    }

    public abstract void handleClick(ClickWrapper click);

    public abstract void handleShow(Player player);

}

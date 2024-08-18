package me.stella.nms;

import me.stella.HyperFarming;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public interface NMSProtocol {

    Random getWorldRandom(World world);

    void breakBlock(Block block);

    void replant(Block block, Material type);

    byte getGrowth(Block block);

    void damageTool(Player player, ItemStack stack);

    ItemStack buildSkull(String texture);

    ItemStack setNBTTag(ItemStack stack, String key, int level);

    int getNBTTag(ItemStack stack, String key);

    boolean hasNBTTag(ItemStack stack, String key);

    ItemStack setGUITag(ItemStack icon, String key, String data);

    String getGUITag(ItemStack icon, String key);

    String getTitle(Inventory inventory);

    default Class<?> getNMSClass(String name) throws Exception {
        return Class.forName(name, true, HyperFarming.inst().getClass().getClassLoader());
    }

}

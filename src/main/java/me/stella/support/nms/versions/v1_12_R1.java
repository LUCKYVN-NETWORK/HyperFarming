package me.stella.support.nms.versions;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.stella.support.nms.NMSProtocol;
import me.stella.utility.objects.LegacyDataWrapper;
import me.stella.utility.BukkitUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class v1_12_R1 implements NMSProtocol {

    private final Class<?> nbtTagCompound;
    private final Class<?> nmsItemStack;
    private final Class<?> nmsEntityLiving;
    private final Class<?> craftPlayer;
    private final Class<?> craftItemStack;
    private final Method methodSetType;
    private final Method methodGetData;
    private final Method methodDamage;
    private final Field fieldWorldRandom;
    private final Field nmsStackHandle;
    private final Map<String, LegacyDataWrapper> replants;

    public v1_12_R1() throws Exception {
        this.nbtTagCompound = getNMSClass("net.minecraft.server.v1_12_R1.NBTTagCompound");
        this.nmsItemStack = getNMSClass("net.minecraft.server.v1_12_R1.ItemStack");
        this.nmsEntityLiving = getNMSClass("net.minecraft.server.v1_12_R1.EntityLiving");
        this.craftItemStack = getNMSClass("org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack");
        this.craftPlayer = getNMSClass("org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer");
        Class<?> craftBlock = getNMSClass("org.bukkit.craftbukkit.v1_12_R1.block.CraftBlock");
        this.methodSetType = craftBlock.getMethod("setTypeIdAndData", int.class, byte.class, boolean.class);
        this.methodGetData = craftBlock.getMethod("getData");
        this.methodDamage = this.nmsItemStack.getMethod("damage", int.class, this.nmsEntityLiving);
        this.fieldWorldRandom = getNMSClass("net.minecraft.server.v1_12_R1.World").getDeclaredField("random");
        this.nmsStackHandle = this.craftItemStack.getDeclaredField("handle");
        this.fieldWorldRandom.setAccessible(true);
        this.nmsStackHandle.setAccessible(true);
        this.replants = new HashMap<>();
        this.replants.put("CROPS", LegacyDataWrapper.build(59, (byte)0));
        this.replants.put("CARROT", LegacyDataWrapper.build(141, (byte)0));
        this.replants.put("POTATO", LegacyDataWrapper.build(142, (byte)0));
        this.replants.put("BEETROOT_BLOCK", LegacyDataWrapper.build(207, (byte)0));
        this.replants.put("PUMPKIN_0", LegacyDataWrapper.build(86, (byte)0));
        this.replants.put("PUMPKIN_1", LegacyDataWrapper.build(86, (byte)1));
        this.replants.put("PUMPKIN_2", LegacyDataWrapper.build(86, (byte)2));
        this.replants.put("PUMPKIN_3", LegacyDataWrapper.build(86, (byte)3));
        this.replants.put("MELON", LegacyDataWrapper.build(103, (byte)0));
        this.replants.put("SUGAR_CANE_BLOCK", LegacyDataWrapper.build(83, (byte)0));
        this.replants.put("CACTUS", LegacyDataWrapper.build(81, (byte)0));
        this.replants.put("COCOA", LegacyDataWrapper.build(127, (byte)1));

    }

    @Override
    public Random getWorldRandom(World world) {
        try {
            Object worldHandle = world.getClass().getMethod("getHandle").invoke(world);
            return ((Random) this.fieldWorldRandom.get(worldHandle));
        } catch(Exception err) { err.printStackTrace(); }
        return new Random();
    }

    @Override
    public void breakBlock(Block block) {
        try {
            methodSetType.invoke(block, 0, (byte)0, true);
        } catch(Exception err) { err.printStackTrace(); }
    }

    @Override
    public void replant(Block block, Material type) {
        try {
            String name = type.name();
            if(name.equals("PUMPKIN"))
                name = name.concat("_" + (byte) methodGetData.invoke(block));
            LegacyDataWrapper replantWrapper = this.replants.get(name);
            //HyperFarming.console.log(Level.INFO, replantWrapper.getId() + ":" + replantWrapper.getData() + " - " + block.toString());
            methodSetType.invoke(block, replantWrapper.getId(), replantWrapper.getData(), true);
        } catch(Exception err) { err.printStackTrace(); }
    }

    @Override
    public byte getGrowth(Block block) {
        try {
            return (byte) methodGetData.invoke(block);
        } catch(Exception err) { err.printStackTrace(); }
        return (byte)0;
    }

    @Override
    public void damageTool(Player player, ItemStack stack) {
        try {
            Object nmsItem = this.nmsStackHandle.get(stack);
            this.methodDamage.invoke(nmsItem, 1, craftPlayer.getMethod("getHandle").invoke(player));
        } catch(Exception err) { err.printStackTrace(); }
    }

    @Override
    public ItemStack buildSkull(String texture) {
        try {
            Material skullMaterial = BukkitUtils.returnOneOf("SKULL", "PLAYER_HEAD");
            assert skullMaterial != null;
            ItemStack hardStack = new ItemStack(skullMaterial);
            SkullMeta skullMeta = (SkullMeta) hardStack.getItemMeta();
            Field skullProfile = skullMeta.getClass().getDeclaredField("profile"); skullProfile.setAccessible(true);
            GameProfile gameProfileObject = (GameProfile) skullProfile.get(skullMeta);
            if(gameProfileObject == null)
                gameProfileObject = new GameProfile(UUID.randomUUID(), "skull_" + System.currentTimeMillis());
            gameProfileObject.getProperties().put("textures", new Property("textures", texture));
            skullProfile.set(skullMeta, gameProfileObject);
            hardStack.setItemMeta(skullMeta);
            return hardStack;
        } catch(Exception err) { err.printStackTrace(); }
        return null;
    }

    @Override
    public ItemStack setNBTTag(ItemStack stack, String key, int level) {
        if(stack == null)
            return null;
        if(!BukkitUtils.tools.contains(stack.getType()))
            return stack;
        try {
            Object itemStackHandler = craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, stack);
            Class<?> itemStackNMSClass = itemStackHandler.getClass();
            Object nbtTag = (boolean) itemStackNMSClass.getMethod("hasTag").invoke(itemStackHandler) ?
                    itemStackNMSClass.getMethod("getTag").invoke(itemStackHandler) :
                    nbtTagCompound.getConstructors()[0].newInstance();
            //HyperFarming.console.log(Level.INFO, "Enchant invoked! " + key + ":" + level);
            if(level >= 1)
                nbtTagCompound.getMethod("setInt", String.class, int.class).invoke(nbtTag, key, level);
            else
                nbtTagCompound.getMethod("remove", String.class).invoke(nbtTag, key);
            itemStackNMSClass.getMethod("setTag", nbtTagCompound).invoke(itemStackHandler, nbtTag);
            return (ItemStack) craftItemStack.getMethod("asBukkitCopy", itemStackNMSClass).invoke(null, itemStackHandler);
        } catch(Exception err) { err.printStackTrace(); }
        return stack;
    }

    @Override
    public int getNBTTag(ItemStack stack, String key) {
        if(stack == null)
            return 0;
        if(!BukkitUtils.tools.contains(stack.getType()))
            return 0;
        try {
            Object itemStackHandler = craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, stack);
            Class<?> itemStackNMSClass = itemStackHandler.getClass();
            Object nbtTag = (boolean) itemStackNMSClass.getMethod("hasTag").invoke(itemStackHandler) ?
                    itemStackNMSClass.getMethod("getTag").invoke(itemStackHandler) :
                    nbtTagCompound.getConstructors()[0].newInstance();
            return Integer.parseInt(String.valueOf(nbtTagCompound.getMethod("getInt", String.class).invoke(nbtTag, key)));
        } catch(Exception err) { err.printStackTrace(); }
        return 0;
    }

    @Override
    public boolean hasNBTTag(ItemStack stack, String key) {
        if(stack == null)
            return false;
        if(!BukkitUtils.tools.contains(stack.getType()))
            return false;
        try {
            Object itemStackHandler = craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, stack);
            Class<?> itemStackNMSClass = itemStackHandler.getClass();
            Object nbtTag = (boolean) itemStackNMSClass.getMethod("hasTag").invoke(itemStackHandler) ?
                    itemStackNMSClass.getMethod("getTag").invoke(itemStackHandler) :
                    nbtTagCompound.getConstructors()[0].newInstance();
            return (boolean) nbtTagCompound.getMethod("hasKey", String.class).invoke(nbtTag, key);
        } catch(Exception err) { err.printStackTrace(); }
        return false;
    }

    @Override
    public ItemStack setGUITag(ItemStack icon, String key, String data) {
        if(icon == null)
            return null;
        try {
            Object itemStackHandler = craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, icon);
            Class<?> itemStackNMSClass = itemStackHandler.getClass();
            Object nbtTag = (boolean) itemStackNMSClass.getMethod("hasTag").invoke(itemStackHandler) ?
                    itemStackNMSClass.getMethod("getTag").invoke(itemStackHandler) :
                    nbtTagCompound.getConstructors()[0].newInstance();
            nbtTagCompound.getMethod("setString", String.class, String.class).invoke(nbtTag, key, data);
            itemStackNMSClass.getMethod("setTag", nbtTagCompound).invoke(itemStackHandler, nbtTag);
            return (ItemStack) craftItemStack.getMethod("asBukkitCopy", itemStackNMSClass).invoke(null, itemStackHandler);
        } catch(Exception err) { err.printStackTrace(); }
        return icon;
    }

    @Override
    public String getGUITag(ItemStack icon, String key) {
        if(icon == null)
            return "";
        try {
            Object itemStackHandler = craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, icon);
            Class<?> itemStackNMSClass = itemStackHandler.getClass();
            Object nbtTag = (boolean) itemStackNMSClass.getMethod("hasTag").invoke(itemStackHandler) ?
                    itemStackNMSClass.getMethod("getTag").invoke(itemStackHandler) :
                    nbtTagCompound.getConstructors()[0].newInstance();
            return String.valueOf(nbtTagCompound.getMethod("getString", String.class).invoke(nbtTag, key));
        } catch(Exception err) { err.printStackTrace(); }
        return "#none";
    }

    @Override
    public String getTitle(Inventory inventory) {
        try {
            return String.valueOf(inventory.getClass().getMethod("getTitle").invoke(inventory));
        } catch(Exception err) {}
        return "#nullinv";
    }
}

package me.stella.nms.versions;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.stella.HyperFarming;
import me.stella.nms.NMSProtocol;
import me.stella.utility.BukkitUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

public class v1_18_R2 implements NMSProtocol {

    private final Class<?> baseNMSWorld;
    private final Class<?> generatorAccess;
    private final Class<?> blockPosition;
    private final Class<?> iBlockData;
    private final Class<?> nmsItemStack;
    private final Class<?> nmsPlayer;
    private final Class<?> nbtTagCompound;
    private final Class<?> materialBukkit;
    private final Class<?> craftBlockData;
    private final Class<?> craftItemStack;
    private final Method setType;
    private final Method getData;
    private final Method getNMS;
    private final Field blockGeneratorAccess;
    private final Field blockPos;
    private final Map<String, Object> blockDataBank;

    public v1_18_R2() throws Exception {
        this.baseNMSWorld = getNMSClass("net.minecraft.world.level.World");
        this.generatorAccess = getNMSClass("net.minecraft.world.level.GeneratorAccess");
        this.blockPosition = getNMSClass("net.minecraft.core.BlockPosition");
        this.iBlockData = getNMSClass("net.minecraft.world.level.block.state.IBlockData");
        this.nbtTagCompound = getNMSClass("net.minecraft.nbt.NBTTagCompound");
        this.nmsItemStack = getNMSClass("net.minecraft.world.item.ItemStack");
        this.nmsPlayer = getNMSClass("net.minecraft.server.level.EntityPlayer");
        this.craftBlockData = getNMSClass("org.bukkit.craftbukkit.v1_18_R2.block.data.CraftBlockData");
        this.materialBukkit = getNMSClass("org.bukkit.Material");
        this.craftItemStack = getNMSClass("org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack");
        Class<?> craftBukkitBlock = getNMSClass("org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock");
        this.setType = craftBukkitBlock.getMethod("setTypeAndData", this.generatorAccess, this.blockPosition, this.iBlockData, this.iBlockData, boolean.class);
        this.getData = craftBukkitBlock.getMethod("getData");
        this.getNMS = craftBukkitBlock.getMethod("getNMS");
        this.blockGeneratorAccess = craftBukkitBlock.getDeclaredField("world"); this.blockGeneratorAccess.setAccessible(true);
        this.blockPos = craftBukkitBlock.getDeclaredField("position"); this.blockPos.setAccessible(true);
        this.blockDataBank = new HashMap<>();
        this.blockDataBank.put("AIR", buildPresetBlockData("AIR"));
        this.blockDataBank.put("WHEAT", buildPresetBlockData("WHEAT"));
        this.blockDataBank.put("POTATOES", buildPresetBlockData("POTATOES"));
        this.blockDataBank.put("CARROTS", buildPresetBlockData("CARROTS"));
        this.blockDataBank.put("BEETROOTS", buildPresetBlockData("BEETROOTS"));
        this.blockDataBank.put("PUMPKIN", buildPresetBlockData("PUMPKIN"));
        this.blockDataBank.put("MELON", buildPresetBlockData("MELON"));
        this.blockDataBank.put("SUGAR_CANE", buildPresetBlockData("SUGAR_CANE"));
        this.blockDataBank.put("CACTUS", buildPresetBlockData("CACTUS"));
        this.blockDataBank.put("COCOA", buildPresetBlockData("COCOA"));
    }

    private Object buildPresetBlockData(String materialId) throws Exception {
        Object material = materialBukkit.getMethod("getMaterial", String.class).invoke(null, materialId);
        Object blockData = materialBukkit.getMethod("createBlockData").invoke(material);
        return craftBlockData.getMethod("getState").invoke(blockData);
    }

    @Override
    public Random getWorldRandom(World world) {
        try {
            Object handle = world.getClass().getMethod("getHandle").invoke(world);
            return (Random) baseNMSWorld.getField("v").get(handle);
        } catch(Throwable err) { err.printStackTrace(); }
        return null;
    }

    @Override
    public void breakBlock(Block block) {
        try {
            this.setType.invoke(block, this.blockGeneratorAccess.get(block), this.blockPos.get(block),
                    this.getNMS.invoke(block), this.blockDataBank.get("AIR"), true);
        } catch(Throwable err) { err.printStackTrace(); }
    }

    @Override
    public void replant(Block block, Material type) {
        String typeKey = type.name();
        try {
            this.setType.invoke(block, this.blockGeneratorAccess.get(block), this.blockPos.get(block),
                    this.getNMS.invoke(block), this.blockDataBank.get(typeKey), true);
        } catch(Throwable err) { err.printStackTrace(); }
    }

    @Override
    public byte getGrowth(Block block) {
        try {
            return (byte) this.getData.invoke(block);
        } catch(Throwable t) { t.printStackTrace(); }
        return (byte)0;
    }

    @Override
    public void damageTool(Player player, ItemStack stack) {
        try {
            Field handleField = this.craftItemStack.getDeclaredField("handle");
            handleField.setAccessible(true);
            Object handle = handleField.get(stack);
            this.nmsItemStack.getMethod("a", int.class, Random.class, this.nmsPlayer)
                    .invoke(handle, 1, getWorldRandom(player.getWorld()), player.getClass().getMethod("getHandle").invoke(player));
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
            Object nbtTag = (boolean) itemStackNMSClass.getMethod("s").invoke(itemStackHandler) ?
                    itemStackNMSClass.getMethod("t").invoke(itemStackHandler) :
                    nbtTagCompound.getConstructors()[0].newInstance();
            //HyperFarming.console.log(Level.INFO, "Enchant invoked! " + key + ":" + level);
            if(level >= 1)
                nbtTagCompound.getMethod("a", String.class, int.class).invoke(nbtTag, key, level);
            else
                nbtTagCompound.getMethod("r", String.class).invoke(nbtTag, key);
            itemStackNMSClass.getMethod("c", nbtTagCompound).invoke(itemStackHandler, nbtTag);
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
            Object nbtTag = (boolean) itemStackNMSClass.getMethod("s").invoke(itemStackHandler) ?
                    itemStackNMSClass.getMethod("t").invoke(itemStackHandler) :
                    nbtTagCompound.getConstructors()[0].newInstance();
            return Integer.parseInt(String.valueOf(nbtTagCompound.getMethod("h", String.class).invoke(nbtTag, key)));
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
            Object nbtTag = (boolean) itemStackNMSClass.getMethod("s").invoke(itemStackHandler) ?
                    itemStackNMSClass.getMethod("t").invoke(itemStackHandler) :
                    nbtTagCompound.getConstructors()[0].newInstance();
            return (boolean) nbtTagCompound.getMethod("e", String.class).invoke(nbtTag, key);
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
            Object nbtTag = (boolean) itemStackNMSClass.getMethod("s").invoke(itemStackHandler) ?
                    itemStackNMSClass.getMethod("t").invoke(itemStackHandler) :
                    nbtTagCompound.getConstructors()[0].newInstance();
            nbtTagCompound.getMethod("a", String.class, String.class).invoke(nbtTag, key, data);
            itemStackNMSClass.getMethod("c", nbtTagCompound).invoke(itemStackHandler, nbtTag);
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
            Object nbtTag = (boolean) itemStackNMSClass.getMethod("s").invoke(itemStackHandler) ?
                    itemStackNMSClass.getMethod("t").invoke(itemStackHandler) :
                    nbtTagCompound.getConstructors()[0].newInstance();
            return String.valueOf(nbtTagCompound.getMethod("l", String.class).invoke(nbtTag, key));
        } catch(Exception err) { err.printStackTrace(); }
        return "#none";
    }

    @Override
    public String getTitle(Inventory inventory) {
        return "";
    }
}

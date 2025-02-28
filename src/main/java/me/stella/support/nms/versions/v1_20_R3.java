package me.stella.support.nms.versions;

import me.stella.support.nms.NMSProtocol;
import me.stella.utility.BukkitUtils;
import me.stella.utility.WorldRandomSource;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class v1_20_R3 implements NMSProtocol {

    private final Class<?> baseNMSWorld;
    private final Class<?> generatorAccess;
    private final Class<?> blockPosition;
    private final Class<?> iBlockData;
    private final Class<?> nmsItemStack;
    private final Class<?> nbtTagCompound;
    private final Class<?> materialBukkit;
    private final Class<?> craftBlockData;
    private final Class<?> craftItemStack;
    private final Class<?> randomSource;
    private final Method setType;
    private final Method getData;
    private final Method getNMS;
    private final Field blockGeneratorAccess;
    private final Field blockPos;
    private final Map<String, Object> blockDataBank;
    private final Class<?> craftWorld;

    private final static Map<World, Random> worldRandom = new ConcurrentHashMap<>();

    public v1_20_R3() throws Exception {
        this.baseNMSWorld = getNMSClass("net.minecraft.world.level.World");
        this.generatorAccess = getNMSClass("net.minecraft.world.level.GeneratorAccess");
        this.blockPosition = getNMSClass("net.minecraft.core.BlockPosition");
        this.iBlockData = getNMSClass("net.minecraft.world.level.block.state.IBlockData");
        this.nbtTagCompound = getNMSClass("net.minecraft.nbt.NBTTagCompound");
        this.nmsItemStack = getNMSClass("net.minecraft.world.item.ItemStack");
        //Class<?> nmsPlayer = getNMSClass("net.minecraft.server.level.EntityPlayer");
        this.randomSource = getNMSClass("net.minecraft.util.RandomSource");
        this.craftBlockData = getNMSClass("org.bukkit.craftbukkit.v1_20_R3.block.data.CraftBlockData");
        this.materialBukkit = getNMSClass("org.bukkit.Material");
        this.craftItemStack = getNMSClass("org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack");
        this.craftWorld = getNMSClass("org.bukkit.craftbukkit.v1_20_R3.CraftWorld");
        Class<?> craftBukkitBlock = getNMSClass("org.bukkit.craftbukkit.v1_20_R3.block.CraftBlock");
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
            return worldRandom.computeIfAbsent(world, w -> new WorldRandomSource());
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
            Object world = this.craftWorld.getMethod("getHandle").invoke(player.getWorld());
            Object handle = handleField.get(stack);
            this.nmsItemStack.getMethod("hurt", int.class, this.randomSource, getNMSClass("net.minecraft.world.entity.EntityLiving"))
                    .invoke(handle, 1, this.baseNMSWorld.getField("z").get(world), player.getClass().getMethod("getHandle").invoke(player));
        } catch(Exception err) { err.printStackTrace(); }
    }

    @Override
    public ItemStack buildSkull(String texture) {
        URL url;
        try {
            url = getUrlFromBase64(texture);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        PlayerProfile profile = getProfile(url.toString());
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwnerProfile(profile);
        head.setItemMeta(meta);
        return head;
    }

    private URL getUrlFromBase64(String base64) throws Exception {
        String decoded = new String(Base64.getDecoder().decode(base64));
        return new URL(decoded.substring("{\"textures\":{\"SKIN\":{\"url\":\"".length(), decoded.length() - "\"}}}".length()));
    }

    private final UUID RANDOM_UUID = UUID.fromString("92864445-51c5-4c3b-9039-517c9927d1b4");
    private PlayerProfile getProfile(String url) {
        PlayerProfile profile = Bukkit.createPlayerProfile(RANDOM_UUID);
        PlayerTextures textures = profile.getTextures();
        URL urlObject;
        try {
            urlObject = new URL(url);
        } catch (Exception exception) {
            throw new RuntimeException("Invalid URL", exception);
        }
        textures.setSkin(urlObject);
        profile.setTextures(textures);
        return profile;
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
            Object nbtTag = (boolean) itemStackNMSClass.getMethod("u").invoke(itemStackHandler) ?
                    itemStackNMSClass.getMethod("v").invoke(itemStackHandler) :
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
            Object nbtTag = (boolean) itemStackNMSClass.getMethod("u").invoke(itemStackHandler) ?
                    itemStackNMSClass.getMethod("v").invoke(itemStackHandler) :
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
            Object nbtTag = (boolean) itemStackNMSClass.getMethod("u").invoke(itemStackHandler) ?
                    itemStackNMSClass.getMethod("v").invoke(itemStackHandler) :
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
            Object nbtTag = (boolean) itemStackNMSClass.getMethod("u").invoke(itemStackHandler) ?
                    itemStackNMSClass.getMethod("v").invoke(itemStackHandler) :
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
            Object nbtTag = (boolean) itemStackNMSClass.getMethod("u").invoke(itemStackHandler) ?
                    itemStackNMSClass.getMethod("v").invoke(itemStackHandler) :
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

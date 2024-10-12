package me.stella.utility;

import me.stella.HyperFarming;
import me.stella.HyperVariables;
import me.stella.nms.MultiVerItems;
import me.stella.nms.NMSProtocol;
import me.stella.objects.PricedCrop;
import me.stella.plugin.HyperSettings;
import me.stella.plugin.data.FarmerData;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BukkitUtils {

    public static final List<Material> tools = new ArrayList<>();
    public static final Set<UUID> use = new HashSet<>();
    public static final Set<UUID> melonCompression = new HashSet<>();
    public static final Map<String, PricedCrop> pricingTable = new HashMap<>();
    public static final NumberFormat formatter = NumberFormat.getInstance();
    public static final Map<String, ItemStack> idItemMap = new HashMap<>();
    public static final Map<String, String> idLookupMap = new HashMap<>();
    public static SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");

    public static String color(String message) {
        if(!message.contains("&#"))
            return ChatColor.translateAlternateColorCodes('&', message);
        else {
            try {
                Pattern hexColorPattern = Pattern.compile("&#([a-fA-F0-9]{6})");
                Matcher match = hexColorPattern.matcher(message);
                StringBuffer buffer = new StringBuffer();
                while(match.find()) {
                    String hexColor = match.group(1);
                    StringBuilder colorBuilder = new StringBuilder("&x");
                    for(char c: hexColor.toCharArray())
                        colorBuilder.append('&').append(c);
                    match.appendReplacement(buffer, colorBuilder.toString());
                }
                match.appendTail(buffer);
                return ChatColor.translateAlternateColorCodes('&', buffer.toString());
            } catch(Throwable t) { }
            return message;
        }
    }

    public static Material returnOneOf(String... materialId) {
        Class<?> material;
        NMSProtocol protocol = HyperVariables.get(NMSProtocol.class);
        try {
            material = protocol.getNMSClass("org.bukkit.Material");
            for(String id: materialId) {
                try {
                    Material materialInst = (Material) material.getMethod("getMaterial", String.class)
                            .invoke(null, id);
                    if(materialInst != null)
                        return materialInst;
                } catch(Throwable err2) {}
            }
        } catch(Exception err) {}
        return null;
    }

    public static boolean isUnbreakable(ItemStack stack) {
        if(!stack.hasItemMeta())
            return false;
        return stack.getItemMeta().isUnbreakable();
    }

    public static ItemStack buildItemStack(ConfigurationSection section) {
        NMSProtocol serverProtocol = HyperVariables.get(NMSProtocol.class);
        try {
            Class<?> bukkitMaterial = serverProtocol.getNMSClass("org.bukkit.Material");
            String material = section.getString("material", "STONE");
            ItemStack item;
            if(material.startsWith("head-"))
                item = serverProtocol.buildSkull(material.replace("head-", ""));
            else if(material.equals("COCOA_BEANS"))
                item = MultiVerItems.buildCocoaStack();
            else {
                Material type;
                try {
                    type = (Material) bukkitMaterial.getMethod("getMaterial", String.class).invoke(null, material);
                } catch(Exception err) {
                    type = Material.STONE;
                }
                item = new ItemStack(type);
            }
            Set<String> keys = section.getKeys(false);
            if(keys.contains("data"))
                serverProtocol.getNMSClass("org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack")
                        .getMethod("setDurability", byte.class).invoke(item, (byte) section.getInt("data", 0));
            ItemMeta meta = item.getItemMeta();
            if(keys.contains("name"))
                meta.setDisplayName(BukkitUtils.color(section.getString("name")));
            if(keys.contains("lore"))
                meta.setLore(section.getStringList("lore").stream().map(BukkitUtils::color).collect(Collectors.toList()));
            item.setItemMeta(meta);
            return item;
        } catch(Exception err) { err.printStackTrace(); }
        return null;
    }

    public static int handleDropLogic(String type, int fortune, boolean silk, Random worldRandom) {
        HyperSettings pluginConfig = HyperVariables.get(HyperSettings.class);
        if(!pluginConfig.shouldFortuneDrops())
            return 1;
        if(pluginConfig.getFortuneBlacklist().contains(type))
            return 1;
        if(silk)
            return 1;
        switch(type) {
            case "WHEAT":
            case "BEETROOT":
            case "PUMPKIN":
            case "SUGAR_CANE":
            case "CACTUS":
                return 1;
            case "CARROT":
            case "POTATO":
                return worldRandom.nextInt(4 + fortune) + 1;
            case "COCOA":
                return fortune == 0 ? worldRandom.nextInt(1) + 2 : worldRandom.nextInt(fortune) + 3;
            case "MELON":
                return fortune == 0 ? worldRandom.nextInt(4) + 3 : worldRandom.nextInt(2 + (3 * fortune)) + 4;
        }
        return 1;
    }

    public static CompletableFuture<Integer> computeLimit(Player player) {
        return CompletableFuture.supplyAsync(() -> {
           int limit = HyperVariables.get(HyperSettings.class).getDefaultLimit();
           for(PermissionAttachmentInfo permission: player.getEffectivePermissions()) {
               try {
                   if(permission.getPermission().startsWith("farm.limit.")) {
                       int limitParam = Integer.parseInt(permission.getPermission().replace("farm.limit.", ""));
                       if(limitParam > limit)
                           limit = limitParam;
                   }
               } catch(Exception err) { continue; }
           }
           return limit;
        });
    }

    public static boolean isPluginEnabled(String name) {
        Plugin bukkitPlugin = Bukkit.getPluginManager().getPlugin(name);
        if(bukkitPlugin == null)
            return false;
        return bukkitPlugin.isEnabled();
    }

    public static void sendActionBarAsync(Player player, String message) {
        (new BukkitRunnable() {
            @Override
            public void run() {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
            }
        }).runTaskAsynchronously(HyperFarming.inst());
    }

    public static List<ItemStack> buildItemStream(ItemStack origin, int amount) {
        List<ItemStack> output = new ArrayList<>();
        if(amount == 0)
            return output;
        int stacks = amount / 64;
        ItemStack fullStackClone = origin.clone();
        fullStackClone.setAmount(64);
        for(int i = 0; i < stacks; i++)
            output.add(fullStackClone.clone());
        if(amount - (64 * stacks) > 0) {
            ItemStack finalStackClone = origin.clone();
            finalStackClone.setAmount(amount % 64);
            output.add(finalStackClone);
        }
        return output;
    }

    public static List<Material> filter(List<String> keys) {
        ClassLoader server = HyperFarming.inst().getServer().getClass().getClassLoader();
        return keys.stream().filter(key -> {
            try {
                Class<?> materialBukkit = Class.forName("org.bukkit.Material", true, server);
                Method methodMaterial = materialBukkit.getDeclaredMethod("getMaterial", String.class);
                return methodMaterial.invoke(null, key) != null;
            } catch(Exception err) {}
            return false;
        }).map(key -> {
            try {
                Class<?> materialBukkit = Class.forName("org.bukkit.Material", true, server);
                Method methodMaterial = materialBukkit.getDeclaredMethod("getMaterial", String.class);
                return (Material) methodMaterial.invoke(null, key);
            } catch(Exception err) { err.printStackTrace(); }
            return BukkitUtils.returnOneOf("WOOD_PICKAXE", "WOODEN_PICKAXE");
        }).collect(Collectors.toList());
    }

    public static void initPricingTable() {
        pricingTable.clear();
        HyperSettings config = HyperVariables.get(HyperSettings.class);
        FarmerData.getDataTypes().forEach(type -> pricingTable.put(type, config.getCropPricing(type)));
    }

    public static boolean isBasic(ItemStack stackA, ItemStack stackB) {
        if(stackA.getType() != stackB.getType())
            return false;
        if(stackA.getDurability() != stackB.getDurability())
            return false;
        return !stackB.hasItemMeta();
    }

    public static int getRoundedInt(int max, double multiplier) {
        double dm = (double) max * multiplier;
        if(dm > ((int)dm))
            return ((int)dm) + 1;
        return (int) dm;
    }

    public static void initToolList() {
        tools.clear();
        tools.addAll(filter(Arrays.asList(
                "WOOD_HOE",
                "WOODEN_HOE",
                "STONE_HOE",
                "IRON_HOE",
                "GOLD_HOE",
                "GOLDEN_HOE",
                "DIAMOND_HOE",
                "NETHERITE_HOE",
                "WOOD_PICKAXE",
                "WOODEN_PICKAXE",
                "STONE_PICKAXE",
                "IRON_PICKAXE",
                "GOLD_PICKAXE",
                "GOLDEN_PICKAXE",
                "DIAMOND_PICKAXE",
                "NETHERITE_PICKAXE",
                "WOOD_AXE",
                "WOODEN_AXE",
                "STONE_AXE",
                "IRON_AXE",
                "GOLD_AXE",
                "GOLDEN_AXE",
                "DIAMOND_AXE",
                "NETHERITE_AXE"
        )));
    }

    public static void initCropMap() {
        idItemMap.clear();
        idLookupMap.clear();
        idItemMap.put("WHEAT", new ItemStack(Material.WHEAT));
        idItemMap.put("POTATO", new ItemStack(BukkitUtils.returnOneOf("POTATO_ITEM", "POTATO")));
        idItemMap.put("CARROT", new ItemStack(BukkitUtils.returnOneOf("CARROT_ITEM", "CARROT")));
        idItemMap.put("BEETROOT", new ItemStack(Material.BEETROOT));
        idItemMap.put("MELON", new ItemStack(Material.MELON));
        idItemMap.put("MELON_BLOCK", new ItemStack(BukkitUtils.returnOneOf("MELON_BLOCK", "MELON")));
        idItemMap.put("PUMPKIN", new ItemStack(Material.PUMPKIN));
        idItemMap.put("SUGAR_CANE", new ItemStack(Material.SUGAR_CANE));
        idItemMap.put("CACTUS", new ItemStack(Material.CACTUS));
        idItemMap.put("COCOA", new ItemStack(MultiVerItems.buildCocoaStack()));
        idItemMap.forEach((key, stack) -> {
            idLookupMap.put(stack.toString(), key);
        });
    }
}

package me.stella.plugin.listeners;

import me.stella.HyperFarming;
import me.stella.HyperVariables;
import me.stella.nms.MultiVerItems;
import me.stella.nms.NMSProtocol;
import me.stella.objects.CropsWrapper;
import me.stella.plugin.HyperSettings;
import me.stella.plugin.data.FarmerData;
import me.stella.support.SupportedPlugin;
import me.stella.utility.BukkitUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerFarmListener implements Listener {
    private final Map<String, CropsWrapper> storable;
    private final Map<String, Integer> maxGrowth;
    private final Map<String, Material> seededTypes;
    private static final Map<Player, BukkitTask> actionBarTasks = new HashMap<>();
    private static final Map<Player, Map<String, AtomicInteger>> cache = Collections.synchronizedMap(new HashMap<>());
    public static final Map<Player, Map<String, AtomicInteger>> activityCache = Collections.synchronizedMap(new HashMap<>());

    public static void startActionBarQueue(final Player player) {
        final HyperSettings config = HyperVariables.get(HyperSettings.class);
        actionBarTasks.put(player, (new BukkitRunnable() {
            @Override
            public void run() {
                synchronized (cache) {
                    try {
                        if(!cache.containsKey(player))
                            cache.put(player, new HashMap<>());
                        //HyperFarming.console.log(Level.INFO, cache.toString());
                        Map<String, AtomicInteger> playerCache = cache.get(player);
                        Set<String> reset = new HashSet<>();
                        playerCache.forEach((type, amount) -> {
                            if(amount.get() > 0) {
                                reset.add(type);
                                BukkitUtils.sendActionBarAsync(player, BukkitUtils.color(config.getMessage("action-bar-add")
                                        .replace("{amount}", BukkitUtils.formatter.format(amount))
                                        .replace("{type}", config.getTypeName(type))));
                                // HyperFarming.console.log(Level.INFO, "Cache flush -> " + type + " -> " + amount);
                            }
                        });
                        if(!reset.isEmpty())
                            player.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                        reset.forEach(e -> playerCache.get(e).set(0));
                    } catch(Exception err) { this.cancel(); }
                }
            }
        }).runTaskTimerAsynchronously(HyperFarming.inst(), 0L, 20L));
    }

    public static void stopActionBarQueue(final Player player) {
        if(!actionBarTasks.containsKey(player))
            actionBarTasks.get(player).cancel();
        actionBarTasks.remove(player);
        cache.remove(player);
    }

    public PlayerFarmListener() {
        this.storable = new HashMap<>();
        this.storable.put("CROPS", CropsWrapper.build("WHEAT", new ItemStack(Material.WHEAT)));
        this.storable.put("POTATO", CropsWrapper.build("POTATO", new ItemStack(BukkitUtils.returnOneOf("POTATO_ITEM", "POTATO"))));
        this.storable.put("POTATOES", CropsWrapper.build("POTATO", new ItemStack(BukkitUtils.returnOneOf("POTATO_ITEM", "POTATO"))));
        this.storable.put("CARROT", CropsWrapper.build("CARROT", new ItemStack(BukkitUtils.returnOneOf("CARROT_ITEM", "CARROT"))));
        this.storable.put("CARROTS", CropsWrapper.build("CARROT", new ItemStack(BukkitUtils.returnOneOf("CARROT_ITEM", "CARROT"))));
        this.storable.put("BEETROOT_BLOCK", CropsWrapper.build("BEETROOT", new ItemStack(Material.BEETROOT)));
        this.storable.put("MELON_BLOCK", CropsWrapper.build("MELON", new ItemStack(BukkitUtils.returnOneOf("MELON_SLICES", "MELON"))));
        this.storable.put("MELON", CropsWrapper.build("MELON", new ItemStack(BukkitUtils.returnOneOf("MELON_SLICES", "MELON"))));
        this.storable.put("PUMPKIN", CropsWrapper.build("PUMPKIN", new ItemStack(Material.PUMPKIN)));
        this.storable.put("COCOA", CropsWrapper.build("COCOA", MultiVerItems.buildCocoaStack()));
        this.storable.put("SUGAR_CANE_BLOCK", CropsWrapper.build("SUGAR_CANE", new ItemStack(Material.SUGAR_CANE)));
        this.storable.put("CACTUS", CropsWrapper.build("CACTUS", new ItemStack(Material.CACTUS)));
        this.maxGrowth = new HashMap<>();
        this.maxGrowth.put("CROPS", 7);
        this.maxGrowth.put("POTATO", 7);
        this.maxGrowth.put("CARROT", 7);
        this.maxGrowth.put("BEETROOT_BLOCK", 3);
        this.maxGrowth.put("MELON_BLOCK", 0);
        this.maxGrowth.put("PUMPKIN", 0);
        this.maxGrowth.put("COCOA", 9);
        this.maxGrowth.put("SUGAR_CANE_BLOCK", 0);
        this.maxGrowth.put("CACTUS", 0);
        this.seededTypes = new HashMap<>();
        this.seededTypes.put("WHEAT", BukkitUtils.returnOneOf("SEEDS", "WHEAT_SEEDS"));
        this.seededTypes.put("BEETROOT", Material.BEETROOT_SEEDS);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(final BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        final Material blockType = block.getType();
        final ItemStack breakStack = player.getInventory().getItemInMainHand();
        final FarmerData farmerData = (FarmerData) player.getMetadata("farmerData").get(0).value();
        final SupportedPlugin externalPluginProt = HyperVariables.get(SupportedPlugin.class);
        // random debug shit
        /*
        HyperFarming.console.log(Level.INFO, "Storable -> " + this.storable.containsKey(blockType.name()));
        HyperFarming.console.log(Level.INFO, "Tool -> " + breakStack.getType().name() + " - " + BukkitUtils.tools.contains(breakStack.getType()));
        HyperFarming.console.log(Level.INFO, "Port -> " + externalPluginProt.getClass().getSimpleName() + " - " + externalPluginProt.canBreak(player, block));
         */
        if(this.storable.containsKey(blockType.name()) && externalPluginProt.canBreak(player, block) && BukkitUtils.use.contains(player.getUniqueId())) {
            event.setDropItems(false);
            final boolean tool = breakStack != null && BukkitUtils.tools.contains(breakStack.getType());
            final NMSProtocol serverProtocol = HyperVariables.get(NMSProtocol.class);
            final World blockWorld = block.getWorld();
            final Random random = serverProtocol.getWorldRandom(blockWorld);
            final HyperSettings config = HyperVariables.get(HyperSettings.class);
            CropsWrapper wrapper = this.storable.get(blockType.name());
            String actionBarId = config.getTypeName(wrapper.getDataID());
            if(farmerData.isFull(wrapper.getDataID())) {
                BukkitUtils.sendActionBarAsync(player, BukkitUtils.color(config.getMessage("action-bar-full")
                        .replace("{type}", actionBarId)));
                event.setCancelled(true);
                return;
            }
            List<Block> toBreak = new ArrayList<>();
            toBreak.add(block);
            if(blockType.name().equals("CACTUS") || blockType.name().equals("SUGAR_CANE_BLOCK")) {
                Location blockLoc = block.getLocation().clone();
                for(int i = blockLoc.getBlockY() + 1; i < 256; i++) {
                    Block nextBlock = new Location(blockWorld, blockLoc.getBlockX(), i, blockLoc.getBlockZ()).getBlock();
                    if(nextBlock.getType() == blockType)
                        toBreak.add(nextBlock);
                    else break;
                }
            }
            final int growthCurrent = serverProtocol.getGrowth(block);
            AtomicBoolean replantInvoked = new AtomicBoolean(false);
            if(serverProtocol.hasNBTTag(breakStack, "replant") && !config.getReplantBlackList().contains(wrapper.getDataID())) {
                (new BukkitRunnable() {
                    @Override
                    public void run() {
                        serverProtocol.replant(block, blockType);
                    }
                }).runTaskLater(HyperFarming.inst(), 2L);
                replantInvoked.set(true);
            }
            for(Block brokenBlock: toBreak)
                (new BukkitRunnable() {
                    @Override
                    public void run() {
                        serverProtocol.breakBlock(brokenBlock);
                    }
                }).runTask(HyperFarming.inst());
            if(player.getGameMode() == GameMode.SURVIVAL && breakStack != null && tool && !BukkitUtils.isUnbreakable(breakStack))
                serverProtocol.damageTool(player, breakStack);
            farmerData.breakBlock();
            // run drops calculations
            (new BukkitRunnable() {
                @Override
                public void run() {
                    if(!(cache.containsKey(player)))
                        synchronized (cache) {
                            cache.put(player, new HashMap<>());
                        }
                    int bonusRequired = BukkitUtils.getRoundedInt(maxGrowth.get(blockType.name()), config.getRequiredGrowthForBonus());
                    int baseDrop = 0;
                    int fortune = tool ? breakStack.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) : 0;
                    for(int k = 0; k < toBreak.size(); k++)
                        baseDrop += (growthCurrent >= bonusRequired) ?
                                BukkitUtils.handleDropLogic(wrapper.getDataID(), fortune, random) : 1;
                    int seedDrops;
                    if(growthCurrent >= bonusRequired)
                        seedDrops = seededTypes.containsKey(wrapper.getDataID()) ? random.nextInt(2) + 1 : 0;
                    else {
                        seedDrops = 1;
                        if(seededTypes.containsKey(wrapper.getDataID()))
                            baseDrop = 0;
                    }
                    if(tool) {
                        String keyMultiplierType = "multiplier_" + wrapper.getDataID();
                        int data;
                        if(serverProtocol.hasNBTTag(breakStack, keyMultiplierType))
                            data = serverProtocol.getNBTTag(breakStack, keyMultiplierType);
                        else if(serverProtocol.hasNBTTag(breakStack, "multiplier_ALL"))
                            data = serverProtocol.getNBTTag(breakStack, "multiplier_ALL");
                        else data = 100;
                        if(data <= 100)
                            data = 100;
                        if(growthCurrent >= bonusRequired) {
                            baseDrop = (int) (double) baseDrop * (data / 100);
                            seedDrops = (int) (double) baseDrop * (data / 100);
                        }
                    }
                    if(replantInvoked.get()) {
                        if(seedDrops > 0)
                            seedDrops--;
                        else
                            baseDrop--;
                    }
                    final int finalSeedDrops = seedDrops;
                    if(seedDrops > 0) {
                        (new BukkitRunnable() {
                            @Override
                            public void run() {
                                ItemStack seedStack = new ItemStack(seededTypes.get(wrapper.getDataID()), finalSeedDrops);
                                blockWorld.dropItemNaturally(player.getEyeLocation(), seedStack.clone());
                            }
                        }).runTask(HyperFarming.inst());
                    }
                    if(baseDrop > 0) {
                        int spare = farmerData.increase(wrapper.getDataID(), baseDrop);
                        int amountAdded = baseDrop - spare;
                        //HyperFarming.console.log(Level.INFO, "Break added -> " + amountAdded);
                        synchronized (cache) {
                            Map<String, AtomicInteger> playerCache = cache.get(player);
                            if(!playerCache.containsKey(wrapper.getDataID()))
                                playerCache.put(wrapper.getDataID(), new AtomicInteger(0));
                            playerCache.get(wrapper.getDataID()).addAndGet(amountAdded);
                        }
                        synchronized (activityCache) {
                            activityCache.get(player).get(wrapper.getDataID()).addAndGet(amountAdded);
                        }
                        if(spare > 0) {
                            List<ItemStack> droppedStream = BukkitUtils.buildItemStream(wrapper.getDropStack(), spare);
                            (new BukkitRunnable() {
                                @Override
                                public void run() {
                                    for(ItemStack stack: droppedStream)
                                        blockWorld.dropItemNaturally(player.getEyeLocation(), stack);
                                }
                            }).runTask(HyperFarming.inst());
                        }
                    }
                }
            }).runTaskAsynchronously(HyperFarming.inst());
        }
    }

}

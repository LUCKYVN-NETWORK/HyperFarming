package me.stella.gui;

import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIBuilder;
import me.stella.HyperFarming;
import me.stella.HyperVariables;
import me.stella.functions.FunctionSell;
import me.stella.functions.FunctionSmartDeposit;
import me.stella.functions.FunctionTake;
import me.stella.nms.NMSProtocol;
import me.stella.objects.ClickWrapper;
import me.stella.objects.PricedCrop;
import me.stella.plugin.HyperSettings;
import me.stella.plugin.data.FarmerData;
import me.stella.plugin.listeners.PlayerFarmListener;
import me.stella.support.EconomyFramework;
import me.stella.utility.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class HyperGUIBuilder {

    protected static Map<Player, Inventory> cachedInventory = new HashMap<>();
    protected static Map<Inventory, Map<String, HyperGUIButton>> buttonCache = new HashMap<>();

    private File file;
    private FileConfiguration config;

    public HyperGUIBuilder(File file) {
        this.file = file;
        reload();
    }

    public void reload() {
        this.file = file.getAbsoluteFile();
        this.config = YamlConfiguration.loadConfiguration(this.file);
        cachedInventory.clear();
        buttonCache.clear();
    }

    public CompletableFuture<Inventory> buildInventory(final Player player) {
        final HyperGUIHandle handle = HyperVariables.get(HyperGUIHandle.class);
        return CompletableFuture.supplyAsync(() -> {
            final NMSProtocol serverProtocol = HyperVariables.get(NMSProtocol.class);
            ((FarmerData)player.getMetadata("farmerData").get(0).value()).setLimit(BukkitUtils.computeLimit(player).join());
            if(cachedInventory.containsKey(player)) {
                Inventory cache = cachedInventory.get(player);
                buttonCache.get(cache).values().forEach(button -> {
                    button.handleShow(player);
                });
                return cache;
            } else {
                Inventory inventory = Bukkit.createInventory(handle, this.config.getInt("layout.size", 45),
                        BukkitUtils.color(this.config.getString("layout.title").replace("{player}", player.getName())));
                Map<String, HyperGUIButton> guiButtons = new HashMap<>();
                this.config.getConfigurationSection("elements").getKeys(false).forEach(button -> {
                    ConfigurationSection buttonSection = this.config.getConfigurationSection("elements." + button);
                    ItemStack representItem = BukkitUtils.buildItemStack(buttonSection).clone();
                    ButtonMode mode; ButtonRenderer renderer;
                    try {
                        mode = ButtonMode.valueOf(buttonSection.getString("mode", "DECORATION"));
                        renderer = ButtonRenderer.valueOf(buttonSection.getString("mode", "DECORATION"));
                    } catch(Exception err) { err.printStackTrace(); mode = ButtonMode.DECORATION; renderer = ButtonRenderer.DECORATION; }
                    representItem = serverProtocol.setGUITag(representItem, "button", mode.name());
                    representItem = serverProtocol.setGUITag(representItem, "renderer", renderer.name());
                    //HyperFarming.console.log(Level.INFO, "Assigned mode -> " + buttonSection.getString("mode") + " -> " + renderer.name());
                    switch(mode) {
                        case SMART:
                        case INFO:
                            representItem = serverProtocol.setGUITag(representItem, "update", String.valueOf(buttonSection.getInt("update-interval", 60)));
                            break;
                        case STORAGE:
                            representItem = serverProtocol.setGUITag(representItem, "update", String.valueOf(buttonSection.getInt("update-interval", 20)));
                            representItem = serverProtocol.setGUITag(representItem, "function", buttonSection.getString("function", "#none"));
                            break;
                    }
                    if(mode == ButtonMode.DECORATION) {
                        final ItemStack iconCloning = representItem.clone();
                        buttonSection.getIntegerList("slots").forEach(slot -> {
                            ItemStack decorClone = iconCloning.clone();
                            decorClone = serverProtocol.setGUITag(decorClone, "buttonID", "button_" + slot);
                            HyperGUIButton buttonDecor = (new HyperGUIButton(decorClone.clone(), slot) {
                                @Override
                                public void handleClick(ClickWrapper click) {
                                    ButtonMode.valueOf(serverProtocol.getGUITag(getBase(), "button")).runClickProcessor(click, this);
                                }
                                @Override
                                public void handleShow(Player player) {
                                    ButtonRenderer.valueOf(serverProtocol.getGUITag(getBase(), "renderer")).render(player, this);
                                }
                            });
                            buttonDecor.showInInventory(player, inventory);
                            guiButtons.put("button_" + slot, buttonDecor);
                        });
                    } else {
                        int slot = buttonSection.getInt("slot", 0);
                        String key = "button_" + slot;
                        representItem = serverProtocol.setGUITag(representItem, "buttonID", key);
                        HyperGUIButton buttonImpl = (new HyperGUIButton(representItem.clone(), slot) {
                            @Override
                            public void handleClick(ClickWrapper click) {
                                ButtonMode mode = ButtonMode.valueOf(serverProtocol.getGUITag(getBase(), "button"));
                                mode.runClickProcessor(click, this);
                            }
                            @Override
                            public void handleShow(Player player) {
                                //HyperFarming.console.log(Level.INFO, "Renderer activated -> " + serverProtocol.getGUITag(getItem(), "renderer"));
                                ButtonRenderer renderer = ButtonRenderer.valueOf(serverProtocol.getGUITag(getBase(), "renderer"));
                                renderer.render(player, this);
                            }
                        });
                        buttonImpl.showInInventory(player, inventory);
                        guiButtons.put(key, buttonImpl);
                    }
                });
                buttonCache.put(inventory, guiButtons);
                return inventory;
            }
        });
    }

    public void openSyncStorage(Player player) {
        (new BukkitRunnable() {
            @Override
            public void run() {
                Inventory storageGUI = buildInventory(player).join();
                (new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.openInventory(storageGUI);
                    }
                }).runTask(HyperFarming.inst());
            }
        }).runTaskAsynchronously(HyperFarming.inst());
    }

    protected void removeInventoryCache(Player player) {
        if(!cachedInventory.containsKey(player))
            return;
        Inventory cache = cachedInventory.get(player);
        buttonCache.remove(cache);
        cachedInventory.remove(player);
    }

    public enum ButtonMode {

        DECORATION(null),
        EXIT((click, data) -> {
            Player player = click.getClicker();
            ClickType clickType = click.getClickType();
            if(clickType.isShiftClick() || clickType.isKeyboardClick())
                return;
            player.closeInventory();
        }),
        INFO((click, button) -> {
            ClickType clickType = click.getClickType();
            if(clickType.isShiftClick() || clickType.isKeyboardClick())
                return;
            button.handleShow(click.getClicker());
        }),
        SMART((click, button) -> {
            Player player = click.getClicker();
            (new BukkitRunnable() {
                @Override
                public void run() {
                    HyperSettings config = HyperVariables.get(HyperSettings.class);
                    List<String[]> processReturn = FunctionSmartDeposit.deposit(player);
                    player.closeInventory();
                    processReturn.forEach(response -> {
                        if(response[0].equals("fail")) {
                            switch(response[1]) {
                                case "storage_full":
                                    player.sendMessage(BukkitUtils.color(config.getMessage("storage-full")));
                                    break;
                                default:
                                    break;
                            }
                            return;
                        }
                        int amount = Integer.parseInt(response[2]);
                        player.sendMessage(BukkitUtils.color(config.getMessage("deposit")
                                .replace("{amount}", BukkitUtils.formatter.format(amount))
                                .replace("{type}", config.getTypeName(response[1]))));
                    });
                }
            }).runTask(HyperFarming.inst());
        }),
        STORAGE((click, button) -> {
            final NMSProtocol protocol = HyperVariables.get(NMSProtocol.class);
            final Player player = click.getClicker();
            final ClickType clickType = click.getClickType();
            final String data = protocol.getGUITag(button.getBase(), "function");
            if(data.equals("#none"))
                return;
            if(!FarmerData.getDataTypes().contains(data))
                return;
            final FarmerData internalData = (FarmerData) player.getMetadata("farmerData").get(0).value();
            final HyperSettings config = HyperVariables.get(HyperSettings.class);
            switch(clickType) {
                case LEFT:
                    if(internalData.getData(data) == 0) {
                        player.closeInventory();
                        player.sendMessage(BukkitUtils.color(config.getMessage("insufficient-balance")
                                .replace("{type}", config.getTypeName(data))));
                        break;
                    }
                    List<String> signLayoutTake = config.getSignLayout("take")
                            .stream().map(line -> BukkitUtils.color(line.replace("{type}", config.getTypeName(data))))
                            .collect(Collectors.toList());
                    if(signLayoutTake.size() < 4) {
                        for(int i = 0; i < 4 - signLayoutTake.size(); i++)
                            signLayoutTake.add("");
                    }
                    player.closeInventory();
                    SignGUIBuilder builderTake = SignGUI.builder();
                    for(int i = 0; i < 4; i++)
                        builderTake.setLine(i, signLayoutTake.get(i));
                    builderTake.setHandler((p, r) -> {
                        (new BukkitRunnable() {
                            @Override
                            public void run() {
                                HyperSettings config = HyperVariables.get(HyperSettings.class);
                                String inputLine = r.getLineWithoutColor(0);
                                if(inputLine.isEmpty())
                                    return;
                                String[] response = FunctionTake.take(p, new String[]{data, inputLine });
                                if(response[0].equals("fail")) {
                                    p.sendMessage(BukkitUtils.color(config.getMessage("invalid_int").replace("%input%", response[2])));
                                } else {
                                    int amount = Integer.parseInt(response[2]);
                                    if(BukkitUtils.melonCompression.contains(player.getUniqueId()) && response[1].equals("MELON")) {
                                        ItemStack melonBlock = new ItemStack(BukkitUtils.returnOneOf("MELON_BLOCK", "MELON")).clone();
                                        int amountBlock = amount / 9; int spare = amount % 9;
                                        for(ItemStack blockStack: BukkitUtils.buildItemStream(melonBlock, amountBlock))
                                            player.getInventory().addItem(blockStack);
                                        player.getInventory().addItem(new ItemStack(Material.MELON, spare));
                                        player.sendMessage(BukkitUtils.color(config.getMessage("take")
                                                .replace("{amount}", BukkitUtils.formatter.format(amount))
                                                .replace("{type}", config.getTypeName(response[1]))));
                                        if(response[1].equals("MELON"))
                                            player.sendMessage(BukkitUtils.color(config.getMessage("melon-reminder-on")));
                                    } else {
                                        ItemStack typeItem = BukkitUtils.idItemMap.get(response[1]).clone();
                                        for (ItemStack itemStack : BukkitUtils.buildItemStream(typeItem, amount))
                                            player.getInventory().addItem(itemStack);
                                        player.sendMessage(BukkitUtils.color(config.getMessage("take")
                                                .replace("{amount}", BukkitUtils.formatter.format(amount))
                                                .replace("{type}", config.getTypeName(response[1]))));
                                        if(response[1].equals("MELON"))
                                            player.sendMessage(BukkitUtils.color(config.getMessage("melon-reminder-off")));
                                    }
                                }
                            }
                        }).runTask(HyperFarming.inst());
                        return Collections.emptyList();
                    });
                    builderTake.build().open(player);
                    break;
                case SHIFT_LEFT:
                    if(internalData.getData(data) == 0) {
                        player.closeInventory();
                        player.sendMessage(BukkitUtils.color(config.getMessage("insufficient-balance")
                                .replace("{type}", config.getTypeName(data))));
                        break;
                    }
                    (new BukkitRunnable() {
                        @Override
                        public void run() {
                            String[] response = FunctionTake.take(player, new String[] { data, "all" });
                            int amount = Integer.parseInt(response[2]);
                            if(BukkitUtils.melonCompression.contains(player.getUniqueId()) && response[1].equals("MELON")) {
                                ItemStack melonBlock = new ItemStack(BukkitUtils.returnOneOf("MELON_BLOCK", "MELON")).clone();
                                int amountBlock = amount / 9; int spare = amount % 9;
                                for(ItemStack blockStack: BukkitUtils.buildItemStream(melonBlock, amountBlock))
                                    player.getInventory().addItem(blockStack);
                                player.getInventory().addItem(new ItemStack(Material.MELON, spare));
                                player.sendMessage(BukkitUtils.color(config.getMessage("take")
                                        .replace("{amount}", BukkitUtils.formatter.format(amount))
                                        .replace("{type}", config.getTypeName(response[1]))));
                                if(response[1].equals("MELON"))
                                    player.sendMessage(BukkitUtils.color(config.getMessage("melon-reminder-on")));
                            } else {
                                ItemStack typeItem = BukkitUtils.idItemMap.get(response[1]).clone();
                                for (ItemStack itemStack : BukkitUtils.buildItemStream(typeItem, amount))
                                    player.getInventory().addItem(itemStack);
                                player.sendMessage(BukkitUtils.color(config.getMessage("take")
                                        .replace("{amount}", BukkitUtils.formatter.format(amount))
                                        .replace("{type}", config.getTypeName(response[1]))));
                                if(response[1].equals("MELON"))
                                    player.sendMessage(BukkitUtils.color(config.getMessage("melon-reminder-off")));
                            }
                        }
                    }).runTask(HyperFarming.inst());
                    break;
                case RIGHT:
                    if(internalData.getData(data) == 0) {
                        player.closeInventory();
                        player.sendMessage(BukkitUtils.color(config.getMessage("insufficient-balance")
                                .replace("{type}", config.getTypeName(data))));
                        break;
                    }
                    List<String> signLayoutSell = config.getSignLayout("sell")
                            .stream().map(line -> BukkitUtils.color(line.replace("{type}", config.getTypeName(data))))
                            .collect(Collectors.toList());
                    if(signLayoutSell.size() < 4) {
                        for(int i = 0; i < 4 - signLayoutSell.size(); i++)
                            signLayoutSell.add("");
                    }
                    player.closeInventory();
                    SignGUIBuilder builderSell = SignGUI.builder();
                    for(int i = 0; i < 4; i++)
                        builderSell.setLine(i, signLayoutSell.get(i));
                    builderSell.setHandler((p, r) -> {
                        (new BukkitRunnable() {
                            @Override
                            public void run() {
                                HyperSettings config = HyperVariables.get(HyperSettings.class);
                                String inputLine = r.getLineWithoutColor(0);
                                if(inputLine.isEmpty())
                                    return;
                                String[] response = FunctionSell.sell(p, new String[]{data, inputLine });
                                if(response[0].equals("fail")) {
                                    p.sendMessage(BukkitUtils.color(config.getMessage("invalid_int").replace("%input%", response[2])));
                                } else {
                                    EconomyFramework serverEconomy = HyperVariables.get(EconomyFramework.class);
                                    PricedCrop pricing = BukkitUtils.pricingTable.get(response[1]);
                                    int amount = Integer.parseInt(response[2]);
                                    double earned = Math.floor(pricing.getPerValue() * ((double) amount / pricing.getPerCount()));
                                    serverEconomy.giveMoney(player, (long) earned);
                                    player.sendMessage(BukkitUtils.color(config.getMessage("sell")
                                            .replace("{amount}", BukkitUtils.formatter.format(amount))
                                            .replace("{type}", config.getTypeName(response[1]))
                                            .replace("{money}", BukkitUtils.formatter.format(earned))));
                                }
                            }
                        }).runTask(HyperFarming.inst());
                        return Collections.emptyList();
                    });
                    builderSell.build().open(player);
                    break;
                case SHIFT_RIGHT:
                    if(internalData.getData(data) == 0) {
                        player.closeInventory();
                        player.sendMessage(BukkitUtils.color(config.getMessage("insufficient-balance")
                                .replace("{type}", config.getTypeName(data))));
                        break;
                    }
                    (new BukkitRunnable() {
                        @Override
                        public void run() {
                            String[] response = FunctionSell.sell(player, new String[] { data, "all" });
                            EconomyFramework serverEconomy = HyperVariables.get(EconomyFramework.class);
                            PricedCrop pricing = BukkitUtils.pricingTable.get(response[1]);
                            int amount = Integer.parseInt(response[2]);
                            double earned = Math.floor(pricing.getPerValue() * ((double) amount / pricing.getPerCount()));
                            serverEconomy.giveMoney(player, (long) earned);
                            player.sendMessage(BukkitUtils.color(config.getMessage("sell")
                                    .replace("{amount}", BukkitUtils.formatter.format(amount))
                                    .replace("{type}", config.getTypeName(response[1]))
                                    .replace("{money}", BukkitUtils.formatter.format(earned))));
                        }
                    }).runTask(HyperFarming.inst());
                    break;
            }
        });

        private final BiConsumer<ClickWrapper, HyperGUIButton> processor;

        ButtonMode(BiConsumer<ClickWrapper, HyperGUIButton> clickProcessor) {
            this.processor = clickProcessor;
        }

        public void runClickProcessor(ClickWrapper click, HyperGUIButton button) {
            if(this.processor == null)
                return;
            this.processor.accept(click, button);
        }
    }


    public enum ButtonRenderer {

        DECORATION(null),
        EXIT(null),
        INFO((player, button) -> {
            FarmerData data = (FarmerData) player.getMetadata("farmerData").get(0).value();
            ItemStack buttonIcon = button.getBase().clone();
            ItemMeta buttonMeta = buttonIcon.getItemMeta();
            final List<String> formattedLore = buttonMeta.getLore().stream().map(line -> {
                synchronized (PlayerFarmListener.activityCache) {
                    Map<String, AtomicInteger> activityThreadCurrent = PlayerFarmListener.activityCache.get(player);
                    AtomicInteger backup = new AtomicInteger(0);
                    return line.replace("{update}", BukkitUtils.timeFormatter.format(Calendar.getInstance().getTime()))
                            .replace("{broken}", BukkitUtils.formatter.format(data.getBlocksBroken()))
                            .replace("{wheat_online}", BukkitUtils.formatter.format(activityThreadCurrent.getOrDefault("WHEAT", backup).get()))
                            .replace("{carrot_online}", BukkitUtils.formatter.format(activityThreadCurrent.getOrDefault("CARROT", backup).get()))
                            .replace("{potato_online}", BukkitUtils.formatter.format(activityThreadCurrent.getOrDefault("POTATO", backup).get()))
                            .replace("{beetroot_online}", BukkitUtils.formatter.format(activityThreadCurrent.getOrDefault("BEETROOT", backup).get()))
                            .replace("{pumpkin_online}", BukkitUtils.formatter.format(activityThreadCurrent.getOrDefault("PUMPKIN", backup).get()))
                            .replace("{melon_online}", BukkitUtils.formatter.format(activityThreadCurrent.getOrDefault("MELON", backup).get()))
                            .replace("{cactus_online}", BukkitUtils.formatter.format(activityThreadCurrent.getOrDefault("CACTUS", backup).get()))
                            .replace("{sugar_cane_online}", BukkitUtils.formatter.format(activityThreadCurrent.getOrDefault("SUGAR_CANE", backup).get()))
                            .replace("{cocoa_online}", BukkitUtils.formatter.format(activityThreadCurrent.getOrDefault("COCOA", backup).get()));
                }
            }).collect(Collectors.toList());
            buttonMeta.setLore(formattedLore);
            buttonIcon.setItemMeta(buttonMeta);
            button.setDisplay(buttonIcon);
            Inventory top = player.getOpenInventory().getTopInventory();
            if(top.getHolder() instanceof HyperGUIHandle)
                button.inject(top);
        }),
        SMART((player, button) -> {
            PlayerInventory inventory = player.getInventory();
            Map<String, Integer> data = new HashMap<>();
            for(int i = 0; i < 36; i++) {
                ItemStack item = inventory.getItem(i);
                if(item == null || item.getType().name().equals("AIR"))
                    continue;
                ItemStack cloneLookup = item.clone();
                cloneLookup.setAmount(1);
                String key = BukkitUtils.idLookupMap.getOrDefault(cloneLookup.toString(), "#none");
                if(key.equals("#none"))
                    continue;
                data.put(key, data.getOrDefault(key, 0) + item.getAmount());
            }
            if(BukkitUtils.melonCompression.contains(player.getUniqueId()))
                data.put("MELON", data.getOrDefault("MELON", 0) + data.getOrDefault("MELON_BLOCK", 0) * 9);
            ItemStack buttonIcon = button.getBase().clone();
            ItemMeta buttonMeta = buttonIcon.getItemMeta();
            final List<String> formattedLore = buttonMeta.getLore().stream().map(line ->
                    line.replace("{wheat}", BukkitUtils.formatter.format(data.getOrDefault("WHEAT",0)))
                    .replace("{carrot}", BukkitUtils.formatter.format(data.getOrDefault("CARROT",0)))
                    .replace("{potato}", BukkitUtils.formatter.format(data.getOrDefault("POTATO",0)))
                    .replace("{beetroot}", BukkitUtils.formatter.format(data.getOrDefault("BEETROOT",0)))
                    .replace("{pumpkin}", BukkitUtils.formatter.format(data.getOrDefault("PUMPKIN",0)))
                    .replace("{melon}", BukkitUtils.formatter.format(data.getOrDefault("MELON",0)))
                    .replace("{cactus}", BukkitUtils.formatter.format(data.getOrDefault("CACTUS",0)))
                    .replace("{sugar_cane}", BukkitUtils.formatter.format(data.getOrDefault("SUGAR_CANE",0)))
                    .replace("{cocoa}", BukkitUtils.formatter.format(data.getOrDefault("COCOA",0))))
                    .collect(Collectors.toList());
            buttonMeta.setLore(formattedLore);
            buttonIcon.setItemMeta(buttonMeta);
            button.setDisplay(buttonIcon);
            Inventory top = player.getOpenInventory().getTopInventory();
            if(top.getHolder() instanceof HyperGUIHandle)
                button.inject(top);
        }),
        STORAGE((player, button) -> {
            final NMSProtocol protocol = HyperVariables.get(NMSProtocol.class);
            String data = protocol.getGUITag(button.getBase(), "function");
            //HyperFarming.console.log(Level.INFO, "Storage update invoked -> " + data);
            if(data.equals("#none"))
                return;
            FarmerData internalData = (FarmerData) player.getMetadata("farmerData").get(0).value();
            ItemStack buttonIcon = button.getBase().clone();
            ItemMeta buttonMeta = buttonIcon.getItemMeta();
            buttonMeta.setLore(buttonMeta.getLore()
                    .stream().map(line -> line.replace("{amount}", BukkitUtils.formatter.format(internalData.getData(data)))
                            .replace("{limit}", BukkitUtils.formatter.format(internalData.getLimit())))
                    .collect(Collectors.toList()));
            buttonIcon.setItemMeta(buttonMeta); button.setDisplay(buttonIcon);
            Inventory top = player.getOpenInventory().getTopInventory();
            if(top.getHolder() instanceof HyperGUIHandle)
                button.inject(top);
        });

        private final BiConsumer<Player, HyperGUIButton> renderer;

        ButtonRenderer(BiConsumer<Player, HyperGUIButton> renderer) {
            this.renderer = renderer;
        }

        public void render(Player player, HyperGUIButton button) {
            if(this.renderer == null)
                return;
            //HyperFarming.console.log(Level.INFO, "Renderer activated -> " + name());
            this.renderer.accept(player, button);
        }
    }



}

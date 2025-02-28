package me.stella.plugin.gui;

import me.stella.HyperFarming;
import me.stella.HyperVariables;
import me.stella.support.nms.NMSProtocol;
import me.stella.utility.objects.ClickWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HyperGUIHandle implements InventoryHolder, Listener {

    private final HyperGUIBuilder builder;

    private final Map<Player, Map<HyperGUIButton, BukkitTask>> updateTasks;

    public HyperGUIHandle(HyperGUIBuilder builder) {
        this.builder = builder;
        this.updateTasks = new ConcurrentHashMap<>();
    }

    public HyperGUIBuilder getInternalBuilder() {
        return this.builder;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void click(InventoryClickEvent event) {
        final Inventory inventory = event.getClickedInventory();
        if(inventory == null)
            return;
        final Player player = (Player) event.getWhoClicked();
        if(!(inventory.getHolder() instanceof HyperGUIHandle))
            return;
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if(clickedItem == null || clickedItem.getType().name().equals("AIR"))
            return;
        Map<String, HyperGUIButton> buttonCache = HyperGUIBuilder.buttonCache.getOrDefault(inventory, new HashMap<>());
        if(buttonCache.isEmpty()) {
            player.closeInventory();
            return;
        }
        NMSProtocol protocol = HyperVariables.get(NMSProtocol.class);
        if(protocol.getGUITag(clickedItem, "buttonID").equals("#none"))
            return;
        HyperGUIButton button = buttonCache.get(protocol.getGUITag(clickedItem, "buttonID"));
        if(button == null)
            return;
        button.handleClick(ClickWrapper.build(player, event.getClick()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void open(InventoryOpenEvent event) {
        Inventory inventory = event.getInventory();
        Player player = (Player) event.getPlayer();
        if(!(inventory.getHolder() instanceof HyperGUIHandle))
            return;
        Map<String, HyperGUIButton> buttons = HyperGUIBuilder.buttonCache.getOrDefault(inventory, new HashMap<>());
        if(buttons.isEmpty()) {
            player.closeInventory();
            return;
        }
        final NMSProtocol serverProtocol = HyperVariables.get(NMSProtocol.class);
        Map<HyperGUIButton, BukkitTask> tasks = new HashMap<>();
        buttons.values().forEach(button -> button.handleShow(player));
        new HashSet<>(buttons.values()).stream()
                .filter(button -> {
                    String type = serverProtocol.getGUITag(button.getBase(), "button");
                    return type.equals("INFO") || type.equals("STORAGE") || type.equals("SMART");
                }).forEach(button -> {
                    long interval = Integer.parseInt(serverProtocol.getGUITag(button.getBase(), "update"));
                    BukkitTask taskUpdate = (new BukkitRunnable() {
                        @Override
                        public void run() {
                            button.handleShow(player);
                        }
                    }).runTaskTimer(HyperFarming.inst(), interval, interval);
                    tasks.put(button, taskUpdate);
                });
        if(!tasks.isEmpty())
            updateTasks.put(player, tasks);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void close(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        Player player = (Player) event.getPlayer();
        if(!(inventory.getHolder() instanceof HyperGUIHandle))
            return;
        (new BukkitRunnable() {
            @Override
            public void run() {
                Map<HyperGUIButton, BukkitTask> tasks = updateTasks.getOrDefault(player, new HashMap<>());
                tasks.forEach((button, task) -> {
                    button.handleShow(player);
                    task.cancel();
                });
                updateTasks.remove(player);
            }
        }).runTaskAsynchronously(HyperFarming.inst());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void quit(PlayerQuitEvent event) {
        Map<HyperGUIButton, BukkitTask> activeUpdates = updateTasks.getOrDefault(event.getPlayer(), new HashMap<>());
        activeUpdates.values().forEach(BukkitTask::cancel);
        updateTasks.remove(event.getPlayer());
        builder.removeInventoryCache(event.getPlayer());
    }
}

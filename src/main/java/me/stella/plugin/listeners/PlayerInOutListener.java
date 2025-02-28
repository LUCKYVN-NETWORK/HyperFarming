package me.stella.plugin.listeners;

import me.stella.HyperFarming;
import me.stella.utility.objects.PlayerWrapper;
import me.stella.plugin.data.DataBuilder;
import me.stella.plugin.data.FarmerData;
import me.stella.utility.BukkitUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerInOutListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        final Player player = e.getPlayer();
        handshakeIn(player);
    }

    public static void handshakeIn(final Player player) {
        BukkitUtils.use.add(player.getUniqueId());
        (new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    FarmerData injection = DataBuilder.readDataInjection(player);
                    player.setMetadata("farmerData",
                            new FixedMetadataValue(HyperFarming.inst(), injection));
                    PlayerFarmListener.startActionBarQueue(player);
                    Map<String, AtomicInteger> activity = new HashMap<>();
                    FarmerData.getDataTypes().forEach(e -> activity.put(e, new AtomicInteger(0)));
                    PlayerFarmListener.activityCache.put(player, activity);
                } catch(Exception err) { err.printStackTrace(); }
            }
        }).runTaskAsynchronously(HyperFarming.inst());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e) {
        final Player player = e.getPlayer();
        PlayerFarmListener.activityCache.remove(player);
        BukkitUtils.use.remove(player.getUniqueId());
        PlayerFarmListener.stopActionBarQueue(player);
        final PlayerWrapper playerWrapper = PlayerWrapper.buildWrapper(player);
        handshakeOut(playerWrapper);
    }

    public static void handshakeOut(final PlayerWrapper asyncWrapper) {
        (new Thread(() -> {
            try {
                DataBuilder.saveData(asyncWrapper);
            } catch(Exception err) { err.printStackTrace(); }
        })).start();
    }

}

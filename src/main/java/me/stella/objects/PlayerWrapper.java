package me.stella.objects;

import me.stella.plugin.data.FarmerData;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerWrapper {

    private final String name;
    private final UUID uuid;
    private final FarmerData data;

    protected PlayerWrapper(String name, UUID uuid, FarmerData data) {
        this.name = name;
        this.uuid = uuid;
        this.data = data;
    }

    public static PlayerWrapper buildWrapper(Player player) {
        return new PlayerWrapper(player.getName(), player.getUniqueId(),
                (FarmerData) player.getMetadata("farmerData").get(0).value());
    }

    public String getName() {
        return name;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public FarmerData getData() {
        return data;
    }
}

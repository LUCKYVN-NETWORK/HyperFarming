package me.stella.objects;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class ClickWrapper {

    private final Player clicker;
    private final ClickType clickType;

    protected ClickWrapper(Player player, ClickType click) {
        this.clicker = player;
        this.clickType = click;
    }

    public Player getClicker() {
        return clicker;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public static ClickWrapper build(Player paramPlayer, ClickType paramClickType) {
        return new ClickWrapper(paramPlayer, paramClickType);
    }
}

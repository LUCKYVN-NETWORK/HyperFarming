package me.stella.support;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface SupportedPlugin {

    boolean canBreak(Player player, Block block);

}

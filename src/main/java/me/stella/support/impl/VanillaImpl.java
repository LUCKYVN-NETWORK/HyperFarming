package me.stella.support.impl;

import me.stella.support.SupportedPlugin;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class VanillaImpl implements SupportedPlugin {

    @Override
    public boolean canBreak(Player player, Block block) {
        return true;
    }
}

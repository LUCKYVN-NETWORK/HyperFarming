package me.stella.support.impl;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import me.stella.HyperVariables;
import me.stella.plugin.HyperSettings;
import me.stella.support.SupportedPlugin;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class SuperiorSkyblockImpl implements SupportedPlugin {

    public SuperiorSkyblockImpl(){}

    @Override
    public boolean canBreak(Player player, Block block) {
        HyperSettings settings = HyperVariables.get(HyperSettings.class);
        if(!settings.onlyBreakableInSuperiorSkyblockIsland() && !player.getWorld().getName().equals("SuperiorWorld"))
            return true;
        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(player);
        Island islandAt = SuperiorSkyblockAPI.getIslandAt(block.getLocation());
        if(islandAt == null)
            return false;
        return islandAt.equals(superiorPlayer.getIsland()) || islandAt.getCoopPlayers().contains(superiorPlayer);
    }

}

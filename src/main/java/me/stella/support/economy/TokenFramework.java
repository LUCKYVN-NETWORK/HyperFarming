package me.stella.support.economy;

import me.realized.tokenmanager.TokenManagerPlugin;
import me.stella.support.EconomyFramework;
import me.stella.utility.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TokenFramework implements EconomyFramework {

    private final TokenManagerPlugin tokenManagerPlugin;

    public TokenFramework() {
        if(!BukkitUtils.isPluginEnabled("TokenManager"))
            throw new RuntimeException("Unable to locate TokenManager!");
        this.tokenManagerPlugin = (TokenManagerPlugin) Bukkit.getPluginManager().getPlugin("TokenManager");
    }

    @Override
    public void giveMoney(Player player, long amount) {
        this.tokenManagerPlugin.addTokens(player, amount);
    }
}

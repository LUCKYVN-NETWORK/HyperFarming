package me.stella.support.economy;

import me.stella.HyperVariables;
import me.stella.support.EconomyFramework;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultFramework implements EconomyFramework {

    private final Economy economy;

    public VaultFramework() {
        RegisteredServiceProvider<Economy> vault = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if(vault.getProvider() == null)
            throw new RuntimeException("Unable to locate Vault!");
        this.economy = vault.getProvider();
    }

    @Override
    public void giveMoney(Player player, long amount) {
        this.economy.depositPlayer(player, amount);
    }
}

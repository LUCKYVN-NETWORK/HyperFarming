package me.stella.plugin;

import me.stella.objects.PricedCrop;
import me.stella.plugin.data.FarmerData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class HyperSettings {

    private File file;
    private FileConfiguration config;

    public HyperSettings(File config) {
        this.file = config.getAbsoluteFile();
        reload();
    }

    public void reload() {
        this.file = file.getAbsoluteFile();
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public String getMessage(String key) {
        return this.config.getString("message." +  key, "");
    }

    public List<String> getMessageBlock(String key) {
        return this.config.getStringList("message." + key);
    }

    public List<String> getSignLayout(String action) {
        return this.config.getStringList("sign." + action);
    }

    public List<String> getEnchantmentLore(String enchant) {
        return this.config.getStringList("enchant." + enchant);
    }

    public boolean shouldFortuneDrops() {
        return this.config.getBoolean("engine.fortune-drops", true);
    }

    public double getRequiredGrowthForBonus() {
        return this.config.getDouble("engine.growth-bonus-requirement", 1.0D);
    }

    public List<String> getFortuneBlacklist() {
        return this.config.getStringList("engine.fortune-blacklist");
    }

    public List<String> getReplantBlackList() {
        return this.config.getStringList("engine.replant-blacklist");
    }

    public int getDefaultLimit() {
        return this.config.getInt("engine.default-limit");
    }

    public boolean onlyBreakableInSuperiorSkyblockIsland() {
        return this.config.getBoolean("engine.support.superiorskyblock.only-at-island", false);
    }

    public boolean allowSuperiorSkyblockCoop() {
        return this.config.getBoolean("engine.support.superiorskyblock.allow-coop", true);
    }

    public PricedCrop getCropPricing(String type) {
        ConfigurationSection pricingSection = this.config.getConfigurationSection("engine.pricing." + type.toLowerCase(Locale.ROOT));
        double price = pricingSection.getDouble("price", 1000.0D);
        int amount = pricingSection.getInt("amount", 64);
        return PricedCrop.build(price, amount);
    }

    public String getUsage(String command) {
        return this.config.getString("commands." + command, "");
    }

    public String getTypeName(String typeKey) {
        return this.config.getString("message.plants." + typeKey, typeKey.replace("_", " "));
    }

    public String getEconomyMode() {
        return this.config.getString("engine.economy-mode", "Vault");
    }
}

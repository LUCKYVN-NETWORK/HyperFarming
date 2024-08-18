package me.stella.plugin.data;

import me.stella.HyperFarming;
import me.stella.HyperVariables;
import me.stella.objects.PlayerWrapper;
import me.stella.plugin.HyperSettings;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DataBuilder {

    private static boolean exportToConfig(FileConfiguration dataConfig, FarmerData data) {
        try {
            Map<String, Integer> exportedData = data.exportData();
            exportedData.forEach((type, amount) -> {
                dataConfig.set("data." + type, amount);
            });
            return true;
        } catch(Exception err) { err.printStackTrace(); }
        return false;
    }

    private static FarmerData importToData(ConfigurationSection sectionData, int limit, long blocksBroken) {
        Map<String, Integer> dataMap = new HashMap<>();
        sectionData.getKeys(false).forEach(type -> {
            dataMap.put(type, sectionData.getInt(type, 0));
        });
        return new FarmerData(limit, dataMap, blocksBroken);
    }

    public static void saveData(PlayerWrapper player) throws Exception {
        FarmerData farmerData = player.getData();
        File route = new File(HyperFarming.inst().getDataFolder(), "data/" + player.getUniqueId().toString().concat(".yml"));
        if(!route.exists())
            route.createNewFile();
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(route);
        configuration.set("player-name", player.getName());
        configuration.set("limit", farmerData.getLimit());
        configuration.set("broke", farmerData.getBlocksBroken());
        configuration.set("saved", System.currentTimeMillis());
        exportToConfig(configuration, farmerData);
        configuration.save(route);
    }

    public static FarmerData readDataInjection(Player player) throws Exception {
        HyperSettings settings = HyperVariables.get(HyperSettings.class);
        File route = new File(HyperFarming.inst().getDataFolder(), "data/" + player.getUniqueId().toString().concat(".yml"));
        if(!route.exists())
            return new FarmerData(settings.getDefaultLimit());
        FileConfiguration config = YamlConfiguration.loadConfiguration(route);
        int limit = config.getInt("limit", settings.getDefaultLimit());
        long broken = config.getLong("broke", 0L);
        return importToData(config.getConfigurationSection("data"), limit, broken);
    }

}

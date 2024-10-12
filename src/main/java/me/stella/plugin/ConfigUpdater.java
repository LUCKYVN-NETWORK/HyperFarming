package me.stella.plugin;

import me.stella.HyperFarming;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ConfigUpdater {

    public static void update(String path) {
        HyperFarming main = HyperFarming.inst();
        InputStream configStream = main.getResource(path);
        if(configStream == null)
            throw new RuntimeException("Unable to read config file from JAR resources!");
        File dataFolder = main.getDataFolder();
        File tempCfgFile = new File(dataFolder, "tmp_cfg.yml");
        try {
            OutputStream cfgOutStream = new FileOutputStream(tempCfgFile);
            byte[] buffer = new byte[1024]; int len;
            while((len = configStream.read(buffer)) > 0)
                cfgOutStream.write(buffer, 0, len);
            cfgOutStream.close();
            configStream.close();
            File mainConfigFile = new File(dataFolder, path);
            FileConfiguration configMain = YamlConfiguration.loadConfiguration(mainConfigFile);
            FileConfiguration configTmp = YamlConfiguration.loadConfiguration(tempCfgFile.getAbsoluteFile());
            configTmp.getKeys(true).forEach(key -> {
                if(!configMain.contains(key))
                    configMain.set(key, configTmp.get(key));
            });
            configMain.save(mainConfigFile); tempCfgFile.delete();
        } catch(Exception err) { err.printStackTrace(); }
    }

}

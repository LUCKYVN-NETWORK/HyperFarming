package me.stella.plugin;

import me.stella.HyperFarming;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class IOWorker {

    public static void writeResourceToFolder(String resource, String destination) {
        HyperFarming main = HyperFarming.inst();
        InputStream configStream = main.getResource(resource);
        if(configStream == null)
            throw new RuntimeException("Unable to read resource from JAR! Please contact the developer");
        File dataFolder = main.getDataFolder();
        if(!dataFolder.exists())
            dataFolder.mkdirs();
        File destFile = new File(dataFolder, destination);
        if(destFile.exists())
            return;
        try {
            OutputStream out = new FileOutputStream(destFile);
            byte[] buffer = new byte[4096]; int data;
            while((data = configStream.read(buffer)) > 0)
                out.write(buffer, 0, data);
            out.flush(); out.close(); configStream.close();
        } catch(Exception err) { err.printStackTrace(); }

    }

    public static void updateConfig(String resource, String output) {
        HyperFarming main = HyperFarming.inst();
        InputStream configStream = main.getResource(resource);
        if(configStream == null)
            throw new RuntimeException("Unable to read config file from JAR resources!");
        File dataFolder = main.getDataFolder();
        File tempCfgFile = new File(dataFolder, "tmp_cfg.yml");
        try {
            OutputStream cfgOutStream = new FileOutputStream(tempCfgFile);
            byte[] buffer = new byte[4096]; int len;
            while((len = configStream.read(buffer)) > 0)
                cfgOutStream.write(buffer, 0, len);
            cfgOutStream.close();
            configStream.close();
            File mainConfigFile = new File(dataFolder, output);
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

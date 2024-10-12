package me.stella;

import me.stella.gui.HyperGUIBuilder;
import me.stella.gui.HyperGUIHandle;
import me.stella.nms.MultiVerItems;
import me.stella.nms.NMSProtocol;
import me.stella.objects.PlayerWrapper;
import me.stella.plugin.IOWorker;
import me.stella.plugin.HyperSettings;
import me.stella.plugin.commands.FarmCommand;
import me.stella.plugin.commands.FarmTabCompleter;
import me.stella.plugin.listeners.PlayerFarmListener;
import me.stella.plugin.listeners.PlayerInOutListener;
import me.stella.support.EconomyFramework;
import me.stella.support.SupportedPlugin;
import me.stella.support.economy.TokenFramework;
import me.stella.support.economy.VaultFramework;
import me.stella.support.impl.SuperiorSkyblockImpl;
import me.stella.support.impl.VanillaImpl;
import me.stella.utility.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class HyperFarming extends JavaPlugin {

    public static final Logger console = Logger.getLogger("Minecraft");
    private static HyperFarming main;

    public static HyperFarming inst() {
        return main;
    }

    @Override
    public void onEnable() {
        main = this;
        String craftBukkitVer = getServer().getClass().getName().split("\\.")[3];
        MultiVerItems.LEGACY = craftBukkitVer.equals("v1_12_R1");
        this.setupConfiguration();
        HyperSettings settings = new HyperSettings(new File(getDataFolder(), "config.yml"));
        HyperGUIBuilder guiBuilder = new HyperGUIBuilder(new File(getDataFolder(), "menu_1_12.yml"));
        File dataDirectory = new File(getDataFolder(), "data");
        if(!dataDirectory.exists())
            dataDirectory.mkdirs();
        HyperVariables.inject(HyperSettings.class, settings);
        HyperVariables.inject(HyperGUIBuilder.class, guiBuilder);
        HyperVariables.inject(HyperGUIHandle.class, new HyperGUIHandle(guiBuilder));
        String nmsPackage = "me.stella.nms.versions.";
        String developers = getDescription().getAuthors().toString();
        String pluginVer = getDescription().getVersion();
        ClassLoader hyperFarmLoader = this.getClass().getClassLoader();
        ClassLoader serverLoader = getServer().getClass().getClassLoader();
        try {
            Class<?> pluginClassLoader = Class.forName("org.bukkit.plugin.java.PluginClassLoader", true, serverLoader);
            String nmsImplClass = nmsPackage + craftBukkitVer;
            console.log(Level.INFO, "[HyperBoot] Loading NMS support class " + nmsImplClass);
            Method findClassMethod = pluginClassLoader.getDeclaredMethod("findClass", String.class);
            findClassMethod.trySetAccessible();
            Class<?> nmsImpl = (Class<?>) findClassMethod.invoke(hyperFarmLoader, nmsImplClass);
            NMSProtocol protocol = (NMSProtocol) nmsImpl.getConstructors()[0].newInstance();
            HyperVariables.inject(NMSProtocol.class, protocol);
            //protocol.setGUITag(new ItemStack(Material.STONE), "button", HyperGUIBuilder.ButtonMode.DECORATION.name());
            settings.getMessageBlock("startup").stream()
                    .map(string -> string.replace("{version}", pluginVer)
                            .replace("{dev}", developers)
                            .replace("{server}", craftBukkitVer))
                    .forEach(msg -> console.log(Level.INFO, BukkitUtils.color(msg)));
            System.gc();
            System.runFinalization();
        } catch(Exception err) { err.printStackTrace(); }
        if(HyperVariables.get(NMSProtocol.class) == null)
            throw new RuntimeException("Unable to setup NMS Protocol!");
        String economyMode = settings.getEconomyMode();
        EconomyFramework framework;
        switch(economyMode) {
            case "Vault":
                framework = new VaultFramework();
                console.log(Level.INFO, BukkitUtils.color(settings.getMessage("hooked-vault")));
                break;
            case "TokenManager":
                framework = new TokenFramework();
                console.log(Level.INFO, BukkitUtils.color(settings.getMessage("hooked-token-manager")));
                break;
            default:
                throw new RuntimeException("Economy mode is invalid! Please check and reboot!");
        }
        HyperVariables.inject(EconomyFramework.class, framework);
        this.setupVariableMaps();
        this.setupSupport();
        this.setupListeners();
        this.setupCommands();
        (new BukkitRunnable() {
            @Override
            public void run() {
                getServer().getOnlinePlayers().forEach(PlayerInOutListener::handshakeIn);
            }
        }).runTask(this);
        (new BukkitRunnable() {
            @Override
            public void run() {
                System.gc();
                System.runFinalization();
            }
        }).runTaskTimerAsynchronously(this, 200L, 600L);
    }

    private void setupConfiguration() {
        String configResource = MultiVerItems.LEGACY ? "config_1_12.yml" : "config_1_13.yml";
        String menuResource = MultiVerItems.LEGACY ? "menu_1_12_yml" : "menu_1_13.yml";
        IOWorker.writeResourceToFolder(configResource, "config.yml");
        IOWorker.updateConfig(configResource, "config.yml");
        IOWorker.writeResourceToFolder(menuResource, "menu_1_12.yml");
        IOWorker.updateConfig(menuResource, "menu_1_12.yml");
    }

    private void setupVariableMaps() {
        BukkitUtils.initToolList();
        BukkitUtils.initCropMap();
        BukkitUtils.initPricingTable();
    }

    private void setupCommands() {
        PluginCommand farmCommand = getCommand("farm");
        farmCommand.setExecutor(new FarmCommand());
        farmCommand.setTabCompleter(new FarmTabCompleter());
    }

    private void setupListeners() {
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new PlayerInOutListener(), this);
        manager.registerEvents(new PlayerFarmListener(), this);
        manager.registerEvents(HyperVariables.get(HyperGUIHandle.class), this);
    }

    private void setupSupport() {
        HyperSettings config = HyperVariables.get(HyperSettings.class);
        Map<String, Class<?>> frameworkMap = new HashMap<>();
        frameworkMap.put("SuperiorSkyblock2", SuperiorSkyblockImpl.class);
        boolean hooked = false;
        for(String framework: frameworkMap.keySet()) {
            if(BukkitUtils.isPluginEnabled(framework)) {
                hooked = true;
                try {
                    Class<?> frameworkClass = frameworkMap.get(framework);
                    HyperVariables.inject(SupportedPlugin.class, frameworkClass.getConstructors()[0].newInstance());
                    console.log(Level.INFO, BukkitUtils.color(config.getMessage("hooked-support")
                            .replace("{plugin}", framework)));
                } catch(Exception err) { err.printStackTrace(); }
                break;
            }
        }
        if(!hooked)
            HyperVariables.inject(SupportedPlugin.class, new VanillaImpl());
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            PlayerInOutListener.handshakeOut(PlayerWrapper.buildWrapper(player));
        });
        getServer().getScheduler().cancelTasks(this);
    }
}

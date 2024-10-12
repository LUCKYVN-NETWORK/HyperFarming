package me.stella.plugin.commands;

import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface FarmSubCommand {

    default boolean isPlayerOnly() {
        return true;
    }

    default int getArguments() {
        return 0;
    }

    default String getPermission() {
        return "";
    }

    String getUsage();

    boolean perform(CommandSender sender, String[] args);

    default Map<Integer, List<String>> getSuggestions() {
        return new HashMap<>();
    }

    default String[] inject(String param, String[] base) {
        String[] out = new String[base.length + 1];
        out[0] = param;
        System.arraycopy(base, 0, out, 1, base.length);
        return out;
    }

}

package me.stella.plugin.commands;

import me.stella.HyperFarming;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class FarmTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        Set<String> basicSubCommands = FarmCommand.subCommandMap.keySet();
        if(args.length == 1)
            return basicSubCommands.stream()
                    .filter(cmd -> cmd.startsWith(args[0]) && commandSender.hasPermission(FarmCommand.subCommandMap.get(cmd).getPermission()))
                    .collect(Collectors.toList());
        else {
            FarmSubCommand subCommand = FarmCommand.subCommandMap.get(args[0]);
            if(subCommand == null)
                return Collections.emptyList();
            List<String> suggestions = new ArrayList<>(subCommand.getSuggestions().getOrDefault(args.length, new ArrayList<>()));
            if(suggestions.isEmpty())
                Bukkit.getOnlinePlayers().forEach(p -> suggestions.add(p.getName()));
            return suggestions.stream().filter(cmd -> cmd.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                    .collect(Collectors.toList());
        }
    }
}

package com.zerek.feathermarket.commands;


import com.zerek.feathermarket.FeatherMarket;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class MarketTabCompleter implements TabCompleter {

    public MarketTabCompleter() {
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        List<String> options = new ArrayList<String>();

        if (args.length == 1){
            options.add("help");
            options.add("player");
            options.add("post");
            options.add("remove");
            options.add("search");
            options.add("showcase");
        }
        else if (args.length == 2){
            switch (args[0]){
                case "post":
                case "remove":
                    options.add("buying");
                    options.add("selling");
                    break;
                case "player":
                    return null;
            }
        }
        return options;
    }
}

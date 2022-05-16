package com.zerek.feathermarket.commands;

import com.zerek.feathermarket.FeatherMarket;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static net.kyori.adventure.text.Component.text;

public class MarketCommand implements CommandExecutor {

    private final FeatherMarket plugin;
    private final Map<String, String> messages = new HashMap<String, String>();
    private final MiniMessage mm =  MiniMessage.miniMessage();

    public MarketCommand(FeatherMarket plugin) {
        this.plugin = plugin;
        this.init();
    }

    private void init() {
        ConfigurationSection messagesYml = plugin.getConfig().getConfigurationSection("messages");
        messagesYml.getKeys(false).forEach(key -> messages.put(key,messagesYml.getString(key)));
    }

    private boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player && sender.hasPermission("feather.market")){

            if (args.length == 0 ||  (args.length == 1 && args[0].chars().allMatch(Character::isDigit))){
                List<OfflinePlayer> onlinePlayers = new ArrayList<>(plugin.getServer().getOnlinePlayers());
                onlinePlayers.removeIf(player -> isVanished((Player) player));
                List<Component> ads = plugin.getMarketManager().getAds((Player) sender,onlinePlayers);
                if (!plugin.getPaginateUtility().displayPage(args, (Player) sender,ads)) sender.sendMessage(mm.deserialize(messages.get("online-no-ads")));
                return true;
            }
            else{
                switch (args[0]){
                    //posting an ad
                    case "post":
                        if (args.length == 1) return false;
                        switch (args[1]){
                            case "selling":
                            case "buying":
                                final StringBuilder adMessage = new StringBuilder();
                                for (int i = 2; i < args.length; i++) {
                                    if (i != 2) adMessage.append(" ");
                                    adMessage.append(args[i]);
                                }
                                if (adMessage.length() == 0){
                                    return false;
                                }
                                if (plugin.getMarketManager().postAd((OfflinePlayer) sender, args[1].toLowerCase(),adMessage.toString())){
                                    sender.sendMessage(mm.deserialize(messages.get("ad-posted")));
                                    List<Component> playerAds = plugin.getMarketManager().getAds((Player) sender, Collections.singletonList((OfflinePlayer) sender));
                                    plugin.getPaginateUtility().displayPage(args,(Player) sender, playerAds);
                                    return true;
                                }
                                else sender.sendMessage(text("posting failed"));
                            default:
                                return false;
                        }
                    case "remove":
                        if (args.length == 1) return false;
                        switch (args[1]) {
                            case "selling":
                            case "buying":
                                if (plugin.getMarketManager().removeAd((OfflinePlayer) sender, args[1])) {
                                    sender.sendMessage(mm.deserialize(messages.get("ad-removed")));
                                    return true;
                                }
                                else return false;
                            default: return false;
                        }
                    case "search":
                        if (args.length == 1) return false;
                        int termEnd = args.length;
                        if (args[args.length-1].chars().allMatch(Character::isDigit)) termEnd = termEnd-1;
                        final StringBuilder search = new StringBuilder();
                        for (int i = 1; i < termEnd; i++) {
                            if (i != 1) search.append(" ");
                            search.append(args[i]);
                        }

                        List<OfflinePlayer> players = plugin.getMarketManager().searchAds(search.toString());
                        List<Component> resultAds = plugin.getMarketManager().getAds((Player) sender,players);
                        if (!plugin.getPaginateUtility().displayPage(args, (Player) sender,resultAds)) sender.sendMessage(mm.deserialize(messages.get("online-no-ads")));
                        return true;

                    case "player":
                        if (args.length == 1) return false;
                        if (plugin.getMarketManager().isMarketer(plugin.getServer().getOfflinePlayer(args[1]))) {
                            List<OfflinePlayer> player = Collections.singletonList(plugin.getServer().getOfflinePlayer(args[1]));
                            List<Component> playerAds = plugin.getMarketManager().getAds((Player) sender, player);
                            if (!plugin.getPaginateUtility().displayPage(args,(Player) sender, playerAds)) sender.sendMessage(mm.deserialize(messages.get("player-no-ads")));
                            return true;
                        }
                        sender.sendMessage(mm.deserialize(messages.get("player-no-ads")));
                        return true;
                    case "help":
                        sender.sendMessage(mm.deserialize(messages.get("help")));
                        return true;

                    default: return false;
                }
            }
        }
        else{
            sender.sendMessage(mm.deserialize(messages.get("player-command-only")));
        }
        return false;
    }
}

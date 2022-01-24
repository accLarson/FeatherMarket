package com.zerek.feathermarket.managers;

import com.zerek.feathermarket.FeatherMarket;
import com.zerek.feathermarket.data.Marketer;

import com.zerek.feathermarket.utilities.ChatUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.transformation.TransformationType;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;


import java.util.*;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;

public class MarketManager {



    private final FeatherMarket plugin;
    private final Map<String, Object> f = new HashMap<String, Object>();
    LuckPerms luckPerms = LuckPermsProvider.get();
    MiniMessage mm =  MiniMessage.builder()
            .removeDefaultTransformations()
            .transformation(TransformationType.COLOR)
            .transformation(TransformationType.DECORATION)
            .transformation(TransformationType.RESET)
            .build();
    ChatUtility cu;

    public MarketManager(FeatherMarket plugin) {
        this.plugin = plugin;
        this.init();
    }

    private void init() {
        ConfigurationSection formatYml = plugin.getConfig().getConfigurationSection("line-formats");
        formatYml.getKeys(false).forEach(key -> f.put(key,formatYml.get(key)));
        cu = plugin.getChatUtility();
    }

    public boolean isMarketer (OfflinePlayer offlinePlayer){
        return Marketer.exists(offlinePlayer.getUniqueId().toString());
    }

    private boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }

    public Marketer getMarketer(OfflinePlayer offlinePlayer){
        return Marketer.findById(offlinePlayer.getUniqueId().toString());
    }

    private boolean hasAd (OfflinePlayer offlinePlayer, String adType){
        if (isMarketer(offlinePlayer))    return !getMarketer(offlinePlayer).getString(adType).isEmpty();
        else return false;
    }

    public boolean removeAd(OfflinePlayer offlinePlayer, String adType) {
        if (hasAd(offlinePlayer, adType)) {
            getMarketer(offlinePlayer).set(adType,"",adType+"_updated_at",null).saveIt();
            if (!hasAd(offlinePlayer,"selling") && !hasAd(offlinePlayer, "buying"))    getMarketer(offlinePlayer).delete();
            return true;
        }
        return false;
    }

    public List<String> pruneMarket(){
        int expireDays = plugin.getConfig().getInt("prune-inactive-marketer-days");
        List<String> expiredMarketers = new ArrayList<>();
        Marketer.findAll().forEach(marketer -> {
            OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(UUID.fromString(marketer.getString("mojang_uuid")));
            int seenDiff = (int) (System.currentTimeMillis() - offlinePlayer.getLastSeen()) / 86400000;
            if (seenDiff >= expireDays) {
                expiredMarketers.add(offlinePlayer.getName());
                marketer.delete();
            }
        });
        return expiredMarketers;
    }

    public boolean postAd (OfflinePlayer offlinePlayer, String adType, String message){
        if (message.length() > 255) return false;
        Marketer marketer = new Marketer().set("mojang_uuid",offlinePlayer.getUniqueId().toString(),adType,message,adType+"_updated_at",System.currentTimeMillis());
        if (isMarketer(offlinePlayer)) return marketer.saveIt();
        else return marketer.insert();
    }


    public List<OfflinePlayer> searchAds(String searchTerm){
        List<OfflinePlayer> players = new ArrayList<>();
        Marketer.findAll().forEach(marketer -> {
            if (mm.stripTokens(marketer.getString("selling").toLowerCase()).contains(searchTerm.toLowerCase()) || mm.stripTokens(marketer.getString("buying").toLowerCase()).contains(searchTerm.toLowerCase())){
                players.add(plugin.getServer().getOfflinePlayer(UUID.fromString(marketer.getString("mojang_uuid"))));
            }
        });
        return players;
    }

    public int countAds (List<OfflinePlayer> players){
        return (int) players.stream().filter(this::isMarketer).count();
    }

    //Format Ads as a list of Strings
    public List<Component> getAds(Player viewer, List<OfflinePlayer> players) {

        List<Component> formattedLines = new ArrayList<>();

        List<OfflinePlayer> sortedPlayers = players.stream().sorted(Comparator.comparingInt(player -> player.getStatistic(Statistic.PLAY_ONE_MINUTE))).collect(Collectors.toList());
        sortedPlayers = sortedPlayers.stream().sorted(Comparator.comparing(OfflinePlayer::isOnline)).collect((Collectors.toList()));
        Collections.reverse(sortedPlayers);

        sortedPlayers.forEach(player -> {
            if (isMarketer(player)) {

                //------------------ create blank component list
                TextComponent prefix = text("");
                TextComponent nameHover = text("");
                TextComponent name = text("");
                TextComponent buyingHover = text("");
                TextComponent buying = text("");
                TextComponent buyingAge = text("");
                TextComponent sellingHover = text("");
                TextComponent selling = text("");
                TextComponent sellingAge = text("");


                //------------------ create the formatted line (parent component)
                TextComponent formattedLine = (TextComponent) mm.parse(f.get("line-prefix").toString());

                //------------------ set player prefix and name
                if (player.isOnline() && !isVanished((Player) player)) {
                    String luckPermsPrefix = luckPerms.getPlayerAdapter(Player.class).getUser((Player) player).getCachedData().getMetaData().getPrefix();
                    if (luckPermsPrefix != null) prefix = LegacyComponentSerializer.legacyAmpersand().deserialize(luckPermsPrefix);
                    name = (TextComponent) mm.parse(f.get("name-prefix") + player.getName() + f.get("name-suffix"));
                } else name = (TextComponent) mm.parse(f.get("offline-name-prefix") + player.getName() + f.get("offline-name-suffix"));

                String seenDiff = String.valueOf((System.currentTimeMillis() - player.getLastSeen()) / 86400000);
                if (seenDiff.equals("0")) nameHover = (TextComponent) mm.parse(f.get("hover-time-prefix") + "Today");
                else nameHover = (TextComponent) mm.parse(f.get("hover-time-prefix") + seenDiff + f.get("hover-time-suffix"));
                if (player.isOnline() && isVanished((Player) player)) nameHover = (TextComponent) mm.parse(f.get("hover-time-prefix") + "Today");
                name = name.hoverEvent(HoverEvent.showText(nameHover));

                //------------------ set buying label
                if (hasAd(player, "buying")) {
                    Marketer marketer = getMarketer(player);
                    String hoverName = f.get("buying-hover-name-prefix") + player.getName() + f.get("buying-hover-name-suffix");
                    String hoverDate = f.get("buying-hover-date-prefix") + marketer.getDate("buying_updated_at").toString() + f.get("buying-hover-date-suffix");
                    buyingHover = (TextComponent) mm.parse(hoverName + hoverDate + "\n<reset>" + marketer.getString("buying"));
                    buying = (TextComponent) mm.parse(f.get("label-prefix") + "Buying" + f.get("label-suffix")).hoverEvent(HoverEvent.showText(buyingHover));
                    if (player == viewer) buying = buying.clickEvent(ClickEvent.suggestCommand("/market post buying " + marketer.getString("buying")));
                    else if (player.isOnline() && !isVanished((Player) player)) buying = buying.clickEvent(ClickEvent.suggestCommand("/msg " + player.getName() + " "));
                    String adPostDiff = String.valueOf((System.currentTimeMillis() - marketer.getLong("buying_updated_at")) / 86400000);
                    if (adPostDiff.equals("0")) buyingAge = (TextComponent) mm.parse(f.get("label-time-prefix") + "Today");
                    else buyingAge = (TextComponent) mm.parse(f.get("label-time-prefix") + adPostDiff + f.get("label-time-suffix"));
                }

                //------------------ set selling label
                if (hasAd(player, "selling")) {
                    Marketer marketer = getMarketer(player);
                    String hoverName = f.get("selling-hover-name-prefix") + player.getName() + f.get("selling-hover-name-suffix");
                    String hoverDate = f.get("selling-hover-date-prefix") + marketer.getDate("selling_updated_at").toString() + f.get("selling-hover-date-suffix");
                    sellingHover = (TextComponent) mm.parse(hoverName + hoverDate + "\n<reset>" + marketer.getString("selling"));
                    selling = (TextComponent) mm.parse(f.get("label-prefix") + "Selling" + f.get("label-suffix")).hoverEvent(HoverEvent.showText(sellingHover));
                    if (player == viewer) selling = selling.clickEvent(ClickEvent.suggestCommand("/market post selling " + marketer.getString("selling")));
                    else if (player.isOnline() && !isVanished((Player) player)) selling = selling.clickEvent(ClickEvent.suggestCommand("/msg " + player.getName() + " "));
                    String adPostDiff = String.valueOf((System.currentTimeMillis() - marketer.getLong("selling_updated_at")) / 86400000);
                    if (adPostDiff.equals("0")) sellingAge = (TextComponent) mm.parse(f.get("label-time-prefix") + "Today");
                    else sellingAge = (TextComponent) mm.parse(f.get("label-time-prefix") + adPostDiff + f.get("label-time-suffix"));
                }

                //------------------ format line
                formattedLine = formattedLine.append(cu.addSpacing(prefix, (Integer) f.get("prefix-pixels")));               //prefix
                formattedLine = formattedLine.append(cu.addSpacing(name, (Integer) f.get("name-pixels")));                   //name

                formattedLine = formattedLine.append(cu.addSpacing(buying, (Integer) f.get("label-pixels")));                //buying
                formattedLine = formattedLine.append(cu.addSpacing(buyingAge, (Integer) f.get("label-time-pixels")));        //buyingAge

                formattedLine = formattedLine.append(cu.addSpacing(selling, (Integer) f.get("label-pixels")));               //selling
                formattedLine = formattedLine.append(cu.addSpacing(sellingAge, (Integer) f.get("label-time-pixels")));       //sellingAge

                formattedLines.add(formattedLine);
            }
        });

        return formattedLines;
    }
}
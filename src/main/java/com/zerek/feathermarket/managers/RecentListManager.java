package com.zerek.feathermarket.managers;

import com.zerek.feathermarket.FeatherMarket;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RecentListManager {
    private final FeatherMarket plugin;
    private final List<Player> recentSellingList = new ArrayList<>();
    private final List<Player> recentBuyingList = new ArrayList<>();

    public RecentListManager(FeatherMarket plugin) {
        this.plugin = plugin;
    }

    public boolean isListed(Player player, String adType){
        if (adType.equals("selling")) return this.recentSellingList.contains(player);
        else return this.recentBuyingList.contains(player);
    }

    public void add(Player player, String adType){
        if (adType.equals("selling")) {
            this.recentSellingList.add(player);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> recentSellingList.remove(player), 72000L);
        } else {
            this.recentBuyingList.add(player);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> recentBuyingList.remove(player), 72000L);
        }
    }


}



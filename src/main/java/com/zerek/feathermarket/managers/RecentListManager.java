package com.zerek.feathermarket.managers;

import com.zerek.feathermarket.FeatherMarket;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RecentListManager {
    private final FeatherMarket plugin;
    private final List<Player> recentSellingList = new ArrayList<>();
    private final List<Player> recentBuyingList = new ArrayList<>();
    private final List<Player> recentShowcaseList = new ArrayList<>();

    public RecentListManager(FeatherMarket plugin) {
        this.plugin = plugin;
    }

    public boolean isListed(Player player, String list){
        if (list.equals("selling")) return this.recentSellingList.contains(player);
        if (list.equals("buying")) return this.recentBuyingList.contains(player);
        if (list.equals("showcase")) return this.recentShowcaseList.contains(player);
        return false;
    }

    public void add(Player player, String list){
        if (list.equals("selling")) {
            this.recentSellingList.add(player);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> recentSellingList.remove(player), 72000L);
        }
        if (list.equals("buying")) {
            this.recentBuyingList.add(player);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> recentBuyingList.remove(player), 72000L);
        }
        if (list.equals("showcase")) {
            this.recentShowcaseList.add(player);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> recentShowcaseList.remove(player), 1200L);
        }
    }


}



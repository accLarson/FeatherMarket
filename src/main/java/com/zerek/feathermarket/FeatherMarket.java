package com.zerek.feathermarket;

import com.zerek.feathermarket.commands.MarketCommand;
import com.zerek.feathermarket.commands.MarketTabCompleter;
import com.zerek.feathermarket.managers.DatabaseManager;
import com.zerek.feathermarket.managers.MarketManager;
import com.zerek.feathermarket.managers.RecentListManager;
import com.zerek.feathermarket.utilities.ChatUtility;
import com.zerek.feathermarket.utilities.ItemLabelUtility;
import com.zerek.feathermarket.utilities.PaginateUtility;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class FeatherMarket extends JavaPlugin {

    private static Logger logger;
    private DatabaseManager databaseManager;
    private ChatUtility chatUtility;
    private MarketManager marketManager;
    private PaginateUtility paginateUtility;
    private RecentListManager recentListManager;
    private ItemLabelUtility itemLabelUtility;


    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        this.logger = Logger.getLogger("Minecraft");
        this.databaseManager = new DatabaseManager(this);
        this.chatUtility = new ChatUtility(this);
        this.marketManager = new MarketManager(this);
        this.paginateUtility = new PaginateUtility(this);
        this.recentListManager = new RecentListManager(this);
        this.itemLabelUtility = new ItemLabelUtility(this);

        this.getCommand("market").setExecutor(new MarketCommand(this));
        this.getCommand("market").setTabCompleter(new MarketTabCompleter());

        this.getLogger().info("Pruned Marketers: " + String.join(" ", marketManager.pruneMarket()));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public ChatUtility getChatUtility() {
        return chatUtility;
    }
    public MarketManager getMarketManager() {
        return marketManager;
    }
    public RecentListManager getRecentListManager() {
        return recentListManager;
    }
    public Logger getLog(){
        return logger;
    }
    public PaginateUtility getPaginateUtility() {
        return paginateUtility;
    }
    public ItemLabelUtility getItemLabelUtility() {
        return itemLabelUtility;
    }
}

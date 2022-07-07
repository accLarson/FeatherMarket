package com.zerek.feathermarket;

import com.zerek.feathermarket.commands.MarketCommand;
import com.zerek.feathermarket.commands.MarketTabCompleter;
import com.zerek.feathermarket.managers.DatabaseManager;
import com.zerek.feathermarket.managers.MarketManager;
import com.zerek.feathermarket.managers.RecentListManager;
import com.zerek.feathermarket.utilities.ChatUtility;
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


    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        logger = Logger.getLogger("Minecraft");
        databaseManager = new DatabaseManager(this);
        chatUtility = new ChatUtility(this);
        marketManager = new MarketManager(this);
        paginateUtility = new PaginateUtility(this);
        recentListManager = new RecentListManager(this);

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
}

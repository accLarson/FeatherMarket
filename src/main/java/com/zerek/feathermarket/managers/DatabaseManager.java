package com.zerek.feathermarket.managers;

import com.zerek.feathermarket.FeatherMarket;
import org.javalite.activejdbc.Base;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {

    private static Connection connection;
    private final FeatherMarket plugin;
    private File file;

    public DatabaseManager(FeatherMarket plugin) {
        this.plugin = plugin;
        this.initConnection();
        this.initTables();
    }

    public Connection getConnection() {
        try {
            if(connection.isClosed()) {
                this.initConnection();
            }
        } catch (SQLException e) {
            plugin.getLog().severe("[FeatherMarket] Unable to receive connection.");
        }
        return connection;
    }

    public void close() {
        if (connection != null) {
            try {
                Base.close();
                connection.close();
            } catch (SQLException e) {
                plugin.getLog().severe("[FeatherMarket] Unable to close DatabaseManager connection.");
            }
        }
    }

    private void initConnection() {
        File folder = this.plugin.getDataFolder();
        if(!folder.exists()) {
            folder.mkdir();
        }
        this.file = new File(folder.getAbsolutePath() + File.separator +  "FeatherMarket.db");
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.file.getAbsolutePath());
            Base.attach(this.connection);
        } catch (SQLException e) {
            plugin.getLog().severe("[FeatherMarket] Unable to initialize DatabaseManager connection.");
        }
    }

    private boolean existsTable(String table) {
        try {
            if(!connection.isClosed()) {
                ResultSet rs = connection.getMetaData().getTables(null, null, table, null);
                return rs.next();
            } else {
                return false;
            }
        } catch (SQLException e) {
            plugin.getLog().severe("[FeatherMarket] Unable to query table metadata.");
            return false;
        }
    }

    private void initTables() {
        if(!this.existsTable("MARKETERS")) {
            plugin.getLog().info("[FeatherMarket] Creating MARKETERS table.");
            String query = "CREATE TABLE IF NOT EXISTS `MARKETERS` ("
                         + " `mojang_uuid`               VARCHAR(255) PRIMARY KEY NOT NULL, "
                         + " `created_at`                DATETIME, "
                         + " `buying`                    VARCHAR(255) DEFAULT '', "
                         + " `selling`                   VARCHAR(255) DEFAULT '', "
                         + " `buying_updated_at`         DATETIME, "
                         + " `selling_updated_at`        DATETIME );";
            try {
                if(!connection.isClosed()) {
                    connection.createStatement().execute(query);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLog().severe("[FeatherMarket] Unable to create MARKETERS table.");
            }
        }
    }
}
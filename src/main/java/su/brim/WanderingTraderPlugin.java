package su.brim;

import su.brim.command.WanderingTraderCommand;
import su.brim.config.TradeConfig;
import su.brim.listener.WanderingTraderListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class WanderingTraderPlugin extends JavaPlugin {

    private static WanderingTraderPlugin instance;
    private TradeConfig tradeConfig;

    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Load trade configuration
        tradeConfig = new TradeConfig(this);
        tradeConfig.loadTrades();
        
        // Register event listener
        getServer().getPluginManager().registerEvents(new WanderingTraderListener(this), this);
        
        // Register command
        PluginCommand command = getCommand("wanderingtrader");
        if (command != null) {
            WanderingTraderCommand cmdExecutor = new WanderingTraderCommand(this);
            command.setExecutor(cmdExecutor);
            command.setTabCompleter(cmdExecutor);
        }
        
        getLogger().info("WanderingTrader enabled! Wandering Traders will now sell Nether items for diamonds.");
        getLogger().info("Loaded " + tradeConfig.getAllTrades().size() + " custom trades.");
    }

    @Override
    public void onDisable() {
        getLogger().info("WanderingTrader disabled.");
    }

    public static WanderingTraderPlugin getInstance() {
        return instance;
    }

    public TradeConfig getTradeConfig() {
        return tradeConfig;
    }

    public void reloadTradeConfig() {
        reloadConfig();
        tradeConfig.loadTrades();
        getLogger().info("Reloaded " + tradeConfig.getAllTrades().size() + " custom trades.");
    }
}

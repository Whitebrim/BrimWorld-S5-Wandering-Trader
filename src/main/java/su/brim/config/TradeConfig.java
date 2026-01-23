package su.brim.config;

import su.brim.WanderingTraderPlugin;
import su.brim.model.Trade;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

/**
 * Handles loading and managing custom trades from config.yml
 * Uses thread-safe collections for Folia compatibility.
 */
public class TradeConfig {

    private final WanderingTraderPlugin plugin;
    private final CopyOnWriteArrayList<Trade> trades;
    private final Random random;
    
    private int minTrades;
    private int maxTrades;
    private boolean replaceAllTrades;

    public TradeConfig(WanderingTraderPlugin plugin) {
        this.plugin = plugin;
        this.trades = new CopyOnWriteArrayList<>();
        this.random = new Random();
    }

    /**
     * Loads all trades from the config file.
     * Thread-safe for Folia's parallel region execution.
     */
    public void loadTrades() {
        trades.clear();
        
        // Load general settings
        minTrades = plugin.getConfig().getInt("settings.min-trades", 5);
        maxTrades = plugin.getConfig().getInt("settings.max-trades", 8);
        replaceAllTrades = plugin.getConfig().getBoolean("settings.replace-all-trades", true);
        
        ConfigurationSection tradesSection = plugin.getConfig().getConfigurationSection("trades");
        if (tradesSection == null) {
            plugin.getLogger().warning("No trades section found in config.yml!");
            return;
        }

        for (String tradeId : tradesSection.getKeys(false)) {
            ConfigurationSection tradeSection = tradesSection.getConfigurationSection(tradeId);
            if (tradeSection == null) continue;

            try {
                Trade trade = loadTrade(tradeId, tradeSection);
                if (trade != null && trade.isEnabled()) {
                    trades.add(trade);
                    plugin.getLogger().fine("Loaded trade: " + trade);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load trade '" + tradeId + "': " + e.getMessage());
            }
        }
    }

    private Trade loadTrade(String id, ConfigurationSection section) {
        boolean enabled = section.getBoolean("enabled", true);
        
        // Parse result
        String resultMaterialStr = section.getString("result.material");
        if (resultMaterialStr == null) {
            plugin.getLogger().warning("Trade '" + id + "' missing result material!");
            return null;
        }
        
        Material resultMaterial = Material.matchMaterial(resultMaterialStr);
        if (resultMaterial == null) {
            plugin.getLogger().warning("Trade '" + id + "' has invalid result material: " + resultMaterialStr);
            return null;
        }
        
        int resultAmount = section.getInt("result.amount", 1);
        
        // Parse primary cost
        String costMaterialStr = section.getString("cost.material", "DIAMOND");
        Material costMaterial = Material.matchMaterial(costMaterialStr);
        if (costMaterial == null) {
            plugin.getLogger().warning("Trade '" + id + "' has invalid cost material: " + costMaterialStr);
            return null;
        }
        
        int costAmount = section.getInt("cost.amount", 1);
        
        // Parse secondary cost (optional)
        Material secondCostMaterial = null;
        int secondCostAmount = 0;
        
        if (section.contains("second-cost")) {
            String secondCostMaterialStr = section.getString("second-cost.material");
            if (secondCostMaterialStr != null) {
                secondCostMaterial = Material.matchMaterial(secondCostMaterialStr);
                if (secondCostMaterial != null) {
                    secondCostAmount = section.getInt("second-cost.amount", 1);
                }
            }
        }
        
        // Parse enchantments (for enchanted books)
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        ConfigurationSection enchantSection = section.getConfigurationSection("enchantments");
        if (enchantSection != null) {
            for (String enchantKey : enchantSection.getKeys(false)) {
                Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchantKey.toLowerCase()));
                if (enchantment != null) {
                    int level = enchantSection.getInt(enchantKey, 1);
                    enchantments.put(enchantment, level);
                } else {
                    plugin.getLogger().warning("Trade '" + id + "' has invalid enchantment: " + enchantKey);
                }
            }
        }
        
        int maxUses = section.getInt("max-uses", 3);
        int weight = section.getInt("weight", 10);
        
        return new Trade(id, resultMaterial, resultAmount,
                costMaterial, costAmount,
                secondCostMaterial, secondCostAmount,
                maxUses, weight, enabled, enchantments);
    }

    /**
     * Selects random trades based on configured min/max values.
     * Uses weighted random selection.
     * @return List of randomly selected trades
     */
    public List<Trade> selectRandomTrades() {
        if (trades.isEmpty()) {
            return Collections.emptyList();
        }

        int numTrades = minTrades + random.nextInt(Math.max(1, maxTrades - minTrades + 1));
        numTrades = Math.min(numTrades, trades.size());

        // Create weighted list
        List<Trade> weightedPool = new ArrayList<>();
        for (Trade trade : trades) {
            for (int i = 0; i < trade.getWeight(); i++) {
                weightedPool.add(trade);
            }
        }

        // Select random trades without duplicates
        List<Trade> selected = new ArrayList<>();
        List<Trade> availableTrades = new ArrayList<>(trades);
        
        for (int i = 0; i < numTrades && !availableTrades.isEmpty(); i++) {
            // Build weighted pool from available trades
            List<Trade> currentPool = new ArrayList<>();
            for (Trade trade : availableTrades) {
                for (int j = 0; j < trade.getWeight(); j++) {
                    currentPool.add(trade);
                }
            }
            
            if (currentPool.isEmpty()) break;
            
            Trade selectedTrade = currentPool.get(random.nextInt(currentPool.size()));
            selected.add(selectedTrade);
            availableTrades.remove(selectedTrade);
        }

        return selected;
    }

    public List<Trade> getAllTrades() {
        return new ArrayList<>(trades);
    }

    public int getMinTrades() {
        return minTrades;
    }

    public int getMaxTrades() {
        return maxTrades;
    }

    public boolean isReplaceAllTrades() {
        return replaceAllTrades;
    }
}

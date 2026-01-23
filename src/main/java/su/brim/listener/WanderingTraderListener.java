package su.brim.listener;

import su.brim.WanderingTraderPlugin;
import su.brim.model.Trade;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens for Wandering Trader spawns and replaces their trades.
 * Folia-compatible: uses EntityScheduler for thread-safe entity modification.
 */
public class WanderingTraderListener implements Listener {

    private final WanderingTraderPlugin plugin;
    
    // Track processed traders to avoid duplicate processing
    // Using ConcurrentHashMap for thread-safety in Folia's parallel regions
    private final Set<UUID> processedTraders;

    public WanderingTraderListener(WanderingTraderPlugin plugin) {
        this.plugin = plugin;
        this.processedTraders = ConcurrentHashMap.newKeySet();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() != EntityType.WANDERING_TRADER) {
            return;
        }

        WanderingTrader trader = (WanderingTrader) event.getEntity();
        UUID traderId = trader.getUniqueId();

        // Prevent duplicate processing
        if (!processedTraders.add(traderId)) {
            return;
        }

        // Use Folia's EntityScheduler to ensure we're on the correct thread
        // The entity scheduler runs the task on the region that owns this entity
        trader.getScheduler().run(plugin, scheduledTask -> {
            try {
                applyCustomTrades(trader);
            } finally {
                // Clean up after a delay to handle edge cases
                // Schedule cleanup on the entity's region
                trader.getScheduler().runDelayed(plugin, cleanupTask -> {
                    processedTraders.remove(traderId);
                }, null, 100L); // 5 seconds (100 ticks)
            }
        }, null);
    }

    /**
     * Applies custom trades to the wandering trader.
     * Must be called from the entity's owning region thread.
     */
    private void applyCustomTrades(WanderingTrader trader) {
        List<Trade> selectedTrades = plugin.getTradeConfig().selectRandomTrades();
        
        if (selectedTrades.isEmpty()) {
            plugin.getLogger().warning("No trades available to apply to Wandering Trader!");
            return;
        }

        List<MerchantRecipe> recipes = new ArrayList<>();

        // Keep original trades if configured to not replace all
        if (!plugin.getTradeConfig().isReplaceAllTrades()) {
            recipes.addAll(trader.getRecipes());
        }

        // Add custom trades
        for (Trade trade : selectedTrades) {
            recipes.add(trade.toMerchantRecipe());
        }

        // Apply the new recipe list
        trader.setRecipes(recipes);
        
        plugin.getLogger().fine("Applied " + selectedTrades.size() + 
                " custom trades to Wandering Trader at " + trader.getLocation());
    }

    /**
     * Cleanup method called when plugin disables.
     */
    public void cleanup() {
        processedTraders.clear();
    }
}

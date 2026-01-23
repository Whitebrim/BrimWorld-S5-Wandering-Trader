package su.brim.command;

import su.brim.WanderingTraderPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;

/**
 * Command handler for /wanderingtrader command.
 * Folia-compatible: no thread-unsafe operations.
 */
public class WanderingTraderCommand implements CommandExecutor, TabCompleter {

    private final WanderingTraderPlugin plugin;

    public WanderingTraderCommand(WanderingTraderPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("wanderingtrader.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadTradeConfig();
                sender.sendMessage("§aWanderingTrader configuration reloaded!");
                sender.sendMessage("§7Loaded " + plugin.getTradeConfig().getAllTrades().size() + " trades.");
            }
            case "list" -> {
                sender.sendMessage("§6=== WanderingTrader Trades ===");
                var trades = plugin.getTradeConfig().getAllTrades();
                if (trades.isEmpty()) {
                    sender.sendMessage("§cNo trades configured!");
                } else {
                    for (var trade : trades) {
                        sender.sendMessage(String.format("§7- §f%s§7: %dx %s for %d diamonds (weight: %d)",
                                trade.getId(),
                                trade.getResultAmount(),
                                formatMaterial(trade.getResultMaterial().name()),
                                trade.getCostAmount(),
                                trade.getWeight()));
                    }
                }
            }
            case "info" -> {
                sender.sendMessage("§6=== WanderingTrader Info ===");
                sender.sendMessage("§7Min trades per trader: §f" + plugin.getTradeConfig().getMinTrades());
                sender.sendMessage("§7Max trades per trader: §f" + plugin.getTradeConfig().getMaxTrades());
                sender.sendMessage("§7Replace all vanilla trades: §f" + plugin.getTradeConfig().isReplaceAllTrades());
                sender.sendMessage("§7Total configured trades: §f" + plugin.getTradeConfig().getAllTrades().size());
            }
            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== WanderingTrader Commands ===");
        sender.sendMessage("§e/wanderingtrader reload §7- Reload configuration");
        sender.sendMessage("§e/wanderingtrader list §7- List all configured trades");
        sender.sendMessage("§e/wanderingtrader info §7- Show plugin info");
    }

    private String formatMaterial(String material) {
        return material.toLowerCase().replace("_", " ");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("wanderingtrader.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return List.of("reload", "list", "info").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        return Collections.emptyList();
    }
}

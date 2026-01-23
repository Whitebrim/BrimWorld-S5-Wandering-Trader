package su.brim.model;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a custom trade for the Wandering Trader.
 */
public class Trade {

    private final String id;
    private final Material resultMaterial;
    private final int resultAmount;
    private final Material costMaterial;
    private final int costAmount;
    private final Material secondCostMaterial;
    private final int secondCostAmount;
    private final int maxUses;
    private final int weight;
    private final boolean enabled;
    private final Map<Enchantment, Integer> enchantments;

    public Trade(String id, Material resultMaterial, int resultAmount,
                 Material costMaterial, int costAmount,
                 Material secondCostMaterial, int secondCostAmount,
                 int maxUses, int weight, boolean enabled,
                 Map<Enchantment, Integer> enchantments) {
        this.id = id;
        this.resultMaterial = resultMaterial;
        this.resultAmount = resultAmount;
        this.costMaterial = costMaterial;
        this.costAmount = costAmount;
        this.secondCostMaterial = secondCostMaterial;
        this.secondCostAmount = secondCostAmount;
        this.maxUses = maxUses;
        this.weight = weight;
        this.enabled = enabled;
        this.enchantments = enchantments != null ? new HashMap<>(enchantments) : Collections.emptyMap();
    }

    public String getId() {
        return id;
    }

    public Material getResultMaterial() {
        return resultMaterial;
    }

    public int getResultAmount() {
        return resultAmount;
    }

    public Material getCostMaterial() {
        return costMaterial;
    }

    public int getCostAmount() {
        return costAmount;
    }

    public Material getSecondCostMaterial() {
        return secondCostMaterial;
    }

    public int getSecondCostAmount() {
        return secondCostAmount;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return Collections.unmodifiableMap(enchantments);
    }

    public boolean hasEnchantments() {
        return !enchantments.isEmpty();
    }

    /**
     * Creates a MerchantRecipe from this trade configuration.
     * @return The MerchantRecipe ready to be added to a trader
     */
    public MerchantRecipe toMerchantRecipe() {
        ItemStack result = new ItemStack(resultMaterial, resultAmount);
        
        // Handle enchanted books
        if (resultMaterial == Material.ENCHANTED_BOOK && !enchantments.isEmpty()) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
            if (meta != null) {
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    meta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
                }
                result.setItemMeta(meta);
            }
        }
        
        MerchantRecipe recipe = new MerchantRecipe(result, 0, maxUses, false);
        
        // Add primary cost
        recipe.addIngredient(new ItemStack(costMaterial, costAmount));
        
        // Add secondary cost if present
        if (secondCostMaterial != null && secondCostAmount > 0) {
            recipe.addIngredient(new ItemStack(secondCostMaterial, secondCostAmount));
        }
        
        return recipe;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "id='" + id + '\'' +
                ", result=" + resultAmount + "x " + resultMaterial +
                ", cost=" + costAmount + "x " + costMaterial +
                (secondCostMaterial != null ? ", secondCost=" + secondCostAmount + "x " + secondCostMaterial : "") +
                ", maxUses=" + maxUses +
                ", weight=" + weight +
                ", enabled=" + enabled +
                '}';
    }
}

package dev.chojo.simplepos.entity.response;

import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.Recipe;
import dev.chojo.simplepos.entity.RecipeEntry;

import java.math.BigDecimal;
import java.util.List;

/**
 * A product with its recipe. {@code unitCost} is what one unit costs at the ingredients' current prices, sent so the
 * interface shows the same margin everywhere; the profit in the statistics uses what the sold units actually cost.
 */
public record ProductResponse(Integer id, CategoryResponse category, String name, BigDecimal price, boolean active,
                              RecipeResponse recipe, BigDecimal unitCost) {
    public static ProductResponse of(Product product) {
        return new ProductResponse(product.getId(), CategoryResponse.of(product.getCategory()), product.getName(),
                product.getPrice(), product.isActive(), RecipeResponse.of(product.getRecipe()), product.unitCost());
    }

    public record RecipeResponse(List<RecipeEntryResponse> entries) {
        /**
         * The recipe as the API shows it, or null for none.
         */
        static RecipeResponse of(Recipe recipe) {
            if (recipe == null) return null;
            List<RecipeEntry> entries = recipe.getEntries() == null ? List.of() : recipe.getEntries();
            return new RecipeResponse(entries.stream().map(RecipeEntryResponse::of).toList());
        }
    }

    public record RecipeEntryResponse(IngredientResponse ingredient, int amount) {
        static RecipeEntryResponse of(RecipeEntry entry) {
            return new RecipeEntryResponse(IngredientResponse.of(entry.getIngredient()), entry.getAmount());
        }
    }
}

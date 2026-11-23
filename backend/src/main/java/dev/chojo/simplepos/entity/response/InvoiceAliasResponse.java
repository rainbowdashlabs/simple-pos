package dev.chojo.simplepos.entity.response;

import dev.chojo.simplepos.entity.CountUnit;
import dev.chojo.simplepos.entity.InvoiceAlias;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A remembered invoice name with what it stood for last time; the ingredient is null for pledge. The last price is as
 * printed, without VAT when {@code lastPriceNet}.
 */
public record InvoiceAliasResponse(Integer id, String text, String gtin, InvoiceAlias.Kind kind,
                                   IngredientResponse ingredient, CountUnit unit, BigDecimal lastPrice,
                                   boolean lastPriceNet, Instant lastUsed) {
    public static InvoiceAliasResponse of(InvoiceAlias alias) {
        return new InvoiceAliasResponse(alias.getId(), alias.getText(), alias.getGtin(), alias.getKind(),
                alias.getIngredient() == null ? null : IngredientResponse.of(alias.getIngredient()),
                alias.getUnit(), alias.getLastPrice(), alias.isLastPriceNet(), alias.getLastUsed());
    }
}

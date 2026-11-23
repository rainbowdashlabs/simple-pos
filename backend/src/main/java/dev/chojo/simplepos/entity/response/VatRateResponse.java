package dev.chojo.simplepos.entity.response;

import dev.chojo.simplepos.entity.VatRate;

import java.math.BigDecimal;

public record VatRateResponse(Integer id, String name, BigDecimal rate, boolean isDefault, boolean active) {
    /**
     * The rate as the API shows it, or null for none.
     */
    public static VatRateResponse of(VatRate rate) {
        return rate == null ? null
                : new VatRateResponse(rate.getId(), rate.getName(), rate.getRate(), rate.isDefaultRate(), rate.isActive());
    }
}

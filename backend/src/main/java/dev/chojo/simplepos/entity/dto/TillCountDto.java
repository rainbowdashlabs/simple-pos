package dev.chojo.simplepos.entity.dto;

import dev.chojo.simplepos.entity.TillCount;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record TillCountDto(int id, BigDecimal counted, BigDecimal expected, BigDecimal difference,
                           Map<Integer, Integer> denominations, String countedBy, Instant created) {
    public static TillCountDto of(TillCount count) {
        return new TillCountDto(count.getId(), count.getCounted(), count.getExpected(), count.getDifference(),
                count.getDenominations(), count.getCountedBy().getUsername(), count.getCreated());
    }
}

package dev.chojo.simplepos.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * The time zone that decides where a business day starts and ends in the daily statistics
 * ({@code pos.time-zone}, {@code Europe/Berlin} by default). The database and the server may run in UTC, so without it
 * sales shortly after midnight would count for the day before. The application refuses to start with an unknown zone
 * or a fixed offset, which PostgreSQL would read with the opposite sign.
 */
@Component
public class BusinessTimeZone {
    private final ZoneId zone;

    public BusinessTimeZone(@Value("${pos.time-zone:Europe/Berlin}") String zone) {
        ZoneId parsed = ZoneId.of(zone);
        if (parsed instanceof ZoneOffset || zone.matches("(?i)(UTC|GMT|UT)[+-].*")) {
            throw new IllegalArgumentException("pos.time-zone must be a region such as Europe/Berlin, not an offset like "
                    + zone + ": PostgreSQL reads offsets with the opposite sign.");
        }
        this.zone = parsed;
    }

    /**
     * The zone's id, as PostgreSQL's {@code AT TIME ZONE} takes it.
     */
    public String id() {
        return zone.getId();
    }
}

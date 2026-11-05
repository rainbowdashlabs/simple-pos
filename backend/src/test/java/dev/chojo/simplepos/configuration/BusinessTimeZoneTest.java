package dev.chojo.simplepos.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BusinessTimeZoneTest {
    @Test
    void acceptsRegions() {
        assertEquals("Europe/Berlin", new BusinessTimeZone("Europe/Berlin").id());
        assertEquals("UTC", new BusinessTimeZone("UTC").id());
    }

    @Test
    void refusesOffsets() {
        for (String offset : new String[]{"+02:00", "UTC+2", "GMT+02:00", "-05:00"}) {
            assertThrows(IllegalArgumentException.class, () -> new BusinessTimeZone(offset), offset);
        }
    }

    @Test
    void refusesUnknownZones() {
        assertThrows(RuntimeException.class, () -> new BusinessTimeZone("Mars/Olympus"));
    }
}

package dev.chojo.simplepos.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;

import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VersionControllerTest {
    @Test
    void shouldReportOnlyTheVersion() {
        Properties properties = new Properties();
        properties.setProperty("version", "0.1.1");
        properties.setProperty("time", "2026-11-04T12:00:00Z");
        properties.setProperty("group", "dev.chojo");

        VersionController controller = new VersionController(new BuildProperties(properties));

        assertEquals(Map.of("version", "0.1.1"), controller.getVersion().getBody());
    }
}

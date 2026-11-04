package dev.chojo.simplepos.controller;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VersionControllerTest {
    @Test
    void shouldReportOnlyTheVersion() throws Exception {
        var resource = new ByteArrayResource("0.1.1 main-abc1234 @ 2026-11-04 12:00\n".getBytes(StandardCharsets.UTF_8));

        assertEquals("0.1.1", new VersionController(resource).getVersion().getBody().get("version"));
    }
}

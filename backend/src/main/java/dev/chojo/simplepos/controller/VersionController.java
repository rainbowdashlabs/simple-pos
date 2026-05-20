package dev.chojo.simplepos.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/version")
public class VersionController {
    private final String version;

    public VersionController(@Value("classpath:version") Resource versionResource) throws IOException {
        this.version = versionResource.getContentAsString(StandardCharsets.UTF_8).trim();
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> getVersion() {
        return ResponseEntity.ok(Map.of("version", version));
    }
}

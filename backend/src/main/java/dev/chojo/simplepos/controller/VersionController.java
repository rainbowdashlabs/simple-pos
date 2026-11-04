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

/**
 * The application version, without the branch, commit or build time the version file also records: those say
 * more about the deployment than a public endpoint needs to.
 */
@RestController
@RequestMapping("/api/version")
public class VersionController {
    private final String version;

    public VersionController(@Value("classpath:version") Resource versionResource) throws IOException {
        this.version = versionResource.getContentAsString(StandardCharsets.UTF_8).trim().split("\\s+")[0];
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> getVersion() {
        return ResponseEntity.ok(Map.of("version", version));
    }
}

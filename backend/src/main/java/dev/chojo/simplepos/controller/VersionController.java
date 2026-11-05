package dev.chojo.simplepos.controller;

import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * The application version, without the branch, commit or build time: those say more about the deployment than a
 * public endpoint needs to.
 */
@RestController
@RequestMapping("/api/version")
public class VersionController {
    private final String version;

    public VersionController(BuildProperties buildProperties) {
        this.version = buildProperties.getVersion();
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> getVersion() {
        return ResponseEntity.ok(Map.of("version", version));
    }
}

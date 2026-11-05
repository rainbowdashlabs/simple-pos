package dev.chojo.simplepos.configuration.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * SHA-256 fingerprints of secrets and names, so they can be stored or counted without being kept.
 */
public final class Sha256 {
    private Sha256() {
    }

    /**
     * The SHA-256 of the text's UTF-8 bytes, hex encoded.
     */
    public static String hex(String text) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is always available", e);
        }
    }
}

package dev.chojo.simplepos.entity.security;

public record TokenDto(String token, long expiresIn) {
}

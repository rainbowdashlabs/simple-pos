package dev.chojo.simplepos.entity.security;

public record TokenResponse(TokenDto token, TokenDto refresh) {

}

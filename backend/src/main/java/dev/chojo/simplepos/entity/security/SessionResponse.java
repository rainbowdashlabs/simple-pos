package dev.chojo.simplepos.entity.security;

/**
 * What the browser gets when a session starts or is refreshed: the login token and whose it is. The refresh token
 * travels only in its HttpOnly cookie.
 */
public record SessionResponse(TokenDto token, String username) {
}

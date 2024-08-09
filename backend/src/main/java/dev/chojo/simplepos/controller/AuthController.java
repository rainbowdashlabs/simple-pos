package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.configuration.security.JwtService;
import dev.chojo.simplepos.entity.Token;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.UserDto;
import dev.chojo.simplepos.entity.security.TokenDto;
import dev.chojo.simplepos.entity.security.TokenResponse;
import dev.chojo.simplepos.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Map<String, Object> LOGIN_CLAIM = Map.of("type", "login");
    private static final Map<String, Object> REFRESH_CLAIM = Map.of("type", "refresh");
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    @Value("${security.jwt.refresh-expiration-time}")
    private long jwtRefreshExpiration;

    public AuthController(AuthenticationService authenticationService, JwtService jwtService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.ACCEPTED)
    ResponseEntity<TokenResponse> login(@RequestParam String username, @RequestParam String password) {
        User authenticatedUser = authenticationService.authenticate(username, password);

        TokenResponse tokenResponse = new TokenResponse(
                new TokenDto(jwtService.generateToken(LOGIN_CLAIM, authenticatedUser, jwtService.getExpirationTime()), jwtService.getExpirationTime()),
                new TokenDto(jwtService.generateToken(REFRESH_CLAIM, authenticatedUser, jwtRefreshExpiration), jwtRefreshExpiration)
        );
        authenticationService.saveRefreshToken(new Token(tokenResponse.refresh().token(), authenticatedUser));

        return ResponseEntity.accepted().body(tokenResponse);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.ACCEPTED)
    ResponseEntity<TokenResponse> refresh(@RequestParam String token) {
        String username = jwtService.extractUsername(token);
        User user = authenticationService.findByUsername(username);
        if (!jwtService.isTokenValid(token, user, REFRESH_CLAIM)) {
            var detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid Token");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, detail, null);
        }

        Optional<Token> optToken = authenticationService.retrieveToken(user, token);
        if (optToken.isEmpty()) {
            var detail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Unknown token");
            throw new ErrorResponseException(HttpStatus.UNAUTHORIZED, detail, null);
        }

        log.info("Attempting to invalidate refresh token {}", optToken.get());
        boolean invalidated = authenticationService.invalidateRefreshToken(optToken.get());

        if (!invalidated) {
            var detail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Token already used.");
            throw new ErrorResponseException(HttpStatus.UNAUTHORIZED, detail, null);
        }
        log.info("Valid token. Creating new login and refresh token");

        TokenResponse tokenResponse = new TokenResponse(
                new TokenDto(jwtService.generateToken(LOGIN_CLAIM, user, jwtService.getExpirationTime()), jwtService.getExpirationTime()),
                new TokenDto(jwtService.generateToken(REFRESH_CLAIM, user, jwtRefreshExpiration), jwtRefreshExpiration)
        );
        authenticationService.saveRefreshToken(new Token(tokenResponse.refresh().token(), user));
        return ResponseEntity.accepted().body(tokenResponse);
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void register(@RequestBody UserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);
    }
}

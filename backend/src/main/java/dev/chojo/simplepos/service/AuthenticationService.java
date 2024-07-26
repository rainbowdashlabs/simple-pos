package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.Token;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dao.UserDto;
import dev.chojo.simplepos.repository.TokenRepository;
import dev.chojo.simplepos.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository, TokenRepository tokenRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder
    ) {
        this.tokenRepository = tokenRepository;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User signup(UserDto input) {
        User user = new User(null, input.username(), passwordEncoder.encode(input.password()), false);

        return userRepository.save(user);
    }

    public void saveRefreshToken(Token token) {
        tokenRepository.save(token);
    }

    public User findByUsername(String username){
        return userRepository.findByUsername(username).orElseThrow();
    }

    public User authenticate(String username, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );

        return userRepository.findByUsername(username)
                .orElseThrow();
    }

    @Transactional
    public boolean invalidateRefreshToken(User user, String token) {
        return tokenRepository.deleteByUserAndToken(user, token) == 1;
    }
}

package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.Token;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.repository.TokenRepository;
import dev.chojo.simplepos.repository.UserRepository;
import org.slf4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class UserService implements UserDetailsService, UserDetailsPasswordService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder encoder;
    private static final Logger log = getLogger(UserService.class);

    public UserService(UserRepository userRepository, TokenRepository tokenRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.encoder = encoder;
    }

    public Optional<Token> getToken(String token) {
        return tokenRepository.findByToken(token);
    }

    @Override
    public UserDetails updatePassword(UserDetails details, String newPassword) {
        User byName = userRepository.findByUsername(details.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Username %s not known"));
        byName.setPassword(newPassword);
        userRepository.save(byName);
        return byName;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Retrieved user data");
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username %s not known"));
    }

    public void create(String username, String password) {
        User user = new User(null, username, encoder.encode(password), false);
        userRepository.save(user);
    }
}

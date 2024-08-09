package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.repository.UserRepository;
import org.slf4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class UserService implements UserDetailsService, UserDetailsPasswordService {
    private final UserRepository userRepository;
    private static final Logger log = getLogger(UserService.class);

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User updatePassword(UserDetails details, String newPassword) {
        User byName = userRepository.findByUsername(details.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Username %s not known"));
        byName.setPassword(newPassword);
        userRepository.save(byName);
        return byName;
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Retrieved user data");
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username %s not known"));
    }
}

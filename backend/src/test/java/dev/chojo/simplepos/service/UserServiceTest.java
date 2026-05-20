package dev.chojo.simplepos.service;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;

    @Test
    void shouldLoadUserByUsername() {
        userRepository.save(new User(null, "loadable", "pass", true));

        User loaded = userService.loadUserByUsername("loadable");
        assertNotNull(loaded);
        assertEquals("loadable", loaded.getUsername());
    }

    @Test
    void shouldThrowWhenUsernameNotFound() {
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("nonexistent");
        });
    }

    @Test
    void shouldUpdatePassword() {
        User user = userRepository.save(new User(null, "pwduser", "oldpassword", true));

        User updated = userService.updatePassword(user, "newpassword");
        assertEquals("newpassword", updated.getPassword());

        User found = userRepository.findByUsername("pwduser").orElseThrow();
        assertEquals("newpassword", found.getPassword());
    }

    @Test
    void shouldReturnUserWithCorrectEnabledStatus() {
        userRepository.save(new User(null, "disabled", "pass", false));

        User loaded = userService.loadUserByUsername("disabled");
        assertFalse(loaded.isEnabled());
    }

    @Test
    void loadedUserImplementsUserDetails() {
        userRepository.save(new User(null, "details", "secret", true));

        User loaded = userService.loadUserByUsername("details");
        assertEquals("details", loaded.getUsername());
        assertEquals("secret", loaded.getPassword());
        assertTrue(loaded.isEnabled());
        assertNotNull(loaded.getAuthorities());
    }
}

package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserRepositoryTest {
    @Autowired
    UserRepository userRepository;

    @Test
    void shouldSaveUser() {
        User user = userRepository.save(new User(null, "alice", "secret", true));
        assertNotNull(user.getId());
    }

    @Test
    void shouldFindByUsername() {
        userRepository.save(new User(null, "bob", "pass", true));
        Optional<User> found = userRepository.findByUsername("bob");
        assertTrue(found.isPresent());
        assertEquals("bob", found.get().getUsername());
    }

    @Test
    void shouldNotFindNonExistentUsername() {
        Optional<User> found = userRepository.findByUsername("nonexistent");
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldPersistEnabledFlag() {
        User disabled = userRepository.save(new User(null, "disabled_user", "pass", false));
        User found = userRepository.findById(disabled.getId()).orElseThrow();
        assertFalse(found.isEnabled());
    }

    @Test
    void shouldUpdatePassword() {
        User user = userRepository.save(new User(null, "charlie", "oldpass", true));
        user.setPassword("newpass");
        userRepository.save(user);

        User found = userRepository.findByUsername("charlie").orElseThrow();
        assertEquals("newpass", found.getPassword());
    }

    @Test
    void shouldEnforceUniqueUsername() {
        userRepository.save(new User(null, "unique", "pass1", true));
        assertThrows(Exception.class, () -> {
            userRepository.saveAndFlush(new User(null, "unique", "pass2", true));
        });
    }

    @Test
    void shouldDeleteUser() {
        User user = userRepository.save(new User(null, "deletable", "pass", true));
        userRepository.deleteById(user.getId());
        assertTrue(userRepository.findByUsername("deletable").isEmpty());
    }

    @Test
    void shouldFindById() {
        User user = userRepository.save(new User(null, "findme", "pass", true));
        Optional<User> found = userRepository.findById(user.getId());
        assertTrue(found.isPresent());
        assertEquals("findme", found.get().getUsername());
    }
}

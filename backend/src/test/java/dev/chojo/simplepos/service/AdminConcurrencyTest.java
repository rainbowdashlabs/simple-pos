package dev.chojo.simplepos.service;

import dev.chojo.simplepos.TestcontainersConfiguration;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.UpdateUserRequest;
import dev.chojo.simplepos.exceptions.ConflictException;
import dev.chojo.simplepos.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Two admins demote each other at the same time, each in its own committed transaction: exactly one must win.
 * <p>
 * Because this test commits, the context is discarded afterwards; the next context cleans and migrates the schema
 * afresh, so no data leaks into other tests.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AdminConcurrencyTest {
    @Autowired
    UserAdminService userAdminService;
    @Autowired
    UserRepository userRepository;

    @Test
    void shouldKeepOneAdminWhenTwoAdminsDemoteEachOther() throws Exception {
        userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ADMIN && user.isEnabled())
                .forEach(user -> {
                    user.setEnabled(false);
                    userRepository.save(user);
                });
        User first = userRepository.save(new User(null, "firstadmin", "x", true, Role.ADMIN));
        User second = userRepository.save(new User(null, "secondadmin", "x", true, Role.ADMIN));
        CountDownLatch start = new CountDownLatch(1);

        List<Throwable> failures = new ArrayList<>();
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            List<Future<?>> demotions = new ArrayList<>();
            for (User target : List.of(first, second)) {
                demotions.add(executor.submit(() -> {
                    start.await();
                    return userAdminService.update(target.getId(), new UpdateUserRequest(Role.STAFF, null));
                }));
            }
            start.countDown();
            for (Future<?> demotion : demotions) {
                try {
                    demotion.get(30, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    failures.add(e.getCause());
                }
            }
        }

        assertEquals(1, failures.size());
        assertInstanceOf(ConflictException.class, failures.getFirst());
        assertEquals(1, userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ADMIN && user.isEnabled())
                .count());
    }
}

package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    /**
     * Finds the enabled users of a role and locks their rows, in id order, until the current transaction ends.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM users u WHERE u.role = ?1 AND u.enabled = true ORDER BY u.id")
    List<User> lockEnabledByRole(Role role);
}

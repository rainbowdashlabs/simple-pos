package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
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

    /**
     * Gives every user without a role one: enabled users become ADMIN, disabled ones USER. Users created before
     * roles existed have none; the disabled ones are signups nobody approved, so they must not become admins.
     *
     * @return the number of users changed
     */
    @Modifying
    @Query("""
            UPDATE users u
            SET u.role = CASE WHEN u.enabled = true THEN dev.chojo.simplepos.entity.Role.ADMIN
                              ELSE dev.chojo.simplepos.entity.Role.USER END
            WHERE u.role IS NULL""")
    int assignRoleToUsersWithoutRole();
}

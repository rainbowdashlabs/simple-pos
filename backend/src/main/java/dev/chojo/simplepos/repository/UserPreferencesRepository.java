package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Integer> {
    Optional<UserPreferences> findByUser(User user);
}

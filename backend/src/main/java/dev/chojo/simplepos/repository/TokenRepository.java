package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Token;
import dev.chojo.simplepos.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {
    Optional<Token> findByToken(String token);
    Optional<Token> findByUserAndToken(User user, String token);
    /**
     * Deletes the refresh token in a single statement, so of two concurrent calls with the same token only one sees
     * it deleted.
     *
     * @return the number of deleted tokens, 1 if the token existed and 0 otherwise
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Token t WHERE t.user = ?1 AND t.token = ?2")
    int deleteByUserAndToken(User user, String token);

}

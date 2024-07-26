package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Token;
import dev.chojo.simplepos.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {
    Optional<Token> findByToken(String token);

    long deleteByUserAndToken(User user, String token);
}

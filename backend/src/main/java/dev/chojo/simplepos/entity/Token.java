package dev.chojo.simplepos.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.Instant;

/**
 * A refresh token that may still be used, stored as its SHA-256 fingerprint so that read access to the database is
 * not enough to take over a session. {@code expires} lets expired ones be deleted without reading them.
 */
@Entity
public class Token {
    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String token;

    private Instant expires;

    public Token(String token, User user, Instant expires) {
        this.token = token;
        this.user = user;
        this.expires = expires;
    }

    public Instant getExpires() {
        return expires;
    }

    public Token() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

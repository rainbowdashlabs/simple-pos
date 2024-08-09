package dev.chojo.simplepos.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import org.springframework.context.annotation.Lazy;

@Entity
public class Token {
    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    @Lazy
    private User user;

    private String token;

    public Token(String token, User user) {
        this.token = token;
        this.user = user;
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

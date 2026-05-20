package dev.chojo.simplepos.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class UserPreferences {
    @Id
    @GeneratedValue
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private String locale = "de";

    private String colorTheme = "blue";

    private String darkMode = "dark";

    public UserPreferences() {
    }

    public UserPreferences(User user) {
        this.user = user;
    }

    public Integer getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getColorTheme() {
        return colorTheme;
    }

    public void setColorTheme(String colorTheme) {
        this.colorTheme = colorTheme;
    }

    public String getDarkMode() {
        return darkMode;
    }

    public void setDarkMode(String darkMode) {
        this.darkMode = darkMode;
    }
}

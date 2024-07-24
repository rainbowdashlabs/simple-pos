package dev.chojo.simplepos.configuration.security;

import dev.chojo.simplepos.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class LoginService implements AuthenticationSuccessHandler {
    private final UserService userService;

    public LoginService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication auth) throws IOException, ServletException {
        auth.getCredentials();
        res.getOutputStream().write(new String("This is your token").getBytes());
    }
}

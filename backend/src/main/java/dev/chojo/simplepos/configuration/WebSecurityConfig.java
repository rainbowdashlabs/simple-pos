package dev.chojo.simplepos.configuration;

import dev.chojo.simplepos.configuration.security.JwtAuthenticationFilter;
import dev.chojo.simplepos.configuration.security.LoginService;
import dev.chojo.simplepos.configuration.security.SecurityErrorResolver;
import dev.chojo.simplepos.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalAuthentication()
public class WebSecurityConfig {

    private static final String TOKEN = "authorization";


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider, JwtAuthenticationFilter jwtAuthenticationFilter, LoginService loginService,
                                                   SecurityErrorResolver securityErrorResolver) throws Exception {
        return http
                .cors(conf -> conf.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable) // TODO enable and figure out this stuff
                .authorizeHttpRequests(conf -> conf
                        .requestMatchers("/api/auth/**", "/api/demo/**", "/api/user", "/api/version").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .formLogin(form -> form.loginPage("/#login")
                        .loginProcessingUrl("/api/login")
                        .permitAll()
                        .successHandler(loginService)
                        .failureHandler(securityErrorResolver::resolve))
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint(securityErrorResolver::resolve)
                        .accessDeniedHandler(securityErrorResolver::resolve))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Authenticates against the stored users. Whether an account is disabled is only checked after the password
     * matched, so a wrong password never reveals that a user exists or is disabled.
     */
    @Bean
    public AuthenticationProvider authenticationProvider(UserService userService, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        provider.setPasswordEncoder(encoder);
        provider.setPreAuthenticationChecks(user -> {
            if (!user.isAccountNonLocked()) throw new LockedException("Account is locked");
            if (!user.isAccountNonExpired()) throw new AccountExpiredException("Account is expired");
        });
        provider.setPostAuthenticationChecks(user -> {
            if (!user.isEnabled()) throw new DisabledException("Account is disabled");
            if (!user.isCredentialsNonExpired()) throw new CredentialsExpiredException("Credentials are expired");
        });
        provider.setUserDetailsPasswordService(userService);
        return provider;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.applyPermitDefaultValues();
        configuration.setAllowedOrigins(List.of("*")); // todo
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

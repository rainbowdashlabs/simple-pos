package dev.chojo.simplepos.configuration;

import dev.chojo.simplepos.configuration.security.JwtAuthenticationFilter;
import dev.chojo.simplepos.configuration.security.SecurityErrorResolver;
import dev.chojo.simplepos.entity.Role;
import dev.chojo.simplepos.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity
public class WebSecurityConfig {

    /**
     * Everything the frontend loads comes from this application. Styles allow inline code because components and
     * the icon library set styles at runtime; scripts do not.
     */
    static final String CONTENT_SECURITY_POLICY = "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; "
            + "img-src 'self' data:; font-src 'self' data:; connect-src 'self'; object-src 'none'; base-uri 'self'; "
            + "form-action 'self'; frame-ancestors 'none'";

    private final List<String> allowedOrigins;

    /**
     * @param allowedOrigins {@code security.cors.allowed-origins}; blank entries are ignored
     */
    public WebSecurityConfig(@Value("${security.cors.allowed-origins:}") List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins.stream().filter(origin -> !origin.isBlank()).toList();
    }


    /**
     * ADMIN includes STAFF, STAFF includes USER. Used by the request rules and by method security.
     */
    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role(Role.ADMIN.name()).implies(Role.STAFF.name())
                .role(Role.STAFF.name()).implies(Role.USER.name())
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider, JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   SecurityErrorResolver securityErrorResolver) throws Exception {
        return http
                .cors(conf -> conf.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers.contentSecurityPolicy(csp -> csp.policyDirectives(CONTENT_SECURITY_POLICY)))
                .csrf(AbstractHttpConfigurer::disable) // TODO enable and figure out this stuff
                .authorizeHttpRequests(conf -> conf
                        .requestMatchers("/api/auth/**", "/api/demo/**", "/api/version").permitAll()
                        .requestMatchers("/api/admin/**").hasRole(Role.ADMIN.name())
                        .requestMatchers("/api/user/me", "/api/user/preferences").hasRole(Role.USER.name())
                        .requestMatchers("/api/**").hasRole(Role.STAFF.name())
                        .anyRequest().permitAll())
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

    /**
     * Cross-origin access only for the origins listed in {@code security.cors.allowed-origins}. The frontend is
     * served by this application itself, so by default no other origin is allowed.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        if (allowedOrigins.isEmpty()) return source;

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
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

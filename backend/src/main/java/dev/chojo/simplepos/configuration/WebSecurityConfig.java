package dev.chojo.simplepos.configuration;

import dev.chojo.simplepos.configuration.security.LoginService;
import dev.chojo.simplepos.configuration.security.TokenFilter;
import dev.chojo.simplepos.service.UserService;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private static final String TOKEN = "authorization";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider, TokenFilter filter, LoginService loginService) throws Exception {
        return http
                .securityMatcher("/#login", "/#logout", "/api/login")
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable) // TODO enable and figure out this stuff
                .formLogin(form -> {
                    form.loginPage("/#login")
                            .loginProcessingUrl("/api/login")
                            .permitAll()
                            .successHandler(loginService)
                            .failureHandler(((request, response, exception) -> {
                                if (exception instanceof DisabledException) {
                                    response.getOutputStream().print("Account is disabled. Ask an administrator to enable it.");
                                } else {
                                    response.getOutputStream().print("Invalid credentials");
                                }
                                response.setStatus(HttpStatus.BAD_REQUEST.value());
                            }));
                })
                .authorizeHttpRequests(request -> {
                    request.requestMatchers("/").authenticated();
                })
                .logout(logout -> {
                    logout.logoutSuccessUrl("/#login");
                })
                .exceptionHandling(handler -> {
                    handler.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.FORBIDDEN));
                })
                .addFilterAfter(filter, BasicAuthenticationFilter.class)
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserService service, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(encoder);
        provider.setUserDetailsService(service);
        provider.setUserDetailsPasswordService(service);
        return provider;

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

//    @Bean
//    public FilterRegistrationBean<TokenFilter> tokenFilter(TokenFilter tokenFilter){
//        FilterRegistrationBean<TokenFilter> registrationBean = new FilterRegistrationBean<>();
//        registrationBean.setFilter(tokenFilter);
//        registrationBean.setOrder(Integer.MAX_VALUE);
//        return registrationBean;
//    }
}

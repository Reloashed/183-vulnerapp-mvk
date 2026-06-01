package ch.bbw.m183.vulnerapp.security;

import ch.bbw.m183.vulnerapp.service.RestfulFormService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, RestfulFormService restfulFormService) {
        return http
                .headers(
                        headers -> headers.xssProtection(
                                xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                        ).contentSecurityPolicy(
                                cps -> cps.policyDirectives("default-src 'self'; script-src 'self'; object-src 'none';")
                        )
                )
                .formLogin(restfulFormService.restfulFormLogin())
                .exceptionHandling(restfulFormService.unauthorizedPerDefault())
                .csrf(x -> x.spa().ignoringRequestMatchers("/login").ignoringRequestMatchers("/api/user/whoami"))
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers(HttpMethod.POST, "/api/blog")
                                .hasAuthority("POST_BLOG")
                                .requestMatchers(HttpMethod.GET, "/api/user/whoami")
                                .hasAuthority("WHOAMI")
                                .requestMatchers("/api/admin/*")
                                .hasRole("ADMIN")
                                .anyRequest()
                                .permitAll())
                .build();
    }
}

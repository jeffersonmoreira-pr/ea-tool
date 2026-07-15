package com.eatool.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Every route requires an authenticated session except the health-check, which
 * must stay reachable for infrastructure monitoring without a login.
 *
 * Login uses the OIDC Authorization Code flow against the Keycloak dev realm
 * (see docker/keycloak/ea-tool-realm.json). Logout also ends the Keycloak
 * session (OIDC RP-Initiated Logout) so a fresh login is required afterwards.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.frontend-base-url:http://localhost:8080}")
    private String frontendBaseUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository)
            throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2.defaultSuccessUrl(frontendBaseUrl, true))
                .logout(logout -> logout.logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository)))
                // JS reads the XSRF-TOKEN cookie and sends it back as a header
                // (e.g. on the logout fetch call), following Spring Security's
                // recommended pattern for browser-based single page apps.
                // CsrfTokenRequestAttributeHandler (instead of the XOR-based
                // default) is required so the raw cookie value validates
                // directly against the header sent by plain JS/fetch.
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                // Forces the deferred CsrfToken to resolve (and its cookie to be
                // written) on every request, so the frontend always has a
                // fresh XSRF-TOKEN cookie to send back without a warm-up call.
                .addFilterAfter(new CsrfCookieFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private LogoutSuccessHandler oidcLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository) {
        OidcClientInitiatedLogoutSuccessHandler handler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        handler.setPostLogoutRedirectUri(frontendBaseUrl);
        return handler;
    }
}

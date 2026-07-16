package com.eatool.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import com.eatool.backend.catalogusers.CatalogUserProvisioningOidcUserService;

/**
 * Every route requires an authenticated session except the health-check and
 * the Local Login invite flow (see issue #9), which must stay reachable so a
 * new user can set their password before they can log in.
 *
 * Two authentication methods coexist (ADR-0004): SSO via the OIDC Authorization
 * Code flow against Keycloak (auto-provisioning a Catalog User on first login,
 * see CatalogUserProvisioningOidcUserService), and Local Login via a
 * username/password form backed by a DaoAuthenticationProvider. Unauthenticated
 * users land on a custom login page that offers both. Authorization (Role,
 * Access Scope, Edit Permission) is resolved independently of the login method
 * (see CurrentUserService).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.frontend-base-url:http://localhost:8080}")
    private String frontendBaseUrl;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider localAuthenticationProvider(
            UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ClientRegistrationRepository clientRegistrationRepository,
            DaoAuthenticationProvider localAuthenticationProvider,
            CatalogUserProvisioningOidcUserService catalogUserProvisioningOidcUserService)
            throws Exception {
        http
                .authenticationProvider(localAuthenticationProvider)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health").permitAll()
                        // The Local Login pages and invite flow must be usable
                        // without a session: a freshly created user has no
                        // password yet, so they need to reach the login page and
                        // the set-password endpoint before they can authenticate
                        // (issue #9). Account creation itself is Admin-only (under
                        // /api/catalog-users below), so there is no self-signup.
                        .requestMatchers("/login.html", "/set-password.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/local-login/invitations/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/local-login/set-password").permitAll()
                        // The Catalog Users admin API (list users, change Role,
                        // create Local Login accounts) is Admin-only. It must come
                        // before the generic GET rule below, since request matchers
                        // are evaluated in order and the first match wins —
                        // otherwise a GET on /api/catalog-users would match the
                        // authenticated-only rule and leak the user list to
                        // non-Admins (issue #8).
                        .requestMatchers("/api/catalog-users/**").hasRole("ADMIN")
                        // The Email Delivery (SMTP Relay) admin API is Admin-only
                        // and must also come before the generic GET rule below, so
                        // a GET on /api/email-delivery never leaks the relay
                        // configuration to non-Admins (issue #23).
                        .requestMatchers("/api/email-delivery/**").hasRole("ADMIN")
                        // Reading the catalog is allowed for any authenticated
                        // Catalog User (Viewer included); this GET rule must come
                        // before the write rules below so reads never require a
                        // write Role.
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                        // Creating, editing or deleting Applications and master
                        // data requires an Editor or Admin Role (see issue #6 and
                        // ADR-0005). Viewers are denied with 403. Fine-grained
                        // Edit Permission per record is handled separately (#11).
                        .requestMatchers(
                                "/api/applications/**",
                                "/api/vendors/**",
                                "/api/departments/**",
                                "/api/business-areas/**")
                        .hasAnyRole("EDITOR", "ADMIN")
                        .anyRequest().authenticated())
                // Local Login: a username/password form posting to /login. The
                // custom login page also links to the SSO button, so both methods
                // coexist on one screen (issue #9). Because a custom login page is
                // configured, unauthenticated users are sent here instead of
                // straight to Keycloak.
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl(frontendBaseUrl, true)
                        .failureUrl("/login.html?error")
                        .permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login.html")
                        .defaultSuccessUrl(frontendBaseUrl, true)
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(catalogUserProvisioningOidcUserService)))
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

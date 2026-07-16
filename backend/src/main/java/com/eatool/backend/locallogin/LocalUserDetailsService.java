package com.eatool.backend.locallogin;

import java.util.List;
import java.util.Locale;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eatool.backend.catalogusers.CatalogUser;
import com.eatool.backend.catalogusers.CatalogUserRepository;
import com.eatool.backend.catalogusers.LoginMethod;

/**
 * Loads Local Login users for the DaoAuthenticationProvider (issue #9). Only
 * Catalog Users whose login method is LOCAL and who have already set a password
 * can authenticate this way; their Role becomes a {@code ROLE_*} authority, so
 * authorization matches the SSO path exactly.
 */
@Service
public class LocalUserDetailsService implements UserDetailsService {

    private final CatalogUserRepository catalogUserRepository;

    public LocalUserDetailsService(CatalogUserRepository catalogUserRepository) {
        this.catalogUserRepository = catalogUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CatalogUser user = catalogUserRepository.findByEmail(username == null ? "" : username.toLowerCase(Locale.ROOT))
                .filter(candidate -> candidate.getLoginMethod() == LoginMethod.LOCAL)
                .filter(candidate -> candidate.getPasswordHash() != null)
                .orElseThrow(() -> new UsernameNotFoundException("No Local Login account for: " + username));

        return new User(
                user.getEmail(),
                user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }
}

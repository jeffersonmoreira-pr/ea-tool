package com.eatool.backend.locallogin;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eatool.backend.catalogusers.CatalogUser;
import com.eatool.backend.catalogusers.CatalogUserRepository;
import com.eatool.backend.catalogusers.LoginMethod;
import com.eatool.backend.catalogusers.Role;
import com.eatool.backend.common.BadRequestException;
import com.eatool.backend.common.ConflictException;

/**
 * Creates Local Login accounts and drives the invite/password-set flow
 * (issue #9, ADR-0004). Accounts are always created by an Admin (never
 * self-signup); the password is set once via a single-use, expiring invite
 * link. Authorization (Role, Access Scope) is stored on the same Catalog User
 * record, so it stays independent of the login method.
 */
@Service
public class LocalLoginService {

    private static final Duration INVITE_VALIDITY = Duration.ofHours(48);
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int TOKEN_BYTES = 32;

    private final CatalogUserRepository catalogUserRepository;
    private final LocalLoginInvitationRepository invitationRepository;
    private final InvitationMailer invitationMailer;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String frontendBaseUrl;

    public LocalLoginService(
            CatalogUserRepository catalogUserRepository,
            LocalLoginInvitationRepository invitationRepository,
            InvitationMailer invitationMailer,
            PasswordEncoder passwordEncoder,
            @Value("${app.frontend-base-url:http://localhost:8080}") String frontendBaseUrl) {
        this.catalogUserRepository = catalogUserRepository;
        this.invitationRepository = invitationRepository;
        this.invitationMailer = invitationMailer;
        this.passwordEncoder = passwordEncoder;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    /**
     * Creates a Local Login account with no password yet and emails (or, in
     * dev, logs) a single-use invite link for the user to set it.
     */
    @Transactional
    public CatalogUser createLocalUser(String rawName, String rawEmail, String rawRole) {
        String name = requireText(rawName, "Name");
        String email = requireText(rawEmail, "Email").toLowerCase(Locale.ROOT);
        Role role = parseRole(rawRole);

        if (catalogUserRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("A Catalog User with this email already exists.");
        }

        CatalogUser user = catalogUserRepository.save(new CatalogUser(email, name, role, LoginMethod.LOCAL));

        String token = generateToken();
        invitationRepository.save(
                new LocalLoginInvitation(user.getId(), token, Instant.now().plus(INVITE_VALIDITY)));

        invitationMailer.sendInvite(email, name, inviteLink(token));
        return user;
    }

    /** Validates the invite token and returns whom it belongs to, for the set-password page. */
    @Transactional(readOnly = true)
    public InvitationDetails describeInvitation(String token) {
        LocalLoginInvitation invitation = redeemableInvitation(token);
        CatalogUser user = catalogUserRepository.findById(invitation.getCatalogUserId())
                .orElseThrow(() -> new BadRequestException("This invitation is no longer valid."));
        return new InvitationDetails(user.getEmail(), user.getName());
    }

    /** Sets the password once for a valid invite, then consumes the invitation. */
    @Transactional
    public void setPassword(String token, String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new BadRequestException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        LocalLoginInvitation invitation = redeemableInvitation(token);
        CatalogUser user = catalogUserRepository.findById(invitation.getCatalogUserId())
                .orElseThrow(() -> new BadRequestException("This invitation is no longer valid."));

        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        invitation.markUsed();
        catalogUserRepository.save(user);
        invitationRepository.save(invitation);
    }

    private LocalLoginInvitation redeemableInvitation(String token) {
        LocalLoginInvitation invitation = invitationRepository.findByToken(token == null ? "" : token)
                .orElseThrow(() -> new BadRequestException("This invitation link is invalid or has expired."));
        if (!invitation.isRedeemable(Instant.now())) {
            throw new BadRequestException("This invitation link is invalid or has expired.");
        }
        return invitation;
    }

    private String inviteLink(String token) {
        return frontendBaseUrl + "/set-password.html?token=" + token;
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String requireText(String value, String label) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            throw new BadRequestException(label + " is required.");
        }
        return trimmed;
    }

    private Role parseRole(String rawRole) {
        String value = rawRole == null ? "" : rawRole.trim();
        if (value.isEmpty()) {
            throw new BadRequestException("Role is required.");
        }
        try {
            return Role.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException error) {
            throw new BadRequestException("Role must be VIEWER, EDITOR, or ADMIN.");
        }
    }

    /** Minimal, non-sensitive view of an invitation for the set-password page. */
    public record InvitationDetails(String email, String name) {
    }
}

package com.eatool.backend.locallogin;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.eatool.backend.locallogin.LocalLoginService.InvitationDetails;

/**
 * Public endpoints backing the Local Login invite flow (issue #9): validating
 * an invite token to render the set-password page, and setting the password
 * once. These are reachable without an authenticated session (see
 * SecurityConfig permitAll), since the user has no password yet. Account
 * creation itself is Admin-only and lives in the Catalog Users admin API — no
 * self-signup exists.
 */
@RestController
@RequestMapping("/api/local-login")
public class LocalLoginController {

    private final LocalLoginService localLoginService;

    public LocalLoginController(LocalLoginService localLoginService) {
        this.localLoginService = localLoginService;
    }

    @GetMapping("/invitations/{token}")
    public InvitationDetails invitation(@PathVariable String token) {
        return localLoginService.describeInvitation(token);
    }

    @PostMapping("/set-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setPassword(@RequestBody SetPasswordRequest request) {
        localLoginService.setPassword(request.getToken(), request.getPassword());
    }
}

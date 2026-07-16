package com.eatool.backend.locallogin;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalLoginInvitationRepository extends JpaRepository<LocalLoginInvitation, UUID> {

    Optional<LocalLoginInvitation> findByToken(String token);
}

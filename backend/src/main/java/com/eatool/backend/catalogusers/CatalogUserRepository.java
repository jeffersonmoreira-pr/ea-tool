package com.eatool.backend.catalogusers;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogUserRepository extends JpaRepository<CatalogUser, UUID> {

    Optional<CatalogUser> findByEmail(String email);
}

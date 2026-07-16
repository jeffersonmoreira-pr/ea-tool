package com.eatool.backend.masterdata;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessAreaRepository extends JpaRepository<BusinessArea, UUID> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}

package com.eatool.backend.applications;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    Optional<Application> findFirstByVendorId(UUID vendorId);

    Optional<Application> findFirstByDepartmentId(UUID departmentId);

    Optional<Application> findFirstByBusinessAreaId(UUID businessAreaId);
}

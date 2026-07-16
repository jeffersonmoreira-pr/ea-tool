package com.eatool.backend.editpermission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EditPermissionRepository extends JpaRepository<EditPermission, UUID> {

    boolean existsByRecordTypeAndRecordIdAndCatalogUserId(
            EditableRecordType recordType, UUID recordId, UUID catalogUserId);

    Optional<EditPermission> findByRecordTypeAndRecordIdAndCatalogUserId(
            EditableRecordType recordType, UUID recordId, UUID catalogUserId);

    List<EditPermission> findByCatalogUserIdOrderByGrantedAt(UUID catalogUserId);
}

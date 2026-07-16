package com.eatool.backend.editpermission;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.transaction.annotation.Transactional;

import com.eatool.backend.catalogusers.CatalogUser;
import com.eatool.backend.catalogusers.CatalogUserRepository;
import com.eatool.backend.catalogusers.Role;
import com.eatool.backend.masterdata.Department;
import com.eatool.backend.masterdata.DepartmentRepository;

import org.assertj.core.api.Assertions;

/**
 * Unit-level coverage of {@link EditPermissionService#canEdit} branches that
 * the write endpoints cannot reach directly (issue #11). In particular, a
 * Viewer with an Edit Permission granted by mistake still cannot edit, because
 * the Editor Role stays a prerequisite — the HTTP layer would already deny a
 * Viewer via the Role filter, so this asserts the rule at the source.
 */
@SpringBootTest
@Transactional
class EditPermissionServiceTests {

    @Autowired
    private EditPermissionService editPermissionService;

    @Autowired
    private CatalogUserRepository catalogUserRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EditPermissionRepository editPermissionRepository;

    private OidcUser principal(String email, String roleAuthority) {
        OidcIdToken token = new OidcIdToken(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("sub", email, "email", email));
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roleAuthority));
        return new DefaultOidcUser(authorities, token);
    }

    @Test
    void viewerWithStrayGrantStillCannotEdit() {
        CatalogUser viewer = catalogUserRepository.save(
                new CatalogUser("stray-viewer@example.com", "Stray Viewer", Role.VIEWER));
        Department department = departmentRepository.save(new Department("Stray " + UUID.randomUUID()));
        editPermissionRepository.save(
                new EditPermission(EditableRecordType.DEPARTMENT, department.getId(), viewer.getId()));

        boolean canEdit = editPermissionService.canEdit(
                EditableRecordType.DEPARTMENT, department.getId(), principal("stray-viewer@example.com", "ROLE_VIEWER"));

        Assertions.assertThat(canEdit).isFalse();
    }

    @Test
    void editorWithGrantCanEdit() {
        CatalogUser editor = catalogUserRepository.save(
                new CatalogUser("grant-editor@example.com", "Grant Editor", Role.EDITOR));
        Department department = departmentRepository.save(new Department("EdGrant " + UUID.randomUUID()));
        editPermissionRepository.save(
                new EditPermission(EditableRecordType.DEPARTMENT, department.getId(), editor.getId()));

        boolean canEdit = editPermissionService.canEdit(
                EditableRecordType.DEPARTMENT, department.getId(), principal("grant-editor@example.com", "ROLE_EDITOR"));

        Assertions.assertThat(canEdit).isTrue();
    }

    @Test
    void editorWithoutGrantCannotEdit() {
        catalogUserRepository.save(new CatalogUser("nogrant-editor@example.com", "NoGrant Editor", Role.EDITOR));
        Department department = departmentRepository.save(new Department("NoGrant " + UUID.randomUUID()));

        boolean canEdit = editPermissionService.canEdit(
                EditableRecordType.DEPARTMENT,
                department.getId(),
                principal("nogrant-editor@example.com", "ROLE_EDITOR"));

        Assertions.assertThat(canEdit).isFalse();
    }

    @Test
    void adminCanEditWithoutGrant() {
        Department department = departmentRepository.save(new Department("AdminNoGrant " + UUID.randomUUID()));

        boolean canEdit = editPermissionService.canEdit(
                EditableRecordType.DEPARTMENT, department.getId(), principal("admin@example.com", "ROLE_ADMIN"));

        Assertions.assertThat(canEdit).isTrue();
    }

    @Test
    void nullPrincipalCannotEdit() {
        Department department = departmentRepository.save(new Department("NullPrincipal " + UUID.randomUUID()));

        Assertions.assertThat(
                        editPermissionService.canEdit(EditableRecordType.DEPARTMENT, department.getId(), null))
                .isFalse();
    }
}

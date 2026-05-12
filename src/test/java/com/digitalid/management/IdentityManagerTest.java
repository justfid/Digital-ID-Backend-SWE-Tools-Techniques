package com.digitalid.management;

import com.digitalid.audit.AuditLogger;
import com.digitalid.audit.InMemoryAuditLogger;
import com.digitalid.auth.AuthorisationManager;
import com.digitalid.auth.AuthorisationManagerImpl;
import com.digitalid.model.DigitalId;
import com.digitalid.model.DigitalIdStatus;
import com.digitalid.model.OrganisationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IdentityManagerTest {

    private IdentityManager manager;
    private DigitalId activeId;

    @BeforeEach
    void setUp() {
        AuthorisationManager authManager = new AuthorisationManagerImpl();
        AuditLogger auditLogger = new InMemoryAuditLogger();
        manager = new IdentityManagerImpl(OrganisationType.CENTRAL_AUTHORITY, authManager, auditLogger);
        activeId = manager.createIdentity("NID123456", LocalDate.of(1990, 6, 15), "Jane Doe", null, null);
    }

    @Test
    void should_returnActiveStatus_when_identityCreated() {
        assertEquals(DigitalIdStatus.ACTIVE, activeId.getStatus());
    }

    @Test
    void should_throwWithDescriptiveMessage_when_nationalIdNumberIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                manager.createIdentity(null, LocalDate.of(1990, 1, 1), "John Smith", null, null));

        assertTrue(ex.getMessage().contains("nationalIdNumber"));
    }

    @Test
    void should_throwWithDescriptiveMessage_when_nationalIdNumberIsBlank() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                manager.createIdentity("   ", LocalDate.of(1990, 1, 1), "John Smith", null, null));

        assertTrue(ex.getMessage().contains("nationalIdNumber"));
    }

    @Test
    void should_throwWithDescriptiveMessage_when_dateOfBirthIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                manager.createIdentity("NID999", null, "John Smith", null, null));

        assertTrue(ex.getMessage().contains("dateOfBirth"));
    }

    @Test
    void should_throwWithDescriptiveMessage_when_fullNameIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                manager.createIdentity("NID999", LocalDate.of(1990, 1, 1), null, null, null));

        assertTrue(ex.getMessage().contains("fullName"));
    }

    @Test
    void should_throwWithDescriptiveMessage_when_fullNameIsBlank() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                manager.createIdentity("NID999", LocalDate.of(1990, 1, 1), "   ", null, null));

        assertTrue(ex.getMessage().contains("fullName"));
    }

    @Test
    void should_updateAddress_when_idIsActive() {
        DigitalId result = manager.updateAddress(activeId.getId(), "123 Main St");

        assertEquals("123 Main St", result.getAddress());
    }

    @Test
    void should_throwWithDescriptiveMessage_when_updatingAddressOnRevokedId() {
        manager.revoke(activeId.getId(), "test revoke");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                manager.updateAddress(activeId.getId(), "123 Main St"));

        assertTrue(ex.getMessage().contains("REVOKED"));
    }

    @Test
    void should_throwWithDescriptiveMessage_when_updatingAddressForUnknownId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                manager.updateAddress(UUID.randomUUID(), "123 Main St"));

        assertTrue(ex.getMessage().contains("No Digital ID found"));
    }

    @Test
    void should_updateEmail_when_idIsActive() {
        DigitalId result = manager.updateEmail(activeId.getId(), "jane@example.com");

        assertEquals("jane@example.com", result.getEmail());
    }

    @Test
    void should_throwWithDescriptiveMessage_when_updatingEmailOnRevokedId() {
        manager.revoke(activeId.getId(), "test revoke");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                manager.updateEmail(activeId.getId(), "jane@example.com"));

        assertTrue(ex.getMessage().contains("REVOKED"));
    }

    @Test
    void should_updateTemporaryRestriction_when_idIsActive() {
        DigitalId result = manager.updateTemporaryRestriction(activeId.getId(), true);

        assertTrue(result.isTemporaryRestriction());
    }

    @Test
    void should_throwWithDescriptiveMessage_when_updatingRestrictionOnRevokedId() {
        manager.revoke(activeId.getId(), "test revoke");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                manager.updateTemporaryRestriction(activeId.getId(), true));

        assertTrue(ex.getMessage().contains("REVOKED"));
    }

    @Test
    void should_changeStatusToSuspended_when_suspended() {
        manager.suspend(activeId.getId(), "Under investigation");

        assertEquals(DigitalIdStatus.SUSPENDED, manager.findById(activeId.getId()).getStatus());
    }

    @Test
    void should_throwWithDescriptiveMessage_when_suspendingAlreadySuspendedId() {
        manager.suspend(activeId.getId(), "first suspension");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                manager.suspend(activeId.getId(), "second suspension"));

        assertTrue(ex.getMessage().contains("SUSPENDED"));
    }

    @Test
    void should_throwWithDescriptiveMessage_when_suspendingRevokedId() {
        manager.revoke(activeId.getId(), "test revoke");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                manager.suspend(activeId.getId(), "attempt suspend"));

        assertTrue(ex.getMessage().contains("REVOKED"));
    }

    @Test
    void should_changeStatusToActive_when_reactivatedFromSuspended() {
        manager.suspend(activeId.getId(), "temp suspension");
        manager.reactivate(activeId.getId(), "cleared");

        assertEquals(DigitalIdStatus.ACTIVE, manager.findById(activeId.getId()).getStatus());
    }

    @Test
    void should_throwWithDescriptiveMessage_when_reactivatingActiveId() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                manager.reactivate(activeId.getId(), "already active"));

        assertTrue(ex.getMessage().contains("ACTIVE"));
    }

    @Test
    void should_throwWithDescriptiveMessage_when_reactivatingRevokedId() {
        manager.revoke(activeId.getId(), "test revoke");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                manager.reactivate(activeId.getId(), "attempt reactivate"));

        assertTrue(ex.getMessage().contains("REVOKED"));
    }

    @Test
    void should_changeStatusToRevoked_when_revoked() {
        manager.revoke(activeId.getId(), "fraud");

        assertEquals(DigitalIdStatus.REVOKED, manager.findById(activeId.getId()).getStatus());
    }

    @Test
    void should_throwWithDescriptiveMessage_when_revokingAlreadyRevokedId() {
        manager.revoke(activeId.getId(), "first revoke");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                manager.revoke(activeId.getId(), "second revoke"));

        assertTrue(ex.getMessage().contains("REVOKED"));
    }

    @Test
    void should_returnCorrectDigitalId_when_findById() {
        DigitalId found = manager.findById(activeId.getId());

        assertEquals(activeId.getId(), found.getId());
    }

    @Test
    void should_throwWithDescriptiveMessage_when_findByIdForUnknownId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                manager.findById(UUID.randomUUID()));

        assertTrue(ex.getMessage().contains("No Digital ID found"));
    }
}
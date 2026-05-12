package com.digitalid.management;

import com.digitalid.audit.AuditLogger;
import com.digitalid.audit.InMemoryAuditLogger;
import com.digitalid.auth.AuthorisationManager;
import com.digitalid.auth.AuthorisationManagerImpl;
import com.digitalid.model.DigitalId;
import com.digitalid.model.DigitalIdStatus;
import com.digitalid.model.OrganisationType;
import com.digitalid.model.Result;
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
        manager = new IdentityManagerImpl(OrganisationType.CENTRAL_AUTHORITY, new InMemoryDigitalIdRepository(), authManager, auditLogger);
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
        Result<DigitalId> result = manager.updateAddress(activeId.getId(), "123 Main St");

        assertTrue(result.isSuccess());
        assertEquals("123 Main St", result.getValue().getAddress());
    }

    @Test
    void should_returnFailureResult_when_updatingAddressOnRevokedId() {
        manager.revoke(activeId.getId(), "test revoke");

        Result<DigitalId> result = manager.updateAddress(activeId.getId(), "123 Main St");

        assertFalse(result.isSuccess());
        assertTrue(result.getFailureReason().contains("REVOKED"));
    }

    @Test
    void should_throwWithDescriptiveMessage_when_updatingAddressForUnknownId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                manager.updateAddress(UUID.randomUUID(), "123 Main St"));

        assertTrue(ex.getMessage().contains("No Digital ID found"));
    }

    @Test
    void should_updateEmail_when_idIsActive() {
        Result<DigitalId> result = manager.updateEmail(activeId.getId(), "jane@example.com");

        assertTrue(result.isSuccess());
        assertEquals("jane@example.com", result.getValue().getEmail());
    }

    @Test
    void should_returnFailureResult_when_updatingEmailOnRevokedId() {
        manager.revoke(activeId.getId(), "test revoke");

        Result<DigitalId> result = manager.updateEmail(activeId.getId(), "jane@example.com");

        assertFalse(result.isSuccess());
        assertTrue(result.getFailureReason().contains("REVOKED"));
    }

    @Test
    void should_updateTemporaryRestriction_when_idIsActive() {
        Result<DigitalId> result = manager.updateTemporaryRestriction(activeId.getId(), true);

        assertTrue(result.isSuccess());
        assertTrue(result.getValue().isTemporaryRestriction());
    }

    @Test
    void should_returnFailureResult_when_updatingRestrictionOnRevokedId() {
        manager.revoke(activeId.getId(), "test revoke");

        Result<DigitalId> result = manager.updateTemporaryRestriction(activeId.getId(), true);

        assertFalse(result.isSuccess());
        assertTrue(result.getFailureReason().contains("REVOKED"));
    }

    @Test
    void should_changeStatusToSuspended_when_suspended() {
        Result<DigitalId> result = manager.suspend(activeId.getId(), "Under investigation");

        assertTrue(result.isSuccess());
        assertEquals(DigitalIdStatus.SUSPENDED, result.getValue().getStatus());
    }

    @Test
    void should_returnFailureResult_when_suspendingAlreadySuspendedId() {
        manager.suspend(activeId.getId(), "first suspension");

        Result<DigitalId> result = manager.suspend(activeId.getId(), "second suspension");

        assertFalse(result.isSuccess());
        assertTrue(result.getFailureReason().contains("SUSPENDED"));
    }

    @Test
    void should_returnFailureResult_when_suspendingRevokedId() {
        manager.revoke(activeId.getId(), "test revoke");

        Result<DigitalId> result = manager.suspend(activeId.getId(), "attempt suspend");

        assertFalse(result.isSuccess());
        assertTrue(result.getFailureReason().contains("REVOKED"));
    }

    @Test
    void should_changeStatusToActive_when_reactivatedFromSuspended() {
        manager.suspend(activeId.getId(), "temp suspension");

        Result<DigitalId> result = manager.reactivate(activeId.getId(), "cleared");

        assertTrue(result.isSuccess());
        assertEquals(DigitalIdStatus.ACTIVE, result.getValue().getStatus());
    }

    @Test
    void should_returnFailureResult_when_reactivatingActiveId() {
        Result<DigitalId> result = manager.reactivate(activeId.getId(), "already active");

        assertFalse(result.isSuccess());
        assertTrue(result.getFailureReason().contains("ACTIVE"));
    }

    @Test
    void should_returnFailureResult_when_reactivatingRevokedId() {
        manager.revoke(activeId.getId(), "test revoke");

        Result<DigitalId> result = manager.reactivate(activeId.getId(), "attempt reactivate");

        assertFalse(result.isSuccess());
        assertTrue(result.getFailureReason().contains("REVOKED"));
    }

    @Test
    void should_changeStatusToRevoked_when_revoked() {
        Result<DigitalId> result = manager.revoke(activeId.getId(), "fraud");

        assertTrue(result.isSuccess());
        assertEquals(DigitalIdStatus.REVOKED, result.getValue().getStatus());
    }

    @Test
    void should_returnFailureResult_when_revokingAlreadyRevokedId() {
        manager.revoke(activeId.getId(), "first revoke");

        Result<DigitalId> result = manager.revoke(activeId.getId(), "second revoke");

        assertFalse(result.isSuccess());
        assertTrue(result.getFailureReason().contains("REVOKED"));
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

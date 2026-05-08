package com.digitalid.management;

import com.digitalid.model.DigitalId;
import com.digitalid.model.DigitalIdStatus;
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
        manager = new IdentityManagerImpl();
        activeId = manager.createIdentity("NID123456", LocalDate.of(1990, 6, 15), "Jane Doe", null, null);
    }

    @Test
    void createIdentityReturnsDigitalIdWithActiveStatus() {
        assertEquals(DigitalIdStatus.ACTIVE, activeId.getStatus());
    }

    @Test
    void createIdentityThrowsIfNationalIdNumberIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.createIdentity(null, LocalDate.of(1990, 1, 1), "John Smith", null, null));
    }

    @Test
    void createIdentityThrowsIfNationalIdNumberIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.createIdentity("   ", LocalDate.of(1990, 1, 1), "John Smith", null, null));
    }

    @Test
    void createIdentityThrowsIfDateOfBirthIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.createIdentity("NID999", null, "John Smith", null, null));
    }

    @Test
    void createIdentityThrowsIfFullNameIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.createIdentity("NID999", LocalDate.of(1990, 1, 1), null, null, null));
    }

    @Test
    void createIdentityThrowsIfFullNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.createIdentity("NID999", LocalDate.of(1990, 1, 1), "   ", null, null));
    }

    @Test
    void updateAddressSucceedsOnActiveId() {
        DigitalId result = manager.updateAddress(activeId.getId(), "123 Main St");
        assertEquals("123 Main St", result.getAddress());
    }

    @Test
    void updateAddressThrowsIfRevoked() {
        manager.revoke(activeId.getId(), "test revoke");
        assertThrows(IllegalStateException.class, () ->
                manager.updateAddress(activeId.getId(), "123 Main St"));
    }

    @Test
    void updateAddressThrowsIfIdDoesNotExist() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.updateAddress(UUID.randomUUID(), "123 Main St"));
    }

    @Test
    void updateEmailSucceedsOnActiveId() {
        DigitalId result = manager.updateEmail(activeId.getId(), "jane@example.com");
        assertEquals("jane@example.com", result.getEmail());
    }

    @Test
    void updateEmailThrowsIfRevoked() {
        manager.revoke(activeId.getId(), "test revoke");
        assertThrows(IllegalStateException.class, () ->
                manager.updateEmail(activeId.getId(), "jane@example.com"));
    }

    @Test
    void updateTemporaryRestrictionSucceedsOnActiveId() {
        DigitalId result = manager.updateTemporaryRestriction(activeId.getId(), true);
        assertTrue(result.isTemporaryRestriction());
    }

    @Test
    void updateTemporaryRestrictionThrowsIfRevoked() {
        manager.revoke(activeId.getId(), "test revoke");
        assertThrows(IllegalStateException.class, () ->
                manager.updateTemporaryRestriction(activeId.getId(), true));
    }

    @Test
    void suspendChangesStatusToSuspended() {
        manager.suspend(activeId.getId(), "Under investigation");
        assertEquals(DigitalIdStatus.SUSPENDED, manager.findById(activeId.getId()).getStatus());
    }

    @Test
    void suspendThrowsIfAlreadySuspended() {
        manager.suspend(activeId.getId(), "first suspension");
        assertThrows(IllegalStateException.class, () ->
                manager.suspend(activeId.getId(), "second suspension"));
    }

    @Test
    void suspendThrowsIfRevoked() {
        manager.revoke(activeId.getId(), "test revoke");
        assertThrows(IllegalStateException.class, () ->
                manager.suspend(activeId.getId(), "attempt suspend"));
    }

    @Test
    void reactivateChangesStatusFromSuspendedToActive() {
        manager.suspend(activeId.getId(), "temp suspension");
        manager.reactivate(activeId.getId(), "cleared");
        assertEquals(DigitalIdStatus.ACTIVE, manager.findById(activeId.getId()).getStatus());
    }

    @Test
    void reactivateThrowsIfAlreadyActive() {
        assertThrows(IllegalStateException.class, () ->
                manager.reactivate(activeId.getId(), "already active"));
    }

    @Test
    void reactivateThrowsIfRevoked() {
        manager.revoke(activeId.getId(), "test revoke");
        assertThrows(IllegalStateException.class, () ->
                manager.reactivate(activeId.getId(), "attempt reactivate"));
    }

    @Test
    void revokeChangesStatusToRevoked() {
        manager.revoke(activeId.getId(), "fraud");
        assertEquals(DigitalIdStatus.REVOKED, manager.findById(activeId.getId()).getStatus());
    }

    @Test
    void revokeThrowsIfAlreadyRevoked() {
        manager.revoke(activeId.getId(), "first revoke");
        assertThrows(IllegalStateException.class, () ->
                manager.revoke(activeId.getId(), "second revoke"));
    }

    @Test
    void findByIdReturnsCorrectDigitalId() {
        DigitalId found = manager.findById(activeId.getId());
        assertEquals(activeId.getId(), found.getId());
    }

    @Test
    void findByIdThrowsIfIdDoesNotExist() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.findById(UUID.randomUUID()));
    }
}
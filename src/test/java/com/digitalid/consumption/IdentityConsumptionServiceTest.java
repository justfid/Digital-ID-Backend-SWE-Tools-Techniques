package com.digitalid.consumption;

import com.digitalid.management.IdentityManager;
import com.digitalid.management.IdentityManagerImpl;
import com.digitalid.model.DigitalId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IdentityConsumptionServiceTest {

    private IdentityManager manager;
    private IdentityConsumptionService service;
    private DigitalId activeId;

    @BeforeEach
    void setUp() {
        manager = new IdentityManagerImpl();
        service = new IdentityConsumptionServiceImpl(manager);
        activeId = manager.createIdentity("NID123456", LocalDate.of(1990, 6, 15), "Jane Doe", null, null);
    }

    @Test
    void checkValidityReturnsTrueForActiveId() {
        VerificationResponse response = service.checkValidity(activeId.getId());
        assertTrue(response.isValid());
    }

    @Test
    void checkValidityReturnsFalseForSuspendedId() {
        manager.suspend(activeId.getId(), "investigation");
        VerificationResponse response = service.checkValidity(activeId.getId());
        assertFalse(response.isValid());
    }

    @Test
    void checkValidityReturnsFalseForRevokedId() {
        manager.revoke(activeId.getId(), "fraud");
        VerificationResponse response = service.checkValidity(activeId.getId());
        assertFalse(response.isValid());
    }

    @Test
    void checkValidityReturnsFalseForNonExistentId() {
        VerificationResponse response = service.checkValidity(UUID.randomUUID());
        assertFalse(response.isValid());
    }

    @Test
    void checkTaxEligibilityReturnsTrueForActiveIdWithNoSuspensionsInPeriod() {
        LocalDate periodStart = LocalDate.now().minusDays(30);
        LocalDate periodEnd = LocalDate.now().plusDays(30);
        VerificationResponse response = service.checkTaxEligibility(activeId.getId(), periodStart, periodEnd);
        assertTrue(response.isValid());
    }

    @Test
    void checkTaxEligibilityReturnsFalseForNonExistentId() {
        LocalDate periodStart = LocalDate.now().minusDays(30);
        LocalDate periodEnd = LocalDate.now().plusDays(30);
        VerificationResponse response = service.checkTaxEligibility(UUID.randomUUID(), periodStart, periodEnd);
        assertFalse(response.isValid());
    }

    @Test
    void checkTaxEligibilityReturnsFalseIfStatusIsNotActive() {
        manager.suspend(activeId.getId(), "investigation");
        LocalDate periodStart = LocalDate.now().minusDays(30);
        LocalDate periodEnd = LocalDate.now().plusDays(30);
        VerificationResponse response = service.checkTaxEligibility(activeId.getId(), periodStart, periodEnd);
        assertFalse(response.isValid());
    }

    @Test
    void checkTaxEligibilityReturnsFalseIfSuspendedDuringPeriod() {
        // Suspend then reactivate so current status is ACTIVE, but the suspension entry timestamp is today
        manager.suspend(activeId.getId(), "temp suspension");
        manager.reactivate(activeId.getId(), "cleared");
        // Period straddles today so the suspension timestamp falls within it
        LocalDate periodStart = LocalDate.now().minusDays(1);
        LocalDate periodEnd = LocalDate.now().plusDays(1);
        VerificationResponse response = service.checkTaxEligibility(activeId.getId(), periodStart, periodEnd);
        assertFalse(response.isValid());
    }

    @Test
    void checkTaxEligibilityReturnsTrueIfSuspendedOutsidePeriod() {
        // Suspend then reactivate so current status is ACTIVE, but the suspension entry timestamp is today
        manager.suspend(activeId.getId(), "temp suspension");
        manager.reactivate(activeId.getId(), "cleared");
        // Period is entirely in the future so the suspension (which happened now) falls outside it
        LocalDate periodStart = LocalDate.now().plusDays(30);
        LocalDate periodEnd = LocalDate.now().plusDays(60);
        VerificationResponse response = service.checkTaxEligibility(activeId.getId(), periodStart, periodEnd);
        assertTrue(response.isValid());
    }

    @Test
    void checkLicenceEligibilityReturnsTrueForActiveIdWithNoRestriction() {
        VerificationResponse response = service.checkLicenceEligibility(activeId.getId());
        assertTrue(response.isValid());
    }

    @Test
    void checkLicenceEligibilityReturnsFalseForNonExistentId() {
        VerificationResponse response = service.checkLicenceEligibility(UUID.randomUUID());
        assertFalse(response.isValid());
    }

    @Test
    void checkLicenceEligibilityReturnsFalseIfStatusIsNotActive() {
        manager.suspend(activeId.getId(), "investigation");
        VerificationResponse response = service.checkLicenceEligibility(activeId.getId());
        assertFalse(response.isValid());
    }

    @Test
    void checkLicenceEligibilityReturnsFalseIfTemporaryRestrictionIsTrue() {
        manager.updateTemporaryRestriction(activeId.getId(), true);
        VerificationResponse response = service.checkLicenceEligibility(activeId.getId());
        assertFalse(response.isValid());
    }

    @Test
    void checkLicenceEligibilityReturnsTrueAfterTemporaryRestrictionIsLifted() {
        manager.updateTemporaryRestriction(activeId.getId(), true);
        manager.updateTemporaryRestriction(activeId.getId(), false);
        VerificationResponse response = service.checkLicenceEligibility(activeId.getId());
        assertTrue(response.isValid());
    }
}
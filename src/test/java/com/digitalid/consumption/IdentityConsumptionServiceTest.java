package com.digitalid.consumption;

import com.digitalid.audit.InMemoryAuditLogger;
import com.digitalid.auth.AuthorisationManagerImpl;
import com.digitalid.management.IdentityManager;
import com.digitalid.management.IdentityManagerImpl;
import com.digitalid.model.DigitalId;
import com.digitalid.model.OrganisationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IdentityConsumptionServiceTest {

    private IdentityManager manager;

    // Each consuming organisation type has its own service instance
    private IdentityConsumptionService validityService;
    private IdentityConsumptionService taxService;
    private IdentityConsumptionService licenceService;

    private DigitalId activeId;

    @BeforeEach
    void setUp() {
        AuthorisationManagerImpl authManager = new AuthorisationManagerImpl();
        manager = new IdentityManagerImpl(OrganisationType.CENTRAL_AUTHORITY, authManager, new InMemoryAuditLogger());

        validityService = new IdentityConsumptionServiceImpl(manager, OrganisationType.EMPLOYER, authManager, new InMemoryAuditLogger());
        taxService = new IdentityConsumptionServiceImpl(manager, OrganisationType.TAX_AUTHORITY, authManager, new InMemoryAuditLogger());
        licenceService = new IdentityConsumptionServiceImpl(manager, OrganisationType.DRIVING_LICENCE_AUTHORITY, authManager, new InMemoryAuditLogger());

        activeId = manager.createIdentity("NID123456", LocalDate.of(1990, 6, 15), "Jane Doe", null, null);
    }

    @Test
    void checkValidityReturnsTrueForActiveId() {
        VerificationResponse response = validityService.checkValidity(activeId.getId());
        assertTrue(response.isValid());
    }

    @Test
    void checkValidityReturnsFalseForSuspendedId() {
        manager.suspend(activeId.getId(), "investigation");
        VerificationResponse response = validityService.checkValidity(activeId.getId());
        assertFalse(response.isValid());
    }

    @Test
    void checkValidityReturnsFalseForRevokedId() {
        manager.revoke(activeId.getId(), "fraud");
        VerificationResponse response = validityService.checkValidity(activeId.getId());
        assertFalse(response.isValid());
    }

    @Test
    void checkValidityReturnsFalseForNonExistentId() {
        VerificationResponse response = validityService.checkValidity(UUID.randomUUID());
        assertFalse(response.isValid());
    }

    @Test
    void checkTaxEligibilityReturnsTrueForActiveIdWithNoSuspensionsInPeriod() {
        LocalDate periodStart = LocalDate.now().minusDays(30);
        LocalDate periodEnd = LocalDate.now().plusDays(30);
        VerificationResponse response = taxService.checkTaxEligibility(activeId.getId(), periodStart, periodEnd);
        assertTrue(response.isValid());
    }

    @Test
    void checkTaxEligibilityReturnsFalseForNonExistentId() {
        LocalDate periodStart = LocalDate.now().minusDays(30);
        LocalDate periodEnd = LocalDate.now().plusDays(30);
        VerificationResponse response = taxService.checkTaxEligibility(UUID.randomUUID(), periodStart, periodEnd);
        assertFalse(response.isValid());
    }

    @Test
    void checkTaxEligibilityReturnsFalseIfStatusIsNotActive() {
        manager.suspend(activeId.getId(), "investigation");
        LocalDate periodStart = LocalDate.now().minusDays(30);
        LocalDate periodEnd = LocalDate.now().plusDays(30);
        VerificationResponse response = taxService.checkTaxEligibility(activeId.getId(), periodStart, periodEnd);
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
        VerificationResponse response = taxService.checkTaxEligibility(activeId.getId(), periodStart, periodEnd);
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
        VerificationResponse response = taxService.checkTaxEligibility(activeId.getId(), periodStart, periodEnd);
        assertTrue(response.isValid());
    }

    @Test
    void checkLicenceEligibilityReturnsTrueForActiveIdWithNoRestriction() {
        VerificationResponse response = licenceService.checkLicenceEligibility(activeId.getId());
        assertTrue(response.isValid());
    }

    @Test
    void checkLicenceEligibilityReturnsFalseForNonExistentId() {
        VerificationResponse response = licenceService.checkLicenceEligibility(UUID.randomUUID());
        assertFalse(response.isValid());
    }

    @Test
    void checkLicenceEligibilityReturnsFalseIfStatusIsNotActive() {
        manager.suspend(activeId.getId(), "investigation");
        VerificationResponse response = licenceService.checkLicenceEligibility(activeId.getId());
        assertFalse(response.isValid());
    }

    @Test
    void checkLicenceEligibilityReturnsFalseIfTemporaryRestrictionIsTrue() {
        manager.updateTemporaryRestriction(activeId.getId(), true);
        VerificationResponse response = licenceService.checkLicenceEligibility(activeId.getId());
        assertFalse(response.isValid());
    }

    @Test
    void checkLicenceEligibilityReturnsTrueAfterTemporaryRestrictionIsLifted() {
        manager.updateTemporaryRestriction(activeId.getId(), true);
        manager.updateTemporaryRestriction(activeId.getId(), false);
        VerificationResponse response = licenceService.checkLicenceEligibility(activeId.getId());
        assertTrue(response.isValid());
    }
}
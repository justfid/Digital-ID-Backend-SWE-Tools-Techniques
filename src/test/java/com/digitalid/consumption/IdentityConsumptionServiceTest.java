package com.digitalid.consumption;

import com.digitalid.audit.InMemoryAuditLogger;
import com.digitalid.auth.AuthorisationManagerImpl;
import com.digitalid.management.IdentityManager;
import com.digitalid.management.IdentityManagerImpl;
import com.digitalid.management.InMemoryDigitalIdRepository;
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
        manager = new IdentityManagerImpl(OrganisationType.CENTRAL_AUTHORITY, new InMemoryDigitalIdRepository(), authManager, new InMemoryAuditLogger());

        validityService = new IdentityConsumptionServiceImpl(manager, OrganisationType.EMPLOYER, authManager, new InMemoryAuditLogger());
        taxService = new IdentityConsumptionServiceImpl(manager, OrganisationType.TAX_AUTHORITY, authManager, new InMemoryAuditLogger());
        licenceService = new IdentityConsumptionServiceImpl(manager, OrganisationType.DRIVING_LICENCE_AUTHORITY, authManager, new InMemoryAuditLogger());

        activeId = manager.createIdentity("NID123456", LocalDate.of(1990, 6, 15), "Jane Doe", null, null);
    }

    @Test
    void should_returnValidResponse_when_idIsActive() {
        VerificationResponse response = validityService.checkValidity(activeId.getId());

        assertTrue(response.isValid());
    }

    @Test
    void should_returnInvalidResponse_when_idIsSuspended() {
        manager.suspend(activeId.getId(), "investigation");

        VerificationResponse response = validityService.checkValidity(activeId.getId());

        assertFalse(response.isValid());
    }

    @Test
    void should_returnInvalidResponse_when_idIsRevoked() {
        manager.revoke(activeId.getId(), "fraud");

        VerificationResponse response = validityService.checkValidity(activeId.getId());

        assertFalse(response.isValid());
    }

    @Test
    void should_returnInvalidResponse_when_checkValidityCalledForNonExistentId() {
        VerificationResponse response = validityService.checkValidity(UUID.randomUUID());

        assertFalse(response.isValid());
    }

    @Test
    void should_returnValidResponse_when_taxEligibilityCheckedForActiveIdWithNoSuspensionsInPeriod() {
        LocalDate periodStart = LocalDate.now().minusDays(30);
        LocalDate periodEnd = LocalDate.now().plusDays(30);

        VerificationResponse response = taxService.checkTaxEligibility(activeId.getId(), periodStart, periodEnd);

        assertTrue(response.isValid());
    }

    @Test
    void should_returnInvalidResponse_when_taxEligibilityCheckedForNonExistentId() {
        LocalDate periodStart = LocalDate.now().minusDays(30);
        LocalDate periodEnd = LocalDate.now().plusDays(30);

        VerificationResponse response = taxService.checkTaxEligibility(UUID.randomUUID(), periodStart, periodEnd);

        assertFalse(response.isValid());
    }

    @Test
    void should_returnInvalidResponse_when_taxEligibilityCheckedForCurrentlySuspendedId() {
        manager.suspend(activeId.getId(), "investigation");
        LocalDate periodStart = LocalDate.now().minusDays(30);
        LocalDate periodEnd = LocalDate.now().plusDays(30);

        VerificationResponse response = taxService.checkTaxEligibility(activeId.getId(), periodStart, periodEnd);

        assertFalse(response.isValid());
    }

    @Test
    void should_returnInvalidResponse_when_suspensionOccurredWithinTaxPeriod() {
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
    void should_returnValidResponse_when_suspensionOccurredOutsideTaxPeriod() {
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
    void should_returnValidResponse_when_idIsActiveWithNoTemporaryRestriction() {
        VerificationResponse response = licenceService.checkLicenceEligibility(activeId.getId());
        assertTrue(response.isValid());
    }

    @Test
    void should_returnInvalidResponse_when_licenceEligibilityCheckedForNonExistentId() {
        VerificationResponse response = licenceService.checkLicenceEligibility(UUID.randomUUID());
        assertFalse(response.isValid());
    }

    @Test
    void should_returnInvalidResponse_when_licenceEligibilityCheckedForSuspendedId() {
        manager.suspend(activeId.getId(), "investigation");
        VerificationResponse response = licenceService.checkLicenceEligibility(activeId.getId());
        assertFalse(response.isValid());
    }

    @Test
    void should_returnInvalidResponse_when_temporaryRestrictionIsSet() {
        manager.updateTemporaryRestriction(activeId.getId(), true);
        VerificationResponse response = licenceService.checkLicenceEligibility(activeId.getId());
        assertFalse(response.isValid());
    }

    @Test
    void should_returnValidResponse_when_temporaryRestrictionIsLifted() {
        manager.updateTemporaryRestriction(activeId.getId(), true);
        manager.updateTemporaryRestriction(activeId.getId(), false);
        VerificationResponse response = licenceService.checkLicenceEligibility(activeId.getId());
        assertTrue(response.isValid());
    }

    @Test
    void should_returnInvalidResponse_when_identityWasSuspendedBeforePeriodAndReactivatedDuringIt() {
        // Edge case: suspension predates the period start, reactivation occurs during the period.
        manager.suspend(activeId.getId(), "suspended before period");
        manager.reactivate(activeId.getId(), "reactivated during period");

        // periodStart is tomorrow so both events above (timestamped today) predate the period
        LocalDate periodStart = LocalDate.now().plusDays(1);
        LocalDate periodEnd = LocalDate.now().plusDays(30);

        VerificationResponse response = taxService.checkTaxEligibility(activeId.getId(), periodStart, periodEnd);

        assertFalse(response.isValid());
    }
}
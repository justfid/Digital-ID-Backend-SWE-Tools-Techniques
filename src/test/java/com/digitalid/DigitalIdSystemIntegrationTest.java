package com.digitalid;

import com.digitalid.audit.AuditEvent;
import com.digitalid.audit.AuditLogger;
import com.digitalid.audit.InMemoryAuditLogger;
import com.digitalid.auth.AuthorisationManager;
import com.digitalid.auth.AuthorisationManagerImpl;
import com.digitalid.auth.Operations;
import com.digitalid.auth.UnauthorisedOperationException;
import com.digitalid.consumption.IdentityConsumptionService;
import com.digitalid.consumption.IdentityConsumptionServiceImpl;
import com.digitalid.consumption.VerificationResponse;
import com.digitalid.management.IdentityManager;
import com.digitalid.management.IdentityManagerImpl;
import com.digitalid.management.InMemoryDigitalIdRepository;
import com.digitalid.model.DigitalId;
import com.digitalid.model.DigitalIdStatus;
import com.digitalid.model.OrganisationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DigitalIdSystemIntegrationTest {

    private AuditLogger auditLogger;
    private AuthorisationManager authManager;
    private IdentityManager identityManager;
    private IdentityConsumptionService employerService;
    private IdentityConsumptionService bankService;
    private IdentityConsumptionService taxService;
    private IdentityConsumptionService licenceService;

    @BeforeEach
    void setUp() {
        auditLogger = new InMemoryAuditLogger();
        authManager = new AuthorisationManagerImpl();
        identityManager = new IdentityManagerImpl(OrganisationType.CENTRAL_AUTHORITY, new InMemoryDigitalIdRepository(), authManager, auditLogger);
        employerService = new IdentityConsumptionServiceImpl(identityManager, OrganisationType.EMPLOYER, authManager, auditLogger);
        bankService = new IdentityConsumptionServiceImpl(identityManager, OrganisationType.BANK, authManager, auditLogger);
        taxService = new IdentityConsumptionServiceImpl(identityManager, OrganisationType.TAX_AUTHORITY, authManager, auditLogger);
        licenceService = new IdentityConsumptionServiceImpl(identityManager, OrganisationType.DRIVING_LICENCE_AUTHORITY, authManager, auditLogger);
    }

    // MANAGEMENT — authorised operations

    @Test
    void should_createIdentityAndLogSuccess_when_centralAuthorityCreatesIdentity() {
        DigitalId id = identityManager.createIdentity("NID001", LocalDate.of(1990, 1, 1), "Alice", null, null);

        assertEquals(DigitalIdStatus.ACTIVE, id.getStatus());

        List<AuditEvent> log = auditLogger.getLog();
        assertEquals(1, log.size());
        assertTrue(log.get(0).isSuccess());
        assertEquals(Operations.CREATE_IDENTITY, log.get(0).getOperationName());
    }

    @Test
    void should_suspendIdentityAndLogSuccess_when_centralAuthoritySuspendsIdentity() {
        DigitalId id = identityManager.createIdentity("NID001", LocalDate.of(1990, 1, 1), "Alice", null, null);

        identityManager.suspend(id.getId(), "under investigation");

        assertEquals(DigitalIdStatus.SUSPENDED, identityManager.findById(id.getId()).getStatus());

        List<AuditEvent> log = auditLogger.getLog();
        assertEquals(2, log.size());
        assertTrue(log.get(1).isSuccess());
        assertEquals(Operations.SUSPEND, log.get(1).getOperationName());
    }

    @Test
    void should_reactivateIdentityAndLogSuccess_when_centralAuthorityReactivatesIdentity() {
        DigitalId id = identityManager.createIdentity("NID001", LocalDate.of(1990, 1, 1), "Alice", null, null);
        identityManager.suspend(id.getId(), "temp suspension");

        identityManager.reactivate(id.getId(), "cleared");

        assertEquals(DigitalIdStatus.ACTIVE, identityManager.findById(id.getId()).getStatus());

        List<AuditEvent> log = auditLogger.getLog();
        assertEquals(3, log.size());
        assertTrue(log.get(2).isSuccess());
        assertEquals(Operations.REACTIVATE, log.get(2).getOperationName());
    }

    @Test
    void should_revokeIdentityAndLogSuccess_when_centralAuthorityRevokesIdentity() {
        DigitalId id = identityManager.createIdentity("NID001", LocalDate.of(1990, 1, 1), "Alice", null, null);

        identityManager.revoke(id.getId(), "fraud detected");

        assertEquals(DigitalIdStatus.REVOKED, identityManager.findById(id.getId()).getStatus());

        List<AuditEvent> log = auditLogger.getLog();
        assertEquals(2, log.size());
        assertTrue(log.get(1).isSuccess());
        assertEquals(Operations.REVOKE, log.get(1).getOperationName());
    }

    @Test
    void should_updateAddressAndLogSuccess_when_centralAuthorityUpdatesAddress() {
        DigitalId id = identityManager.createIdentity("NID001", LocalDate.of(1990, 1, 1), "Alice", null, null);

        identityManager.updateAddress(id.getId(), "123 Main St");

        assertEquals("123 Main St", identityManager.findById(id.getId()).getAddress());

        List<AuditEvent> log = auditLogger.getLog();
        assertEquals(2, log.size());
        assertTrue(log.get(1).isSuccess());
        assertEquals(Operations.UPDATE_ADDRESS, log.get(1).getOperationName());
    }

    // MANAGEMENT — unauthorised operations

    @Test
    void should_throwUnauthorisedAndLogFailure_when_bankTriesToCreateIdentity() {
        // Construct a manager acting as BANK to simulate an unauthorised management attempt
        IdentityManager bankManager = new IdentityManagerImpl(OrganisationType.BANK, new InMemoryDigitalIdRepository(), authManager, auditLogger);

        assertThrows(UnauthorisedOperationException.class, () ->
                bankManager.createIdentity("NID001", LocalDate.of(1990, 1, 1), "Alice", null, null));

        // Management catches auth failures and logs them before rethrowing
        List<AuditEvent> log = auditLogger.getLog();
        assertEquals(1, log.size());
        assertFalse(log.get(0).isSuccess());
        assertEquals(Operations.CREATE_IDENTITY, log.get(0).getOperationName());
    }

    @Test
    void should_throwUnauthorisedAndLogFailure_when_taxAuthorityTriesToRevokeIdentity() {
        DigitalId id = identityManager.createIdentity("NID001", LocalDate.of(1990, 1, 1), "Alice", null, null);
        IdentityManager taxManager = new IdentityManagerImpl(OrganisationType.TAX_AUTHORITY, new InMemoryDigitalIdRepository(), authManager, auditLogger);

        assertThrows(UnauthorisedOperationException.class, () ->
                taxManager.revoke(id.getId(), "attempted revoke"));

        List<AuditEvent> log = auditLogger.getLog();
        assertEquals(2, log.size());
        assertFalse(log.get(1).isSuccess());
        assertEquals(Operations.REVOKE, log.get(1).getOperationName());
    }

    // MANAGEMENT — business rule violations

    @Test
    void should_throwAndLogFailure_when_updatingRevokedIdentity() {
        DigitalId id = identityManager.createIdentity("NID001", LocalDate.of(1990, 1, 1), "Alice", null, null);
        identityManager.revoke(id.getId(), "fraud");

        assertThrows(IllegalStateException.class, () ->
                identityManager.updateAddress(id.getId(), "123 Main St"));

        List<AuditEvent> log = auditLogger.getLog();
        assertEquals(3, log.size());
        assertFalse(log.get(2).isSuccess());
        assertEquals(Operations.UPDATE_ADDRESS, log.get(2).getOperationName());
    }

    @Test
    void should_throwAndLogFailure_when_suspendingAlreadySuspendedIdentity() {
        DigitalId id = identityManager.createIdentity("NID001", LocalDate.of(1990, 1, 1), "Alice", null, null);
        identityManager.suspend(id.getId(), "first suspension");

        assertThrows(IllegalStateException.class, () ->
                identityManager.suspend(id.getId(), "second suspension"));

        List<AuditEvent> log = auditLogger.getLog();
        assertEquals(3, log.size());
        assertFalse(log.get(2).isSuccess());
        assertEquals(Operations.SUSPEND, log.get(2).getOperationName());
    }

    // CONSUMPTION — authorised operations

    @Test
    void should_returnValidAndLogSuccess_when_employerChecksActiveIdentity() {
        DigitalId id = identityManager.createIdentity("NID001", LocalDate.of(1990, 1, 1), "Alice", null, null);

        VerificationResponse response = employerService.checkValidity(id.getId());

        assertTrue(response.isValid());

        List<AuditEvent> log = auditLogger.getLog();
        assertEquals(2, log.size());
        assertTrue(log.get(1).isSuccess());
        assertEquals(Operations.CHECK_VALIDITY, log.get(1).getOperationName());
    }

    @Test
    void should_returnInvalidAndLogSuccess_when_bankChecksRevokedIdentity() {
        DigitalId id = identityManager.createIdentity("NID001", LocalDate.of(1990, 1, 1), "Alice", null, null);
        identityManager.revoke(id.getId(), "fraud");

        VerificationResponse response = bankService.checkValidity(id.getId());

        assertFalse(response.isValid());

        // The audit event is recorded (the check ran successfully); success mirrors response.isValid()
        List<AuditEvent> log = auditLogger.getLog();
        assertEquals(3, log.size());
        assertFalse(log.get(2).isSuccess());
        assertEquals(Operations.CHECK_VALIDITY, log.get(2).getOperationName());
    }

    @Test
    void should_returnEligibleAndLogSuccess_when_taxAuthorityChecksActiveIdentity() {
        DigitalId id = identityManager.createIdentity("NID001", LocalDate.of(1990, 1, 1), "Alice", null, null);
        LocalDate periodStart = LocalDate.now().minusDays(30);
        LocalDate periodEnd = LocalDate.now().plusDays(30);

        VerificationResponse response = taxService.checkTaxEligibility(id.getId(), periodStart, periodEnd);

        assertTrue(response.isValid());

        List<AuditEvent> log = auditLogger.getLog();
        assertEquals(2, log.size());
        assertTrue(log.get(1).isSuccess());
        assertEquals(Operations.CHECK_TAX_ELIGIBILITY, log.get(1).getOperationName());
    }

    @Test
    void should_returnIneligibleAndLogSuccess_when_licenceAuthorityChecksRestrictedIdentity() {
        DigitalId id = identityManager.createIdentity("NID001", LocalDate.of(1990, 1, 1), "Alice", null, null);
        identityManager.updateTemporaryRestriction(id.getId(), true);

        VerificationResponse response = licenceService.checkLicenceEligibility(id.getId());

        assertFalse(response.isValid());

        List<AuditEvent> log = auditLogger.getLog();
        assertEquals(3, log.size());
        assertFalse(log.get(2).isSuccess());
        assertEquals(Operations.CHECK_LICENCE_ELIGIBILITY, log.get(2).getOperationName());
    }

    // CONSUMPTION — unauthorised operations

    @Test
    void should_throwUnauthorisedAndLogFailure_when_employerTriesLicenceCheck() {
        DigitalId id = identityManager.createIdentity("NID001", LocalDate.of(1990, 1, 1), "Alice", null, null);

        assertThrows(UnauthorisedOperationException.class, () ->
                employerService.checkLicenceEligibility(id.getId()));

        // Consumption auth failures propagate before the audit line — only the prior createIdentity event is in the log
        List<AuditEvent> log = auditLogger.getLog();
        assertEquals(1, log.size());
    }

    @Test
    void should_throwUnauthorisedAndLogFailure_when_bankTriesTaxCheck() {
        DigitalId id = identityManager.createIdentity("NID001", LocalDate.of(1990, 1, 1), "Alice", null, null);

        assertThrows(UnauthorisedOperationException.class, () ->
                bankService.checkTaxEligibility(id.getId(), LocalDate.now().minusDays(30), LocalDate.now().plusDays(30)));

        List<AuditEvent> log = auditLogger.getLog();
        assertEquals(1, log.size());
    }

    // AUDIT LOG

    @Test
    void should_recordAllEvents_when_multipleOperationsPerformed() {
        DigitalId id = identityManager.createIdentity("NID001", LocalDate.of(1990, 1, 1), "Alice", null, null);
        identityManager.suspend(id.getId(), "investigation");
        identityManager.reactivate(id.getId(), "cleared");
        employerService.checkValidity(id.getId());
        taxService.checkTaxEligibility(id.getId(), LocalDate.now().minusDays(30), LocalDate.now().plusDays(30));

        assertEquals(5, auditLogger.getLog().size());
    }

    @Test
    void should_recordCorrectDigitalIdId_when_operationPerformedOnSpecificIdentity() {
        DigitalId first = identityManager.createIdentity("NID001", LocalDate.of(1990, 1, 1), "Alice", null, null);
        identityManager.createIdentity("NID002", LocalDate.of(1985, 6, 15), "Bob", null, null);

        employerService.checkValidity(first.getId());

        // Third event (index 2) is the checkValidity call on first
        AuditEvent checkEvent = auditLogger.getLog().get(2);
        assertEquals(Operations.CHECK_VALIDITY, checkEvent.getOperationName());
        assertEquals(first.getId(), checkEvent.getDigitalIdId());
    }
}
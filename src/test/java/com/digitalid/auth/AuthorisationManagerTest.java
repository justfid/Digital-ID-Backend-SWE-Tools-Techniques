package com.digitalid.auth;

import com.digitalid.model.OrganisationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthorisationManagerTest {

    private AuthorisationManager manager;

    @BeforeEach
    void setUp() {
        manager = new AuthorisationManagerImpl();
    }

    // CENTRAL AUTHORITY

    @Test
    void centralAuthorityPermittedToCreateIdentity() {
        assertDoesNotThrow(() -> manager.authorise(
                new OrganisationRequest(OrganisationType.CENTRAL_AUTHORITY, Operations.CREATE_IDENTITY)));
    }

    @Test
    void centralAuthorityPermittedToUpdateAddress() {
        assertDoesNotThrow(() -> manager.authorise(
                new OrganisationRequest(OrganisationType.CENTRAL_AUTHORITY, Operations.UPDATE_ADDRESS)));
    }

    @Test
    void centralAuthorityPermittedToUpdateEmail() {
        assertDoesNotThrow(() -> manager.authorise(
                new OrganisationRequest(OrganisationType.CENTRAL_AUTHORITY, Operations.UPDATE_EMAIL)));
    }

    @Test
    void centralAuthorityPermittedToUpdateTemporaryRestriction() {
        assertDoesNotThrow(() -> manager.authorise(
                new OrganisationRequest(OrganisationType.CENTRAL_AUTHORITY, Operations.UPDATE_TEMPORARY_RESTRICTION)));
    }

    @Test
    void centralAuthorityPermittedToSuspend() {
        assertDoesNotThrow(() -> manager.authorise(
                new OrganisationRequest(OrganisationType.CENTRAL_AUTHORITY, Operations.SUSPEND)));
    }

    @Test
    void centralAuthorityPermittedToReactivate() {
        assertDoesNotThrow(() -> manager.authorise(
                new OrganisationRequest(OrganisationType.CENTRAL_AUTHORITY, Operations.REACTIVATE)));
    }

    @Test
    void centralAuthorityPermittedToRevoke() {
        assertDoesNotThrow(() -> manager.authorise(
                new OrganisationRequest(OrganisationType.CENTRAL_AUTHORITY, Operations.REVOKE)));
    }

    // TAX AUTHORITY

    @Test
    void taxAuthorityPermittedToCheckTaxEligibility() {
        assertDoesNotThrow(() -> manager.authorise(
                new OrganisationRequest(OrganisationType.TAX_AUTHORITY, Operations.CHECK_TAX_ELIGIBILITY)));
    }

    @Test
    void taxAuthorityNotPermittedToCheckValidity() {
        assertThrows(UnauthorisedOperationException.class, () -> manager.authorise(
                new OrganisationRequest(OrganisationType.TAX_AUTHORITY, Operations.CHECK_VALIDITY)));
    }

    @Test
    void taxAuthorityNotPermittedToCreateIdentity() {
        assertThrows(UnauthorisedOperationException.class, () -> manager.authorise(
                new OrganisationRequest(OrganisationType.TAX_AUTHORITY, Operations.CREATE_IDENTITY)));
    }

    // DRIVING LICENCE AUTHORITY

    @Test
    void drivingLicenceAuthorityPermittedToCheckLicenceEligibility() {
        assertDoesNotThrow(() -> manager.authorise(
                new OrganisationRequest(OrganisationType.DRIVING_LICENCE_AUTHORITY, Operations.CHECK_LICENCE_ELIGIBILITY)));
    }

    @Test
    void drivingLicenceAuthorityNotPermittedToCheckValidity() {
        assertThrows(UnauthorisedOperationException.class, () -> manager.authorise(
                new OrganisationRequest(OrganisationType.DRIVING_LICENCE_AUTHORITY, Operations.CHECK_VALIDITY)));
    }

    @Test
    void drivingLicenceAuthorityNotPermittedToCreateIdentity() {
        assertThrows(UnauthorisedOperationException.class, () -> manager.authorise(
                new OrganisationRequest(OrganisationType.DRIVING_LICENCE_AUTHORITY, Operations.CREATE_IDENTITY)));
    }

    // EMPLOYER

    @Test
    void employerPermittedToCheckValidity() {
        assertDoesNotThrow(() -> manager.authorise(
                new OrganisationRequest(OrganisationType.EMPLOYER, Operations.CHECK_VALIDITY)));
    }

    @Test
    void employerNotPermittedToCreateIdentity() {
        assertThrows(UnauthorisedOperationException.class, () -> manager.authorise(
                new OrganisationRequest(OrganisationType.EMPLOYER, Operations.CREATE_IDENTITY)));
    }

    @Test
    void employerNotPermittedToCheckTaxEligibility() {
        assertThrows(UnauthorisedOperationException.class, () -> manager.authorise(
                new OrganisationRequest(OrganisationType.EMPLOYER, Operations.CHECK_TAX_ELIGIBILITY)));
    }

    // BANK

    @Test
    void bankPermittedToCheckValidity() {
        assertDoesNotThrow(() -> manager.authorise(
                new OrganisationRequest(OrganisationType.BANK, Operations.CHECK_VALIDITY)));
    }

    @Test
    void bankNotPermittedToCreateIdentity() {
        assertThrows(UnauthorisedOperationException.class, () -> manager.authorise(
                new OrganisationRequest(OrganisationType.BANK, Operations.CREATE_IDENTITY)));
    }

    @Test
    void bankNotPermittedToCheckLicenceEligibility() {
        assertThrows(UnauthorisedOperationException.class, () -> manager.authorise(
                new OrganisationRequest(OrganisationType.BANK, Operations.CHECK_LICENCE_ELIGIBILITY)));
    }
}
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
    void should_allowCreateIdentity_when_organisationIsCentralAuthority() {
        OrganisationRequest request = new OrganisationRequest(OrganisationType.CENTRAL_AUTHORITY, Operations.CREATE_IDENTITY);

        manager.authorise(request);

        // authorise() is void and stateless; assert the request fields are unchanged (immutability)
        assertEquals(OrganisationType.CENTRAL_AUTHORITY, request.getOrganisationType());
        assertEquals(Operations.CREATE_IDENTITY, request.getOperationName());
    }

    @Test
    void should_allowUpdateAddress_when_organisationIsCentralAuthority() {
        OrganisationRequest request = new OrganisationRequest(OrganisationType.CENTRAL_AUTHORITY, Operations.UPDATE_ADDRESS);

        manager.authorise(request);

        assertEquals(OrganisationType.CENTRAL_AUTHORITY, request.getOrganisationType());
        assertEquals(Operations.UPDATE_ADDRESS, request.getOperationName());
    }

    @Test
    void should_allowUpdateEmail_when_organisationIsCentralAuthority() {
        OrganisationRequest request = new OrganisationRequest(OrganisationType.CENTRAL_AUTHORITY, Operations.UPDATE_EMAIL);

        manager.authorise(request);

        assertEquals(OrganisationType.CENTRAL_AUTHORITY, request.getOrganisationType());
        assertEquals(Operations.UPDATE_EMAIL, request.getOperationName());
    }

    @Test
    void should_allowUpdateTemporaryRestriction_when_organisationIsCentralAuthority() {
        OrganisationRequest request = new OrganisationRequest(OrganisationType.CENTRAL_AUTHORITY, Operations.UPDATE_TEMPORARY_RESTRICTION);

        manager.authorise(request);

        assertEquals(OrganisationType.CENTRAL_AUTHORITY, request.getOrganisationType());
        assertEquals(Operations.UPDATE_TEMPORARY_RESTRICTION, request.getOperationName());
    }

    @Test
    void should_allowSuspend_when_organisationIsCentralAuthority() {
        OrganisationRequest request = new OrganisationRequest(OrganisationType.CENTRAL_AUTHORITY, Operations.SUSPEND);

        manager.authorise(request);

        assertEquals(OrganisationType.CENTRAL_AUTHORITY, request.getOrganisationType());
        assertEquals(Operations.SUSPEND, request.getOperationName());
    }

    @Test
    void should_allowReactivate_when_organisationIsCentralAuthority() {
        OrganisationRequest request = new OrganisationRequest(OrganisationType.CENTRAL_AUTHORITY, Operations.REACTIVATE);

        manager.authorise(request);

        assertEquals(OrganisationType.CENTRAL_AUTHORITY, request.getOrganisationType());
        assertEquals(Operations.REACTIVATE, request.getOperationName());
    }

    @Test
    void should_allowRevoke_when_organisationIsCentralAuthority() {
        OrganisationRequest request = new OrganisationRequest(OrganisationType.CENTRAL_AUTHORITY, Operations.REVOKE);

        manager.authorise(request);

        assertEquals(OrganisationType.CENTRAL_AUTHORITY, request.getOrganisationType());
        assertEquals(Operations.REVOKE, request.getOperationName());
    }

    // TAX AUTHORITY
    @Test
    void should_allowCheckTaxEligibility_when_organisationIsTaxAuthority() {
        OrganisationRequest request = new OrganisationRequest(OrganisationType.TAX_AUTHORITY, Operations.CHECK_TAX_ELIGIBILITY);

        manager.authorise(request);

        assertEquals(OrganisationType.TAX_AUTHORITY, request.getOrganisationType());
        assertEquals(Operations.CHECK_TAX_ELIGIBILITY, request.getOperationName());
    }

    @Test
    void should_throwWithOrgTypeAndOperationInMessage_when_taxAuthorityCallsCheckValidity() {
        OrganisationRequest request = new OrganisationRequest(OrganisationType.TAX_AUTHORITY, Operations.CHECK_VALIDITY);

        UnauthorisedOperationException ex = assertThrows(UnauthorisedOperationException.class,
                () -> manager.authorise(request));

        assertTrue(ex.getMessage().contains("TAX_AUTHORITY"));
        assertTrue(ex.getMessage().contains(Operations.CHECK_VALIDITY));
    }

    @Test
    void should_throwWithOrgTypeAndOperationInMessage_when_taxAuthorityCallsCreateIdentity() {
        OrganisationRequest request = new OrganisationRequest(OrganisationType.TAX_AUTHORITY, Operations.CREATE_IDENTITY);

        UnauthorisedOperationException ex = assertThrows(UnauthorisedOperationException.class,
                () -> manager.authorise(request));

        assertTrue(ex.getMessage().contains("TAX_AUTHORITY"));
        assertTrue(ex.getMessage().contains(Operations.CREATE_IDENTITY));
    }

    // DRIVING LICENCE AUTHORITY
    @Test
    void should_allowCheckLicenceEligibility_when_organisationIsDrivingLicenceAuthority() {
        OrganisationRequest request = new OrganisationRequest(OrganisationType.DRIVING_LICENCE_AUTHORITY, Operations.CHECK_LICENCE_ELIGIBILITY);

        manager.authorise(request);

        assertEquals(OrganisationType.DRIVING_LICENCE_AUTHORITY, request.getOrganisationType());
        assertEquals(Operations.CHECK_LICENCE_ELIGIBILITY, request.getOperationName());
    }

    @Test
    void should_throwWithOrgTypeAndOperationInMessage_when_drivingLicenceAuthorityCallsCheckValidity() {
        OrganisationRequest request = new OrganisationRequest(OrganisationType.DRIVING_LICENCE_AUTHORITY, Operations.CHECK_VALIDITY);

        UnauthorisedOperationException ex = assertThrows(UnauthorisedOperationException.class,
                () -> manager.authorise(request));

        assertTrue(ex.getMessage().contains("DRIVING_LICENCE_AUTHORITY"));
        assertTrue(ex.getMessage().contains(Operations.CHECK_VALIDITY));
    }

    @Test
    void should_throwWithOrgTypeAndOperationInMessage_when_drivingLicenceAuthorityCallsCreateIdentity() {
        OrganisationRequest request = new OrganisationRequest(OrganisationType.DRIVING_LICENCE_AUTHORITY, Operations.CREATE_IDENTITY);

        UnauthorisedOperationException ex = assertThrows(UnauthorisedOperationException.class,
                () -> manager.authorise(request));

        assertTrue(ex.getMessage().contains("DRIVING_LICENCE_AUTHORITY"));
        assertTrue(ex.getMessage().contains(Operations.CREATE_IDENTITY));
    }

    // EMPLOYER
    @Test
    void should_allowCheckValidity_when_organisationIsEmployer() {
        OrganisationRequest request = new OrganisationRequest(OrganisationType.EMPLOYER, Operations.CHECK_VALIDITY);

        manager.authorise(request);

        assertEquals(OrganisationType.EMPLOYER, request.getOrganisationType());
        assertEquals(Operations.CHECK_VALIDITY, request.getOperationName());
    }

    @Test
    void should_throwWithOrgTypeAndOperationInMessage_when_employerCallsCreateIdentity() {
        OrganisationRequest request = new OrganisationRequest(OrganisationType.EMPLOYER, Operations.CREATE_IDENTITY);

        UnauthorisedOperationException ex = assertThrows(UnauthorisedOperationException.class,
                () -> manager.authorise(request));

        assertTrue(ex.getMessage().contains("EMPLOYER"));
        assertTrue(ex.getMessage().contains(Operations.CREATE_IDENTITY));
    }

    @Test
    void should_throwWithOrgTypeAndOperationInMessage_when_employerCallsCheckTaxEligibility() {
        OrganisationRequest request = new OrganisationRequest(OrganisationType.EMPLOYER, Operations.CHECK_TAX_ELIGIBILITY);

        UnauthorisedOperationException ex = assertThrows(UnauthorisedOperationException.class,
                () -> manager.authorise(request));

        assertTrue(ex.getMessage().contains("EMPLOYER"));
        assertTrue(ex.getMessage().contains(Operations.CHECK_TAX_ELIGIBILITY));
    }

    // BANK
    @Test
    void should_allowCheckValidity_when_organisationIsBank() {
        OrganisationRequest request = new OrganisationRequest(OrganisationType.BANK, Operations.CHECK_VALIDITY);

        manager.authorise(request);

        assertEquals(OrganisationType.BANK, request.getOrganisationType());
        assertEquals(Operations.CHECK_VALIDITY, request.getOperationName());
    }

    @Test
    void should_throwWithOrgTypeAndOperationInMessage_when_bankCallsCreateIdentity() {
        OrganisationRequest request = new OrganisationRequest(OrganisationType.BANK, Operations.CREATE_IDENTITY);

        UnauthorisedOperationException ex = assertThrows(UnauthorisedOperationException.class,
                () -> manager.authorise(request));

        assertTrue(ex.getMessage().contains("BANK"));
        assertTrue(ex.getMessage().contains(Operations.CREATE_IDENTITY));
    }

    @Test
    void should_throwWithOrgTypeAndOperationInMessage_when_bankCallsCheckLicenceEligibility() {
        OrganisationRequest request = new OrganisationRequest(OrganisationType.BANK, Operations.CHECK_LICENCE_ELIGIBILITY);

        UnauthorisedOperationException ex = assertThrows(UnauthorisedOperationException.class,
                () -> manager.authorise(request));

        assertTrue(ex.getMessage().contains("BANK"));
        assertTrue(ex.getMessage().contains(Operations.CHECK_LICENCE_ELIGIBILITY));
    }
}
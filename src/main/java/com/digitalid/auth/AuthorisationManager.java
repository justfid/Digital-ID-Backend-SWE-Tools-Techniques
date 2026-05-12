package com.digitalid.auth;

public interface AuthorisationManager {

    /**
     * Throws UnauthorisedOperationException if the organisation in the request is not
     * permitted to perform the named operation.
     */
    void authorise(OrganisationRequest request);
}
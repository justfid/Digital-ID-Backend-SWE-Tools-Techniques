package com.digitalid.auth;

/**
 * Enforces operation-level access control by checking whether a given organisation
 * is permitted to perform the requested operation.
 */
public interface AuthorisationManager {

    /**
     * Verifies that the organisation in the request is permitted to perform the named operation.
     *
     * @param request the organisation and operation to authorise; must not be null
     * @throws UnauthorisedOperationException if the organisation is not permitted to perform the operation
     */
    void authorise(OrganisationRequest request);
}
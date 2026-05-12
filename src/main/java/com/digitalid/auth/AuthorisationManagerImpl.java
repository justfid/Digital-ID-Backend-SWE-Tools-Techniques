package com.digitalid.auth;

import com.digitalid.model.OrganisationType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class AuthorisationManagerImpl implements AuthorisationManager {

    //Permissions are static; they reflect fixed business rules
    private static final Map<OrganisationType, Set<String>> PERMITTED_OPERATIONS;

    static {
        PERMITTED_OPERATIONS = new EnumMap<>(OrganisationType.class);
        PERMITTED_OPERATIONS.put(OrganisationType.CENTRAL_AUTHORITY, Set.of(
                Operations.CREATE_IDENTITY,
                Operations.UPDATE_ADDRESS,
                Operations.UPDATE_EMAIL,
                Operations.UPDATE_TEMPORARY_RESTRICTION,
                Operations.SUSPEND,
                Operations.REACTIVATE,
                Operations.REVOKE
        ));
        PERMITTED_OPERATIONS.put(OrganisationType.TAX_AUTHORITY, Set.of(
                Operations.CHECK_TAX_ELIGIBILITY
        ));
        PERMITTED_OPERATIONS.put(OrganisationType.DRIVING_LICENCE_AUTHORITY, Set.of(
                Operations.CHECK_LICENCE_ELIGIBILITY
        ));
        PERMITTED_OPERATIONS.put(OrganisationType.EMPLOYER, Set.of(
                Operations.CHECK_VALIDITY
        ));
        PERMITTED_OPERATIONS.put(OrganisationType.BANK, Set.of(
                Operations.CHECK_VALIDITY
        ));
    }

    @Override
    public void authorise(OrganisationRequest request) {
        Set<String> allowed = PERMITTED_OPERATIONS.get(request.getOrganisationType());
        if (allowed == null || !allowed.contains(request.getOperationName())) {
            throw new UnauthorisedOperationException(
                    request.getOrganisationType(), request.getOperationName());
        }
    }
}
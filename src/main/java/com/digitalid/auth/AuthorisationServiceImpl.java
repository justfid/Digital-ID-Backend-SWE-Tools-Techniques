package com.digitalid.auth;

import com.digitalid.model.OrganisationType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class AuthorisationServiceImpl implements AuthorisationService {

    //Permissions are static; they reflect fixed business rules
    private static final Map<OrganisationType, Set<String>> PERMITTED_OPERATIONS;

    static {
        PERMITTED_OPERATIONS = new EnumMap<>(OrganisationType.class);
        PERMITTED_OPERATIONS.put(OrganisationType.CENTRAL_AUTHORITY, Set.of(
                "createIdentity",
                "updateAddress",
                "updateEmail",
                "updateTemporaryRestriction",
                "suspend",
                "reactivate",
                "revoke"
        ));
        PERMITTED_OPERATIONS.put(OrganisationType.TAX_AUTHORITY, Set.of(
                "checkTaxEligibility"
        ));
        PERMITTED_OPERATIONS.put(OrganisationType.DRIVING_LICENCE_AUTHORITY, Set.of(
                "checkLicenceEligibility"
        ));
        PERMITTED_OPERATIONS.put(OrganisationType.EMPLOYER, Set.of(
                "checkValidity"
        ));
        PERMITTED_OPERATIONS.put(OrganisationType.BANK, Set.of(
                "checkValidity"
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
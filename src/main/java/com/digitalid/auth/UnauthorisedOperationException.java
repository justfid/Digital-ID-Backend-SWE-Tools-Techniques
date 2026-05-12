package com.digitalid.auth;

import com.digitalid.model.OrganisationType;

public class UnauthorisedOperationException extends RuntimeException {

    public UnauthorisedOperationException(OrganisationType organisationType, String operationName) {
        super(organisationType + " is not permitted to perform operation: " + operationName);
    }
}
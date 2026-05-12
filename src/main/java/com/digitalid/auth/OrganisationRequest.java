package com.digitalid.auth;

import com.digitalid.model.OrganisationType;

public class OrganisationRequest {

    private final OrganisationType organisationType;
    private final String operationName;

    public OrganisationRequest(OrganisationType organisationType, String operationName) {
        if (organisationType == null) {
            throw new IllegalArgumentException("organisationType must not be null");
        }
        if (operationName == null || operationName.isBlank()) {
            throw new IllegalArgumentException("operationName must not be null or blank");
        }
        this.organisationType = organisationType;
        this.operationName = operationName;
    }

    public OrganisationType getOrganisationType() {
        return organisationType;
    }

    public String getOperationName() {
        return operationName;
    }
}
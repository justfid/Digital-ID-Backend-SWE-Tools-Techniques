package com.digitalid.consumption;

import com.digitalid.audit.AuditLogger;
import com.digitalid.auth.AuthorisationManager;
import com.digitalid.auth.Operations;
import com.digitalid.auth.OrganisationRequest;
import com.digitalid.management.IdentityManager;
import com.digitalid.model.DigitalId;
import com.digitalid.model.DigitalIdStatus;
import com.digitalid.model.OrganisationType;

import java.time.LocalDate;
import java.util.UUID;

public class IdentityConsumptionServiceImpl implements IdentityConsumptionService {

    private final IdentityManager manager;
    private final OrganisationType organisationType;
    private final AuthorisationManager authorisationManager;
    private final AuditLogger auditLogger;

    public IdentityConsumptionServiceImpl(IdentityManager manager,
                                           OrganisationType organisationType,
                                           AuthorisationManager authorisationManager,
                                           AuditLogger auditLogger) {
        this.manager = manager;
        this.organisationType = organisationType;
        this.authorisationManager = authorisationManager;
        this.auditLogger = auditLogger;
    }

    @Override
    public VerificationResponse checkValidity(UUID id) {
        authorisationManager.authorise(new OrganisationRequest(organisationType, Operations.CHECK_VALIDITY));
        VerificationResponse response = executeCheckValidity(id);
        auditLogger.log(organisationType, Operations.CHECK_VALIDITY, id, response.isValid(), response.getReason());
        return response;
    }

    @Override
    public VerificationResponse checkTaxEligibility(UUID id, LocalDate periodStart, LocalDate periodEnd) {
        authorisationManager.authorise(new OrganisationRequest(organisationType, Operations.CHECK_TAX_ELIGIBILITY));
        VerificationResponse response = executeCheckTaxEligibility(id, periodStart, periodEnd);
        auditLogger.log(organisationType, Operations.CHECK_TAX_ELIGIBILITY, id, response.isValid(), response.getReason());
        return response;
    }

    @Override
    public VerificationResponse checkLicenceEligibility(UUID id) {
        authorisationManager.authorise(new OrganisationRequest(organisationType, Operations.CHECK_LICENCE_ELIGIBILITY));
        VerificationResponse response = executeCheckLicenceEligibility(id);
        auditLogger.log(organisationType, Operations.CHECK_LICENCE_ELIGIBILITY, id, response.isValid(), response.getReason());
        return response;
    }

    private VerificationResponse executeCheckValidity(UUID id) {
        DigitalId digitalId;
        try {
            digitalId = manager.findById(id);
        } catch (IllegalArgumentException e) {
            return VerificationResponse.invalid("Identity not found");
        }

        if (digitalId.getStatus() == DigitalIdStatus.ACTIVE) {
            return VerificationResponse.valid("Identity is active");
        }
        if (digitalId.getStatus() == DigitalIdStatus.SUSPENDED) {
            return VerificationResponse.invalid("Identity is suspended");
        }
        return VerificationResponse.invalid("Identity is revoked");
    }

    private VerificationResponse executeCheckTaxEligibility(UUID id, LocalDate periodStart, LocalDate periodEnd) {
        DigitalId digitalId;
        try {
            digitalId = manager.findById(id);
        } catch (IllegalArgumentException e) {
            return VerificationResponse.invalid("Identity not found");
        }

        if (digitalId.getStatus() != DigitalIdStatus.ACTIVE) {
            return VerificationResponse.invalid("Identity is not active");
        }

        boolean wasSuspendedDuringPeriod = digitalId.getStatusHistory().stream()
                .anyMatch(entry -> entry.getStatus() == DigitalIdStatus.SUSPENDED
                        && !entry.getTimestamp().toLocalDate().isBefore(periodStart)
                        && !entry.getTimestamp().toLocalDate().isAfter(periodEnd));

        if (wasSuspendedDuringPeriod) {
            return VerificationResponse.invalid("Identity was suspended during the reporting period");
        }

        return VerificationResponse.valid("Identity is eligible for tax reporting");
    }

    private VerificationResponse executeCheckLicenceEligibility(UUID id) {
        DigitalId digitalId;
        try {
            digitalId = manager.findById(id);
        } catch (IllegalArgumentException e) {
            return VerificationResponse.invalid("Identity not found");
        }

        if (digitalId.getStatus() != DigitalIdStatus.ACTIVE) {
            return VerificationResponse.invalid("Identity is not active");
        }

        if (digitalId.isTemporaryRestriction()) {
            return VerificationResponse.invalid("Identity has a temporary restriction");
        }

        return VerificationResponse.valid("Identity is eligible for a driving licence");
    }
}
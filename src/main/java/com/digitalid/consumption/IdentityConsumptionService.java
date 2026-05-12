package com.digitalid.consumption;

import java.time.LocalDate;
import java.util.UUID;

public interface IdentityConsumptionService {

    VerificationResponse checkValidity(UUID id);

    VerificationResponse checkTaxEligibility(UUID id, LocalDate periodStart, LocalDate periodEnd);

    VerificationResponse checkLicenceEligibility(UUID id);
}
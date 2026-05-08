package com.digitalid.consumption;

import java.time.LocalDate;
import java.util.UUID;

public interface IdentityConsumptionService {

    ValidityResponse checkValidity(UUID id);

    TaxVerificationResponse checkTaxEligibility(UUID id, LocalDate periodStart, LocalDate periodEnd);

    LicenceVerificationResponse checkLicenceEligibility(UUID id);
}

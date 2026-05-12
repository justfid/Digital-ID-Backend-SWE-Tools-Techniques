package com.digitalid.consumption;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Defines the read-only identity verification operations available to consuming organisations.
 * Each method is authorisation-checked; only the organisation type the service was configured for
 * may call its permitted operations.
 */
public interface IdentityConsumptionService {

    /**
     * Checks whether the given Digital ID is currently valid (ACTIVE status).
     * Intended for use by employers and banks.
     *
     * @param id the UUID of the Digital ID to check
     * @return a {@link VerificationResponse} indicating validity and a human-readable reason
     * @throws com.digitalid.auth.UnauthorisedOperationException if the caller's organisation is not permitted to perform this check
     */
    VerificationResponse checkValidity(UUID id);

    /**
     * Checks whether the given Digital ID was continuously ACTIVE throughout the specified reporting period,
     * with no SUSPENDED status entries falling within that period.
     * Intended for use by the tax authority.
     *
     * @param id          the UUID of the Digital ID to check
     * @param periodStart the inclusive start date of the reporting period
     * @param periodEnd   the inclusive end date of the reporting period
     * @return a {@link VerificationResponse} indicating eligibility and a human-readable reason
     * @throws com.digitalid.auth.UnauthorisedOperationException if the caller's organisation is not permitted to perform this check
     */
    VerificationResponse checkTaxEligibility(UUID id, LocalDate periodStart, LocalDate periodEnd);

    /**
     * Checks whether the given Digital ID is ACTIVE and has no temporary restriction flag set.
     * Intended for use by the driving licence authority.
     *
     * @param id the UUID of the Digital ID to check
     * @return a {@link VerificationResponse} indicating eligibility and a human-readable reason
     * @throws com.digitalid.auth.UnauthorisedOperationException if the caller's organisation is not permitted to perform this check
     */
    VerificationResponse checkLicenceEligibility(UUID id);
}
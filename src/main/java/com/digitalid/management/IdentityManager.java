package com.digitalid.management;

import com.digitalid.model.DigitalId;
import com.digitalid.model.Result;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Defines the identity lifecycle operations available exclusively to the central authority.
 * All mutating operations are authorisation-checked and audit-logged by the implementation.
 */
public interface IdentityManager {

    /**
     * Creates a new Digital ID with ACTIVE status and stores it in the repository.
     *
     * @param nationalIdNumber unique national identifier; must not be null or blank
     * @param dateOfBirth      the holder's date of birth; must not be null
     * @param fullName         the holder's full name; must not be null or blank
     * @param address          optional postal address; may be null
     * @param email            optional email address; may be null
     * @return the newly created {@link DigitalId}
     * @throws com.digitalid.auth.UnauthorisedOperationException if the caller is not permitted to create identities
     * @throws IllegalArgumentException if any required field is null or blank
     */
    DigitalId createIdentity(String nationalIdNumber, LocalDate dateOfBirth, String fullName,
                             String address, String email);

    /**
     * Updates the postal address on an existing Digital ID.
     *
     * @param id         the UUID of the Digital ID to update
     * @param newAddress the replacement address value
     * @return a successful {@link Result} containing the updated identity, or a failure result if the identity is REVOKED
     * @throws IllegalArgumentException if no Digital ID exists for the given id
     */
    Result<DigitalId> updateAddress(UUID id, String newAddress);

    /**
     * Updates the email address on an existing Digital ID.
     *
     * @param id       the UUID of the Digital ID to update
     * @param newEmail the replacement email value
     * @return a successful {@link Result} containing the updated identity, or a failure result if the identity is REVOKED
     * @throws IllegalArgumentException if no Digital ID exists for the given id
     */
    Result<DigitalId> updateEmail(UUID id, String newEmail);

    /**
     * Sets or clears the temporary restriction flag on an existing Digital ID.
     *
     * @param id          the UUID of the Digital ID to update
     * @param restriction {@code true} to apply a restriction, {@code false} to clear it
     * @return a successful {@link Result} containing the updated identity, or a failure result if the identity is REVOKED
     * @throws IllegalArgumentException if no Digital ID exists for the given id
     */
    Result<DigitalId> updateTemporaryRestriction(UUID id, boolean restriction);

    /**
     * Suspends an ACTIVE Digital ID, recording the given reason in the status history.
     *
     * @param id     the UUID of the Digital ID to suspend
     * @param reason a human-readable explanation for the suspension
     * @return a successful {@link Result} containing the updated identity, or a failure result if the identity is already SUSPENDED or REVOKED
     * @throws IllegalArgumentException if no Digital ID exists for the given id
     */
    Result<DigitalId> suspend(UUID id, String reason);

    /**
     * Reactivates a SUSPENDED Digital ID, restoring it to ACTIVE status.
     *
     * @param id     the UUID of the Digital ID to reactivate
     * @param reason a human-readable explanation for the reactivation
     * @return a successful {@link Result} containing the updated identity, or a failure result if the identity is already ACTIVE or REVOKED
     * @throws IllegalArgumentException if no Digital ID exists for the given id
     */
    Result<DigitalId> reactivate(UUID id, String reason);

    /**
     * Permanently revokes a Digital ID; a revoked identity cannot be modified or reinstated.
     *
     * @param id     the UUID of the Digital ID to revoke
     * @param reason a human-readable explanation for the revocation
     * @return a successful {@link Result} containing the updated identity, or a failure result if the identity is already REVOKED
     * @throws IllegalArgumentException if no Digital ID exists for the given id
     */
    Result<DigitalId> revoke(UUID id, String reason);

    /**
     * Retrieves a Digital ID by its UUID without performing any authorisation check.
     * Intended for internal use by the consumption layer.
     *
     * @param id the UUID of the Digital ID to retrieve
     * @return the matching {@link DigitalId}
     * @throws IllegalArgumentException if no Digital ID exists for the given id
     */
    DigitalId findById(UUID id);
}
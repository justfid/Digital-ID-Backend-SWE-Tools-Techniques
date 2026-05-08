package com.digitalid.management;

import com.digitalid.model.DigitalId;

import java.time.LocalDate;
import java.util.UUID;

public interface IdentityManager {

    DigitalId createIdentity(String nationalIdNumber, LocalDate dateOfBirth, String fullName,
                             String address, String email);

    DigitalId updateAddress(UUID id, String newAddress);

    DigitalId updateEmail(UUID id, String newEmail);

    DigitalId updateTemporaryRestriction(UUID id, boolean restriction);

    DigitalId suspend(UUID id, String reason);

    DigitalId reactivate(UUID id, String reason);

    DigitalId revoke(UUID id, String reason);

    DigitalId findById(UUID id);
}

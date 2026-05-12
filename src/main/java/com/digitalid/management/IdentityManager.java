package com.digitalid.management;

import com.digitalid.model.DigitalId;
import com.digitalid.model.Result;

import java.time.LocalDate;
import java.util.UUID;

public interface IdentityManager {

    DigitalId createIdentity(String nationalIdNumber, LocalDate dateOfBirth, String fullName,
                             String address, String email);

    Result<DigitalId> updateAddress(UUID id, String newAddress);

    Result<DigitalId> updateEmail(UUID id, String newEmail);

    Result<DigitalId> updateTemporaryRestriction(UUID id, boolean restriction);

    Result<DigitalId> suspend(UUID id, String reason);

    Result<DigitalId> reactivate(UUID id, String reason);

    Result<DigitalId> revoke(UUID id, String reason);

    DigitalId findById(UUID id);
}
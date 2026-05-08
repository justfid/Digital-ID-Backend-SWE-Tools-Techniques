package com.digitalid.management;

import com.digitalid.model.DigitalId;
import com.digitalid.model.DigitalIdStatus;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IdentityManagerImpl implements IdentityManager {

    private final Map<UUID, DigitalId> store = new HashMap<>();

    @Override
    public DigitalId createIdentity(String nationalIdNumber, LocalDate dateOfBirth, String fullName,
                                    String address, String email) {
        if (nationalIdNumber == null || nationalIdNumber.isBlank()) {
            throw new IllegalArgumentException("nationalIdNumber must not be null or blank");
        }
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("dateOfBirth must not be null");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("fullName must not be null or blank");
        }
        DigitalId digitalId = new DigitalId.Builder(nationalIdNumber, dateOfBirth, fullName)
                .address(address)
                .email(email)
                .build();
        store.put(digitalId.getId(), digitalId);
        return digitalId;
    }

    @Override
    public DigitalId updateAddress(UUID id, String newAddress) {
        DigitalId digitalId = requireExisting(id);
        requireNotRevoked(digitalId);
        digitalId.updateAddress(newAddress);
        return digitalId;
    }

    @Override
    public DigitalId updateEmail(UUID id, String newEmail) {
        DigitalId digitalId = requireExisting(id);
        requireNotRevoked(digitalId);
        digitalId.updateEmail(newEmail);
        return digitalId;
    }

    @Override
    public DigitalId updateTemporaryRestriction(UUID id, boolean restriction) {
        DigitalId digitalId = requireExisting(id);
        requireNotRevoked(digitalId);
        digitalId.updateTemporaryRestriction(restriction);
        return digitalId;
    }

    @Override
    public DigitalId suspend(UUID id, String reason) {
        DigitalId digitalId = requireExisting(id);
        if (digitalId.getStatus() == DigitalIdStatus.SUSPENDED) {
            throw new IllegalStateException("Digital ID is already SUSPENDED");
        }
        if (digitalId.getStatus() == DigitalIdStatus.REVOKED) {
            throw new IllegalStateException("Cannot suspend a REVOKED Digital ID");
        }
        digitalId.updateStatus(DigitalIdStatus.SUSPENDED, reason);
        return digitalId;
    }

    @Override
    public DigitalId reactivate(UUID id, String reason) {
        DigitalId digitalId = requireExisting(id);
        if (digitalId.getStatus() == DigitalIdStatus.REVOKED) {
            throw new IllegalStateException("Cannot reactivate a REVOKED Digital ID");
        }
        if (digitalId.getStatus() == DigitalIdStatus.ACTIVE) {
            throw new IllegalStateException("Digital ID is already ACTIVE");
        }
        digitalId.updateStatus(DigitalIdStatus.ACTIVE, reason);
        return digitalId;
    }

    @Override
    public DigitalId revoke(UUID id, String reason) {
        DigitalId digitalId = requireExisting(id);
        if (digitalId.getStatus() == DigitalIdStatus.REVOKED) {
            throw new IllegalStateException("Digital ID is already REVOKED");
        }
        digitalId.updateStatus(DigitalIdStatus.REVOKED, reason);
        return digitalId;
    }

    @Override
    public DigitalId findById(UUID id) {
        return requireExisting(id);
    }

    private DigitalId requireExisting(UUID id) {
        DigitalId digitalId = store.get(id);
        if (digitalId == null) {
            throw new IllegalArgumentException("No Digital ID found for id: " + id);
        }
        return digitalId;
    }

    private void requireNotRevoked(DigitalId digitalId) {
        if (digitalId.getStatus() == DigitalIdStatus.REVOKED) {
            throw new IllegalStateException("Cannot modify a REVOKED Digital ID");
        }
    }
}

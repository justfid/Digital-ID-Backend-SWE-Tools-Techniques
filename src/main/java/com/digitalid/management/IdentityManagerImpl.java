package com.digitalid.management;

import com.digitalid.model.DigitalId;
import com.digitalid.model.DigitalIdMutator;
import com.digitalid.model.DigitalIdStatus;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IdentityManagerImpl implements IdentityManager {

    private final Map<UUID, DigitalIdMutator> store = new HashMap<>();

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
        store.put(digitalId.getId(), digitalId.getMutator());
        return digitalId;
    }

    @Override
    public DigitalId updateAddress(UUID id, String newAddress) {
        DigitalIdMutator mutator = requireExisting(id);
        requireNotRevoked(mutator.getDigitalId());
        mutator.updateAddress(newAddress);
        return mutator.getDigitalId();
    }

    @Override
    public DigitalId updateEmail(UUID id, String newEmail) {
        DigitalIdMutator mutator = requireExisting(id);
        requireNotRevoked(mutator.getDigitalId());
        mutator.updateEmail(newEmail);
        return mutator.getDigitalId();
    }

    @Override
    public DigitalId updateTemporaryRestriction(UUID id, boolean restriction) {
        DigitalIdMutator mutator = requireExisting(id);
        requireNotRevoked(mutator.getDigitalId());
        mutator.updateTemporaryRestriction(restriction);
        return mutator.getDigitalId();
    }

    @Override
    public DigitalId suspend(UUID id, String reason) {
        DigitalIdMutator mutator = requireExisting(id);
        DigitalId digitalId = mutator.getDigitalId();
        if (digitalId.getStatus() == DigitalIdStatus.SUSPENDED) {
            throw new IllegalStateException("Digital ID is already SUSPENDED");
        }
        if (digitalId.getStatus() == DigitalIdStatus.REVOKED) {
            throw new IllegalStateException("Cannot suspend a REVOKED Digital ID");
        }
        mutator.updateStatus(DigitalIdStatus.SUSPENDED, reason);
        return digitalId;
    }

    @Override
    public DigitalId reactivate(UUID id, String reason) {
        DigitalIdMutator mutator = requireExisting(id);
        DigitalId digitalId = mutator.getDigitalId();
        if (digitalId.getStatus() == DigitalIdStatus.REVOKED) {
            throw new IllegalStateException("Cannot reactivate a REVOKED Digital ID");
        }
        if (digitalId.getStatus() == DigitalIdStatus.ACTIVE) {
            throw new IllegalStateException("Digital ID is already ACTIVE");
        }
        mutator.updateStatus(DigitalIdStatus.ACTIVE, reason);
        return digitalId;
    }

    @Override
    public DigitalId revoke(UUID id, String reason) {
        DigitalIdMutator mutator = requireExisting(id);
        DigitalId digitalId = mutator.getDigitalId();
        if (digitalId.getStatus() == DigitalIdStatus.REVOKED) {
            throw new IllegalStateException("Digital ID is already REVOKED");
        }
        mutator.updateStatus(DigitalIdStatus.REVOKED, reason);
        return digitalId;
    }

    @Override
    public DigitalId findById(UUID id) {
        return requireExisting(id).getDigitalId();
    }

    private DigitalIdMutator requireExisting(UUID id) {
        DigitalIdMutator mutator = store.get(id);
        if (mutator == null) {
            throw new IllegalArgumentException("No Digital ID found for id: " + id);
        }
        return mutator;
    }

    private void requireNotRevoked(DigitalId digitalId) {
        if (digitalId.getStatus() == DigitalIdStatus.REVOKED) {
            throw new IllegalStateException("Cannot modify a REVOKED Digital ID");
        }
    }
}
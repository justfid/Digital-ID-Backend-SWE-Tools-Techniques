package com.digitalid.management;

import com.digitalid.audit.AuditLogger;
import com.digitalid.auth.AuthorisationManager;
import com.digitalid.auth.Operations;
import com.digitalid.auth.OrganisationRequest;
import com.digitalid.model.DigitalId;
import com.digitalid.model.DigitalIdMutator;
import com.digitalid.model.DigitalIdStatus;
import com.digitalid.model.OrganisationType;

import java.time.LocalDate;
import java.util.UUID;

public class IdentityManagerImpl implements IdentityManager {

    private final OrganisationType organisationType;
    private final DigitalIdRepository repository;
    private final AuthorisationManager authorisationManager;
    private final AuditLogger auditLogger;

    public IdentityManagerImpl(OrganisationType organisationType,
                                DigitalIdRepository repository,
                                AuthorisationManager authorisationManager,
                                AuditLogger auditLogger) {
        this.organisationType = organisationType;
        this.repository = repository;
        this.authorisationManager = authorisationManager;
        this.auditLogger = auditLogger;
    }

    @Override
    public DigitalId createIdentity(String nationalIdNumber, LocalDate dateOfBirth, String fullName,
                                    String address, String email) {
        try {
            authorisationManager.authorise(new OrganisationRequest(organisationType, Operations.CREATE_IDENTITY));
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
            repository.save(digitalId);
            auditLogger.log(organisationType, Operations.CREATE_IDENTITY, digitalId.getId(), true, "Identity created");
            return digitalId;
        } catch (RuntimeException e) {
            // Only reached if the operation failed — log before rethrowing
            auditLogger.log(organisationType, Operations.CREATE_IDENTITY, null, false, e.getMessage());
            throw e;
        }
    }

    @Override
    public DigitalId updateAddress(UUID id, String newAddress) {
        try {
            authorisationManager.authorise(new OrganisationRequest(organisationType, Operations.UPDATE_ADDRESS));
            DigitalIdMutator mutator = requireExisting(id);
            requireNotRevoked(mutator.getDigitalId());
            mutator.updateAddress(newAddress);
            auditLogger.log(organisationType, Operations.UPDATE_ADDRESS, id, true, "Address updated");
            return mutator.getDigitalId();
        } catch (RuntimeException e) {
            auditLogger.log(organisationType, Operations.UPDATE_ADDRESS, id, false, e.getMessage());
            throw e;
        }
    }

    @Override
    public DigitalId updateEmail(UUID id, String newEmail) {
        try {
            authorisationManager.authorise(new OrganisationRequest(organisationType, Operations.UPDATE_EMAIL));
            DigitalIdMutator mutator = requireExisting(id);
            requireNotRevoked(mutator.getDigitalId());
            mutator.updateEmail(newEmail);
            auditLogger.log(organisationType, Operations.UPDATE_EMAIL, id, true, "Email updated");
            return mutator.getDigitalId();
        } catch (RuntimeException e) {
            auditLogger.log(organisationType, Operations.UPDATE_EMAIL, id, false, e.getMessage());
            throw e;
        }
    }

    @Override
    public DigitalId updateTemporaryRestriction(UUID id, boolean restriction) {
        try {
            authorisationManager.authorise(new OrganisationRequest(organisationType, Operations.UPDATE_TEMPORARY_RESTRICTION));
            DigitalIdMutator mutator = requireExisting(id);
            requireNotRevoked(mutator.getDigitalId());
            mutator.updateTemporaryRestriction(restriction);
            auditLogger.log(organisationType, Operations.UPDATE_TEMPORARY_RESTRICTION, id, true, "Temporary restriction updated to " + restriction);
            return mutator.getDigitalId();
        } catch (RuntimeException e) {
            auditLogger.log(organisationType, Operations.UPDATE_TEMPORARY_RESTRICTION, id, false, e.getMessage());
            throw e;
        }
    }

    @Override
    public DigitalId suspend(UUID id, String reason) {
        try {
            authorisationManager.authorise(new OrganisationRequest(organisationType, Operations.SUSPEND));
            DigitalIdMutator mutator = requireExisting(id);
            DigitalId digitalId = mutator.getDigitalId();
            if (digitalId.getStatus() == DigitalIdStatus.SUSPENDED) {
                throw new IllegalStateException("Digital ID is already SUSPENDED");
            }
            if (digitalId.getStatus() == DigitalIdStatus.REVOKED) {
                throw new IllegalStateException("Cannot suspend a REVOKED Digital ID");
            }
            mutator.updateStatus(DigitalIdStatus.SUSPENDED, reason);
            auditLogger.log(organisationType, Operations.SUSPEND, id, true, reason);
            return digitalId;
        } catch (RuntimeException e) {
            auditLogger.log(organisationType, Operations.SUSPEND, id, false, e.getMessage());
            throw e;
        }
    }

    @Override
    public DigitalId reactivate(UUID id, String reason) {
        try {
            authorisationManager.authorise(new OrganisationRequest(organisationType, Operations.REACTIVATE));
            DigitalIdMutator mutator = requireExisting(id);
            DigitalId digitalId = mutator.getDigitalId();
            if (digitalId.getStatus() == DigitalIdStatus.REVOKED) {
                throw new IllegalStateException("Cannot reactivate a REVOKED Digital ID");
            }
            if (digitalId.getStatus() == DigitalIdStatus.ACTIVE) {
                throw new IllegalStateException("Digital ID is already ACTIVE");
            }
            mutator.updateStatus(DigitalIdStatus.ACTIVE, reason);
            auditLogger.log(organisationType, Operations.REACTIVATE, id, true, reason);
            return digitalId;
        } catch (RuntimeException e) {
            auditLogger.log(organisationType, Operations.REACTIVATE, id, false, e.getMessage());
            throw e;
        }
    }

    @Override
    public DigitalId revoke(UUID id, String reason) {
        try {
            authorisationManager.authorise(new OrganisationRequest(organisationType, Operations.REVOKE));
            DigitalIdMutator mutator = requireExisting(id);
            DigitalId digitalId = mutator.getDigitalId();
            if (digitalId.getStatus() == DigitalIdStatus.REVOKED) {
                throw new IllegalStateException("Digital ID is already REVOKED");
            }
            mutator.updateStatus(DigitalIdStatus.REVOKED, reason);
            auditLogger.log(organisationType, Operations.REVOKE, id, true, reason);
            return digitalId;
        } catch (RuntimeException e) {
            auditLogger.log(organisationType, Operations.REVOKE, id, false, e.getMessage());
            throw e;
        }
    }

    // findById is an internal lookup used by the consumption layer
    @Override
    public DigitalId findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No Digital ID found for id: " + id));
    }

    private DigitalIdMutator requireExisting(UUID id) {
        return repository.findById(id)
                .map(DigitalId::getMutator)
                .orElseThrow(() -> new IllegalArgumentException("No Digital ID found for id: " + id));
    }

    private void requireNotRevoked(DigitalId digitalId) {
        if (digitalId.getStatus() == DigitalIdStatus.REVOKED) {
            throw new IllegalStateException("Cannot modify a REVOKED Digital ID");
        }
    }
}
package com.digitalid.model;

/**
 * Provides controlled write access to a DigitalId. The constructor is package-private so
 * only DigitalId.getMutator() can produce an instance; callers outside com.digitalid.model
 * cannot construct one independently.
 */
public class DigitalIdMutator {

    private final DigitalId digitalId;

    DigitalIdMutator(DigitalId digitalId) {
        this.digitalId = digitalId;
    }

    public DigitalId getDigitalId() {
        return digitalId;
    }

    public void updateAddress(String address) {
        digitalId.updateAddress(address);
    }

    public void updateEmail(String email) {
        digitalId.updateEmail(email);
    }

    public void updateTemporaryRestriction(boolean temporaryRestriction) {
        digitalId.updateTemporaryRestriction(temporaryRestriction);
    }

    public void updateStatus(DigitalIdStatus newStatus, String reason) {
        digitalId.updateStatus(newStatus, reason);
    }
}
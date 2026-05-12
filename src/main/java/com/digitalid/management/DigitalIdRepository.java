package com.digitalid.management;

import com.digitalid.model.DigitalId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Defines the persistence contract for {@link DigitalId} objects.
 * Implementations are responsible for storing and retrieving identities by their UUID.
 */
public interface DigitalIdRepository {

    /**
     * Persists a Digital ID, replacing any previously stored entry with the same UUID.
     *
     * @param digitalId the Digital ID to store; must not be null
     */
    void save(DigitalId digitalId);

    /**
     * Retrieves a Digital ID by its UUID.
     *
     * @param id the UUID to look up
     * @return an {@link Optional} containing the matching {@link DigitalId}, or empty if not found
     */
    Optional<DigitalId> findById(UUID id);

    /**
     * Returns all stored Digital IDs as an unmodifiable list.
     * The order of elements is not guaranteed.
     *
     * @return an unmodifiable list of all {@link DigitalId} entries in the repository
     */
    List<DigitalId> findAll();
}
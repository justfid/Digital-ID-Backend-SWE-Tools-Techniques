package com.digitalid.management;

import com.digitalid.model.DigitalId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DigitalIdRepository {

    void save(DigitalId digitalId);

    Optional<DigitalId> findById(UUID id);

    List<DigitalId> findAll();
}
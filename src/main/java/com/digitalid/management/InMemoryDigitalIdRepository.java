package com.digitalid.management;

import com.digitalid.model.DigitalId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryDigitalIdRepository implements DigitalIdRepository {

    private final Map<UUID, DigitalId> store = new HashMap<>();

    @Override
    public void save(DigitalId digitalId) {
        store.put(digitalId.getId(), digitalId);
    }

    @Override
    public Optional<DigitalId> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<DigitalId> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }
}
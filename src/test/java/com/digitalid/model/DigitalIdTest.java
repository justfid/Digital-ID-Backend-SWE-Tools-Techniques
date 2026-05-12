package com.digitalid.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DigitalIdTest {

    private DigitalId digitalId;

    @BeforeEach
    void setUp() {
        digitalId = new DigitalId.Builder("NID123456", LocalDate.of(1990, 6, 15), "Jane Doe").build();
    }

    @Test
    void should_haveActiveStatus_when_newDigitalIdCreated() {
        assertEquals(DigitalIdStatus.ACTIVE, digitalId.getStatus());
    }

    @Test
    void should_haveNonNullUuidId_when_newDigitalIdCreated() {
        assertNotNull(digitalId.getId());
        assertInstanceOf(UUID.class, digitalId.getId());
    }

    @Test
    void should_generateDifferentIds_when_twoDigitalIdsCreated() {
        DigitalId second = new DigitalId.Builder("NID999999", LocalDate.of(1985, 3, 22), "John Smith").build();

        assertNotEquals(digitalId.getId(), second.getId());
    }

    @Test
    void should_haveNonEmptyStatusHistory_when_newDigitalIdCreated() {
        assertFalse(digitalId.getStatusHistory().isEmpty());
    }

    @Test
    void should_recordActiveStatusWithInitialCreationReason_when_checkingFirstStatusEntry() {
        StatusEntry first = digitalId.getStatusHistory().get(0);

        assertEquals(DigitalIdStatus.ACTIVE, first.getStatus());
        assertEquals("Initial creation", first.getReason());
    }

    @Test
    void should_throwUnsupportedOperationException_when_attemptingToModifyStatusHistory() {
        List<StatusEntry> history = digitalId.getStatusHistory();
        StatusEntry dummy = new StatusEntry(DigitalIdStatus.SUSPENDED, LocalDateTime.now(), "test");

        // UnsupportedOperationException from Collections.unmodifiableList carries no message
        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
                () -> history.add(dummy));

        assertNull(ex.getMessage());
    }

    @Test
    void should_storeImmutableFieldsFromBuilder_when_digitalIdCreated() {
        assertEquals("Jane Doe", digitalId.getFullName());
        assertEquals("NID123456", digitalId.getNationalIdNumber());
        assertEquals(LocalDate.of(1990, 6, 15), digitalId.getDateOfBirth());
    }

    @Test
    void should_haveNullAddressAndEmail_when_optionalFieldsNotProvided() {
        assertNull(digitalId.getAddress());
        assertNull(digitalId.getEmail());
    }

    @Test
    void should_defaultTemporaryRestrictionToFalse_when_newDigitalIdCreated() {
        assertFalse(digitalId.isTemporaryRestriction());
    }

    @Test
    void should_storeAndReturnAllFields_when_statusEntryCreated() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 5, 8, 12, 0, 0);

        StatusEntry entry = new StatusEntry(DigitalIdStatus.SUSPENDED, timestamp, "Fraud investigation");

        assertEquals(DigitalIdStatus.SUSPENDED, entry.getStatus());
        assertEquals(timestamp, entry.getTimestamp());
        assertEquals("Fraud investigation", entry.getReason());
    }
}
package com.digitalid.audit;

import com.digitalid.auth.Operations;
import com.digitalid.model.OrganisationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuditLoggerTest {

    private AuditLogger logger;
    private final UUID knownId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        logger = new InMemoryAuditLogger();
    }

    @Test
    void should_returnEmptyLog_when_noEventsLogged() {
        assertTrue(logger.getLog().isEmpty());
    }

    @Test
    void should_returnOneEvent_when_oneEventLogged() {
        logger.log(OrganisationType.CENTRAL_AUTHORITY, Operations.CREATE_IDENTITY, knownId, true, "Created");

        assertEquals(1, logger.getLog().size());
    }

    @Test
    void should_recordCorrectOrganisationType_when_eventLogged() {
        logger.log(OrganisationType.TAX_AUTHORITY, Operations.CHECK_TAX_ELIGIBILITY, knownId, true, "Eligible");

        assertEquals(OrganisationType.TAX_AUTHORITY, logger.getLog().get(0).getOrganisationType());
    }

    @Test
    void should_recordCorrectOperationName_when_eventLogged() {
        logger.log(OrganisationType.EMPLOYER, Operations.CHECK_VALIDITY, knownId, true, "Valid");

        assertEquals(Operations.CHECK_VALIDITY, logger.getLog().get(0).getOperationName());
    }

    @Test
    void should_recordCorrectSuccessFlag_when_eventLogged() {
        logger.log(OrganisationType.BANK, Operations.CHECK_VALIDITY, knownId, false, "Invalid");

        assertFalse(logger.getLog().get(0).isSuccess());
    }

    @Test
    void should_recordCorrectDetails_when_eventLogged() {
        logger.log(OrganisationType.CENTRAL_AUTHORITY, Operations.SUSPEND, knownId, true, "Suspended for fraud");

        assertEquals("Suspended for fraud", logger.getLog().get(0).getDetails());
    }

    @Test
    void should_recordNonNullTimestamp_when_eventLogged() {
        logger.log(OrganisationType.CENTRAL_AUTHORITY, Operations.REVOKE, knownId, true, "Revoked");

        assertNotNull(logger.getLog().get(0).getTimestamp());
    }

    @Test
    void should_recordNonNullEventId_when_eventLogged() {
        logger.log(OrganisationType.CENTRAL_AUTHORITY, Operations.CREATE_IDENTITY, knownId, true, "Created");

        assertNotNull(logger.getLog().get(0).getEventId());
        assertInstanceOf(UUID.class, logger.getLog().get(0).getEventId());
    }

    @Test
    void should_returnUnmodifiableList_when_getLogCalled() {
        logger.log(OrganisationType.CENTRAL_AUTHORITY, Operations.CREATE_IDENTITY, knownId, true, "Created");
        List<AuditEvent> log = logger.getLog();
        AuditEvent dummy = new AuditEvent(OrganisationType.EMPLOYER, Operations.CHECK_VALIDITY, null, true, "test");

        // UnsupportedOperationException from Collections.unmodifiableList carries no message
        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
                () -> log.add(dummy));

        assertNull(ex.getMessage());
    }

    @Test
    void should_returnEventsForCorrectId_when_getLogForIdCalled() {
        logger.log(OrganisationType.CENTRAL_AUTHORITY, Operations.CREATE_IDENTITY, knownId, true, "Created");

        List<AuditEvent> result = logger.getLogForId(knownId);

        assertEquals(1, result.size());
        assertEquals(knownId, result.get(0).getDigitalIdId());
    }

    @Test
    void should_returnEmptyList_when_getLogForIdCalledWithUnknownId() {
        logger.log(OrganisationType.CENTRAL_AUTHORITY, Operations.CREATE_IDENTITY, knownId, true, "Created");

        List<AuditEvent> result = logger.getLogForId(UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    void should_allowNullDigitalIdId_when_eventDoesNotRelateToSpecificId() {
        logger.log(OrganisationType.CENTRAL_AUTHORITY, Operations.CREATE_IDENTITY, null, false, "Validation failed");

        AuditEvent event = logger.getLog().get(0);

        assertNull(event.getDigitalIdId());
    }

    @Test
    void should_returnMultipleEvents_when_multipleEventsLogged() {
        logger.log(OrganisationType.CENTRAL_AUTHORITY, Operations.CREATE_IDENTITY, knownId, true, "Created");
        logger.log(OrganisationType.CENTRAL_AUTHORITY, Operations.SUSPEND, knownId, true, "Suspended");
        logger.log(OrganisationType.EMPLOYER, Operations.CHECK_VALIDITY, knownId, true, "Valid");

        assertEquals(3, logger.getLog().size());
    }

    @Test
    void should_returnOnlyMatchingEvents_when_getLogForIdCalledWithSpecificId() {
        UUID otherId = UUID.randomUUID();
        logger.log(OrganisationType.CENTRAL_AUTHORITY, Operations.CREATE_IDENTITY, knownId, true, "Created A");
        logger.log(OrganisationType.CENTRAL_AUTHORITY, Operations.CREATE_IDENTITY, otherId, true, "Created B");
        logger.log(OrganisationType.CENTRAL_AUTHORITY, Operations.SUSPEND, knownId, true, "Suspended A");

        List<AuditEvent> result = logger.getLogForId(knownId);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(e -> knownId.equals(e.getDigitalIdId())));
    }
}
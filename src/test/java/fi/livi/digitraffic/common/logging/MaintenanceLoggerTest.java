package fi.livi.digitraffic.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static fi.livi.digitraffic.common.logging.otel.OpenTelemetryLogging.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static fi.livi.digitraffic.common.logging.LoggingTestUtils.captureSingleLogEvent;
import static fi.livi.digitraffic.common.logging.LoggingTestUtils.keyValues;

class MaintenanceLoggerTest {
    @Test
    void logCleanupWritesSuccessAndMaintenanceFields() {
        final ILoggingEvent event = captureSingleLogEvent(MaintenanceLogger.class, () ->
            MaintenanceLogger.logCleanup("vessel", 12, 100, 1.25));
        final Map<String, Object> keyValues = keyValues(event);

        assertEquals(ch.qos.logback.classic.Level.INFO, event.getLevel());
        assertEquals(Outcome.VALUE_SUCCESS, keyValues.get(Outcome.KEY));
        assertEquals(true, keyValues.get(DbMaintenance.KEY_CLEANUP));
        assertEquals("vessel", keyValues.get(DbMaintenance.KEY_TABLENAME));
        assertEquals(12, keyValues.get(DbMaintenance.KEY_DELETED_COUNT));
        assertEquals(100, keyValues.get(DbMaintenance.KEY_BATCH_SIZE));
        assertEquals(1.25, keyValues.get(KEY_DURATION));
    }

    @Test
    void logErrorWritesFailureFieldsAndCause() {
        final RuntimeException cause = new RuntimeException("cleanup failed");
        final ILoggingEvent event = captureSingleLogEvent(MaintenanceLogger.class, () ->
            MaintenanceLogger.logError("vessel", 0.5, cause));
        final Map<String, Object> keyValues = keyValues(event);

        assertEquals(ch.qos.logback.classic.Level.ERROR, event.getLevel());
        assertEquals(Outcome.VALUE_ERROR, keyValues.get(Outcome.KEY));
        assertEquals(true, keyValues.get(DbMaintenance.KEY_CLEANUP));
        assertEquals("vessel", keyValues.get(DbMaintenance.KEY_TABLENAME));
        assertEquals(0.5, keyValues.get(KEY_DURATION));
        assertEquals(cause.getClass().getName(), event.getThrowableProxy().getClassName());
        assertEquals(cause.getMessage(), event.getThrowableProxy().getMessage());
    }

}

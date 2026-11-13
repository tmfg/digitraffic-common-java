package fi.livi.digitraffic.common.logging;

import fi.livi.digitraffic.common.logging.otel.OpenTelemetryLogging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaintenanceLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceLogger.class);

    public static void logCleanup(final String tableName, final int deletedCount, final int batchSize, final double seconds) {
        LOGGER.atInfo()
            .addKeyValue(OpenTelemetryLogging.Outcome.KEY, OpenTelemetryLogging.Outcome.VALUE_SUCCESS)
            .addKeyValue(OpenTelemetryLogging.DbMaintenance.KEY_CLEANUP, true)
            .addKeyValue(OpenTelemetryLogging.DbMaintenance.KEY_TABLENAME, tableName)
            .addKeyValue(OpenTelemetryLogging.DbMaintenance.KEY_DELETED_COUNT, deletedCount)
            .addKeyValue(OpenTelemetryLogging.DbMaintenance.KEY_BATCH_SIZE, batchSize)
            .addKeyValue(OpenTelemetryLogging.KEY_DURATION, seconds)
            .log();
    }

    public static void logError(final String tableName, final double seconds, final Throwable e) {
        LOGGER.atError()
            .addKeyValue(OpenTelemetryLogging.Outcome.KEY, OpenTelemetryLogging.Outcome.VALUE_ERROR)
            .addKeyValue(OpenTelemetryLogging.DbMaintenance.KEY_CLEANUP, true)
            .addKeyValue(OpenTelemetryLogging.DbMaintenance.KEY_TABLENAME, tableName)
            .addKeyValue(OpenTelemetryLogging.KEY_DURATION, seconds)
            .setCause(e)
            .log();
    }
}

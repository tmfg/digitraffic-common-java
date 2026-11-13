package fi.livi.digitraffic.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import fi.livi.digitraffic.common.logging.otel.OpenTelemetryLogging;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointProperties;

import java.util.Map;

import static fi.livi.digitraffic.common.logging.otel.OpenTelemetryLogging.*;
import static org.junit.jupiter.api.Assertions.*;
import static fi.livi.digitraffic.common.logging.LoggingTestUtils.keyValues;

class HttpLoggerTest {
    @Test
    void urlTemplateIsLoggedWithoutSchemeOrHost() {
        final ILoggingEvent event = captureSingleLogEvent("ais", "https://example.test/v1/messages/{id}", logger -> logger.error(new IllegalStateException("boom")));
        final Map<String, Object> keyValues = keyValues(event);

        assertEquals("/v1/messages/{id}", keyValues.get(Http.KEY_URL_TEMPLATE));
    }

    @Test
    void contextIsLoggedInSnakeCase() {
        final ILoggingEvent event = captureSingleLogEvent("Ais PortForward", logger -> logger.error(new IllegalStateException("boom")));
        final Map<String, Object> keyValues = keyValues(event);

        assertEquals("ais_port_forward", keyValues.get(OpenTelemetryLogging.KEY_CONTEXT));
    }

    @Test
    void errorLogsExceptionTypeAndDefaultStatusCode() {
        final ILoggingEvent event = captureSingleLogEvent(logger -> logger.error(new IllegalStateException("boom")));
        final Map<String, Object> keyValues = keyValues(event);

        assertEquals("IllegalStateException", keyValues.get(Http.KEY_ERROR_TYPE));
        assertEquals(-1, keyValues.get(Http.KEY_STATUS_CODE));
    }

    private static ILoggingEvent captureSingleLogEvent(
        final String context,
        final java.util.function.Consumer<HttpLogger> action) {
        return captureSingleLogEvent(context, "/v1/messages", action);
    }

    private static ILoggingEvent captureSingleLogEvent(
        final String context,
        final String urlTemplate,
        final java.util.function.Consumer<HttpLogger> action) {
        return LoggingTestUtils.captureSingleLogEvent(HttpLogger.class, () -> {
            final HttpLogger logger = new HttpLogger(new HttpLogger.Parameters(
                context,
                "https://example.test/v1/messages",
                "trace-123",
                urlTemplate));
            action.accept(logger);
        });
    }

    private static ILoggingEvent captureSingleLogEvent(final java.util.function.Consumer<HttpLogger> action) {
        return captureSingleLogEvent("ais", action);
    }
}

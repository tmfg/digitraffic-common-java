package fi.livi.digitraffic.common.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.event.KeyValuePair;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import static fi.livi.digitraffic.common.logging.OpenTelemetryLogging.Http;
import static fi.livi.digitraffic.common.logging.OpenTelemetryLogging.KEY_CONTEXT;
import static fi.livi.digitraffic.common.logging.OpenTelemetryLogging.KEY_METHOD;
import static fi.livi.digitraffic.common.logging.OpenTelemetryLogging.KEY_TRACE_ID;

class HttpLoggerTest {
    @Test
    void endLogsStatusCodeAndMetadata() {
        final ILoggingEvent event = captureSingleLogEvent(logger -> logger.end(204));
        final Map<String, Object> keyValues = keyValues(event);

        assertEquals("HttpLogger.logStatus", keyValues.get(KEY_METHOD));
        assertEquals("ais", keyValues.get(KEY_CONTEXT));
        assertEquals("https://example.test/v1/messages", keyValues.get(Http.KEY_URL));
        assertEquals("/v1/messages", keyValues.get(Http.KEY_URL_TEMPLATE));
        assertEquals(204, keyValues.get(Http.KEY_STATUS_CODE));
        assertNull(keyValues.get(Http.KEY_ERROR_TYPE));
        assertEquals("trace-123", keyValues.get(KEY_TRACE_ID));

        assertNotNull(keyValues.get(Http.KEY_DURATION));
        assertTrue((double) keyValues.get(Http.KEY_DURATION) >= 0.0);
    }

    @Test
    void errorLogsExceptionTypeAndDefaultStatusCode() {
        final ILoggingEvent event = captureSingleLogEvent(logger -> logger.error(new IllegalStateException("boom")));
        final Map<String, Object> keyValues = keyValues(event);

        assertEquals("IllegalStateException", keyValues.get(Http.KEY_ERROR_TYPE));
        assertEquals(-1, keyValues.get(Http.KEY_STATUS_CODE));
    }

    private static ILoggingEvent captureSingleLogEvent(final java.util.function.Consumer<HttpLogger> action) {
        final Logger log = (Logger) LoggerFactory.getLogger(HttpLogger.class);
        final ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.setContext(log.getLoggerContext());
        appender.start();
        log.addAppender(appender);

        try {
            final HttpLogger logger = new HttpLogger(new HttpLogger.Parameters(
                "ais",
                "https://example.test/v1/messages",
                "trace-123",
                "/v1/messages"));
            action.accept(logger);

            assertEquals(1, appender.list.size());
            return appender.list.getFirst();
        } finally {
            log.detachAppender(appender);
            appender.stop();
        }
    }

    private static Map<String, Object> keyValues(final ILoggingEvent event) {
        return event.getKeyValuePairs().stream()
            .collect(LinkedHashMap::new, (map, pair) -> map.put(pair.key, pair.value), Map::putAll);
    }
}


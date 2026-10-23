package fi.livi.digitraffic.common.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

import static fi.livi.digitraffic.common.logging.OpenTelemetryLogging.*;
import static org.junit.jupiter.api.Assertions.*;

class HttpLoggerTest {
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


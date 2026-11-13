package fi.livi.digitraffic.common.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class LoggingTestUtils {
    private LoggingTestUtils() {
    }

    static ILoggingEvent captureSingleLogEvent(final Class<?> loggerClass, final Runnable action) {
        final Logger log = (Logger) LoggerFactory.getLogger(loggerClass);
        final ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.setContext(log.getLoggerContext());
        appender.start();
        log.addAppender(appender);

        try {
            action.run();
            assertEquals(1, appender.list.size());
            return appender.list.getFirst();
        } finally {
            log.detachAppender(appender);
            appender.stop();
        }
    }

    static Map<String, Object> keyValues(final ILoggingEvent event) {
        return event.getKeyValuePairs().stream()
            .collect(LinkedHashMap::new, (map, pair) -> map.put(pair.key, pair.value), Map::putAll);
    }
}

package fi.livi.digitraffic.common.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import tools.jackson.core.JsonEncoding;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;

/**
 * The provider reads the fields back out of the message, so these rules decide what ends up indexed.
 */
class LogFieldsTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();
    private final LoggerMessageKeyValuePairJsonProvider provider = new LoggerMessageKeyValuePairJsonProvider();

    @Test
    void givenAnEventWhenRenderedThenPairsAreSpaceSeparated() {
        final Map<String, Object> event = new LinkedHashMap<>();
        event.put("operation", "generateGtfs");
        event.put("rail.gtfs.segments.dummy", 3);
        event.put("rail.gtfs.feed.published", true);

        assertEquals("operation=generateGtfs rail.gtfs.segments.dummy=3 rail.gtfs.feed.published=true",
                LogFields.of(event));
    }

    @Test
    void givenAbsentValuesWhenRenderedThenTheFieldSurvivesAsNull() {
        // Given success and error events must share one field set
        final Map<String, Object> event = new LinkedHashMap<>();
        event.put("error.type", "");
        event.put("rail.gtfs.feeds.degraded", null);

        // A blank value would otherwise be dropped by the provider
        assertEquals("error.type=NULL rail.gtfs.feeds.degraded=NULL", LogFields.of(event));
    }

    @Test
    void givenAStringValueWithASpaceWhenRenderedThenItIsQuotedAutomatically() {
        final Map<String, Object> event = new LinkedHashMap<>();
        event.put("error.message", "too many concurrent operations");
        event.put("next", 1);

        final Map<String, Object> json = toJson(LogFields.of(event));

        assertEquals("too many concurrent operations", json.get("error.message"));
        assertEquals(1, json.get("next"));
    }

    @Test
    void givenAStringValueWithAnEqualsSignWhenRenderedThenTheNextFieldIsNotSwallowed() {
        final Map<String, Object> event = new LinkedHashMap<>();
        event.put("query", "a=b");
        event.put("next", 1);

        final Map<String, Object> json = toJson(LogFields.of(event));

        assertEquals("a=b", json.get("query"));
        assertEquals(1, json.get("next"));
    }

    @Test
    void givenTheLiteralStringNullWhenRenderedThenItIsKeptAsAString() {
        // Unquoted this would be read back as a JSON null, same as a real absent value
        assertEquals("NULL", toJson(LogFields.of(Map.of("value", "NULL"))).get("value"));
    }

    @Test
    void givenAPlainStringValueWhenRenderedThenItIsNotQuoted() {
        // A value that needs no escaping is left as is, so existing logs and dashboards do not change
        assertEquals("operation=generateGtfs", LogFields.of(Map.of("operation", "generateGtfs")));
    }

    @Test
    void givenANotANumberOrInfiniteValueWhenRenderedThenTheFieldIsAbsent() {
        // Infinity round-trips through the provider as a String, NaN as a Double - a field would flip
        // type between runs if either were logged as is, so neither is treated as a measurement
        final Map<String, Object> event = new LinkedHashMap<>();
        event.put("duration", Double.NaN);
        event.put("rate", Double.POSITIVE_INFINITY);
        event.put("delta", Float.NEGATIVE_INFINITY);

        assertEquals("duration=NULL rate=NULL delta=NULL", LogFields.of(event));
    }

    @Test
    void givenAnAbsentValueWhenReadBackThenTheFieldIsAJsonNull() {
        // A JSON null does not create a mapping, so it cannot lock the field to the wrong type
        assertNull(toJson("duration=NULL").get("duration"));
    }

    @Test
    void givenMillisecondsWhenConvertedThenTheValueIsInSeconds() {
        assertEquals(1.234, LogFields.durationSeconds(1234));
        assertEquals(0.001, LogFields.durationSeconds(1));
        assertEquals(60.0, LogFields.durationSeconds(60_000));
    }

    @Test
    void givenADurationWhenConvertedThenTheValueIsInSeconds() {
        assertEquals(1.4, LogFields.durationSeconds(Duration.ofMillis(1400)));
        assertEquals(0.0, LogFields.durationSeconds(null));
    }

    @Test
    void givenAWholeNumberOfSecondsWhenReadBackThenTheFieldIsStillADecimal() {
        // An integral 0 would map the field as a long in OpenSearch and truncate every later decimal
        assertEquals(0.0, toJson("duration=" + LogFields.durationSeconds(0)).get("duration"));
        assertEquals(60.0, toJson("duration=" + LogFields.durationSeconds(60_000)).get("duration"));
    }

    @Test
    void givenALongDurationWhenReadBackThenScientificNotationIsStillANumber() {
        // Double.toString switches to this notation at 10^7
        assertEquals(1.0E7, toJson("duration=" + LogFields.durationSeconds(10_000_000_000L)).get("duration"));
    }

    @Test
    void givenAValueWithSpacesWhenQuotedThenItSurvivesWhole() {
        assertEquals("too many concurrent operations",
                toJson("error.message=" + LogFields.quoted("too many concurrent operations")).get("error.message"));
    }

    @Test
    void givenAValueWithQuotesWhenQuotedThenTheQuotesSurvive() {
        assertEquals("expected \"2.0\" here",
                toJson("error.message=" + LogFields.quoted("expected \"2.0\" here")).get("error.message"));
    }

    @Test
    void givenAMultiLineValueWhenQuotedThenItBecomesOneLine() {
        // A newline would end the log line and lose everything after it
        assertEquals("first second",
                toJson("error.message=" + LogFields.quoted("first\nsecond")).get("error.message"));
    }

    @Test
    void givenAValueWithTrailingBackslashWhenQuotedThenTheNextFieldIsStillRead() {
        final Map<String, Object> json = toJson("a=" + LogFields.quoted("path\\") + " b=2");

        assertEquals("path\\", json.get("a"));
        assertEquals(2, json.get("b"));
    }

    @Test
    void givenABlankValueWhenQuotedThenItBecomesNull() {
        assertEquals("NULL", LogFields.quoted("  "));
        assertEquals("NULL", LogFields.quoted(null));
    }

    private Map<String, Object> toJson(final String formattedMessage) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (final JsonGenerator generator = objectMapper.createGenerator(out, JsonEncoding.UTF8)) {
            generator.writeStartObject();
            provider.writeTo(generator, createEvent(formattedMessage));
            generator.writeEndObject();
        }
        return objectMapper.readValue(out.toString(StandardCharsets.UTF_8), new TypeReference<>() {
        });
    }

    private LoggingEvent createEvent(final String formattedMessage) {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        return new LoggingEvent(getClass().getName(), context.getLogger(getClass()), Level.INFO,
                formattedMessage, null, null);
    }
}

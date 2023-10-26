package fi.livi.digitraffic.common.util;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public final class TimeUtil {


    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");

    public static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final DateTimeFormatter HTTP_DATE_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME;

    public static final ZoneId FINLAND_ZONE = ZoneId.of("Europe/Helsinki");
    public static final ZoneId GMT_ZONE = ZoneId.of("GMT");

    public static final String LAST_MODIFIED_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final DateTimeFormatter LAST_MODIFIED_FORMATTER =
        DateTimeFormatter.ofPattern(LAST_MODIFIED_FORMAT, Locale.US).withZone(GMT_ZONE);

    public static final DateTimeFormatter ISO_DATE_TIME_WITH_MILLIS_AT_UTC =
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .append(DateTimeFormatter.ISO_LOCAL_DATE)
                    .appendLiteral('T')
                    .appendValue(HOUR_OF_DAY, 2)
                    .appendLiteral(':')
                    .appendValue(MINUTE_OF_HOUR, 2)
                    .appendLiteral(':')
                    .appendValue(SECOND_OF_MINUTE, 2)
                    .appendLiteral('.')
                    .appendFraction(MILLI_OF_SECOND, 3, 3, false)
                    .appendLiteral('Z')
                    .toFormatter()
                    .withZone(UTC);

    private TimeUtil() {}

    public static long millisBetween(final ZonedDateTime from, final ZonedDateTime to) {
        return millisBetween(Objects.requireNonNull(from).toInstant(), Objects.requireNonNull(to).toInstant());
    }

    public static long millisBetween(final Instant from, final Instant to) {
        return Objects.requireNonNull(to).toEpochMilli() - Objects.requireNonNull(from).toEpochMilli();
    }

    /**
     * Returns given time as a string(yyyyMMdd).  This will happen in the timezone the given
     * time is!  There will be no zone information in the returned string.
     */
    public static String dateToString(final String datePrefix, final ZonedDateTime timestamp) {
        final String prefix = datePrefix == null ? "" : String.format("%s=", datePrefix);
        return timestamp == null ? "" : String.format("%s%s", prefix, timestamp.format(DATE_FORMATTER));
    }

    /**
     * Returns given time as a string(HHmmss). This will happen in the timezone the given
     * time is and seconds are not rounded! There will be no zone information in the returned string.
     */
    public static String timeToString(final String timePrefix, final ZonedDateTime timestamp) {
        final String prefix = timePrefix == null ? "" : String.format("%s=", timePrefix);
        return timestamp == null ? "" : String.format("%s%s", prefix, timestamp.format(TIME_FORMATTER));
    }

    public static Instant toInstant(final ZonedDateTime from) {
        return from != null ? from.toInstant() : null;
    }

    public static Instant withoutMillis(final Instant from) {
        return from != null ? Instant.ofEpochSecond(from.getEpochSecond()) : null;
    }

    /**
     * Convert from "YYYY-MM-DD" to HTTP date header format:
     * <day-name>, <day> <month> <year> <hour>:<minute>:<second> GMT
     * I.e. Wed, 01 Jun 2022 08:00:00 GMT
     */
    public static String isoLocalDateToHttpDateTime(final String isoLocalDate) {
        if (isoLocalDate == null) {
            return null;
        }
        final LocalDate parsedDate = LocalDate.parse(isoLocalDate, ISO_DATE_FORMATTER);
        return HTTP_DATE_FORMATTER.format(parsedDate.atStartOfDay(GMT_ZONE));
    }

    public static String getInLastModifiedHeaderFormat(final Instant instant) {
        return LAST_MODIFIED_FORMATTER.format(instant);
    }

    public static Instant getGreatest(final Instant first, final Instant second) {
        if (first == null) {
            return second;
        } else if(second == null || first.isAfter(second)) {
            return first;
        }
        return second;
    }


    public static ZonedDateTime toZonedDateTimeAtUtc(final XMLGregorianCalendar calendar) {
        return calendar == null ? null : toZonedDateTimeAtUtc(calendar.toGregorianCalendar().toInstant());
    }

    public static Instant toInstant(final XMLGregorianCalendar calendar) {
        return calendar == null ? null : calendar.toGregorianCalendar().toInstant();
    }

    public static Instant toInstant(final long epochMillis) {
        return Instant.ofEpochMilli(epochMillis);
    }

    public static Instant toInstantWithOutMillis(final long epochMillis) {
        return withoutMillis(Instant.ofEpochMilli(epochMillis));
    }

    public static Instant toInstantWithOutMillis(final ZonedDateTime time) {
        if (time == null) {
            return null;
        }
        return withoutMillis(Instant.ofEpochSecond(time.toEpochSecond()));
    }

    public static Instant toInstantWithOutMillis(final Instant time) {
        if (time == null) {
            return null;
        }
        return withoutMillis(Instant.ofEpochSecond(time.getEpochSecond()));
    }

    /**
     * Needed because some fields in db are Oracle Date type and Date won't have millis.
     */
    public static ZonedDateTime toZonedDateTimeWithoutMillisAtUtc(final XMLGregorianCalendar calendar)  {
        if (calendar != null) {
            try {
                final XMLGregorianCalendar calSeconds =
                        DatatypeFactory.newInstance().newXMLGregorianCalendar(
                                calendar.getYear(),
                                calendar.getMonth(),
                                calendar.getDay(),
                                calendar.getHour(),
                                calendar.getMinute(),
                                calendar.getSecond(),
                                0,
                                calendar.getTimezone());
                return toZonedDateTimeAtUtc(calSeconds);
            } catch (final DatatypeConfigurationException e) {
                throw new IllegalArgumentException("Failed to convert XMLGregorianCalendar " + calendar + " to XMLGregorianCalendar with out millis.", e);
            }
        }
        return null;
    }

    public static ZonedDateTime toZonedDateTimeAtUtc(final ZonedDateTime zonedDateTime) {
        return zonedDateTime == null ? null : toZonedDateTimeAtUtc(zonedDateTime.toInstant());
    }

    public static ZonedDateTime toZonedDateTimeAtUtc(final Instant instant) {
        return instant == null ? null : instant.atZone(UTC);
    }

    public static ZonedDateTime toZonedDateTimeAtUtc(final long epochMillis) {
        return toZonedDateTimeAtUtc(Instant.ofEpochMilli(epochMillis));
    }

    public static ZonedDateTime toZonedDateTimeAtUtc(final Date from) {
        return from == null ? null : toZonedDateTimeAtUtc(from.toInstant());
    }

    public static XMLGregorianCalendar toXMLGregorianCalendarAtUtc(final ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        return toXMLGregorianCalendarAtUtc(zonedDateTime.toInstant());
    }

    public static XMLGregorianCalendar toXMLGregorianCalendarAtUtc(final Instant from) {
        if (from != null) {
            final GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            cal.setTimeInMillis(from.toEpochMilli());
            try {
                return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
            } catch (final DatatypeConfigurationException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static Instant nowWithoutNanos() {
        return withoutNanos(Instant.now());
    }

    public static Instant nowWithoutMillis() {
        return withoutMillis(Instant.now());
    }
    public static ZonedDateTime getZonedDateTimeNowAtUtc() {
        return toZonedDateTimeAtUtc(Instant.now());
    }

    public static ZonedDateTime getZonedDateTimeNowWithoutMillisAtUtc() {
        return withoutMillisAtUtc(toZonedDateTimeAtUtc(Instant.now()));
    }

    public static Instant withoutNanos(final Instant from) {
        if (from != null) {
            return Instant.ofEpochMilli(from.toEpochMilli());
        }
        return null;
    }


    public static ZonedDateTime withoutMillisAtUtc(final ZonedDateTime from) {
        if (from != null) {
            return toZonedDateTimeAtUtc(from.with(MILLI_OF_SECOND, 0));
        }
        return null;
    }

    public static ZonedDateTime toZonedDateTimeWithoutMillisAtUtc(final Instant from) {
        if (from != null) {
            return toZonedDateTimeAtUtc(withoutMillis(from));
        }
        return null;
    }

    public static String toIsoDateTimeWithMillisAtUtc(final Instant from) {
        return ISO_DATE_TIME_WITH_MILLIS_AT_UTC.format(from);
    }

    public static Timestamp toSqlTimestamp(final ZonedDateTime zonedDateTime) {
        return zonedDateTime == null ? null : Timestamp.from(zonedDateTime.toInstant());
    }

    public static Instant appendMillis(final Instant time, final long millis) {
        if (time == null) {
            return null;
        }
        return time.plusMillis(millis);
    }

    public static Instant toInstant(final Timestamp value) {
        return value == null ? null : value.toInstant();
    }

    public static Instant roundInstantSeconds(final Instant from) {
        if ( from == null) {
            return null;
        }
        return Instant.ofEpochSecond(from.getEpochSecond() + (from.getNano() >= 500000000 ? 1 : 0));
    }

    public static Instant floorInstantSeconds(final Instant from) {
        if ( from == null) {
            return null;
        }
        return Instant.ofEpochSecond(from.getEpochSecond());
    }

    public static Instant ceilInstantSeconds(final Instant from) {
        if ( from == null) {
            return null;
        }
        return Instant.ofEpochSecond(from.getEpochSecond() + (from.getNano() > 0 ? 1 : 0));
    }

    public static Long getEpochSeconds(final Instant time) {
        return time == null ? null : time.getEpochSecond();
    }

}

package fi.livi.digitraffic.common.util;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import fi.livi.digitraffic.test.util.TestUtil;

public final class TimeUtilTest {

    final private static String TXT_2023_10_25T09_10_11_900_EEST = "2023-10-25T09:10:11.900+03:00";
    final private static String TXT_2023_10_25T09_10_11_000_EEST = "2023-10-25T09:10:11.000+03:00";
    final private static String ISO_2023_10_25T09_70_11_900Z = "2023-10-25T06:10:11.900Z";
    final private static ZonedDateTime ZDT_2023_10_25T09_10_11_900_EEST = ZonedDateTime.parse(TXT_2023_10_25T09_10_11_900_EEST);
    final private static Instant I_2023_10_25T09_70_11_900Z = Instant.parse(TXT_2023_10_25T09_10_11_900_EEST);
    final private static Instant I_2023_10_25T09_70_11_000Z = Instant.parse(TXT_2023_10_25T09_10_11_000_EEST);

    private static final String DATE_STRING_OFFSET_2 = "2016-01-22T10:00:01+02:00";
    private static final String DATE_STRING_Z = "2016-01-22T08:00:01Z";
    private static final String DATE_STRING_MILLIS_Z = "2016-01-22T08:00:01.500Z";
    private static final String XML_DATE_STRING_Z = "2016-01-22T08:00:01.000Z";

    @Test
    public void millisBetweenNulls() {
        Assertions.assertThrows(NullPointerException.class, () -> TimeUtil.millisBetween(null, ZonedDateTime.now()));
        Assertions.assertThrows(NullPointerException.class, () -> TimeUtil.millisBetween(ZonedDateTime.now(), null));
        Assertions.assertThrows(NullPointerException.class, () -> TimeUtil.millisBetween(null, Instant.now()));
        Assertions.assertThrows(NullPointerException.class, () -> TimeUtil.millisBetween(Instant.now(), null));
        Assertions.assertThrows(NullPointerException.class, () -> TimeUtil.millisBetween((ZonedDateTime) null, null));
        Assertions.assertThrows(NullPointerException.class, () -> TimeUtil.millisBetween((Instant) null, null));
    }

    @Test
    public void millisBetweenZDT() {
        final ZonedDateTime zdt1 = ZonedDateTime.now();
        final ZonedDateTime zdt2 = zdt1.plusSeconds(new Random().nextInt(1000));

        Assertions.assertTrue(TimeUtil.millisBetween(zdt1, zdt2) > 0);
        Assertions.assertTrue(TimeUtil.millisBetween(zdt2, zdt1) < 0);
    }

    @Test
    public void millisBetween() {
        final int millis = TestUtil.getRandom(1, 1000001);
        final Instant i1 = Instant.now();
        final Instant i2 = i1.plusMillis(millis);

        Assertions.assertTrue(TimeUtil.millisBetween(i1, i2) > 0);
        Assertions.assertTrue(TimeUtil.millisBetween(i2, i1) < 0);
        Assertions.assertEquals(millis, TimeUtil.millisBetween(i1, i2));
    }

    @Test
    public void dateToStringZDT() {
        Assertions.assertEquals("", TimeUtil.dateToString("PREFIX", null));
        Assertions.assertEquals("PREFIX=20231025", TimeUtil.dateToString("PREFIX", ZDT_2023_10_25T09_10_11_900_EEST));
        Assertions.assertEquals("20231025", TimeUtil.dateToString(null, ZDT_2023_10_25T09_10_11_900_EEST));
    }

    @Test
    public void timeToStringNull() {
        Assertions.assertEquals("", TimeUtil.timeToString("PREFIX", null));
        Assertions.assertEquals("PREFIX=091011", TimeUtil.timeToString("PREFIX", ZDT_2023_10_25T09_10_11_900_EEST));
        Assertions.assertEquals("091011", TimeUtil.timeToString(null, ZDT_2023_10_25T09_10_11_900_EEST));
    }

    @Test
    public void toInstant() {
        Assertions.assertEquals(Instant.parse(TXT_2023_10_25T09_10_11_900_EEST), TimeUtil.toInstant(ZDT_2023_10_25T09_10_11_900_EEST));
        Assertions.assertNull(TimeUtil.toInstant((ZonedDateTime) null));
    }

    @Test
    public void withoutMillis() {
        Assertions.assertEquals(I_2023_10_25T09_70_11_000Z, TimeUtil.withoutMillis(I_2023_10_25T09_70_11_900Z));
        Assertions.assertNull(TimeUtil.withoutMillis(null));
    }

    @Test
    public void isoLocalDateToHttpDateTime() {
        Assertions.assertEquals("Wed, 25 Oct 2023 00:00:00 GMT", TimeUtil.isoLocalDateToHttpDateTime("2023-10-25"));
        Assertions.assertEquals("Wed, 1 Jun 2022 00:00:00 GMT", TimeUtil.isoLocalDateToHttpDateTime("2022-06-01"));
        Assertions.assertNull(TimeUtil.withoutMillis(null));
    }

    @Test
    public void getInLastModifiedHeaderFormat() throws ParseException {
        final Locale defaultLocale = Locale.getDefault();
        try {
            // parsing the below date string fails with locale en_FI in some environments
            Locale.setDefault(Locale.US);

            final String srcString = "Tue, 03 Sep 2019 13:56:36 GMT";

            final java.util.Date srcDate = DateUtils.parseDate(srcString, TimeUtil.LAST_MODIFIED_FORMAT);
            final Instant srcInstant = Instant.ofEpochMilli(srcDate.getTime());

            assertEquals(srcString, TimeUtil.getInLastModifiedHeaderFormat(srcInstant));
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

    @Test
    public void getGreatest() {
        final Instant now = Instant.now();
        final Instant older = now.minusNanos(1);

        assertEquals(now, TimeUtil.getGreatest(now, older));
        assertEquals(now, TimeUtil.getGreatest(older, now));
        assertEquals(now, TimeUtil.getGreatest(now, now));
    }

    @Test
    public void getGreatestNull() {
        final Instant now = Instant.now();
        assertEquals(now, TimeUtil.getGreatest(null, now));
        assertEquals(now, TimeUtil.getGreatest(now, null));
    }

    @Test
    public void getGreatestNulls() {
        //noinspection ConstantValue
        assertNull(TimeUtil.getGreatest(null, null));
    }

    @Test
    public void toZonedDateTimeAtUtc() throws DatatypeConfigurationException {
        final String DATE_STRING_WINTER = "2016-01-22T10:00:00+02:00";
        final String DATE_STRING_WINTER_Z = "2016-01-22T08:00:00Z";
        final String DATE_STRING_SUMMER = "2016-06-22T10:10:01.102+03:00";
        final String DATE_STRING_SUMMER_Z = "2016-06-22T07:10:01.102Z";

        final GregorianCalendar wc = GregorianCalendar.from((ZonedDateTime.parse(DATE_STRING_WINTER)));
        final ZonedDateTime winterTime = TimeUtil.toZonedDateTimeAtUtc(DatatypeFactory.newInstance().newXMLGregorianCalendar(wc));
        assertEquals(DATE_STRING_WINTER_Z, ISO_OFFSET_DATE_TIME.format(winterTime));

        final GregorianCalendar sc = GregorianCalendar.from((ZonedDateTime.parse(DATE_STRING_SUMMER)));
        final ZonedDateTime summerTime = TimeUtil.toZonedDateTimeAtUtc(DatatypeFactory.newInstance().newXMLGregorianCalendar(sc));
        assertEquals(DATE_STRING_SUMMER_Z, ISO_OFFSET_DATE_TIME.format(summerTime));
    }

    @Test
    public void toInstant_xmlGregorianCalendar() throws DatatypeConfigurationException {
        final GregorianCalendar wc = GregorianCalendar.from((ZonedDateTime.parse(DATE_STRING_OFFSET_2)));
        final Instant convertedInstant = TimeUtil.toInstant(DatatypeFactory.newInstance().newXMLGregorianCalendar(wc));
        assertEquals(DATE_STRING_Z, convertedInstant.toString());
    }

    @Test
    public void toLocalDate_xmlGregorianCalendar() throws DatatypeConfigurationException {
        final XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendarDate(2022, 1, 23, DatatypeConstants.FIELD_UNDEFINED);
        final LocalDate localDate = TimeUtil.toLocalDate(date);
        assertEquals("2022-01-23", localDate.toString());
    }

    @Test
    public void toInstant_epocMillis() {
        final ZonedDateTime zdt = ZonedDateTime.of(2019, 12, 1, 10, 15, 20, 500000000, ZoneOffset.UTC);
        final String DATE_STRING_NANOS = "2019-12-01T10:15:20.500Z";
        final Instant instant = TimeUtil.toInstant(zdt.toInstant().toEpochMilli());
        assertEquals(DATE_STRING_NANOS, instant.toString());
    }

    @Test
    public void toInstantWithOutMillis_epochMilli() {
        final Instant from = Instant.now();
        final Instant to = TimeUtil.toInstantWithOutMillis(from.toEpochMilli());
        assertEquals(Instant.ofEpochSecond(from.getEpochSecond()), to);
    }

    @Test
    public void toInstantWithOutMillis_zdt() {
        final ZonedDateTime from = ZonedDateTime.now();
        final Instant to = TimeUtil.toInstantWithOutMillis(from);
        assertEquals(Instant.ofEpochSecond(from.toEpochSecond()), to);
    }

    @Test
    public void toInstantWithOutMillis_instant() {
        final Instant from = Instant.now();
        final Instant to = TimeUtil.toInstantWithOutMillis(from);
        assertEquals(Instant.ofEpochSecond(from.getEpochSecond()), to);
    }

    @Test
    public void toZonedDateTimeWithoutMillisAtUtc_XmlGregorianCalendar() throws DatatypeConfigurationException {
        final GregorianCalendar gc = GregorianCalendar.from((ZonedDateTime.parse(DATE_STRING_OFFSET_2).plusNanos(500000000)));
        final XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);

        final ZonedDateTime zdtWithOutMillis = TimeUtil.toZonedDateTimeWithoutMillisAtUtc(xmlDate);
        assertEquals(DATE_STRING_Z, zdtWithOutMillis.toString());
    }

    @Test
    public void toInstantWithoutMillisAtUtc_XmlGregorianCalendar() throws DatatypeConfigurationException {
        final GregorianCalendar gc = GregorianCalendar.from((ZonedDateTime.parse(DATE_STRING_OFFSET_2).plusNanos(500000000)));
        final XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);

        final Instant zdtWithOutMillis = TimeUtil.toInstantWithoutMillis(xmlDate);
        assertEquals(DATE_STRING_Z, zdtWithOutMillis.toString());
    }

    @Test
    public void toZonedDateTimeAtUtc_zdt() {
        final ZonedDateTime from = ZonedDateTime.parse(DATE_STRING_OFFSET_2);
        final ZonedDateTime utc = TimeUtil.toZonedDateTimeAtUtc(from);
        assertEquals(DATE_STRING_Z, utc.toString());
    }

    @Test
    public void toZonedDateTimeAtUtc_instant() {
        final Instant from = Instant.parse(DATE_STRING_OFFSET_2);
        final ZonedDateTime utc = TimeUtil.toZonedDateTimeAtUtc(from);
        assertEquals(DATE_STRING_Z, utc.toString());
    }

    @Test
    public void toZonedDateTimeAtUtc_epochMilli() {
        final Instant from = Instant.parse(DATE_STRING_MILLIS_Z);
        final ZonedDateTime utc = TimeUtil.toZonedDateTimeAtUtc(from.toEpochMilli());
        assertEquals(DATE_STRING_MILLIS_Z, utc.toString());
    }

    @Test
    public void toZonedDateTimeAtUtc_date() {
        final Date from = Date.from(Instant.parse(DATE_STRING_MILLIS_Z));
        final ZonedDateTime utc = TimeUtil.toZonedDateTimeAtUtc(from);
        assertEquals(DATE_STRING_MILLIS_Z, utc.toString());
    }

    @Test
    public void toXMLGregorianCalendarAtUtc_zdt() {
        final ZonedDateTime timeAtOffset2 = ZonedDateTime.parse(DATE_STRING_OFFSET_2);
        final XMLGregorianCalendar xmlUtc = TimeUtil.toXMLGregorianCalendarAtUtc(timeAtOffset2);
        assertEquals(XML_DATE_STRING_Z, xmlUtc.toString());
        final ZonedDateTime utc = TimeUtil.toZonedDateTimeAtUtc(xmlUtc);
        assertEquals(timeAtOffset2.toEpochSecond(), utc.toEpochSecond());
    }

    @Test
    public void toXMLGregorianCalendarAtUtc_instant() {
        final Instant instant = Instant.parse(DATE_STRING_Z);
        final XMLGregorianCalendar xmlUtc = TimeUtil.toXMLGregorianCalendarAtUtc(instant);
        assertEquals(XML_DATE_STRING_Z, xmlUtc.toString());
        final ZonedDateTime utc = TimeUtil.toZonedDateTimeAtUtc(xmlUtc);
        assertEquals(instant.getEpochSecond(), utc.toEpochSecond());
    }

    @Test
    public void nowWithoutMillis() {
        final Instant now = Instant.now();
        final Instant nowWithoutMillis = TimeUtil.nowWithoutMillis();
        assertEquals("000", StringUtils.right(String.valueOf(nowWithoutMillis.toEpochMilli()), 3));
        assertTrue(TimeUtil.millisBetween(now, nowWithoutMillis) <= 1000);
    }

    @Test
    public void nowWithoutNanos() {
        final Instant now = Instant.now();
        final Instant nowWithoutNanos = TimeUtil.nowWithoutNanos();

        assertEquals("000000", StringUtils.right(String.valueOf(nowWithoutNanos.getNano()), 6));
        assertTrue(TimeUtil.millisBetween(now, nowWithoutNanos) <= 1);
        assertEquals(now.toEpochMilli(), nowWithoutNanos.toEpochMilli(), 1000);
    }

    @Test
    public void zonedDateTimeNowAtUtc() {
        final long now = ZonedDateTime.now().toInstant().getEpochSecond();
        final ZonedDateTime utc = TimeUtil.getZonedDateTimeNowAtUtc();

        assertEquals(0, utc.getOffset().getTotalSeconds());
        assertEquals(now, utc.toEpochSecond());
    }

    @Test
    public void getZonedDateTimeNowWithoutMillisAtUtc() {
        final long now = ZonedDateTime.now().toInstant().getEpochSecond();
        final ZonedDateTime utc = TimeUtil.getZonedDateTimeNowWithoutMillisAtUtc();

        assertEquals(0, utc.getOffset().getTotalSeconds());
        assertEquals(now, utc.toEpochSecond());
        assertEquals("0", StringUtils.right(String.valueOf(utc.getNano()), 9));
    }

    @Test
    public void withoutNanos_Instant() {
        final Instant now = Instant.now();
        final Instant nowWithoutNanos = TimeUtil.withoutNanos(now);

        assertEquals("000000", StringUtils.right(String.valueOf(nowWithoutNanos.getNano()), 6));
        assertEquals(now.toEpochMilli(), nowWithoutNanos.toEpochMilli());
    }

    @Test
    public void withoutMillisAtUtc_Instant() {
        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime nowWithoutMillis = TimeUtil.withoutMillisAtUtc(now);

        assertEquals(0, nowWithoutMillis.getOffset().getTotalSeconds());
        assertEquals(0, nowWithoutMillis.getNano());
        assertEquals(now.toEpochSecond(), nowWithoutMillis.toEpochSecond());
    }

    @Test
    public void toZonedDateTimeWithoutMillisAtUtc_Instant() {
        final Instant now = Instant.now();
        final ZonedDateTime nowWithoutMillis = TimeUtil.toZonedDateTimeWithoutMillisAtUtc(now);

        assertEquals(0, nowWithoutMillis.getOffset().getTotalSeconds());
        assertEquals(0, nowWithoutMillis.getNano());
        assertEquals(now.getEpochSecond(), nowWithoutMillis.toEpochSecond());
    }

    @Test
    public void toIsoDateTimeWithMillisAtUtc_Instant() {
        final String iso = TimeUtil.toIsoDateTimeWithMillisAtUtc(I_2023_10_25T09_70_11_900Z);

        assertEquals(ISO_2023_10_25T09_70_11_900Z, iso);
    }

    @Test
    public void toSqlTimestamp_zdt() {
        final ZonedDateTime now = Instant.now().atZone(ZoneOffset.UTC);
        final Timestamp sqlTimestamp = TimeUtil.toSqlTimestamp(now);

        assertEquals(now.toInstant().toEpochMilli(), sqlTimestamp.getTime());
    }

    @Test
    public void appendMillis_instant() {
        final Instant from = Instant.now();
        final Instant to = TimeUtil.appendMillis(from, 10);

        assertEquals(10, TimeUtil.millisBetween(from, to));
    }

    @Test
    public void toInstant_sql() {
        final Timestamp now = Timestamp.from(Instant.now());
        final Instant result = TimeUtil.toInstant(now);

        assertEquals(now.getTime(), result.toEpochMilli());
    }

    @Test
    public void roundInstantSeconds() {
        assertEquals(Instant.parse("2023-10-25T10:11:12.000Z"), TimeUtil.roundInstantSeconds(Instant.parse("2023-10-25T10:11:12.000Z")));
        assertEquals(Instant.parse("2023-10-25T10:11:12.000Z"), TimeUtil.roundInstantSeconds(Instant.parse("2023-10-25T10:11:12.001Z")));
        assertEquals(Instant.parse("2023-10-25T10:11:12.000Z"), TimeUtil.roundInstantSeconds(Instant.parse("2023-10-25T10:11:12.499Z")));
        assertEquals(Instant.parse("2023-10-25T10:11:13.000Z"), TimeUtil.roundInstantSeconds(Instant.parse("2023-10-25T10:11:12.500Z")));
        assertEquals(Instant.parse("2023-10-25T10:11:13.000Z"), TimeUtil.roundInstantSeconds(Instant.parse("2023-10-25T10:11:12.999Z")));
    }

    @Test
    public void floorInstantSeconds() {
        assertEquals(Instant.parse("2023-10-25T10:11:12.000Z"), TimeUtil.floorInstantSeconds(Instant.parse("2023-10-25T10:11:12.000Z")));
        assertEquals(Instant.parse("2023-10-25T10:11:12.000Z"), TimeUtil.floorInstantSeconds(Instant.parse("2023-10-25T10:11:12.001Z")));
        assertEquals(Instant.parse("2023-10-25T10:11:12.000Z"), TimeUtil.floorInstantSeconds(Instant.parse("2023-10-25T10:11:12.499Z")));
        assertEquals(Instant.parse("2023-10-25T10:11:12.000Z"), TimeUtil.floorInstantSeconds(Instant.parse("2023-10-25T10:11:12.500Z")));
        assertEquals(Instant.parse("2023-10-25T10:11:12.000Z"), TimeUtil.floorInstantSeconds(Instant.parse("2023-10-25T10:11:12.999Z")));
    }

    @Test
    public void ceilInstantSeconds() {
        assertEquals(Instant.parse("2023-10-25T10:11:12.000Z"), TimeUtil.ceilInstantSeconds(Instant.parse("2023-10-25T10:11:12.000Z")));
        assertEquals(Instant.parse("2023-10-25T10:11:13.000Z"), TimeUtil.ceilInstantSeconds(Instant.parse("2023-10-25T10:11:12.001Z")));
        assertEquals(Instant.parse("2023-10-25T10:11:13.000Z"), TimeUtil.ceilInstantSeconds(Instant.parse("2023-10-25T10:11:12.499Z")));
        assertEquals(Instant.parse("2023-10-25T10:11:13.000Z"), TimeUtil.ceilInstantSeconds(Instant.parse("2023-10-25T10:11:12.500Z")));
        assertEquals(Instant.parse("2023-10-25T10:11:13.000Z"), TimeUtil.ceilInstantSeconds(Instant.parse("2023-10-25T10:11:12.999Z")));
    }

    @Test
    public void getEpochSeconds() {
        assertEquals(1698228672, TimeUtil.getEpochSeconds(Instant.parse("2023-10-25T10:11:12.000Z")));
        assertEquals(1698228672, TimeUtil.getEpochSeconds(Instant.parse("2023-10-25T10:11:12.900Z")));
    }
}
package fi.livi.digitraffic.common.controller;

/**
 * Sunset and deprecation texts
 *
 * I.e. Sunset on 1.1.2022 -> create two constants, date in format YYYY-MM-DD:
 * SUNSET_2022_01_01    = "2022-01-01"
 * API_NOTE_2022_11_01 = SUNSET_TEXT + SUNSET_2022_01_01;
 *
 * No need to include the date constants in the common code - leave them in the application code.
 *
 * Add annotations to deprecated APIs:
 * @Deprecated(forRemoval = true)
 * @Sunset(date = SUNSET_2022_01_01) OR @Sunset(tbd = true)
 * @Operation(summary = "Api description plaa plaa. " + ApiDeprecations.API_NOTE_2022_01_01)
 */
public final class ApiDeprecations {

    private static final String SUNSET_TEXT = "Will be removed after ";

    public static final String SUNSET_FUTURE = "TBD";

    public static final String API_NOTE_FUTURE = "Will be removed in the future";

    private ApiDeprecations() {}
}

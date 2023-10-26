package fi.livi.digitraffic.common.util;

public abstract class ObjectUtil {

    protected ObjectUtil() {
        throw new AssertionError("The " + getClass().getSimpleName() + " class methods should be accessed statically");
    }

    public static String getEnumName(final Enum<?> value) {
        return value == null ? null : value.name();
    }
}

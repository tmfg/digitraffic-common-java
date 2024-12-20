package fi.livi.digitraffic.common.util;

import java.util.function.Supplier;

public abstract class ObjectUtil {

    protected ObjectUtil() {
        throw new AssertionError("The " + getClass().getSimpleName() + " class methods should be accessed statically");
    }

    public static String getEnumName(final Enum<?> value) {
        return value == null ? null : value.name();
    }

    public static <T, R> R callIfNotNull(final T obj, final Supplier<? extends R> supplier) {
        return (obj != null) ? supplier.get()
                             : null;
    }

    public static <R> R callAndIgnoreExeption(final Supplier<? extends R> supplier) {
        try {
            return supplier.get();
        } catch (final Exception e) {
            return null;
        }
    }
}

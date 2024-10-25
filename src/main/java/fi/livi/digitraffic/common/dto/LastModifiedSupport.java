package fi.livi.digitraffic.common.dto;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fi.livi.digitraffic.common.util.TimeUtil;

/**
 * If an object returned by a Controller implements this, LastModifiedAppenderControllerAdvice will call getLastModified()
 * and add the result as the value of the header "last-modified" in the response.
 */
public interface LastModifiedSupport {
    @JsonIgnore
    Instant getLastModified();

    /**
     * Sometimes last modified time is not relevant: e.g. if there is no data, then the value is not required.
     * @return true if data should have last modified value.
     */
    @JsonIgnore
    default boolean shouldContainLastModified() {
        return true;
    }

    /**
     * @param collection to find latest modified time
     * @param lastModifiedAlternative value to use if collection is empty or if value from collection is older than given value.
     * @return last modified time
     */
    static Instant getLastModifiedOf(final Collection<? extends LastModifiedSupport> collection, final Instant lastModifiedAlternative) {
        return TimeUtil.getGreatest(
            collection.stream()
                .map(LastModifiedSupport::getLastModified)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(lastModifiedAlternative),
            lastModifiedAlternative);
    }
}

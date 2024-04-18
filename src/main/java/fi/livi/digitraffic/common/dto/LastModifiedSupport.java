package fi.livi.digitraffic.common.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
}

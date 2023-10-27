package fi.livi.digitraffic.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods annotated with @NoJobLogging will not be monitored for execution time
 * by ScheduledJobLogger.monitorScheduledJob()
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface NoJobLogging {
}

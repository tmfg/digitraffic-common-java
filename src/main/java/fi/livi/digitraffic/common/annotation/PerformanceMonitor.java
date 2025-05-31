package fi.livi.digitraffic.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fi.livi.digitraffic.common.aop.PerformanceMonitorAspect;

/**
 * Annotation to configure PerformanceMonitor limits.
 * Adding this annotation to a non-service class method
 * will make it monitored.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD})
public @interface PerformanceMonitor {

    /**
     * Limits when execution time is logged as error
     *
     * @return maxErroExcecutionTime in millis
     */
    int maxErrorExcecutionTime() default PerformanceMonitorAspect.DEFAULT_ERROR_LIMIT;

    /**
     * Limits when execution time is logged as warning
     *
     * @return maxWarnExcecutionTime in millis
     */
    int maxWarnExcecutionTime() default PerformanceMonitorAspect.DEFAULT_WARNING_LIMIT;

    /**
     * Limits when execution time is logged as info
     *
     * @return maxInfoExcecutionTime in millis
     */
    int maxInfoExcecutionTime() default PerformanceMonitorAspect.DEFAULT_INFO_LIMIT;

    /**
     * Should transactional method be monitored
     * @return monitor
     */
    boolean monitor() default true;
}

package fi.livi.digitraffic.common.aop;

import static fi.livi.digitraffic.common.scheduler.JobLogger.JobType.Scheduled;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import fi.livi.digitraffic.common.scheduler.JobLogger;
import fi.livi.digitraffic.common.scheduler.JobLogger.JobType;

@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ScheduledJobLogger {

    private static final Logger log = LoggerFactory.getLogger(ScheduledJobLogger.class);
    private static final JobType jobType = Scheduled;

    /**
     * Execution start (debug level) and end (info or error level) times will be
     * logged for each method annotated with @Scheduled.
     *
     * Methods annotated with @NoJobLogging will not be monitored.
     */
    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled) && !@annotation(fi.livi.digitraffic.common.annotation.NoJobLogging)")
    public Object monitorScheduledJob(final ProceedingJoinPoint pjp) throws Throwable {
        final String method = pjp.getSignature().getName();
        // Strip away Configuration suffix and Spring proxy classes
        final String jobClass = StringUtils.substringBefore(StringUtils.substringBefore(pjp.getTarget().getClass().getSimpleName(),"Configuration"), "$");

        final StopWatch stopWatch = StopWatch.createStarted();
        final String jobName = jobClass + "." + method;

        JobLogger.logJobStart(log, jobType, jobName);

        Exception error = null;
        try {
            return pjp.proceed();
        } catch (final Exception e) {
            error = e;
            throw e;
        } finally {
            stopWatch.stop();
            if (error == null) {
                JobLogger.logJobEndStatusSuccess(log, jobType, jobName, stopWatch.getTime());
            } else {
                JobLogger.logJobEndStatusFail(log, jobType, jobName, stopWatch.getTime(), error);
            }
        }
    }
}

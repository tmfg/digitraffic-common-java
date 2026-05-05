package fi.livi.digitraffic.common.scheduler;

import org.slf4j.Logger;

import fi.livi.digitraffic.common.util.StringUtil;

public class JobLogger {

    public enum JobType {
        Scheduled,
        Quartz
    }

    public enum JobEndStatus {
        SUCCESS,
        FAIL
    }

    public static void logJobStart(final Logger log, final JobType jobType, final String jobName) {
        log.debug("jobType={} jobName={} start", jobType.name(), jobName);
    }

    public static void logJobEndStatusFail(final Logger log, final JobType jobType, final String jobName, final long timeMs, final Exception lastError) {
        logJobEndStatus(log, jobType, jobName, JobEndStatus.FAIL, timeMs, lastError);
    }

    public static void logJobEndStatusSuccess(final Logger log, final JobType jobType, final String jobName, final long timeMs) {
        logJobEndStatus(log, jobType, jobName, JobEndStatus.SUCCESS, timeMs, null);
    }

    private static void logJobEndStatus(final Logger log, final JobType jobType, final String jobName, final JobEndStatus status, final long timeMs, final Exception error) {
        final String message = StringUtil.format("method=logJobEndStatus jobType={} jobName={} jobEndStatus={} tookMs={}", jobType, jobName, status, timeMs);
        if (error != null) {
            log.error(message, error);
        } else {
            log.info(message);
        }
    }
}
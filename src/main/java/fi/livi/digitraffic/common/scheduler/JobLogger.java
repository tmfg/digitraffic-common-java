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
        log.error(formatMessage(jobType, jobName, JobEndStatus.FAIL, timeMs), lastError);
    }

    public static void logJobEndStatusSuccess(final Logger log, final JobType jobType, final String jobName, final long timeMs) {
        log.info(formatMessage(jobType, jobName, JobEndStatus.SUCCESS, timeMs));
    }

    private static String formatMessage(final JobType jobType, final String jobName, final JobEndStatus status, final long timeMs) {
        return StringUtil.format("jobType={} jobName={} jobEndStatus={} tookMs={}", jobType, jobName, status, timeMs);
    }
}
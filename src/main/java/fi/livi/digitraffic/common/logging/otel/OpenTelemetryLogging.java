package fi.livi.digitraffic.common.logging.otel;

public class OpenTelemetryLogging {
    public static final String KEY_METHOD = "method";
    public static final String KEY_TRACE_ID = "trace_id";
    public static final String KEY_CONTEXT = "context";
    public static final String KEY_DURATION = "duration";

    public static class Outcome {
        public static final String KEY = "outcome";
        public static final String VALUE_SUCCESS = "success";
        public static final String VALUE_ERROR = "error";
    }

    public static class DbMaintenance {
        public static final String KEY_CLEANUP = "maintenance.db.cleanup";
        public static final String KEY_TABLENAME = "maintenance.db.table.name";
        public static final String KEY_DELETED_COUNT = "maintenance.db.cleanup.deleted.count";
        public static final String KEY_BATCH_SIZE = "maintenance.db.cleanup.batch.size";
    }

    public static class Http {
        // temporary keys, change to url.full etc later
        public static final String KEY_URL = "url_full";
        public static final String KEY_URL_TEMPLATE = "url_template";
        public static final String KEY_ERROR_TYPE = "error_type";

        public static final String KEY_STATUS_CODE = "http.response.status_code";
        public static final String KEY_DURATION = "http.client.request.duration";
        public static final String KEY_BODY_SIZE = "http.response.body.size";
    }
}

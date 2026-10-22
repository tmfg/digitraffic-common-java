package fi.livi.digitraffic.common.logging;

public class OpenTelemetryLogging {
    public static final String KEY_METHOD = "method";
    public static final String KEY_CONTEXT = "context";
    public static final String KEY_TRACE_ID = "trace_id";

    public static class Http {
        public static final String KEY_URL = "request.url.full";
        public static final String KEY_URL_TEMPLATE = "request.url.template";
        public static final String KEY_ERROR_TYPE = "request.error.type";
        public static final String KEY_STATUS_CODE = "http.response.status_code";
        public static final String KEY_DURATION = "http.client.request.duration";
        public static final String KEY_BODY_SIZE = "http.response.body.size";
    }
}

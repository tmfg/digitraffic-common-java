package fi.livi.digitraffic.common.logging;

public class HttpLoggerFactory {
    private final String context;
    private final String traceId;
    private final String urlTemplate;

    public HttpLoggerFactory(final String context, final String traceId, final String urlTemplate) {
        this.context = context;
        this.traceId = traceId;
        this.urlTemplate = urlTemplate;
    }

    public HttpLogger logger(final String url) {
        return new HttpLogger(new HttpLogger.Parameters(context, url, traceId, urlTemplate));
    }
}

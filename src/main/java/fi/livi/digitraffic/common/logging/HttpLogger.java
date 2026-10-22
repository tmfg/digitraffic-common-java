package fi.livi.digitraffic.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static fi.livi.digitraffic.common.logging.OpenTelemetryLogging.*;

/**
 * Logs one HTTP operation as a structured OpenTelemetry-style event.
 *
 * <p>Create an instance when the request starts, then call either {@link #end(int)} on completion
 * or {@link #error(Exception)} on failure.
 */
public class HttpLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpLogger.class);

    private final String context;
    private final String url;
    private final String urlTemplate;
    private final long startTime;
    private final String traceId;

    private String errorType;
    private int statusCode = -1;
    private Long contentLength = null;

    /**
     * Creates a logger bound to one HTTP operation.
     *
     * @param parameters immutable operation metadata; {@code context} and {@code url} are required
     * @throws NullPointerException if {@code context} or {@code url} is {@code null}
     */
    public HttpLogger(final Parameters parameters) {
        this.context = Objects.requireNonNull(parameters.context(), "context must not be null");
        this.url = Objects.requireNonNull(parameters.url(), "url must not be null");
        this.traceId = parameters.traceId();
        this.urlTemplate = parameters.urlTemplate();
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Marks the operation as finished and writes one log event.
     *
     * @param statusCode HTTP response status code
     */
    public void end(final int statusCode) {
        this.statusCode = statusCode;

        logStatus();
    }

    public <T> Mono<T> end(final ClientResponse response, final Class<T> clazz) {
        final var lengthFromHeader = response.headers().contentLength();

        this.statusCode = response.statusCode().value();

        if(lengthFromHeader.isPresent()) {
            this.contentLength = lengthFromHeader.getAsLong();
        }

        logStatus();

        return response.bodyToMono(clazz);
    }

    /**
     * Marks the operation as failed and writes one log event.
     *
     * @param exception error that ended the operation
     * @throws NullPointerException if {@code exception} is {@code null}
     */
    public void error(final Exception exception) {
        this.errorType = Objects.requireNonNull(exception, "exception must not be null")
            .getClass().getSimpleName();

        logStatus();
    }

    private void logStatus() {
        final long endTime = System.currentTimeMillis();

        LOGGER.atInfo()
            .addKeyValue(KEY_METHOD, "HttpLogger.logStatus")
            .addKeyValue(KEY_CONTEXT, context)
            .addKeyValue(KEY_TRACE_ID, traceId)
            .addKeyValue(Http.KEY_URL, url)
            .addKeyValue(Http.KEY_URL_TEMPLATE, urlTemplate)
            .addKeyValue(Http.KEY_ERROR_TYPE, errorType)
            .addKeyValue(Http.KEY_STATUS_CODE, statusCode)
            .addKeyValue(Http.KEY_DURATION, (endTime - startTime) / 1000.0)
            .addKeyValue(Http.KEY_BODY_SIZE, this.contentLength)
            .log();
    }

    /**
     * Immutable input data for {@link HttpLogger}.
     *
     * @param context logical operation context, for example an integration name
     * @param url full request URL
     * @param traceId optional distributed-trace identifier
     * @param urlTemplate optional route template without path parameter values
     */
    public record Parameters(String context, String url, String traceId, String urlTemplate) {
    }
}

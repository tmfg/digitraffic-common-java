package fi.livi.digitraffic.common.logging;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static fi.livi.digitraffic.common.logging.OpenTelemetryLogging.*;

/**
 * Logs one HTTP operation as a structured OpenTelemetry-style event.
 *
 * <p>Create an instance when the request starts, then call one of the completion methods:
 * {@link #end(int, Long)} when status and body size are already known, or
 * {@link #log(ClientResponse, Class)} when handling a reactive {@code ClientResponse}.
 * Use {@link #error(Throwable)} to log a terminal failure directly.
 */
public class HttpLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpLogger.class);

    private final String context;
    private final String url;
    private final String urlTemplate;
    private final String traceId;
    private final StopWatch stopWatch = new StopWatch();

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
        this.stopWatch.start();
    }

    /**
     * Marks the operation as finished and writes one log event.
     *
     * @param statusCode HTTP response status code
     * @param contentLength response body size in bytes, or {@code null} when unknown
     */
    public void end(final int statusCode, final Long contentLength) {
        this.stopWatch.stop();
        this.statusCode = statusCode;
        this.contentLength = contentLength;

        logStatus();
    }

    /**
     * Completes the operation from a reactive HTTP response.
     *
     * <p>The status code and optional {@code content-length} header are captured immediately.
     * Logging happens when the body {@link Mono} completes: success logs the status event,
     * and error logs through {@link #error(Throwable)} before propagating the failure.
     *
     * @param response HTTP client response
     * @param clazz target body type
     * @param <T> target body type
     * @return body publisher with logging side effects attached
     */
    public <T> Mono<T> log(final ClientResponse response, final Class<T> clazz) {
        return response.bodyToMono(clazz)
            .doFinally(signal -> {
                if(signal == SignalType.ON_COMPLETE) {
                    final var lengthFromHeader = response.headers().contentLength();
                    final var statusCode = response.statusCode().value();
                    final var contentLength = lengthFromHeader.isPresent() ? lengthFromHeader.getAsLong() : null;

                    end(statusCode, contentLength);
                }
            })
            .doOnError(this::error);
    }

    /**
     * Marks the operation as failed and writes one log event.
     *
     * @param throwable error that ended the operation
     * @throws NullPointerException if {@code throwable} is {@code null}
     */
    public void error(final Throwable throwable) {
        this.errorType = Objects.requireNonNull(throwable, "throwable must not be null")
            .getClass().getSimpleName();

        logStatus();
    }

    private void logStatus() {
        final long duration = this.stopWatch.getTime(TimeUnit.MILLISECONDS);

        LOGGER.atInfo()
            .addKeyValue(KEY_METHOD, "HttpLogger.logStatus")
            .addKeyValue(KEY_CONTEXT, context)
            .addKeyValue(KEY_TRACE_ID, traceId)
            .addKeyValue(Http.KEY_URL, url)
            .addKeyValue(Http.KEY_URL_TEMPLATE, urlTemplate)
            .addKeyValue(Http.KEY_ERROR_TYPE, errorType)
            .addKeyValue(Http.KEY_STATUS_CODE, statusCode)
            .addKeyValue(Http.KEY_DURATION, (duration / 1000.0))
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

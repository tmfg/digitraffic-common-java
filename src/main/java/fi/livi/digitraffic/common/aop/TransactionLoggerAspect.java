package fi.livi.digitraffic.common.aop;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.time.StopWatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import fi.livi.digitraffic.common.util.StringUtil;

@Aspect
@Order
public class TransactionLoggerAspect {
    private static final Logger log = LoggerFactory.getLogger("TransactionLogger");

    private final int limit;

    private final AtomicLong idCounter = new AtomicLong();

    private static final Map<Long, TransactionDetails> activeTransactions = new ConcurrentHashMap<>();

    public TransactionLoggerAspect(final int limit) {
        this.limit = limit;
    }

    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object monitor(final ProceedingJoinPoint pjp) throws Throwable {
        final StopWatch stopWatch = StopWatch.createStarted();
        final MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        final String className = methodSignature.getDeclaringType().getSimpleName();
        final String methodName = methodSignature.getName();
        final String methodKey = className + "." + methodName;
        final Object[] args = pjp.getArgs();
        final Long transactionId = idCounter.incrementAndGet();

        try {
            activeTransactions.put(transactionId, new TransactionDetails(methodKey, args, System.currentTimeMillis()));

            return pjp.proceed();
        } finally {
            final long tookMs = stopWatch.getDuration().toMillis();

            activeTransactions.remove(transactionId);

            if (tookMs > limit) {
                final String arguments = argumentsToString(args);
                log.info("Transaction method={} arguments={} tookMs={}", methodKey, arguments, tookMs);
            }
        }
    }

    public static void logActiveTransactions(final Logger logger) {
        Map.copyOf(activeTransactions)
            .forEach((key, value) -> logger.info("method=logActiveTransactions Active transaction {}",
                value.getLogString()));
    }

    private static String argumentsToString(final Object[] args) {
        final StringBuilder arguments = new StringBuilder(100);
        PerformanceMonitorAspect.buildValueToString(arguments, args);

        return arguments.toString();
    }

    private record TransactionDetails(String method, Object[] args, Long starttime) {
        String getLogString() {
            return StringUtil.format("activeTransaction={} ageMs={} arguments {}", method,
                System.currentTimeMillis() - starttime, argumentsToString(args));
        }
    }
}


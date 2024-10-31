package fi.livi.digitraffic.common.cache;

import org.apache.commons.collections4.map.PassiveExpiringMap;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Synchronized and thread-safe cache with TTL.
 */
public class ExpiringCache<VALUE> {
    private final Map<String, VALUE> cache;

    private static final String DEFAULT_CACHE_KEY = "DEFAULT";

    public ExpiringCache(final Duration d) {
        this.cache = Collections.synchronizedMap(new PassiveExpiringMap<>(d.toMillis()));
    }

    public VALUE get(final Supplier<CacheResult<VALUE>> supplier) {
        return get(DEFAULT_CACHE_KEY, supplier);
    }

    /**
     * Get result from the cache if present and not expired.  Otherwise, use given supplier to generate result.
     * <p>
     * If result from the supplier is marked as cacheable, populate cache with the result.
     */
    public synchronized VALUE get(final String key, final Supplier<CacheResult<VALUE>> supplier) {
        final var response = cache.get(key);

        if(response != null) {
            return response;
        }

        final var cacheResult = supplier.get();

        if(cacheResult.cacheable) {
            cache.put(key, cacheResult.result);
        }

        return cacheResult.result;
    }

    public record CacheResult<VALUE>(boolean cacheable, VALUE result) {
        public CacheResult(final VALUE result) {
            this(true, result);
        }
    }
}

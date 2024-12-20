package fi.livi.digitraffic.common.cache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.function.Supplier;

public class ExpiringCacheTest {
    private class TestSupplier implements Supplier<ExpiringCache.CacheResult<Integer>> {
        private final boolean cacheable;
        private int callCount = 0;

        private TestSupplier(final boolean cacheable) {
            this.cacheable = cacheable;
        }

        @Override
        public ExpiringCache.CacheResult<Integer> get() {
            return new ExpiringCache.CacheResult<>(cacheable, ++callCount);
        }
    }
    @Test
    public void testCache() {
        final var supplier = new TestSupplier(true);
        final var cache = new ExpiringCache<Integer>(Duration.ofMinutes(1));

        // second call from the cache
        Assertions.assertEquals(1, cache.get(supplier));
        Assertions.assertEquals(1, cache.get(supplier));
    }

    @Test
    public void testCacheWithoutTTL() {
        final var supplier = new TestSupplier(true);
        final var cache = new ExpiringCache<Integer>(Duration.ofMinutes(0));

        // second call not from the cache
        Assertions.assertEquals(1, cache.get(supplier));
        Assertions.assertEquals(2, cache.get(supplier));
    }

    @Test
    public void testNotCacheable() {
        final var supplier = new TestSupplier(false);
        final var cache = new ExpiringCache<Integer>(Duration.ofMinutes(1));

        // second call not from the cache
        Assertions.assertEquals(1, cache.get(supplier));
        Assertions.assertEquals(2, cache.get(supplier));
    }
}

package fi.livi.digitraffic.common.service.locking;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.annotation.Scheduled;

import fi.livi.digitraffic.common.annotation.NoJobLogging;
import fi.livi.digitraffic.common.util.StringUtil;
import fi.livi.digitraffic.common.util.ThreadUtil;

/**
 * Service for locking execution of desired service to single node. This is stateful, and it caches the lock state for one second to reduce db queries.
 * Service tries to acquire lock and update cache once a second and returns the latest state from the cache when asked. So every request for lock state
 * won't trigger new db query. This is done to reduce lock-checking from database, because it might happen quite often for some services.
 * <p>
 * Initially timed behaviour is deactivated and will be activated on first call to hasLock method.
 * It is also possible to manually activate/deactivate timed lock reservation. Call to hasLock will always check and activate this behaviour if it is not activated.
 */

public class CachedLockingService implements DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(CachedLockingService.class);
    private static final Set<String> bookedLockNames = new HashSet<>();

    private final LockingService lockingService;
    private final String lockName;
    private final AtomicBoolean hasLock = new AtomicBoolean(false);
    private boolean active = false;

    CachedLockingService(final LockingService lockingService, final String lockName) {
        this.lockingService = lockingService;
        synchronized (bookedLockNames) {
            if (bookedLockNames.contains(lockName)) {
                throw new IllegalArgumentException(
                        String.format("Lock named %s is already used. Lock name must be unique. Try with another name.",
                                lockName));
            }
            bookedLockNames.add(lockName);
        }
        this.lockName = lockName;
        log.info("method=CachedLockingService Created new {}", this);
    }

    /**
     * Removes given lock name from already used lock names.
     * Use with caution if you know what you are doing.
     * @param lockName Name to remove from used ones.
     */
    public static void removeBookedLock(final String lockName) {
        synchronized (bookedLockNames) {
            bookedLockNames.remove(lockName);
        }
    }

    /**
     * If lock is not active, this will also activate lock to try to keep lock all the time.
     * @return true if the current thread has the lock
     */
    public boolean hasLock() {
        if (!active) {
            activate();
        }
        return hasLock.get();
    }

    /**
     * Activates lock to try to keep the lock all the time.
     */
    public void activate() {
        this.active = true;
        acquireLock();
    }

    /**
     * Stops trying to keep the lock all the time and releases the lock.
     * This is reactivated by calling activate() or hasLock().
     */
    public void deactivate() {
        this.active = false;
        hasLock.set(false);
        lockingService.unlock(lockName);
    }

    /**
     * Tries to get lock and waits in maximum given time until gives up trying.
     *
     * @param timeoutMs timeout for trying to get the lock.
     * @return true if lock was successfully acquired.
     */
    public boolean lock(final long timeoutMs) {
        final StopWatch timer = StopWatch.createStarted();
        while (!hasLock() && timer.getDuration().toMillis() < timeoutMs) {
            ThreadUtil.delayMs(100);
        }
        return hasLock();
    }

    public String getInstanceId() {
        return lockingService.getInstanceId();
    }

    public String getLockName() {
        return lockName;
    }

    public String getLockInfoForLogging() {
        return StringUtil.format("lockName={} hasLock={} isActive={} instanceId={}", getLockName(), hasLock.get(),
                active, getInstanceId());
    }

    // Expiration 2 seconds and refresh lock every second
    @NoJobLogging
    @Scheduled(fixedRate = 1000)
    public void acquireLock() {
        if (active) {
            try {
                hasLock.set(lockingService.acquireLock(lockName, 2));
            } catch (final Exception e) {
                hasLock.set(false);
                log.error("method=acquireLock Failed for {}", getLockInfoForLogging(), e);
            }
        } else {
            hasLock.set(false);
        }
    }

    @Override
    public String toString() {
        return StringUtil.format("{} {}", CachedLockingService.class.getSimpleName(), getLockInfoForLogging());
    }

    @Override
    public void destroy() throws Exception {
        log.info("method=destroy {}", getLockInfoForLogging());
        active = false;
        bookedLockNames.remove(lockName);
    }
}

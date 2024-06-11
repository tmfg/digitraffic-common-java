package fi.livi.digitraffic.common.service.locking;

import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.common.dao.LockingDao;
import fi.livi.digitraffic.common.util.StringUtil;

@ConditionalOnNotWebApplication
@Service
public class LockingService {
    private static final Logger log = LoggerFactory.getLogger(LockingService.class);

    private final LockingDao lockingDao;
    private final GenericApplicationContext applicationContext;
    private final String instanceId;


    @Autowired
    public LockingService(final LockingDao lockingDao,
                          final GenericApplicationContext applicationContext) {
        this.lockingDao = lockingDao;
        this.applicationContext = applicationContext;
        this.instanceId = UUID.randomUUID().toString();
    }

    /**
     * Creates cached locking service for given lock name and registers it as a bean to Spring context.
     * @param lockName Name of the lock
     * @return Registered service bean
     */
    public CachedLockingService createCachedLockingService(final String lockName) {
        final String beanName =
                StringUtil.format("{}.{}", StringUtils.uncapitalize(CachedLockingService.class.getSimpleName()),
                        lockName);
        return createCachedLockingService(lockName, beanName);
    }

    /**
     * Creates cached locking service for given lock name but won't register it as a bean to Spring context.
     * @param lockName Name of the lock
     * @return Unregistered service object
     */
    public CachedLockingService createCachedLockingServiceObject(final String lockName) {
        return new CachedLockingService(this, lockName);
    }
    /**
     * Package private method. Only for tests to override bean name.
     * @param lockName Name of the lock
     * @param beanName Bean name to register for the service
     * @return Registered service bean
     */
    CachedLockingService createCachedLockingService(final String lockName, final String beanName) {
        final Optional<CachedLockingService> bean = getBeanIfRegistered(beanName);
        if (bean.isPresent()) {
            log.info("method=createCachedLockingService Bean {} already exist, returning {}", beanName, lockName);
            return bean.get();
        } else {
            log.info("method=createCachedLockingService Bean {} not exist, creating new {}", beanName, lockName);
            // Clean lock name from reserved as test context won't clear static fields between tests
            CachedLockingService.removeBookedLock(lockName);
        }
        // needed to registerBean to initialize @Scheduled annotation
        applicationContext.registerBean(beanName, CachedLockingService.class,
                () -> new CachedLockingService(this, lockName));
        return applicationContext.getBean(beanName, CachedLockingService.class);
    }

    private Optional<CachedLockingService> getBeanIfRegistered(final String beanName) {
        try {
            return Optional.of((CachedLockingService) applicationContext.getBean(beanName));
        } catch (final NoSuchBeanDefinitionException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public boolean acquireLock(final String lockName, final int expirationSeconds) {
        return lockingDao.acquireLock(lockName, instanceId, expirationSeconds);
    }

    @Transactional
    public void unlock(final String lockName) {
        lockingDao.releaseLock(lockName, instanceId);
    }

    public String getInstanceId() {
        return instanceId;
    }

    // Run every hour
    @Scheduled(fixedRate = 1000 * 60 * 60)
    @Transactional
    protected void clearExpiredLocks() {
        // Delete locks that have expired over hour ago
        clearExpiredLocks(60 * 60);
    }

    @Transactional
    protected void clearExpiredLocks(final int secondsSinceExpired) {
        try {
            // Delete locks that have expired over hour ago
            lockingDao.clearExpiredLocks(secondsSinceExpired);
        } catch (final Exception e) {
            log.error("method=clearExpiredLocks Failed", e);
        }
    }
}

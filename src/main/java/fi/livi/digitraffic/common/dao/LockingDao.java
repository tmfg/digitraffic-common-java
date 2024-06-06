package fi.livi.digitraffic.common.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Usage of this needs table in db:
 * CREATE TABLE locking_table
 * (
 *   lock_name    TEXT NOT NULL PRIMARY KEY,
 *   lock_locked  TIMESTAMP(3) WITH TIME ZONE,
 *   lock_expires TIMESTAMP(3) WITH TIME ZONE,
 *   instance_id  TEXT NOT NULL
 * );
 */

@Repository
public class LockingDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Acquires lock for given instanceId.
     * If lock doesn't exist then lock is acquired by inserting new lock-row.
     * If instance already have the lock then lock expiration is updated.
     * If instance doesn't have the lock but lock exists
     * then checks if previous lock has expired and updates the lock-row.
     */
    private static final String ACQUIRE_LOCK_MERGE = """
        insert into locking_table(lock_name, instance_id, lock_locked, lock_expires)
        VALUES (:lockName, :instanceId, clock_timestamp(), clock_timestamp() + :expirationSeconds::integer * interval '1 second')
        ON CONFLICT (lock_name)
        DO UPDATE SET
           instance_id = :instanceId,
           lock_locked = clock_timestamp(),
           lock_expires = clock_timestamp() + :expirationSeconds::integer * interval '1 second'
        where locking_table.instance_id = :instanceId OR locking_table.lock_expires < clock_timestamp()""";

    private static final String RELEASE_LOCK = """
        DELETE FROM LOCKING_TABLE LT
        WHERE LT.LOCK_NAME = :lockName
          AND LT.INSTANCE_ID = :instanceId""";

    private static final String QUERY_LOCK = """
        SELECT LT.LOCK_NAME
        FROM LOCKING_TABLE LT
        WHERE LT.LOCK_NAME = :lockName
          AND LT.INSTANCE_ID = :instanceId
          AND LT.LOCK_EXPIRES > clock_timestamp()""";

    private static final String DELETE_EXPIRED_LOCK = """
        DELETE FROM locking_table
        WHERE lock_expires < clock_timestamp() - :expirationSeconds::integer * interval '1 second'""";

    private static final String PARAMETER_LOCKNAME = "lockName";
    private static final String PARAMETER_INSTANCE_ID = "instanceId";
    private static final String PARAMETER_EXPIRATION_SECONDS = "expirationSeconds";

    @Autowired
    public LockingDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public boolean acquireLock(final String lockName, final String callerInstanceId, final int expirationSeconds) {
        final MapSqlParameterSource params = new MapSqlParameterSource(PARAMETER_LOCKNAME, lockName)
            .addValue(PARAMETER_INSTANCE_ID, callerInstanceId)
            .addValue(PARAMETER_EXPIRATION_SECONDS, expirationSeconds);

        jdbcTemplate.update(ACQUIRE_LOCK_MERGE, params);

        return hasLock(params);
    }

    @Transactional
    public boolean hasLock(final String lockName, final String callerInstanceId) {
        final MapSqlParameterSource params = new MapSqlParameterSource(PARAMETER_LOCKNAME, lockName)
            .addValue(PARAMETER_INSTANCE_ID, callerInstanceId);

        return hasLock(params);
    }

    private boolean hasLock(final MapSqlParameterSource params) {
        // If lock was acquired successfully then query should return one row
        return jdbcTemplate.queryForList(QUERY_LOCK, params, String.class).size() == 1;
    }

    @Transactional
    public void releaseLock(final String lockName, final String callerInstanceId) {
        final MapSqlParameterSource params = new MapSqlParameterSource(PARAMETER_LOCKNAME, lockName)
            .addValue(PARAMETER_INSTANCE_ID, callerInstanceId);

        jdbcTemplate.update(RELEASE_LOCK, params);
    }

    @Transactional
    public void clearExpiredLocks(final int secondsSinceExpired) {
        final MapSqlParameterSource params = new MapSqlParameterSource(PARAMETER_EXPIRATION_SECONDS, secondsSinceExpired);
        jdbcTemplate.update(DELETE_EXPIRED_LOCK, params);
    }
}

package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.CapacityLock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CapacityLockRepository extends JpaRepository<CapacityLock, String> {
    @Modifying
    @Query(value = "INSERT INTO reservation_capacity_locks(lock_key) VALUES (:lockKey) "
            + "ON CONFLICT (lock_key) DO NOTHING", nativeQuery = true)
    int ensureExists(@Param("lockKey") String lockKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CapacityLock c where c.lockKey = :lockKey")
    CapacityLock findForUpdate(@Param("lockKey") String lockKey);
}

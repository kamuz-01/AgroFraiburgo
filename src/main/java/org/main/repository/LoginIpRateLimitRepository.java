package org.main.repository;

import org.main.models.LoginIpRateLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.Optional;

public interface LoginIpRateLimitRepository extends JpaRepository<LoginIpRateLimit, Long> {

    Optional<LoginIpRateLimit> findByIpAddress(String ipAddress);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM LoginIpRateLimit r WHERE r.ipAddress = :ipAddress")
    Optional<LoginIpRateLimit> findByIpAddressForUpdate(@Param("ipAddress") String ipAddress);

    @Modifying
    @Query("DELETE FROM LoginIpRateLimit r WHERE r.atualizadoEm < :cutoff")
    int deleteByAtualizadoEmBefore(@Param("cutoff") LocalDateTime cutoff);
}
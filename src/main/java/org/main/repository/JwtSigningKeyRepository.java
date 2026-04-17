package org.main.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.main.models.JwtSigningKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JwtSigningKeyRepository extends JpaRepository<JwtSigningKey, Long> {

    Optional<JwtSigningKey> findFirstByActiveTrueOrderByKeyVersionDesc();

    Optional<JwtSigningKey> findByKeyVersion(Integer keyVersion);

    List<JwtSigningKey> findByExpiresAtAfterOrderByKeyVersionDesc(LocalDateTime referenceTime);

    long deleteByActiveFalseAndExpiresAtBefore(LocalDateTime referenceTime);

    @Query("select coalesce(max(k.keyVersion), 0) from JwtSigningKey k")
    Integer findMaxKeyVersion();
}
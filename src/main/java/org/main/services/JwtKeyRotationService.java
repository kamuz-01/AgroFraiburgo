package org.main.services;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import org.main.models.JwtSigningKey;
import org.main.repository.JwtSigningKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class JwtKeyRotationService {

    private static final Logger log = LoggerFactory.getLogger(JwtKeyRotationService.class);

    private final JwtSigningKeyRepository jwtSigningKeyRepository;

    @Value("${jwt.bootstrap-secret:}")
    private String bootstrapSecret;

    @Value("${jwt.access-token-ttl-seconds}")
    private long accessTokenTtlSeconds;

    @Value("${jwt.rotation-interval-ms:86400000}")
    private long rotationIntervalMs;

    @Value("${spring.flyway.enabled:true}")
    private boolean flywayEnabled;

    public JwtKeyRotationService(JwtSigningKeyRepository jwtSigningKeyRepository) {
        this.jwtSigningKeyRepository = jwtSigningKeyRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeKeys() {
        if (!flywayEnabled) {
            return;
        }

        if (jwtSigningKeyRepository.count() == 0) {
            JwtSigningKey initialKey = createKey(true, true);
            log.info("JWT signing key initialized with version {}", initialKey.getKeyVersion());
        }

        synchronizeKeyRetention();
    }

    @Scheduled(fixedDelayString = "${jwt.rotation-check-ms:3600000}")
    @Transactional
    public void rotateAndPruneKeys() {
        if (!flywayEnabled) {
            return;
        }

        ensureActiveKeyExists();
        synchronizeKeyRetention();
        rotateIfDue();
        pruneExpiredKeys();
    }

    @Transactional
    public JwtSigningKey getActiveKey() {
        ensureActiveKeyExists();
        return jwtSigningKeyRepository.findFirstByActiveTrueOrderByKeyVersionDesc()
                .orElseThrow(() -> new IllegalStateException("Nenhuma chave JWT ativa disponível."));
    }

    @Transactional
    public Optional<JwtSigningKey> findByVersion(Integer version) {
        if (version == null) {
            return Optional.empty();
        }
        return jwtSigningKeyRepository.findByKeyVersion(version)
                .filter(this::isUsable);
    }

    @Transactional
    public List<JwtSigningKey> findUsableKeys() {
        return jwtSigningKeyRepository.findAll().stream()
                .filter(this::isUsable)
                .toList();
    }

    @Transactional
    public boolean isUsable(JwtSigningKey key) {
        return key != null && (key.isActive() || (key.getExpiresAt() != null && key.getExpiresAt().isAfter(LocalDateTime.now())));
    }

    private void ensureActiveKeyExists() {
        if (jwtSigningKeyRepository.findFirstByActiveTrueOrderByKeyVersionDesc().isPresent()) {
            return;
        }

        JwtSigningKey fallback = jwtSigningKeyRepository.findAll().stream()
                .filter(this::isUsable)
                .findFirst()
                .orElseGet(() -> createKey(true, false));

        if (!fallback.isActive()) {
            fallback.setActive(true);
            jwtSigningKeyRepository.save(fallback);
        }
    }

    private void rotateIfDue() {
        JwtSigningKey activeKey = jwtSigningKeyRepository.findFirstByActiveTrueOrderByKeyVersionDesc()
                .orElseGet(() -> createKey(true, false));

        long ageMs = Duration.between(activeKey.getCreatedAt(), LocalDateTime.now()).toMillis();
        if (ageMs < rotationIntervalMs) {
            return;
        }

        activeKey.setActive(false);
        activeKey.setExpiresAt(maximumRetirementTime(activeKey, LocalDateTime.now()));
        jwtSigningKeyRepository.save(activeKey);

        JwtSigningKey newKey = createKey(true, false);
        log.info("JWT signing key rotated from version {} to {}", activeKey.getKeyVersion(), newKey.getKeyVersion());
    }

    private void pruneExpiredKeys() {
        jwtSigningKeyRepository.deleteByActiveFalseAndExpiresAtBefore(LocalDateTime.now());
    }

    private JwtSigningKey createKey(boolean active, boolean useBootstrapSecret) {
        String secretBase64 = useBootstrapSecret ? normalizeBootstrapSecret(bootstrapSecret) : null;
        if (secretBase64 == null) {
            secretBase64 = generateRandomSecretBase64();
        }

        Integer maxVersion = jwtSigningKeyRepository.findMaxKeyVersion();
        int nextVersion = (maxVersion == null ? 0 : maxVersion) + 1;

        JwtSigningKey key = new JwtSigningKey();
        key.setKeyVersion(nextVersion);
        key.setSecretBase64(secretBase64);
        key.setActive(active);
        key.setCreatedAt(LocalDateTime.now());
        key.setExpiresAt(LocalDateTime.now().plus(retentionWindow()));
        return jwtSigningKeyRepository.save(key);
    }

    private String generateRandomSecretBase64() {
        SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    // A retenção cobre o tempo em que a chave pode assinar tokens + o TTL máximo desses tokens.
    private Duration retentionWindow() {
        return Duration.ofMillis(rotationIntervalMs).plusSeconds(accessTokenTtlSeconds);
    }

    private LocalDateTime maximumRetirementTime(JwtSigningKey key, LocalDateTime referenceTime) {
        LocalDateTime minimumRetirement = referenceTime.plusSeconds(accessTokenTtlSeconds);
        if (key.getCreatedAt() != null) {
            LocalDateTime scheduledRetirement = key.getCreatedAt().plus(retentionWindow());
            if (scheduledRetirement.isAfter(minimumRetirement)) {
                minimumRetirement = scheduledRetirement;
            }
        }
        return minimumRetirement;
    }

    private void synchronizeKeyRetention() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime activeKeyMinimum = now.plusSeconds(accessTokenTtlSeconds);

        List<JwtSigningKey> keys = jwtSigningKeyRepository.findAll();
        boolean changed = false;

        for (JwtSigningKey key : keys) {
            if (key == null) {
                continue;
            }

            LocalDateTime desiredExpiresAt;
            if (key.isActive()) {
                desiredExpiresAt = activeKeyMinimum;
            } else if (key.getCreatedAt() != null) {
                desiredExpiresAt = key.getCreatedAt().plus(retentionWindow());
            } else {
                continue;
            }

            if (key.getExpiresAt() == null || key.getExpiresAt().isBefore(desiredExpiresAt)) {
                key.setExpiresAt(desiredExpiresAt);
                changed = true;
            }
        }

        if (changed) {
            jwtSigningKeyRepository.saveAll(keys);
        }
    }

    private String normalizeBootstrapSecret(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(value.trim());
            if (decoded.length < 32) {
                log.warn("JWT bootstrap secret ignorado porque tem menos de 32 bytes decodificados. Uma chave nova será gerada.");
                return null;
            }
            return Base64.getEncoder().encodeToString(decoded);
        } catch (IllegalArgumentException ex) {
            log.warn("JWT bootstrap secret inválido. Uma chave nova será gerada automaticamente.");
            return null;
        }
    }
}
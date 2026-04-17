package org.Main.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.main.models.JwtSigningKey;
import org.main.repository.JwtSigningKeyRepository;
import org.main.services.JwtKeyRotationService;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtKeyRotationServiceTest {

    private JwtSigningKeyRepository jwtSigningKeyRepository;
    private JwtKeyRotationService service;

    @BeforeEach
    void setUp() {
        jwtSigningKeyRepository = mock(JwtSigningKeyRepository.class);
        service = new JwtKeyRotationService(jwtSigningKeyRepository);
        ReflectionTestUtils.setField(service, "bootstrapSecret", "");
        ReflectionTestUtils.setField(service, "accessTokenTtlSeconds", 604800L);
        ReflectionTestUtils.setField(service, "rotationIntervalMs", 86400000L);
        ReflectionTestUtils.setField(service, "flywayEnabled", true);
    }

    @Test
    void deveConsiderarChaveAtivaMesmoComExpiresAtExpirado() {
        JwtSigningKey activeKey = new JwtSigningKey();
        activeKey.setKeyVersion(1);
        activeKey.setActive(true);
        activeKey.setCreatedAt(LocalDateTime.now().minusDays(2));
        activeKey.setExpiresAt(LocalDateTime.now().minusHours(1));

        when(jwtSigningKeyRepository.findByKeyVersion(1)).thenReturn(Optional.of(activeKey));

        assertThat(service.findByVersion(1)).isPresent();
        assertThat(service.isUsable(activeKey)).isTrue();
    }

    @Test
    void rotateAndPruneKeysDeveEstenderRetencaoEDesativarChaveNoMomentoDaRotacao() {
        LocalDateTime now = LocalDateTime.now();

        JwtSigningKey activeKey = new JwtSigningKey();
        activeKey.setKeyVersion(2);
        activeKey.setActive(true);
        activeKey.setCreatedAt(now.minusHours(25));
        activeKey.setExpiresAt(now.minusHours(1));

        JwtSigningKey staleInactiveKey = new JwtSigningKey();
        staleInactiveKey.setKeyVersion(1);
        staleInactiveKey.setActive(false);
        staleInactiveKey.setCreatedAt(now.minusHours(1));
        staleInactiveKey.setExpiresAt(now.minusMinutes(30));

        when(jwtSigningKeyRepository.findFirstByActiveTrueOrderByKeyVersionDesc()).thenReturn(Optional.of(activeKey));
        when(jwtSigningKeyRepository.findAll()).thenReturn(List.of(activeKey, staleInactiveKey));
        when(jwtSigningKeyRepository.findMaxKeyVersion()).thenReturn(2);
        when(jwtSigningKeyRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtSigningKeyRepository.save(any(JwtSigningKey.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtSigningKeyRepository.deleteByActiveFalseAndExpiresAtBefore(any())).thenReturn(1L);

        service.rotateAndPruneKeys();

        assertThat(activeKey.isActive()).isFalse();
        assertThat(activeKey.getExpiresAt()).isAfter(now);
        assertThat(staleInactiveKey.getExpiresAt()).isAfter(now);
        verify(jwtSigningKeyRepository).saveAll(anyList());
        verify(jwtSigningKeyRepository).deleteByActiveFalseAndExpiresAtBefore(any());
    }
}

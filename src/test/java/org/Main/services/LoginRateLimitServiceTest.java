package org.Main.services;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.main.models.LoginIpRateLimit;
import org.main.repository.LoginIpRateLimitRepository;
import org.main.services.LoginRateLimitService;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginRateLimitServiceTest {

    private final Map<String, LoginIpRateLimit> armazenamento = new ConcurrentHashMap<>();
    private LoginIpRateLimitRepository loginIpRateLimitRepository;
    private LoginRateLimitService service;

    @BeforeEach
    void setUp() {
        loginIpRateLimitRepository = mock(LoginIpRateLimitRepository.class);
        service = new LoginRateLimitService(3, 10, 30, loginIpRateLimitRepository, new SimpleMeterRegistry());

        when(loginIpRateLimitRepository.findByIpAddressForUpdate(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(armazenamento.get(invocation.getArgument(0))));
        when(loginIpRateLimitRepository.save(any(LoginIpRateLimit.class)))
                .thenAnswer(invocation -> {
                    LoginIpRateLimit estado = invocation.getArgument(0);
                    armazenamento.put(estado.getIpAddress(), estado);
                    return estado;
                });
    }

    @Test
    void deveBloquearIpAposLimiteDeTentativas() {
        assertThat(service.verificarBloqueio("10.0.0.1")).isEmpty();
        assertThat(service.registrarFalha("10.0.0.1", "joao")).isEmpty();
        assertThat(service.registrarFalha("10.0.0.1", "joao")).isEmpty();

        var bloqueio = service.registrarFalha("10.0.0.1", "joao");

        assertThat(bloqueio).isPresent();
        assertThat(bloqueio.get().status()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(bloqueio.get().retryAfterSeconds()).isGreaterThan(0);
    }

    @Test
    void sucessoDeveLimparTentativasDoIp() {
        LoginIpRateLimit estado = new LoginIpRateLimit();
        estado.setIpAddress("10.0.0.1");
        estado.setTentativasNaJanela(2);
        estado.setJanelaInicio(LocalDateTime.now());
        armazenamento.put("10.0.0.1", estado);

        service.registrarSucesso("10.0.0.1");

        assertThat(armazenamento.get("10.0.0.1").getTentativasNaJanela()).isZero();
        assertThat(armazenamento.get("10.0.0.1").getJanelaInicio()).isNull();
        assertThat(armazenamento.get("10.0.0.1").getBloqueadoAte()).isNull();
    }
}

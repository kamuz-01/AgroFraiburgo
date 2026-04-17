package org.main.services;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.main.models.LoginIpRateLimit;
import org.main.repository.LoginIpRateLimitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LoginRateLimitService {

    private static final Logger log = LoggerFactory.getLogger(LoginRateLimitService.class);

    private final int maxAttempts;
    private final Duration window;
    private final Duration blockDuration;
    private final LoginIpRateLimitRepository loginIpRateLimitRepository;
    private final Counter loginAttemptsCounter;
    private final Counter loginBlockedCounter;
    private final Counter loginRateLimitedCounter;

    public LoginRateLimitService(@Value("${app.security.login.ip.max-attempts:20}") int maxAttempts,
                                 @Value("${app.security.login.ip.window-minutes:15}") long windowMinutes,
                                 @Value("${app.security.login.ip.block-minutes:30}") long blockMinutes,
                                 LoginIpRateLimitRepository loginIpRateLimitRepository,
                                 MeterRegistry meterRegistry) {
        this.maxAttempts = maxAttempts;
        this.window = Duration.ofMinutes(windowMinutes);
        this.blockDuration = Duration.ofMinutes(blockMinutes);
        this.loginIpRateLimitRepository = loginIpRateLimitRepository;
        this.loginAttemptsCounter = Counter.builder("login.auth.attempts").register(meterRegistry);
        this.loginBlockedCounter = Counter.builder("login.auth.blocked").register(meterRegistry);
        this.loginRateLimitedCounter = Counter.builder("login.auth.rate_limited").register(meterRegistry);
    }

    @Transactional
    public Optional<RateLimitResult> verificarBloqueio(String ip) {
        Optional<LoginIpRateLimit> estadoOpt = loginIpRateLimitRepository.findByIpAddressForUpdate(ip);
        if (estadoOpt.isEmpty()) {
            return Optional.empty();
        }

        LoginIpRateLimit estado = estadoOpt.get();
        LocalDateTime agora = LocalDateTime.now();

        if (estado.getBloqueadoAte() != null) {
            if (agora.isBefore(estado.getBloqueadoAte())) {
                return Optional.of(toResult(estado.getBloqueadoAte(), "Muitas tentativas de login por este endereço. Tente novamente mais tarde."));
            }

            estado.setBloqueadoAte(null);
        }

        if (estado.getJanelaInicio() != null && estado.getJanelaInicio().plus(window).isBefore(agora)) {
            estado.setJanelaInicio(null);
            estado.setTentativasNaJanela(0);
            loginIpRateLimitRepository.save(estado);
        }

        return Optional.empty();
    }

    @Transactional
    public Optional<RateLimitResult> registrarFalha(String ip, String nomeLogin) {
        loginAttemptsCounter.increment();

        LoginIpRateLimit estado = obterOuCriarEstado(ip);
        LocalDateTime agora = LocalDateTime.now();

        if (estado.getBloqueadoAte() != null && agora.isBefore(estado.getBloqueadoAte())) {
            loginRateLimitedCounter.increment();
            loginBlockedCounter.increment();
            log.warn("Tentativa de login recebida de IP já bloqueado {} para o usuário {}.", ip, nomeLogin);
            return Optional.of(toResult(estado.getBloqueadoAte(), "Muitas tentativas de login por este endereço. Tente novamente mais tarde."));
        }

        if (estado.getBloqueadoAte() != null && !agora.isBefore(estado.getBloqueadoAte())) {
            estado.setBloqueadoAte(null);
        }

        if (estado.getJanelaInicio() == null || estado.getJanelaInicio().plus(window).isBefore(agora)) {
            estado.setJanelaInicio(agora);
            estado.setTentativasNaJanela(0);
        }

        int tentativasAtualizadas = estado.getTentativasNaJanela() + 1;
        estado.setTentativasNaJanela(tentativasAtualizadas);

        if (tentativasAtualizadas >= maxAttempts) {
            estado.setBloqueadoAte(agora.plus(blockDuration));
            loginIpRateLimitRepository.save(estado);
            loginRateLimitedCounter.increment();
            loginBlockedCounter.increment();
            log.warn("IP {} bloqueado após {} tentativas de login em {} minutos. Usuário informado: {}", ip, maxAttempts, window.toMinutes(), nomeLogin);
            return Optional.of(toResult(estado.getBloqueadoAte(), "Muitas tentativas de login por este endereço. Tente novamente mais tarde."));
        }

        loginIpRateLimitRepository.save(estado);
        return Optional.empty();
    }

    @Transactional
    public void registrarSucesso(String ip) {
        Optional<LoginIpRateLimit> estadoOpt = loginIpRateLimitRepository.findByIpAddressForUpdate(ip);
        if (estadoOpt.isEmpty()) {
            return;
        }

        LoginIpRateLimit estado = estadoOpt.get();
        estado.setTentativasNaJanela(0);
        estado.setJanelaInicio(null);
        estado.setBloqueadoAte(null);
        loginIpRateLimitRepository.save(estado);
    }

    private LoginIpRateLimit obterOuCriarEstado(String ip) {
        return loginIpRateLimitRepository.findByIpAddressForUpdate(ip)
                .orElseGet(() -> criarEstadoComRetry(ip));
    }

    private LoginIpRateLimit criarEstadoComRetry(String ip) {
        LoginIpRateLimit novo = new LoginIpRateLimit();
        novo.setIpAddress(ip);
        novo.setTentativasNaJanela(0);
        try {
            return loginIpRateLimitRepository.save(novo);
        } catch (DataIntegrityViolationException ex) {
            return loginIpRateLimitRepository.findByIpAddressForUpdate(ip)
                    .orElseThrow(() -> ex);
        }
    }

    private RateLimitResult toResult(LocalDateTime bloqueadoAte, String message) {
        long retryAfter = Math.max(1L, Duration.between(LocalDateTime.now(), bloqueadoAte).toSeconds());
        return new RateLimitResult(HttpStatus.TOO_MANY_REQUESTS, message, retryAfter);
    }

    public record RateLimitResult(HttpStatus status, String message, long retryAfterSeconds) {
    }
}
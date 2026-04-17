package org.main.services;

import org.main.repository.LoginIpRateLimitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LoginIpRateLimitCleanupService {

    private static final Logger log = LoggerFactory.getLogger(LoginIpRateLimitCleanupService.class);

    private final LoginIpRateLimitRepository loginIpRateLimitRepository;
    private final int retentionDays;

    public LoginIpRateLimitCleanupService(LoginIpRateLimitRepository loginIpRateLimitRepository,
                                          @Value("${app.security.login.ip.cleanup.retention-days:30}") int retentionDays) {
        this.loginIpRateLimitRepository = loginIpRateLimitRepository;
        this.retentionDays = retentionDays;
    }

    @Scheduled(fixedDelayString = "${app.security.login.ip.cleanup.fixed-delay-ms:86400000}")
    @Transactional
    public void limparRegistrosAntigos() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        int removidos = loginIpRateLimitRepository.deleteByAtualizadoEmBefore(cutoff);
        if (removidos > 0) {
            log.info("Limpeza periódica de rate limit de IP removeu {} registros antigos (cutoff {}).", removidos, cutoff);
        }
    }
}
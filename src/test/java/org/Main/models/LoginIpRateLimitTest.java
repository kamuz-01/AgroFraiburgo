package org.Main.models;

import org.junit.jupiter.api.Test;
import org.main.models.LoginIpRateLimit;

import static org.assertj.core.api.Assertions.assertThat;

class LoginIpRateLimitTest {

    @Test
    void prePersistDevePreencherTimestamps() {
        LoginIpRateLimit rateLimit = new LoginIpRateLimit();

        rateLimit.prePersist();

        assertThat(rateLimit.getCriadoEm()).isNotNull();
        assertThat(rateLimit.getAtualizadoEm()).isNotNull();
    }

    @Test
    void preUpdateDeveAtualizarTimestamp() throws InterruptedException {
        LoginIpRateLimit rateLimit = new LoginIpRateLimit();
        rateLimit.prePersist();
        var atualizadoAnterior = rateLimit.getAtualizadoEm();

        Thread.sleep(5);
        rateLimit.preUpdate();

        assertThat(rateLimit.getAtualizadoEm()).isAfter(atualizadoAnterior);
    }
}

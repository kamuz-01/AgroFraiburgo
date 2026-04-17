package org.Main.services;

import org.junit.jupiter.api.Test;
import org.main.repository.LoginIpRateLimitRepository;
import org.main.services.LoginIpRateLimitCleanupService;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginIpRateLimitCleanupServiceTest {

    @Test
    void deveRemoverRegistrosMaisAntigosQueOCutoff() {
        LoginIpRateLimitRepository repository = mock(LoginIpRateLimitRepository.class);
        LoginIpRateLimitCleanupService service = new LoginIpRateLimitCleanupService(repository, 30);
        when(repository.deleteByAtualizadoEmBefore(org.mockito.ArgumentMatchers.any(LocalDateTime.class))).thenReturn(4);

        service.limparRegistrosAntigos();

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(repository).deleteByAtualizadoEmBefore(captor.capture());
        assertThat(captor.getValue()).isBeforeOrEqualTo(LocalDateTime.now().minusDays(29));
    }
}

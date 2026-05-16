package org.main.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.main.models.RecuperacaoSenhaToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecuperacaoSenhaTokenRepository extends JpaRepository<RecuperacaoSenhaToken, Integer> {

    Optional<RecuperacaoSenhaToken> findByTokenAndUsadoEmIsNullAndExpiraEmAfter(String token, LocalDateTime agora);

    boolean existsByUsuario_IdUsuarioAndCriadoEmAfter(Integer idUsuario, LocalDateTime depoisDe);

    long countByUsuario_IdUsuarioAndCriadoEmAfter(Integer idUsuario, LocalDateTime depoisDe);

    long countByIpAddressAndCriadoEmAfter(String ipAddress, LocalDateTime depoisDe);

    @Modifying
    @Query("delete from RecuperacaoSenhaToken t where t.usuario.idUsuario = :idUsuario")
    void deleteAllByUsuarioId(@Param("idUsuario") Integer idUsuario);

    @Modifying
    @Query("""
        update RecuperacaoSenhaToken t
        set t.usadoEm = :agora
        where t.usuario.idUsuario = :idUsuario
          and t.usadoEm is null
        """)
    int marcarTokensAtivosComoUsados(@Param("idUsuario") Integer idUsuario,
                                     @Param("agora") LocalDateTime agora);
}

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

    @Modifying
    @Query("delete from RecuperacaoSenhaToken t where t.usuario.idUsuario = :idUsuario")
    void deleteAllByUsuarioId(@Param("idUsuario") Integer idUsuario);
}

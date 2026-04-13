package org.main.repository;

import java.util.List;
import java.util.Optional;

import org.main.DTOs.UsuarioDTO;
import org.main.enums.StatusConta;
import org.main.enums.TipoUsuario;
import org.main.models.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByNomeLogin(String nomeLogin);

    List<Usuario> findByTipoUsuarioAndStatusConta(TipoUsuario tipo, StatusConta status);

    Page<Usuario> findByTipoUsuarioAndStatusConta(TipoUsuario tipo, StatusConta status, Pageable pageable);

    long countByTipoUsuarioAndStatusConta(TipoUsuario tipo, StatusConta status);
    
    boolean existsByNomeLogin(String nomeLogin);
    
    boolean existsByEmail(String email);

	boolean existsByEmailAndIdUsuarioNot(String email, Integer idUsuario);

	Page<Usuario> findByStatusConta(StatusConta status, Pageable pageable);
	
	Page<Usuario> findByTipoUsuario(TipoUsuario tipo, Pageable pageable);
    
    @Query("SELECT u FROM Usuario u WHERE u.oauthProvider = :provider AND u.oauthId = :oauthId")
    Optional<Usuario> findByOauthProviderAndOauthId(@Param("provider") String provider, @Param("oauthId") String oauthId);
    
    @Query("SELECT u FROM Usuario u WHERE u.tipoUsuario IN ('CONSUMIDOR', 'PRODUTOR')")
    List<UsuarioDTO> findConsumidoresEProdutores();

    @Query("""
        SELECT u
        FROM Usuario u
        WHERE u.tipoUsuario = :tipo
          AND u.statusConta = :status
          AND (
            LOWER(COALESCE(u.nome, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
            OR LOWER(COALESCE(u.sobrenome, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
            OR LOWER(COALESCE(u.cidade, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
            OR LOWER(COALESCE(u.estado, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
          )
        ORDER BY u.nome ASC, u.sobrenome ASC, u.idUsuario ASC
        """)
    Page<Usuario> buscarProdutoresPorTermo(@Param("termo") String termo,
                                           @Param("tipo") TipoUsuario tipo,
                                           @Param("status") StatusConta status,
                                           Pageable pageable);

    @Query("""
        SELECT COUNT(u)
        FROM Usuario u
        WHERE u.tipoUsuario = :tipo
          AND u.statusConta = :status
          AND (
            LOWER(COALESCE(u.nome, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
            OR LOWER(COALESCE(u.sobrenome, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
            OR LOWER(COALESCE(u.cidade, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
            OR LOWER(COALESCE(u.estado, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
          )
        """)
    long countProdutoresPorTermo(@Param("termo") String termo,
                                     @Param("tipo") TipoUsuario tipo,
                                     @Param("status") StatusConta status);
}
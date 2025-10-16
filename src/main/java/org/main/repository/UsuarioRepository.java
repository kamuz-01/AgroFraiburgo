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
    
    boolean existsByNomeLogin(String nomeLogin);
    
    boolean existsByEmail(String email);

	Page<Usuario> findByStatusConta(StatusConta status, Pageable pageable);
	
	Page<Usuario> findByTipoUsuario(TipoUsuario tipo, Pageable pageable);
    
    @Query("SELECT u FROM Usuario u WHERE u.oauthProvider = :provider AND u.oauthId = :oauthId")
    Optional<Usuario> findByOauthProviderAndOauthId(@Param("provider") String provider, @Param("oauthId") String oauthId);
    
    @Query("SELECT u FROM Usuario u WHERE u.tipoUsuario IN ('CONSUMIDOR', 'PRODUTOR')")
    List<UsuarioDTO> findConsumidoresEProdutores();
}
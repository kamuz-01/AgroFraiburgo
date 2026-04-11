package org.main.services;

import org.main.enums.TipoUsuario;
import org.main.models.FavoritoProdutor;
import org.main.models.FavoritoProdutorId;
import org.main.neo4j.Neo4jInteracaoService;
import org.main.repository.FavoritoProdutorRepository;
import org.main.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoritoProdutorService {

    private final FavoritoProdutorRepository favoritoProdutorRepository;
    private final UsuarioRepository usuarioRepository;
    private final Neo4jInteracaoService neo4jInteracaoService;

    public FavoritoProdutorService(FavoritoProdutorRepository favoritoProdutorRepository,
                                  UsuarioRepository usuarioRepository,
                                  Neo4jInteracaoService neo4jInteracaoService) {
        this.favoritoProdutorRepository = favoritoProdutorRepository;
        this.usuarioRepository = usuarioRepository;
        this.neo4jInteracaoService = neo4jInteracaoService;
    }

    @Transactional
    public ToggleResultado toggle(Integer idUsuario, Integer idProdutor) {
        if (idUsuario == null) {
            throw new IllegalArgumentException("Usuário inválido");
        }
        if (idProdutor == null) {
            throw new IllegalArgumentException("Produtor inválido");
        }

        boolean produtorExiste = usuarioRepository.findById(idProdutor)
                .map(u -> u.getTipoUsuario() == TipoUsuario.PRODUTOR)
                .orElse(false);

        if (!produtorExiste) {
            throw new IllegalArgumentException("Produtor não encontrado");
        }

        FavoritoProdutorId id = new FavoritoProdutorId(idUsuario, idProdutor);
        boolean exists = favoritoProdutorRepository.existsById(id);
        if (exists) {
            favoritoProdutorRepository.deleteById(id);
            neo4jInteracaoService.atualizarFavoritoProdutor(idUsuario, idProdutor, false);
            return new ToggleResultado(false);
        }

        favoritoProdutorRepository.save(new FavoritoProdutor(idUsuario, idProdutor));
        neo4jInteracaoService.atualizarFavoritoProdutor(idUsuario, idProdutor, true);
        return new ToggleResultado(true);
    }

    public record ToggleResultado(boolean favoritado) {}
}

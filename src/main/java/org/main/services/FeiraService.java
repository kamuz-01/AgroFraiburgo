package org.main.services;

import org.main.models.Feira;
import org.main.repository.FeiraRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class FeiraService {

    private final FeiraRepository feiraRepository;

    public FeiraService(FeiraRepository feiraRepository) {
        this.feiraRepository = feiraRepository;
    }

    public List<Feira> listarTodas() {
        return feiraRepository.findAll();
    }

    public Feira salvar(Feira feira) {
        return feiraRepository.save(feira);
    }

    public Optional<Feira> buscarPorId(Integer id) {
        return feiraRepository.findById(id);
    }

    public Feira atualizar(Integer id, Feira feiraAtualizada) {
        return feiraRepository.findById(id).map(f -> {
            f.setNomeLocal(feiraAtualizada.getNomeLocal());
            f.setLogradouro(feiraAtualizada.getLogradouro());
            f.setNumero(feiraAtualizada.getNumero());
            f.setBairro(feiraAtualizada.getBairro());
            f.setComplemento(feiraAtualizada.getComplemento());
            f.setStatusFeira(feiraAtualizada.getStatusFeira());
            return feiraRepository.save(f);
        }).orElseThrow(() -> new RuntimeException("Feira não encontrada"));
    }
}
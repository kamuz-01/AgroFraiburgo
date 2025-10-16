package org.main.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.main.models.Produtor;
import org.main.enums.StatusProduto;
import org.main.models.Produto;
import org.main.repository.ProdutoRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    @Value("${upload.base-path:imagens-usuarios}")
    private String basePath;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public Produto salvarProduto(Produtor produtor,
                                 String nomeProduto,
                                 String descricao,
                                 Double preco,
                                 String unidadeMedida,
                                 Double quantidadeEstoque,
                                 String statusProduto,
                                 MultipartFile imagem) throws IOException {

        if (imagem == null || imagem.isEmpty()) {
            throw new IllegalArgumentException("A imagem do produto é obrigatória.");
        }

        // Cria diretório se não existir
        Path produtorPath = Paths.get(basePath, String.valueOf(produtor.getIdProdutor()), "produtos");
        Files.createDirectories(produtorPath);

        // Gera nome único
        String nomeArquivoOriginal = imagem.getOriginalFilename();
        String nomeArquivo = System.currentTimeMillis() + "_" +
                (nomeArquivoOriginal != null ? nomeArquivoOriginal : "imagem.png");

        // Salva o arquivo
        Path destino = produtorPath.resolve(nomeArquivo);
        Files.copy(imagem.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        // Cria e popula o objeto
        Produto produto = new Produto();
        produto.setProdutor(produtor);
        produto.setNomeProduto(nomeProduto);
        produto.setDescricao(descricao);
        produto.setPreco(preco);
        produto.setUnidadeMedida(unidadeMedida);
        produto.setQuantidadeEstoque(quantidadeEstoque);
        produto.setStatusProduto(StatusProduto.valueOf(statusProduto));
        produto.setImagemProduto("/" + produtorPath.resolve(nomeArquivo).toString().replace("\\", "/"));

        // Persiste
        return produtoRepository.save(produto);
    }
    
    public List<Produto> listarPorProdutor(Integer idProdutor) {
        return produtoRepository.findByProdutorIdProdutor(idProdutor);
    }
    
    public Produto buscarPorIdEProprietario(Integer idProduto, Integer idProdutor) {
        return produtoRepository
                .findByIdProdutoAndProdutor_IdProdutor(idProduto, idProdutor)
                .orElseThrow(() -> new AccessDeniedException("Produto não encontrado ou não pertence a você."));
    }
    
    public Produto atualizarProduto(Integer idProduto, Integer idProdutor, 
            String nome, String descricao, Double preco,
            String unidade, Double quantidade, String status,
            MultipartFile imagem) throws IOException {

        Produto produto = buscarPorIdEProprietario(idProduto, idProdutor);

        produto.setNomeProduto(nome);
        produto.setDescricao(descricao);
        produto.setPreco(preco);
        produto.setUnidadeMedida(unidade);
        produto.setQuantidadeEstoque(quantidade);

        // Tratar status de forma segura
        if (status != null && !status.isBlank()) {
            produto.setStatusProduto(StatusProduto.valueOf(status.trim().toUpperCase()));
        }

        // Salvar nova imagem, se houver
        if (imagem != null && !imagem.isEmpty()) {
            Path produtorPath = Paths.get(basePath, String.valueOf(idProdutor), "produtos");
            Files.createDirectories(produtorPath);

            String nomeArquivo = System.currentTimeMillis() + "_" + imagem.getOriginalFilename();
            Path destino = produtorPath.resolve(nomeArquivo);
            Files.copy(imagem.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            produto.setImagemProduto("/" + produtorPath.resolve(nomeArquivo).toString().replace("\\", "/"));
        }

        return produtoRepository.save(produto);
    }
    
    public void verificarPropriedadeProduto(Produto produto, Produtor produtor) {
        if (!produto.getProdutor().getIdProdutor().equals(produtor.getIdProdutor())) {
            throw new AccessDeniedException("Você não tem permissão para alterar este produto.");
        }
    }
    
    public boolean removerProduto(Integer idProduto, Integer idProdutor) {
        Long removed = produtoRepository.deleteByIdProdutoAndProdutor_IdProdutor(idProduto, idProdutor);
        return removed != null && removed > 0;
    }
}
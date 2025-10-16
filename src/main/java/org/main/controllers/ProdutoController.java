package org.main.controllers;

import org.main.models.Produto;
import org.main.models.Produtor;
import org.main.repository.ProdutorRepository;
import org.main.services.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.transaction.Transactional;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ProdutorRepository produtorRepository;

    // Cadastro do produto
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> cadastrarProduto(
            Authentication auth,
            @RequestParam("nome_produto") String nomeProduto,
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestParam("preco") Double preco,
            @RequestParam("unidade_medida") String unidadeMedida,
            @RequestParam("quantidade_estoque") Double quantidadeEstoque,
            @RequestParam("status_produto") String statusProduto,
            @RequestPart("imagem_produto") MultipartFile imagem
    ) {
        try {
            // Recupera o ID do usuário autenticado
            Long idUsuario = Long.parseLong(auth.getName());

            // Busca o produtor correspondente
            Produtor produtor = produtorRepository.findById(idUsuario.intValue())
                    .orElseThrow(() -> new RuntimeException("Produtor não encontrado"));

            // Salva o produto
            Produto produto = produtoService.salvarProduto(
                    produtor,
                    nomeProduto,
                    descricao,
                    preco,
                    unidadeMedida,
                    quantidadeEstoque,
                    statusProduto,
                    imagem
            );

            return ResponseEntity.ok(produto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Erro ao cadastrar produto: " + e.getMessage());
        }
    }
    
    // Lista só os produtos do produtor logado
    @GetMapping("/me")
    public ResponseEntity<List<Produto>> listarMeusProdutos(Authentication auth) {
        Integer idUsuario = Integer.valueOf(auth.getName()); // seu JwtAuth coloca id como principal
        List<Produto> lista = produtoService.listarPorProdutor(idUsuario);
        return ResponseEntity.ok(lista);
    }
    
    // Atualizar produto (somente dono)
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> atualizarProduto(
            Authentication auth,
            @PathVariable Integer id,
            @RequestParam("nome_produto") String nomeProduto,
            @RequestParam(value="descricao", required=false) String descricao,
            @RequestParam("preco") Double preco,
            @RequestParam("unidade_medida") String unidade,
            @RequestParam("quantidade_estoque") Double quantidade,
            @RequestParam("status_produto") String status,
            @RequestPart(value="imagem_produto", required=false) MultipartFile imagem) {

        Integer idUsuario = Integer.valueOf(auth.getName());

        try {
            Produto atualizado = produtoService.atualizarProduto(
                    id, idUsuario, nomeProduto, descricao, preco, unidade, quantidade, status, imagem);
            return ResponseEntity.ok(atualizado);
        } catch (AccessDeniedException ade) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ade.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao atualizar produto: " + e.getMessage());
        }
    }

    // Excluir produto (somente dono)
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> excluirProduto(Authentication auth, @PathVariable Integer id) {
        Integer idUsuario = Integer.valueOf(auth.getName());
        boolean ok = produtoService.removerProduto(id, idUsuario);
        if (ok) return ResponseEntity.noContent().build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Produto não encontrado ou você não tem permissão.");
    }

    // Listagem dos produtos de um produtor
    @GetMapping("/produtor/{idProdutor}")
    public ResponseEntity<List<Produto>> listarProdutosPorProdutor(@PathVariable Integer idProdutor) {
        return ResponseEntity.ok(produtoService.listarPorProdutor(idProdutor));
    }
    
    // API pública para listar todos os produtos
    //@GetMapping("/publicos")
    //public ResponseEntity<List<ProdutoDTO>> listarPublicos() {
        // ideal retornar DTO para não expor tudo
        // aqui apenas exemplo simples - implementar ProdutoDTO mapeando os campos públicos
    //}
}
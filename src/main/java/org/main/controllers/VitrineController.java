package org.main.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.main.enums.StatusConta;
import org.main.enums.StatusProduto;
import org.main.enums.TipoUsuario;
import org.main.models.Avaliacao;
import org.main.models.Produto;
import org.main.models.Usuario;
import org.main.models.UsuarioLogado;
import org.main.models.FavoritoProdutorId;
import org.main.neo4j.Neo4jInteracaoService;
import org.main.repository.AvaliacaoRepository;
import org.main.repository.FavoritoProdutorRepository;
import org.main.repository.ProdutoRepository;
import org.main.repository.UsuarioRepository;
import org.main.services.AvaliacaoService;
import org.main.services.RecomendacaoService;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class VitrineController {

    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final AvaliacaoService avaliacaoService;
    private final RecomendacaoService recomendacaoService;
    private final Neo4jInteracaoService neo4jInteracaoService;
    private final FavoritoProdutorRepository favoritoProdutorRepository;

    public VitrineController(UsuarioRepository usuarioRepository,
                             ProdutoRepository produtoRepository,
                             AvaliacaoRepository avaliacaoRepository,
                             AvaliacaoService avaliacaoService,
                             RecomendacaoService recomendacaoService,
                             Neo4jInteracaoService neo4jInteracaoService,
                             FavoritoProdutorRepository favoritoProdutorRepository) {
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.avaliacaoService = avaliacaoService;
        this.recomendacaoService = recomendacaoService;
        this.neo4jInteracaoService = neo4jInteracaoService;
        this.favoritoProdutorRepository = favoritoProdutorRepository;
    }

    @GetMapping("/inicio_usuarios")
    public String inicioUsuarios(Model model, Authentication authentication) {
        long totalProdutoresAtivos = usuarioRepository.countByTipoUsuarioAndStatusConta(TipoUsuario.PRODUTOR, StatusConta.ATIVO);
        long totalProdutos = produtoRepository.count();

        List<Produto> produtosDestaque = produtoRepository.findTop4ByOrderByDataCriacaoDesc();
        List<Usuario> produtoresDestaque = usuarioRepository.findByTipoUsuarioAndStatusConta(TipoUsuario.PRODUTOR, StatusConta.ATIVO);
        if (produtoresDestaque.size() > 3) {
            produtoresDestaque = produtoresDestaque.subList(0, 3);
        }

        model.addAttribute("totalProdutoresAtivos", totalProdutoresAtivos);
        model.addAttribute("totalProdutos", totalProdutos);
        model.addAttribute("produtosDestaque", toProdutoCards(produtosDestaque));
        model.addAttribute("produtoresDestaque", toProdutorCards(produtoresDestaque));

        Integer idUsuario = currentUserId(authentication);
        if (idUsuario != null && isConsumidorOuModerador(authentication)) {
            List<Produto> recomendados = recomendacaoService.recomendarParaUsuario(idUsuario, 4);
            model.addAttribute("produtosRecomendados", toProdutoCards(recomendados));
        }

        return "inicio_usuarios";
    }

    @GetMapping("/produtores")
    public String listarProdutores(Model model) {
        List<Usuario> produtores = usuarioRepository.findByTipoUsuarioAndStatusConta(TipoUsuario.PRODUTOR, StatusConta.ATIVO);
        model.addAttribute("produtores", toProdutorCards(produtores));
        model.addAttribute("totalProdutores", produtores.size());
        return "lista_produtores-familiares";
    }

    @GetMapping("/produtores/{id}")
    public String perfilProdutor(@PathVariable Integer id, Model model, Authentication authentication) {
        Usuario produtor = usuarioRepository.findById(id)
                .filter(u -> u.getTipoUsuario() == TipoUsuario.PRODUTOR)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produtor não encontrado"));

        List<Produto> produtos = produtoRepository.findAllByProdutor_IdProdutor(id);
        long totalAvaliacoes = avaliacaoRepository.contarConsumidoresDistintosPorProdutor(id);
        Double media = avaliacaoRepository.buscarMediaPorProdutor(id);

        int disponiveis = (int) produtos.stream()
                .filter(p -> p.getStatusProduto() == StatusProduto.COM_ESTOQUE)
                .count();

        model.addAttribute("produtor", produtor);
        model.addAttribute("produtos", produtos);
        model.addAttribute("totalProdutos", produtos.size());
        model.addAttribute("produtosDisponiveis", disponiveis);
        model.addAttribute("totalAvaliacoes", totalAvaliacoes);
        model.addAttribute("mediaAvaliacao", media);

        List<Avaliacao> avaliacoesRecentes = avaliacaoRepository.findTop10ByIdProdutorOrderByDataAvaliacaoDesc(id);
        model.addAttribute("avaliacoesRecentes", avaliacoesRecentes);

        Integer idUsuario = currentUserId(authentication);
        if (idUsuario != null && isConsumidorOuModerador(authentication)) {
            boolean favoritado = false;
            try {
                favoritado = favoritoProdutorRepository.existsById(new FavoritoProdutorId(idUsuario, id));
            } catch (DataAccessException ex) {
                // Quando o schema ainda não foi migrado (ex.: tabela não existe),
                // não derruba a página: apenas considera como não favoritado.
                favoritado = false;
            }
            model.addAttribute("produtorFavoritado", favoritado);
        }

        return "perfil_produtor";
    }

    @PostMapping("/produtores/{id}/avaliar")
    public String avaliarProdutor(Authentication auth,
                                 @PathVariable Integer id,
                                 @RequestParam("nota") Integer nota,
                                 @RequestParam(value = "comentario", required = false) String comentario) {
        Integer idConsumidor;
        try {
            idConsumidor = Integer.valueOf(auth.getName());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
        }

        try {
            avaliacaoService.criarOuAtualizar(idConsumidor, id, nota, comentario);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        return "redirect:/produtores/" + id;
    }

    @GetMapping("/produto/{id}")
    public String detalhesProduto(@PathVariable Integer id, Model model, Authentication authentication) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

        Integer idUsuario = currentUserId(authentication);
        if (idUsuario != null && isConsumidorOuModerador(authentication)) {
            neo4jInteracaoService.registrarVisualizacao(idUsuario, produto.getIdProduto());
        }

        Integer idProdutor = produto.getProdutor() != null ? produto.getProdutor().getIdProdutor() : null;
        if (idProdutor == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Produto sem produtor" );
        }

        Usuario produtor = usuarioRepository.findById(idProdutor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Produtor não encontrado"));

        long totalAvaliacoes = avaliacaoRepository.contarConsumidoresDistintosPorProdutor(idProdutor);
        Double media = avaliacaoRepository.buscarMediaPorProdutor(idProdutor);

        model.addAttribute("produto", produto);
        model.addAttribute("produtor", produtor);
        model.addAttribute("totalAvaliacoes", totalAvaliacoes);
        model.addAttribute("mediaAvaliacao", media);

        return "detalhes_produto";
    }

    private boolean isConsumidorOuModerador(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return false;
        for (GrantedAuthority a : auth.getAuthorities()) {
            if (a == null) continue;
            String role = a.getAuthority();
            if ("ROLE_CONSUMIDOR".equals(role) || "ROLE_MODERADOR".equals(role)) {
                return true;
            }
        }
        return false;
    }

    private Integer currentUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof UsuarioLogado u) {
            return u.getId();
        }

        // fallback legado
        try {
            return Integer.valueOf(auth.getName());
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<ProdutoCard> toProdutoCards(List<Produto> produtos) {
        List<ProdutoCard> cards = new ArrayList<>();
        for (Produto produto : produtos) {
            if (produto == null) continue;
            Integer idProdutor = produto.getProdutor() != null ? produto.getProdutor().getIdProdutor() : null;
            Usuario produtor = (idProdutor != null) ? usuarioRepository.findById(idProdutor).orElse(null) : null;

            RatingStats rating = (idProdutor != null) ? ratingForProdutor(idProdutor) : RatingStats.vazio();

            cards.add(new ProdutoCard(
                    produto.getIdProduto(),
                    produto.getNomeProduto(),
                    produto.getDescricao(),
                    produto.getPreco(),
                    produto.getUnidadeMedida(),
                    produto.getImagemProduto(),
                    produto.getStatusProduto(),
                    produtor,
                    rating.media(),
                    rating.total()
            ));
        }
        return cards;
    }

    private List<ProdutorCard> toProdutorCards(List<Usuario> produtores) {
        List<ProdutorCard> cards = new ArrayList<>();
        for (Usuario usuario : produtores) {
            if (usuario == null) continue;
            RatingStats rating = ratingForProdutor(usuario.getIdUsuario());
            cards.add(new ProdutorCard(
                    usuario.getIdUsuario(),
                    usuario.getNome(),
                    usuario.getSobrenome(),
                    usuario.getCidade(),
                    usuario.getEstado(),
                    usuario.getImagemPerfil(),
                    usuario.getEmail(),
                    usuario.getTelefone(),
                    iniciais(usuario.getNome(), usuario.getSobrenome()),
                    rating.media(),
                    rating.total()
            ));
        }
        return cards;
    }

    private RatingStats ratingForProdutor(Integer idProdutor) {
        if (idProdutor == null) return RatingStats.vazio();
        long total = avaliacaoRepository.contarConsumidoresDistintosPorProdutor(idProdutor);
        Double media = avaliacaoRepository.buscarMediaPorProdutor(idProdutor);
        return new RatingStats(media, total);
    }

    private String iniciais(String nome, String sobrenome) {
        String n = (nome == null ? "" : nome.trim());
        String s = (sobrenome == null ? "" : sobrenome.trim());
        StringBuilder sb = new StringBuilder();
        if (!n.isEmpty()) sb.append(Character.toUpperCase(n.charAt(0)));
        if (!s.isEmpty()) sb.append(Character.toUpperCase(s.charAt(0)));
        if (sb.length() == 0 && !n.isEmpty()) {
            sb.append(Character.toUpperCase(n.charAt(0)));
        }
        return sb.length() == 0 ? "?" : sb.toString();
    }

    public record ProdutorCard(
            Integer id,
            String nome,
            String sobrenome,
            String cidade,
            String estado,
            String imagemPerfil,
            String email,
            String telefone,
            String iniciais,
            Double mediaAvaliacao,
            long totalAvaliacoes
    ) {
        public String nomeCompleto() {
            String n = Objects.toString(nome, "").trim();
            String s = Objects.toString(sobrenome, "").trim();
            return (n + " " + s).trim();
        }

        public String localizacao() {
            String c = Objects.toString(cidade, "").trim();
            String e = Objects.toString(estado, "").trim();
            String loc = (c + (c.isEmpty() || e.isEmpty() ? "" : ", ") + e).trim();
            return loc.isEmpty() ? "Fraiburgo, SC" : loc;
        }

        public boolean temImagem() {
            return imagemPerfil != null && !imagemPerfil.isBlank();
        }

        public boolean temTelefone() {
            return telefone != null && !telefone.isBlank();
        }

        public boolean temEmail() {
            return email != null && !email.isBlank();
        }

        public double mediaAvaliacaoNormalizada() {
            return mediaAvaliacao == null ? 0.0 : mediaAvaliacao.doubleValue();
        }

        public boolean temAvaliacoes() {
            return totalAvaliacoes > 0;
        }
    }

    public record ProdutoCard(
            Integer id,
            String nome,
            String descricao,
            Double preco,
            String unidadeMedida,
            String imagemProduto,
            StatusProduto status,
            Usuario produtor,
            Double mediaAvaliacaoProdutor,
            long totalAvaliacoesProdutor
    ) {
        public boolean disponivel() {
            return status == StatusProduto.COM_ESTOQUE;
        }

        public String descricaoCurta() {
            if (descricao == null) return "";
            String trimmed = descricao.trim();
            if (trimmed.length() <= 96) return trimmed;
            return trimmed.substring(0, 93) + "...";
        }

        public double mediaAvaliacaoNormalizada() {
            return mediaAvaliacaoProdutor == null ? 0.0 : mediaAvaliacaoProdutor.doubleValue();
        }

        public boolean temAvaliacoes() {
            return totalAvaliacoesProdutor > 0;
        }
    }

    public record RatingStats(Double media, long total) {
        public static RatingStats vazio() {
            return new RatingStats(null, 0);
        }

        public double mediaNormalizada() {
            return media == null ? 0.0 : media.doubleValue();
        }

        public boolean temAvaliacoes() {
            return total > 0;
        }
    }
}

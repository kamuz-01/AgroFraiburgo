package org.main.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.main.enums.StatusConta;
import org.main.enums.StatusProduto;
import org.main.enums.TipoUsuario;
import org.main.models.Avaliacao;
import org.main.models.Feira;
import org.main.models.Produto;
import org.main.models.Usuario;
import org.main.models.UsuarioLogado;
import org.main.models.FavoritoProdutorId;
import org.main.neo4j.Neo4jInteracaoService;
import org.main.repository.AvaliacaoRepository;
import org.main.repository.FavoritoProdutorRepository;
import org.main.repository.FeiraRepository;
import org.main.repository.ProdutoRepository;
import org.main.repository.UsuarioRepository;
import org.main.services.AvaliacaoService;
import org.main.services.RecomendacaoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.ResponseBody;
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
    private final FeiraRepository feiraRepository;

    public VitrineController(UsuarioRepository usuarioRepository,
                             ProdutoRepository produtoRepository,
                             AvaliacaoRepository avaliacaoRepository,
                             AvaliacaoService avaliacaoService,
                             RecomendacaoService recomendacaoService,
                             Neo4jInteracaoService neo4jInteracaoService,
                             FavoritoProdutorRepository favoritoProdutorRepository,
                             FeiraRepository feiraRepository) {
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.avaliacaoService = avaliacaoService;
        this.recomendacaoService = recomendacaoService;
        this.neo4jInteracaoService = neo4jInteracaoService;
        this.favoritoProdutorRepository = favoritoProdutorRepository;
        this.feiraRepository = feiraRepository;
    }

    @GetMapping("/inicio_usuarios")
    public String inicioUsuarios(Model model, Authentication authentication) {
        long totalProdutoresAtivos = usuarioRepository.countByTipoUsuarioAndStatusConta(TipoUsuario.PRODUTOR, StatusConta.ATIVO);
        long totalProdutos = produtoRepository.count();
        model.addAttribute("q", "");

        List<Produto> produtosDestaque = produtoRepository.findTop4ByOrderByDataCriacaoDesc();
        int pageSizeProdutos = 4;
        List<Usuario> produtoresDestaque = usuarioRepository.findByTipoUsuarioAndStatusConta(TipoUsuario.PRODUTOR, StatusConta.ATIVO);
        if (produtoresDestaque.size() > 3) {
            produtoresDestaque = produtoresDestaque.subList(0, 3);
        }

        model.addAttribute("totalProdutoresAtivos", totalProdutoresAtivos);
        model.addAttribute("totalProdutos", totalProdutos);
        model.addAttribute("produtosDestaque", toProdutoCards(produtosDestaque));
        model.addAttribute("currentProductPage", 0);
        model.addAttribute("pageSizeProdutos", pageSizeProdutos);
        model.addAttribute("hasMoreProdutos", totalProdutos > produtosDestaque.size());
        model.addAttribute("produtoresDestaque", toProdutorCards(produtoresDestaque));

        Integer idUsuario = currentUserId(authentication);
        if (idUsuario != null && isConsumidorOuModerador(authentication)) {
            List<Produto> recomendados = recomendacaoService.recomendarParaUsuario(idUsuario, 4);
            model.addAttribute("produtosRecomendados", toProdutoCards(recomendados));
        }

        return "inicio_usuarios";
    }

    @GetMapping("/inicio_usuarios/produtos/carregar-mais")
    @ResponseBody
    public ProdutosPageResponse carregarMaisProdutos(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "4") int size) {
        Page<Produto> produtosPage = produtosPaginados(page, size);
        return new ProdutosPageResponse(
                toProdutoCardResponses(produtosPage.getContent()),
                produtosPage.getNumber(),
                produtosPage.getSize(),
                produtosPage.getTotalElements(),
                produtosPage.hasNext()
        );
    }

    @GetMapping("/produtores")
    public String listarProdutores(Model model,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "9") int size,
                                   @RequestParam(required = false) String q) {
        Page<Usuario> produtoresPage = produtoresPaginados(page, size, q);
        model.addAttribute("produtores", toProdutorCards(produtoresPage.getContent()));
        model.addAttribute("totalProdutores", produtoresPage.getTotalElements());
        model.addAttribute("currentPage", produtoresPage.getNumber());
        model.addAttribute("pageSize", produtoresPage.getSize());
        model.addAttribute("hasMore", produtoresPage.hasNext());
        model.addAttribute("q", q);
        return "lista_produtores-familiares";
    }

    @GetMapping("/produtores/carregar-mais")
    @ResponseBody
    public ProdutoresPageResponse carregarMaisProdutores(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "9") int size,
                                                         @RequestParam(required = false) String q) {
        Page<Usuario> produtoresPage = produtoresPaginados(page, size, q);
        return new ProdutoresPageResponse(
                toProdutorCardResponses(produtoresPage.getContent()),
                produtoresPage.getNumber(),
                produtoresPage.getSize(),
                produtoresPage.getTotalElements(),
                produtoresPage.hasNext()
        );
    }

    @GetMapping("/buscar")
    public String buscar(@RequestParam(required = false) String q,
                         @RequestParam(required = false) Double minPreco,
                         @RequestParam(required = false) Double maxPreco,
                         Model model,
                         Authentication authentication) {
        String termo = q == null ? "" : q.trim();
        Double precoMinimo = minPreco;
        Double precoMaximo = maxPreco;

        if (precoMinimo != null && precoMaximo != null && precoMinimo > precoMaximo) {
            Double temp = precoMinimo;
            precoMinimo = precoMaximo;
            precoMaximo = temp;
        }

        boolean temFiltroPreco = precoMinimo != null || precoMaximo != null;
        String faixaPrecoTexto = null;
        if (temFiltroPreco) {
            String precoMinTexto = precoMinimo != null
                ? String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", precoMinimo)
                : "Sem mínimo";
            String precoMaxTexto = precoMaximo != null
                ? String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", precoMaximo)
                : "Sem máximo";
            faixaPrecoTexto = precoMinTexto + " - " + precoMaxTexto;
        }

        model.addAttribute("q", termo);
        model.addAttribute("minPreco", precoMinimo);
        model.addAttribute("maxPreco", precoMaximo);
        model.addAttribute("termoBusca", termo);
        model.addAttribute("temFiltroPreco", temFiltroPreco);
        model.addAttribute("faixaPrecoTexto", faixaPrecoTexto);
        model.addAttribute("homeUrl", homeUrlFor(authentication));

        if (termo.isBlank() && !temFiltroPreco) {
            model.addAttribute("produtosEncontrados", List.of());
            model.addAttribute("produtoresEncontrados", List.of());
            model.addAttribute("totalProdutosEncontrados", 0);
            model.addAttribute("totalProdutoresEncontrados", 0);
            return "resultado_busca";
        }

        List<Produto> produtosEncontrados = produtoRepository.buscarPorTermoEPreco(
                termo.isBlank() ? null : termo,
                precoMinimo,
                precoMaximo);

        List<Usuario> produtoresEncontrados = termo.isBlank()
                ? List.of()
                : usuarioRepository.buscarProdutoresPorTermo(
                        termo,
                        TipoUsuario.PRODUTOR,
                        StatusConta.ATIVO,
                        PageRequest.of(0, 24, Sort.by(Sort.Order.asc("nome"), Sort.Order.asc("sobrenome"), Sort.Order.asc("idUsuario")))
                ).getContent();

        model.addAttribute("produtosEncontrados", toProdutoCards(produtosEncontrados));
        model.addAttribute("produtoresEncontrados", toProdutorCards(produtoresEncontrados));
        model.addAttribute("totalProdutosEncontrados", produtosEncontrados.size());
        model.addAttribute("totalProdutoresEncontrados", produtoresEncontrados.size());

        Integer idUsuario = currentUserId(authentication);
        if (idUsuario != null && isConsumidorOuModerador(authentication)) {
            List<Produto> recomendados = recomendacaoService.recomendarParaUsuario(idUsuario, 4);
            model.addAttribute("produtosRecomendados", toProdutoCards(recomendados));
        }

        return "resultado_busca";
    }

    @GetMapping("/offline")
    public String offline() {
        return "offline";
    }

    @GetMapping("/feira")
    public String detalhesFeira(Model model) {
        Feira feira = feiraRepository.findFirstByStatusFeiraOrderByIdFeiraDesc(org.main.enums.StatusFeira.EM_ANDAMENTO)
                .orElse(null);

        model.addAttribute("feira", feira);
        model.addAttribute("feiraEncontrada", feira != null);

        if (feira != null) {
            String enderecoCompleto = enderecoCompleto(feira);
            model.addAttribute("enderecoCompleto", enderecoCompleto);
            model.addAttribute("mapaUrl", mapaUrl(enderecoCompleto));
            model.addAttribute("googleMapsUrl", googleMapsUrl(enderecoCompleto));
        }

        return "detalhes_feira";
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

    private String homeUrlFor(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "/inicio_usuarios";
        }

        for (GrantedAuthority authority : auth.getAuthorities()) {
            if (authority == null) continue;
            String role = authority.getAuthority();
            if ("ROLE_PRODUTOR".equals(role)) {
                return "/home_produtor";
            }
            if ("ROLE_MODERADOR".equals(role)) {
                return "/home_moderador";
            }
            if ("ROLE_CONSUMIDOR".equals(role)) {
                return "/home_consumidor";
            }
        }

        return "/inicio_usuarios";
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

    private List<ProdutoCardResponse> toProdutoCardResponses(List<Produto> produtos) {
        List<ProdutoCardResponse> cards = new ArrayList<>();
        for (Produto produto : produtos) {
            if (produto == null) continue;
            Integer idProdutor = produto.getProdutor() != null ? produto.getProdutor().getIdProdutor() : null;
            Usuario produtor = (idProdutor != null) ? usuarioRepository.findById(idProdutor).orElse(null) : null;
            RatingStats rating = (idProdutor != null) ? ratingForProdutor(idProdutor) : RatingStats.vazio();

            cards.add(new ProdutoCardResponse(
                    produto.getIdProduto(),
                    produto.getNomeProduto(),
                    new ProdutoCard(
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
                    ).descricaoCurta(),
                    produto.getPreco(),
                    produto.getUnidadeMedida(),
                    produto.getImagemProduto(),
                    localizacaoProduto(produtor),
                    rating.mediaNormalizada(),
                    rating.total(),
                    produto.getStatusProduto() == StatusProduto.COM_ESTOQUE,
                    produto.getImagemProduto() != null && !produto.getImagemProduto().isBlank()
            ));
        }
        return cards;
    }

    private List<ProdutorCard> toProdutorCards(List<Usuario> produtores) {
        List<ProdutorCard> cards = new ArrayList<>();
        for (Usuario usuario : produtores) {
            if (usuario == null) continue;
            RatingStats rating = ratingForProdutor(usuario.getIdUsuario());
            long totalFavoritos = favoritoProdutorRepository.countFavoritosPorProdutor(usuario.getIdUsuario());
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
                    totalFavoritos
            ));
        }
        return cards;
    }

    private List<ProdutorCardResponse> toProdutorCardResponses(List<Usuario> produtores) {
        List<ProdutorCardResponse> cards = new ArrayList<>();
        for (Usuario usuario : produtores) {
            if (usuario == null) continue;
            RatingStats rating = ratingForProdutor(usuario.getIdUsuario());
            long totalFavoritos = favoritoProdutorRepository.countFavoritosPorProdutor(usuario.getIdUsuario());
            cards.add(new ProdutorCardResponse(
                    usuario.getIdUsuario(),
                    Objects.toString(usuario.getNome(), "").trim(),
                    Objects.toString(usuario.getSobrenome(), "").trim(),
                    Objects.toString(usuario.getCidade(), "").trim(),
                    Objects.toString(usuario.getEstado(), "").trim(),
                    ((Objects.toString(usuario.getNome(), "").trim()) + " " + Objects.toString(usuario.getSobrenome(), "").trim()).trim(),
                    localizacao(usuario),
                    usuario.getImagemPerfil(),
                    usuario.getEmail(),
                    usuario.getTelefone(),
                    iniciais(usuario.getNome(), usuario.getSobrenome()),
                    rating.mediaNormalizada(),
                    totalFavoritos,
                    usuario.getImagemPerfil() != null && !usuario.getImagemPerfil().isBlank(),
                    usuario.getTelefone() != null && !usuario.getTelefone().isBlank(),
                    usuario.getEmail() != null && !usuario.getEmail().isBlank(),
                    rating.temAvaliacoes()
            ));
        }
        return cards;
    }

    private Page<Produto> produtosPaginados(int page, int size) {
        int pageIndex = Math.max(page, 0);
        int pageSize = Math.max(1, Math.min(size, 24));
        Pageable pageable = PageRequest.of(
                pageIndex,
                pageSize,
                Sort.by(Sort.Order.desc("dataCriacao"), Sort.Order.desc("idProduto"))
        );
        return produtoRepository.findAll(pageable);
    }

    private Page<Usuario> produtoresPaginados(int page, int size, String q) {
        int pageIndex = Math.max(page, 0);
        int pageSize = Math.max(1, Math.min(size, 24));
        Pageable pageable = PageRequest.of(
                pageIndex,
                pageSize,
                Sort.by(Sort.Order.asc("nome"), Sort.Order.asc("sobrenome"), Sort.Order.asc("idUsuario"))
        );
        String termo = q == null ? "" : q.trim();
        if (termo.isBlank()) {
            return usuarioRepository.findByTipoUsuarioAndStatusConta(TipoUsuario.PRODUTOR, StatusConta.ATIVO, pageable);
        }

        return usuarioRepository.buscarProdutoresPorTermo(termo, TipoUsuario.PRODUTOR, StatusConta.ATIVO, pageable);
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

    private String localizacao(Usuario usuario) {
        String cidade = Objects.toString(usuario.getCidade(), "").trim();
        String estado = Objects.toString(usuario.getEstado(), "").trim();
        String loc = (cidade + (cidade.isEmpty() || estado.isEmpty() ? "" : ", ") + estado).trim();
        return loc.isEmpty() ? "Fraiburgo, SC" : loc;
    }

    private String localizacaoProduto(Usuario usuario) {
        if (usuario == null) {
            return "Fraiburgo";
        }
        String cidade = Objects.toString(usuario.getCidade(), "").trim();
        return cidade.isEmpty() ? "Fraiburgo" : cidade;
    }

    private String enderecoCompleto(Feira feira) {
        String complemento = Objects.toString(feira.getComplemento(), "").trim();
        String base = String.format("%s, %s - %s", feira.getLogradouro(), feira.getNumero(), feira.getBairro()).trim();
        if (!complemento.isBlank()) {
            base = base + ", " + complemento;
        }
        return base + ", Fraiburgo - SC, Brasil";
    }

    private String mapaUrl(String enderecoCompleto) {
        String consulta = URLEncoder.encode(enderecoCompleto, StandardCharsets.UTF_8);
        return "https://www.google.com/maps?q=" + consulta + "&output=embed";
    }

    private String googleMapsUrl(String enderecoCompleto) {
        String consulta = URLEncoder.encode(enderecoCompleto, StandardCharsets.UTF_8);
        return "https://www.google.com/maps/search/?api=1&query=" + consulta;
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
            long totalFavoritos
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

        public boolean temFavoritos() {
            return totalFavoritos > 0;
        }
    }

    public record ProdutorCardResponse(
            Integer id,
            String nome,
            String sobrenome,
            String cidade,
            String estado,
            String nomeCompleto,
            String localizacao,
            String imagemPerfil,
            String email,
            String telefone,
            String iniciais,
            double mediaAvaliacaoNormalizada,
            long totalFavoritos,
            boolean temImagem,
            boolean temTelefone,
            boolean temEmail,
            boolean temAvaliacoes
    ) {}

        public record ProdutosPageResponse(
            List<ProdutoCardResponse> produtos,
            int currentPage,
            int pageSize,
            long totalProdutos,
            boolean hasMore
        ) {}

        public record ProdutoCardResponse(
            Integer id,
            String nome,
            String descricaoCurta,
            Double preco,
            String unidadeMedida,
            String imagemProduto,
            String cidade,
            double mediaAvaliacaoNormalizada,
            long totalAvaliacoesProdutor,
            boolean disponivel,
            boolean temImagem
        ) {}

    public record ProdutoresPageResponse(
            List<ProdutorCardResponse> produtores,
            int currentPage,
            int pageSize,
            long totalProdutores,
            boolean hasMore
    ) {}

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

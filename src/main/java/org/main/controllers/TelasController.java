package org.main.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.time.LocalDate;

import org.main.enums.StatusConta;
import org.main.enums.StatusProduto;
import org.main.enums.TipoUsuario;
import org.main.models.Usuario;
import org.main.models.Produto;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.main.repository.AvaliacaoRepository;
import org.main.repository.FavoritoProdutorRepository;
import org.main.repository.ProdutoRepository;
import org.main.repository.UsuarioRepository;
import org.main.services.RecomendacaoService;
import org.main.services.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.main.web.annotation.CurrentUserId;

@Controller
public class TelasController {
	
	private final UsuarioRepository usuarioRepository;
	private final ProdutoRepository produtoRepository;
	private final AvaliacaoRepository avaliacaoRepository;
	private final FavoritoProdutorRepository favoritoProdutorRepository;
	private final UsuarioService usuarioService;
	private final RecomendacaoService recomendacaoService;
	
	public TelasController(UsuarioService usuarioService,
	                       UsuarioRepository usuarioRepository,
	                       ProdutoRepository produtoRepository,
	                       AvaliacaoRepository avaliacaoRepository,
	                       FavoritoProdutorRepository favoritoProdutorRepository,
	                       RecomendacaoService recomendacaoService) {
	    this.usuarioService = usuarioService;
	    this.usuarioRepository = usuarioRepository;
	    this.produtoRepository = produtoRepository;
	    this.avaliacaoRepository = avaliacaoRepository;
	    this.favoritoProdutorRepository = favoritoProdutorRepository;
	    this.recomendacaoService = recomendacaoService;
	}

	@GetMapping("/")
    public String mostrarPaginaInicial() {
        return "pagina_inicial";
    }
	
	@GetMapping("/login")
	public String mostrarPaginaLogin() {
		return "login";
	}
	
	@GetMapping("/login.html")
	public String redirecionarLoginHtml() {
	    return "redirect:/login";
	}
	
	@GetMapping("/criar_conta")
	public String mostrarPaginaCriarConta() {
		return "criar_conta";
	}
	
	@GetMapping("/produtos")
	public String mostrarProdutos() {
		return "produtos";
	}
	
	@GetMapping("/sobre")
	public String mostrarPaginaSobre() {
	    return "sobre";
	}

	@GetMapping("/perfil")
	public String mostrarPerfilUsuario(Model model, @CurrentUserId Integer idUsuario) {
		Usuario usuario = carregarUsuarioObrigatorio(idUsuario);

		model.addAttribute("usuario", usuario);
		preencherDadosUsuario(model, usuario);
		return "perfil_usuario";
	}

	@PostMapping("/perfil")
	public String atualizarPerfilUsuario(@CurrentUserId Integer idUsuario,
									@RequestParam(required = false) String nome,
									@RequestParam(required = false) String sobrenome,
									@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataNascimento,
									@RequestParam(required = false) String sexo,
									@RequestParam(required = false) String telefone,
									@RequestParam(required = false) String email,
									@RequestParam(required = false) String cidade,
									@RequestParam(required = false) String estado,
									@RequestParam(required = false) MultipartFile imagemPerfil,
								@RequestParam(defaultValue = "false") boolean removerImagemPerfil,
								@RequestParam(required = false) MultipartFile imagemCapa,
								@RequestParam(defaultValue = "false") boolean removerImagemCapa,
									RedirectAttributes redirectAttributes) {
		carregarUsuarioObrigatorio(idUsuario);

		try {
			usuarioService.atualizarPerfilUsuario(idUsuario, nome, sobrenome, dataNascimento, sexo, telefone, email, cidade, estado, imagemPerfil, removerImagemPerfil, imagemCapa, removerImagemCapa);
			redirectAttributes.addFlashAttribute("mensagemSucesso", "Dados atualizados com sucesso.");
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
		} catch (java.io.IOException ex) {
			redirectAttributes.addFlashAttribute("mensagemErro", "Não foi possível salvar as imagens do perfil.");
		}

		return "redirect:/perfil";
	}

	private Usuario carregarUsuarioObrigatorio(Integer idUsuario) {
		if (idUsuario == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
		}

		return usuarioRepository.findById(idUsuario)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
	}

	private void preencherDadosUsuario(Model model, Usuario usuario) {
		model.addAttribute("nome", usuario.getNome());
		model.addAttribute("tipoUsuario", usuario.getTipoUsuario());
		model.addAttribute("imagemPerfil", usuario.getImagemPerfil());
		model.addAttribute("imagemCapa", usuario.getImagemCapa());
		model.addAttribute("homeUrl", homeUrlFor(usuario.getTipoUsuario()));
	}
	
	@GetMapping("/upload_documentos")
	public String mostrarUploadDocumentos() {
		return "upload_documentos";
	}
	
	@GetMapping("/home_produtor")
	public String homeProdutor(Model model, @CurrentUserId Integer idUsuario) {
	    Usuario usuario = carregarUsuarioObrigatorio(idUsuario);

	    preencherDadosUsuario(model, usuario);
	    addInicioUsuariosModel(model, idUsuario, usuario.getTipoUsuario());

	    return "home_produtor";
	}

	@GetMapping("/catalogo_produtor")
	public String catalogoProdutor(Model model, @CurrentUserId Integer idUsuario) {
	    Usuario usuario = carregarUsuarioObrigatorio(idUsuario);

	    preencherDadosUsuario(model, usuario);
	    addInicioUsuariosModel(model, idUsuario, usuario.getTipoUsuario());

	    return "catalogo_produtor";
	}
	
	@GetMapping("/home_consumidor")
	public String homeConsumidor(Model model, @CurrentUserId Integer idUsuario) {
	    Usuario usuario = carregarUsuarioObrigatorio(idUsuario);

	    preencherDadosUsuario(model, usuario);
	    addInicioUsuariosModel(model, idUsuario, usuario.getTipoUsuario());

	    return "home_consumidor";
	}
	
	@GetMapping("/home_moderador")
	public String homeModerador(Model model, @CurrentUserId Integer idUsuario) {
		Usuario usuario = carregarUsuarioObrigatorio(idUsuario);

		preencherDadosUsuario(model, usuario);
		addInicioUsuariosModel(model, idUsuario, usuario.getTipoUsuario());

		return "home_moderador";
	}
	
	// Logout
    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login?logout=true";
    }

	private void addInicioUsuariosModel(Model model, Integer idUsuario, TipoUsuario tipoUsuario) {
		model.addAttribute("q", "");
		long totalProdutoresAtivos = usuarioRepository.countByTipoUsuarioAndStatusConta(TipoUsuario.PRODUTOR, StatusConta.ATIVO);
		long totalProdutos = produtoRepository.count();

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

		if (idUsuario != null && (tipoUsuario == TipoUsuario.CONSUMIDOR || tipoUsuario == TipoUsuario.MODERADOR)) {
			List<Produto> recomendados = recomendacaoService.recomendarParaUsuario(idUsuario, 4);
			model.addAttribute("produtosRecomendados", toProdutoCards(recomendados));
		}
	}

	private String homeUrlFor(TipoUsuario tipoUsuario) {
		if (tipoUsuario == TipoUsuario.PRODUTOR) {
			return "/home_produtor";
		}
		if (tipoUsuario == TipoUsuario.MODERADOR) {
			return "/home_moderador";
		}
		return "/home_consumidor";
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
    
	//----------------------------------------------------------------------------
	// Telas do produtor
	//----------------------------------------------------------------------------
	@GetMapping("/cadastro_produtos")
	public String cadastrarProdutor(Model model, Authentication auth, @CurrentUserId Integer idUsuario) {
	    if (auth == null || !auth.isAuthenticated()) {
	        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
	    }

	    boolean isProdutor = auth.getAuthorities().stream()
	            .anyMatch(a -> a.getAuthority().equals("ROLE_PRODUTOR"));

        if (!isProdutor) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a moderadores");
        }

	    Usuario usuario = carregarUsuarioObrigatorio(idUsuario);

	    preencherDadosUsuario(model, usuario);

        return "cadastro_produtos";
	}
	
	@GetMapping("/lista_produtos")
	public String listagemProdutor(Model model, Authentication auth, @CurrentUserId Integer idUsuario) {
	    if (auth == null || !auth.isAuthenticated()) {
	        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
	    }

	    boolean isProdutor = auth.getAuthorities().stream()
	            .anyMatch(a -> a.getAuthority().equals("ROLE_PRODUTOR"));

        if (!isProdutor) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a moderadores");
        }

	    Usuario usuario = carregarUsuarioObrigatorio(idUsuario);

	    preencherDadosUsuario(model, usuario);

        return "lista_produtos";
	}
    
    //----------------------------------------------------------------------------
    // Telas do moderador
    //----------------------------------------------------------------------------
    @GetMapping("/cadastro_moderadores")
	public String cadastrarModerador(Model model, Authentication auth, @CurrentUserId Integer idUsuario) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
        }

        boolean isModerador = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MODERADOR"));

        if (!isModerador) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a moderadores");
        }

	    Usuario usuario = carregarUsuarioObrigatorio(idUsuario);

	    preencherDadosUsuario(model, usuario);

        return "cadastro_moderadores";
    }
    
    @GetMapping("/produtores_pendentes")
    public String paginaPendentes(Model model, Authentication auth, @CurrentUserId Integer idUsuario) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
        }

        boolean isModerador = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MODERADOR"));

        if (!isModerador) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a moderadores");
        }

	    Usuario usuario = carregarUsuarioObrigatorio(idUsuario);

	    preencherDadosUsuario(model, usuario);

        return "produtores_pendentes";
    }
    
    @GetMapping("/listagem_feiras")
    public String paginaFeiras(Model model, Authentication auth, @CurrentUserId Integer idUsuario) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
        }

        boolean isModerador = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MODERADOR"));

        if (!isModerador) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a moderadores");
        }

	    Usuario usuario = carregarUsuarioObrigatorio(idUsuario);

	    preencherDadosUsuario(model, usuario);

        return "listagem_feiras";
    }
    
    @GetMapping("/cadastro_feira")
    public String cadastrarFeiras(Model model, Authentication auth, @CurrentUserId Integer idUsuario) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
        }

        boolean isModerador = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MODERADOR"));

        if (!isModerador) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a moderadores");
        }

	    Usuario usuario = carregarUsuarioObrigatorio(idUsuario);

	    preencherDadosUsuario(model, usuario);

        return "cadastro_feira";
    }
    
    @GetMapping("/administrar_usuarios")
    public String administrarUsuarios(Model model, Authentication auth, @CurrentUserId Integer idUsuario) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
        }

        boolean isModerador = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MODERADOR"));

        if (!isModerador) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a moderadores");
        }

	    Usuario usuario = carregarUsuarioObrigatorio(idUsuario);

	    preencherDadosUsuario(model, usuario);

        return "moderacao_usuarios";
    }

	@GetMapping({"/moderacao_usuarios", "/moderacao_usuarios.html"})
	public String moderacaoUsuariosAlias(Model model, Authentication auth, @CurrentUserId Integer idUsuario) {
		return administrarUsuarios(model, auth, idUsuario);
	}
}
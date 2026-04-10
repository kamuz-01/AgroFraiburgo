package org.main.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.main.enums.StatusConta;
import org.main.enums.StatusProduto;
import org.main.enums.TipoUsuario;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.main.models.Usuario;
import org.main.models.Produto;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.main.repository.AvaliacaoRepository;
import org.main.repository.ProdutoRepository;
import org.main.repository.UsuarioRepository;
import org.main.services.JwtService;
import org.main.services.RecomendacaoService;
import org.main.services.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class TelasController {
	
	private final UsuarioRepository usuarioRepository;
	private final ProdutoRepository produtoRepository;
	private final AvaliacaoRepository avaliacaoRepository;
	private final JwtService jwtService;
	private final UsuarioService usuarioService;
	private final RecomendacaoService recomendacaoService;
	
	public TelasController(UsuarioService usuarioService,
	                       UsuarioRepository usuarioRepository,
	                       ProdutoRepository produtoRepository,
	                       AvaliacaoRepository avaliacaoRepository,
	                       JwtService jwtService,
	                       RecomendacaoService recomendacaoService) {
	    this.usuarioService = usuarioService;
	    this.usuarioRepository = usuarioRepository;
	    this.produtoRepository = produtoRepository;
	    this.avaliacaoRepository = avaliacaoRepository;
	    this.jwtService = jwtService;
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
	
	@GetMapping("/upload_documentos")
	public String mostrarUploadDocumentos() {
		return "upload_documentos";
	}
	
	@GetMapping("/home_produtor")
	public String homeProdutor(Model model, Authentication auth) {
	    
	    if (auth == null) {
	        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
	    }

	    Integer idUsuario = null;

	    try {
	        if (auth instanceof UsernamePasswordAuthenticationToken) {
	            String principal = auth.getName();

	            // Se for número -> OAuth2 / idUsuario
	            if (principal.matches("\\d+")) {
	                idUsuario = Integer.valueOf(principal);

	            // Se for texto -> login tradicional
	            } else {
	                idUsuario = usuarioService.buscarPorNomeLogin(principal)
	                        .map(Usuario::getIdUsuario)
	                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + principal));
	            }

	        } else if (auth instanceof JwtAuthenticationToken jwtAuth) {
	            String token = jwtAuth.getToken().getTokenValue();
	            Long uidLong = jwtService.extractUserId(token);
	            if (uidLong == null) {
	                throw new RuntimeException("JWT sem uid");
	            }
	            idUsuario = uidLong.intValue();

	        } else {
	            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tipo de autenticação não suportado");
	        }

	        Usuario usuario = usuarioRepository.findById(idUsuario)
	                .orElseThrow(() -> new RuntimeException("Usuário ainda não finalizou o cadastro"));

	        model.addAttribute("nome", usuario.getNome());
	        model.addAttribute("tipoUsuario", usuario.getTipoUsuario());
	        model.addAttribute("imagemPerfil", usuario.getImagemPerfil());
	        model.addAttribute("imagemCapa", usuario.getImagemCapa());
	        addInicioUsuariosModel(model, idUsuario, usuario.getTipoUsuario());

	        return "home_produtor";

	    } catch (Exception e) {
	        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
	    }
	}
	
	@GetMapping("/home_consumidor")
	public String homeConsumidor(Model model, Authentication auth) {

	    if (auth == null) {
	        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
	    }

	    Integer idUsuario = null;

	    try {
	        if (auth instanceof UsernamePasswordAuthenticationToken) {
	            String principal = auth.getName();

	            // Se for número -> OAuth2 / idUsuario
	            if (principal.matches("\\d+")) {
	                idUsuario = Integer.valueOf(principal);

	            // Se for texto -> login tradicional
	            } else {
	                idUsuario = usuarioService.buscarPorNomeLogin(principal)
	                        .map(Usuario::getIdUsuario)
	                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + principal));
	            }

	        } else if (auth instanceof JwtAuthenticationToken jwtAuth) {
	            String token = jwtAuth.getToken().getTokenValue();
	            Long uidLong = jwtService.extractUserId(token);
	            if (uidLong == null) {
	                throw new RuntimeException("JWT sem uid");
	            }
	            idUsuario = uidLong.intValue();

	        } else {
	            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tipo de autenticação não suportado");
	        }

	        Usuario usuario = usuarioRepository.findById(idUsuario)
	                .orElseThrow(() -> new RuntimeException("Usuário ainda não finalizou o cadastro"));

	        model.addAttribute("nome", usuario.getNome());
	        model.addAttribute("tipoUsuario", usuario.getTipoUsuario());
	        model.addAttribute("imagemPerfil", usuario.getImagemPerfil());
	        model.addAttribute("imagemCapa", usuario.getImagemCapa());
	        addInicioUsuariosModel(model, idUsuario, usuario.getTipoUsuario());

	        return "home_consumidor";

	    } catch (Exception e) {
	        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
	    }
	}
	
	@GetMapping("/home_moderador")
	public String homeModerador(Model model, Authentication auth) {
		if (auth == null) {
	        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
	    }

	    Integer idUsuario = null;

	    try {
	        if (auth instanceof UsernamePasswordAuthenticationToken) {
	            String principal = auth.getName();

	            // Se for número -> OAuth2 / idUsuario
	            if (principal.matches("\\d+")) {
	                idUsuario = Integer.valueOf(principal);

	            // Se for texto -> login tradicional
	            } else {
	                idUsuario = usuarioService.buscarPorNomeLogin(principal)
	                        .map(Usuario::getIdUsuario)
	                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + principal));
	            }

	        } else if (auth instanceof JwtAuthenticationToken jwtAuth) {
	            String token = jwtAuth.getToken().getTokenValue();
	            Long uidLong = jwtService.extractUserId(token);
	            if (uidLong == null) {
	                throw new RuntimeException("JWT sem uid");
	            }
	            idUsuario = uidLong.intValue();

	        } else {
	            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tipo de autenticação não suportado");
	        }

	        Usuario usuario = usuarioRepository.findById(idUsuario)
	                .orElseThrow(() -> new RuntimeException("Usuário ainda não finalizou o cadastro"));

	        model.addAttribute("nome", usuario.getNome());
	        model.addAttribute("tipoUsuario", usuario.getTipoUsuario());
	        model.addAttribute("imagemPerfil", usuario.getImagemPerfil());
	        model.addAttribute("imagemCapa", usuario.getImagemCapa());
	        addInicioUsuariosModel(model, idUsuario, usuario.getTipoUsuario());

	        return "home_moderador";

	    } catch (Exception e) {
	        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
	    }
	}
	
	// Logout
    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login?logout=true";
    }

	private void addInicioUsuariosModel(Model model, Integer idUsuario, TipoUsuario tipoUsuario) {
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

		if (idUsuario != null && (tipoUsuario == TipoUsuario.CONSUMIDOR || tipoUsuario == TipoUsuario.MODERADOR)) {
			List<Produto> recomendados = recomendacaoService.recomendarParaUsuario(idUsuario, 4);
			model.addAttribute("produtosRecomendados", toProdutoCards(recomendados));
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
    
	//----------------------------------------------------------------------------
	// Telas do produtor
	//----------------------------------------------------------------------------
	@GetMapping("/cadastro_produtos")
	public String cadastrarProdutor(Model model, Authentication auth) {
		
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
        }

        // Checa direto pelas roles já definidas no filtro
        boolean isProdutor = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PRODUTOR"));
        
		Integer idUsuario = null;
		try {
			if (auth instanceof UsernamePasswordAuthenticationToken) {
				String principal = auth.getName();
				idUsuario = Integer.valueOf(principal);
			} else if (auth instanceof JwtAuthenticationToken jwtAuth) {
				String token = jwtAuth.getToken().getTokenValue();
				Long uidLong = jwtService.extractUserId(token);
				if (uidLong == null) {
					throw new RuntimeException("JWT sem uid");
				}
				idUsuario = uidLong.intValue();
			} else {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tipo de autenticação não suportado");
			}
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
		}

        if (!isProdutor) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a moderadores");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário ainda não finalizou o cadastro"));

        model.addAttribute("nome", usuario.getNome());
        model.addAttribute("tipoUsuario", usuario.getTipoUsuario());
        model.addAttribute("imagemPerfil", usuario.getImagemPerfil());
        model.addAttribute("imagemCapa", usuario.getImagemCapa());

        return "cadastro_produtos";
	}
	
	@GetMapping("/lista_produtos")
	public String listagemProdutor(Model model, Authentication auth) {
		
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
        }

        // Checa direto pelas roles já definidas no filtro
        boolean isProdutor = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PRODUTOR"));
        
		Integer idUsuario = null;
		try {
			if (auth instanceof UsernamePasswordAuthenticationToken) {
				String principal = auth.getName();
				idUsuario = Integer.valueOf(principal);
			} else if (auth instanceof JwtAuthenticationToken jwtAuth) {
				String token = jwtAuth.getToken().getTokenValue();
				Long uidLong = jwtService.extractUserId(token);
				if (uidLong == null) {
					throw new RuntimeException("JWT sem uid");
				}
				idUsuario = uidLong.intValue();
			} else {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tipo de autenticação não suportado");
			}
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
		}

        if (!isProdutor) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a moderadores");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário ainda não finalizou o cadastro"));

        model.addAttribute("nome", usuario.getNome());
        model.addAttribute("tipoUsuario", usuario.getTipoUsuario());
        model.addAttribute("imagemPerfil", usuario.getImagemPerfil());
        model.addAttribute("imagemCapa", usuario.getImagemCapa());

        return "lista_produtos";
	}
    
    //----------------------------------------------------------------------------
    // Telas do moderador
    //----------------------------------------------------------------------------
    @GetMapping("/cadastro_moderadores")
    public String cadastrarModerador(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
        }

        // Checa direto pelas roles já definidas no filtro
        boolean isModerador = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MODERADOR"));
        
		Integer idUsuario = null;
		try {
			if (auth instanceof UsernamePasswordAuthenticationToken) {
				String principal = auth.getName();
				idUsuario = Integer.valueOf(principal);
			} else if (auth instanceof JwtAuthenticationToken jwtAuth) {
				String token = jwtAuth.getToken().getTokenValue();
				Long uidLong = jwtService.extractUserId(token);
				if (uidLong == null) {
					throw new RuntimeException("JWT sem uid");
				}
				idUsuario = uidLong.intValue();
			} else {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tipo de autenticação não suportado");
			}
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
		}

        if (!isModerador) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a moderadores");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário ainda não finalizou o cadastro"));

        model.addAttribute("nome", usuario.getNome());
        model.addAttribute("tipoUsuario", usuario.getTipoUsuario());
        model.addAttribute("imagemPerfil", usuario.getImagemPerfil());
        model.addAttribute("imagemCapa", usuario.getImagemCapa());

        return "cadastro_moderadores";
    }
    
    @GetMapping("/produtores_pendentes")
    public String paginaPendentes(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
        }

        // 🔹 Checa direto pelas roles já definidas no filtro
        boolean isModerador = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MODERADOR"));
        
		Integer idUsuario = null;
		try {
			if (auth instanceof UsernamePasswordAuthenticationToken) {
				String principal = auth.getName();
				idUsuario = Integer.valueOf(principal);
			} else if (auth instanceof JwtAuthenticationToken jwtAuth) {
				String token = jwtAuth.getToken().getTokenValue();
				Long uidLong = jwtService.extractUserId(token);
				if (uidLong == null) {
					throw new RuntimeException("JWT sem uid");
				}
				idUsuario = uidLong.intValue();
			} else {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tipo de autenticação não suportado");
			}
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
		}

        if (!isModerador) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a moderadores");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário ainda não finalizou o cadastro"));

        model.addAttribute("nome", usuario.getNome());
        model.addAttribute("tipoUsuario", usuario.getTipoUsuario());
        model.addAttribute("imagemPerfil", usuario.getImagemPerfil());
        model.addAttribute("imagemCapa", usuario.getImagemCapa());

        return "produtores_pendentes";
    }
    
    @GetMapping("/listagem_feiras")
    public String paginaFeiras(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
        }

        // Checa direto pelas roles já definidas no filtro
        boolean isModerador = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MODERADOR"));
        
		Integer idUsuario = null;
		try {
			if (auth instanceof UsernamePasswordAuthenticationToken) {
				String principal = auth.getName();
				idUsuario = Integer.valueOf(principal);
			} else if (auth instanceof JwtAuthenticationToken jwtAuth) {
				String token = jwtAuth.getToken().getTokenValue();
				Long uidLong = jwtService.extractUserId(token);
				if (uidLong == null) {
					throw new RuntimeException("JWT sem uid");
				}
				idUsuario = uidLong.intValue();
			} else {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tipo de autenticação não suportado");
			}
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
		}

        if (!isModerador) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a moderadores");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário ainda não finalizou o cadastro"));

        model.addAttribute("nome", usuario.getNome());
        model.addAttribute("tipoUsuario", usuario.getTipoUsuario());
        model.addAttribute("imagemPerfil", usuario.getImagemPerfil());
        model.addAttribute("imagemCapa", usuario.getImagemCapa());

        return "listagem_feiras";
    }
    
    @GetMapping("/cadastro_feira")
    public String cadastrarFeiras(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
        }

        // Checa direto pelas roles já definidas no filtro
        boolean isModerador = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MODERADOR"));
        
		Integer idUsuario = null;
		try {
			if (auth instanceof UsernamePasswordAuthenticationToken) {
				String principal = auth.getName();
				idUsuario = Integer.valueOf(principal);
			} else if (auth instanceof JwtAuthenticationToken jwtAuth) {
				String token = jwtAuth.getToken().getTokenValue();
				Long uidLong = jwtService.extractUserId(token);
				if (uidLong == null) {
					throw new RuntimeException("JWT sem uid");
				}
				idUsuario = uidLong.intValue();
			} else {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tipo de autenticação não suportado");
			}
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
		}

        if (!isModerador) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a moderadores");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário ainda não finalizou o cadastro"));

        model.addAttribute("nome", usuario.getNome());
        model.addAttribute("tipoUsuario", usuario.getTipoUsuario());
        model.addAttribute("imagemPerfil", usuario.getImagemPerfil());
        model.addAttribute("imagemCapa", usuario.getImagemCapa());

        return "cadastro_feira";
    }
    
    @GetMapping("/administrar_usuarios")
    public String administrarUsuarios(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
        }

        // Checa direto pelas roles já definidas no filtro
        boolean isModerador = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MODERADOR"));
        
		Integer idUsuario = null;
		try {
			if (auth instanceof UsernamePasswordAuthenticationToken) {
				String principal = auth.getName();
				idUsuario = Integer.valueOf(principal);
			} else if (auth instanceof JwtAuthenticationToken jwtAuth) {
				String token = jwtAuth.getToken().getTokenValue();
				Long uidLong = jwtService.extractUserId(token);
				if (uidLong == null) {
					throw new RuntimeException("JWT sem uid");
				}
				idUsuario = uidLong.intValue();
			} else {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tipo de autenticação não suportado");
			}
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
		}

        if (!isModerador) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a moderadores");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário ainda não finalizou o cadastro"));

        model.addAttribute("nome", usuario.getNome());
        model.addAttribute("tipoUsuario", usuario.getTipoUsuario());
        model.addAttribute("imagemPerfil", usuario.getImagemPerfil());
        model.addAttribute("imagemCapa", usuario.getImagemCapa());

        return "moderacao_usuarios";
    }
}
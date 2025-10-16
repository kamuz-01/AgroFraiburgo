package org.main.controllers;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.main.models.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.main.repository.UsuarioRepository;
import org.main.services.JwtService;
import org.main.services.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class TelasController {
	
	private final UsuarioRepository usuarioRepository;
	private final JwtService jwtService;
	private final UsuarioService usuarioService;
	
	public TelasController(UsuarioService usuarioService,
	                       UsuarioRepository usuarioRepository,
	                       JwtService jwtService) {
	    this.usuarioService = usuarioService;
	    this.usuarioRepository = usuarioRepository;
	    this.jwtService = jwtService;
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
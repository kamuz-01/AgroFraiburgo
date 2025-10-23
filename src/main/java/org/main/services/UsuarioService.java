package org.main.services;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import org.main.DTOs.CadastroUsuarioDTO;
import org.main.DTOs.DocumentosProdutorDTO;
import org.main.enums.StatusConta;
import org.main.enums.TipoUsuario;
import org.main.models.DocumentosProdutor;
import org.main.models.Usuario;
import org.main.repository.DocumentosProdutorRepository;
import org.main.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailBoasVindasService emailBoasVindasService;
    
    @Autowired
    private DocumentosProdutorRepository documentosProdutorRepository;

    // Diretórios base
    private static final Path BASE_DIR = Paths.get(System.getProperty("user.dir"), "imagens-usuarios");
    private static final Path DEFAULTS_DIR = BASE_DIR.resolve("defaults");
    private static final Path DOCS_DIR = Paths.get(System.getProperty("user.dir"), "documentos-produtores");

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, EmailBoasVindasService emailBoasVindasService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailBoasVindasService = emailBoasVindasService;

        try {
            inicializarDiretorios();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public Optional<Usuario> buscarPorNomeLogin(String nomeLogin) {
        return usuarioRepository.findByNomeLogin(nomeLogin);
    }

    private void inicializarDiretorios() throws IOException {
        Path defaultsPerfil = DEFAULTS_DIR.resolve("imagem-perfil");
        Path defaultsCapa = DEFAULTS_DIR.resolve("imagem-capa");

        Files.createDirectories(defaultsPerfil);
        Files.createDirectories(defaultsCapa);
        Files.createDirectories(DOCS_DIR);
    }

    private void validarDuplicidade(CadastroUsuarioDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }
        if (usuarioRepository.existsByNomeLogin(dto.getNomeLogin())) {
            throw new IllegalArgumentException("Nome de login já cadastrado.");
        }
    }

    // 🔹 Cadastro de consumidor
    public Usuario cadastrarConsumidor(CadastroUsuarioDTO dto) throws IOException {
        if (dto.getTipoUsuario() != TipoUsuario.CONSUMIDOR) {
            throw new IllegalArgumentException("Este método é apenas para consumidores.");
        }
        validarDuplicidade(dto);

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setSobrenome(dto.getSobrenome());
        usuario.setCpf(dto.getCpf());
        usuario.setDataNascimento(dto.getDataNascimento());
        usuario.setSexo(dto.getSexo());
        usuario.setTelefone(dto.getTelefone());
        usuario.setEmail(dto.getEmail());
        usuario.setNomeLogin(dto.getNomeLogin());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setTipoUsuario(TipoUsuario.CONSUMIDOR);
        usuario.setCidade(dto.getCidade());
        usuario.setEstado(dto.getEstado());
        usuario.setStatusConta(StatusConta.ATIVO);

        // Salvar primeiro para gerar ID
        Usuario salvo = usuarioRepository.save(usuario);

        // Configurar imagens
        configurarImagens(salvo, dto);
        
        // 	Enviar email de boas vindas ao consdumidor cadastrado
        emailBoasVindasService.enviarEmailBoasVindas(salvo);

        return usuarioRepository.save(salvo);
    }

    // Cadastro de produtor
    public Usuario cadastrarProdutor(CadastroUsuarioDTO dto, DocumentosProdutorDTO documentos) throws IOException {
        if (dto.getTipoUsuario() != TipoUsuario.PRODUTOR) {
            throw new IllegalArgumentException("Este método é apenas para produtores.");
        }

        // Criar produtor
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setSobrenome(dto.getSobrenome());
        usuario.setCpf(dto.getCpf());
        usuario.setDataNascimento(dto.getDataNascimento());
        usuario.setSexo(dto.getSexo());
        usuario.setTelefone(dto.getTelefone());
        usuario.setEmail(dto.getEmail());
        usuario.setNomeLogin(dto.getNomeLogin());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setTipoUsuario(TipoUsuario.PRODUTOR);
        usuario.setCidade(dto.getCidade());
        usuario.setEstado(dto.getEstado());
        usuario.setStatusConta(StatusConta.PENDENTE);

        // Salvar primeiro para gerar ID
        Usuario salvo = usuarioRepository.save(usuario);

        // Criar diretórios do usuário
        Path baseUsuario = BASE_DIR.resolve(String.valueOf(salvo.getIdUsuario()));
        Path perfilDir = baseUsuario.resolve("imagem-perfil");
        Path capaDir = baseUsuario.resolve("imagem-capa");
        Path docsDir = DOCS_DIR.resolve(String.valueOf(salvo.getIdUsuario()));

        Files.createDirectories(perfilDir);
        Files.createDirectories(capaDir);
        Files.createDirectories(docsDir);

        // Upload imagem perfil
        if (dto.getImagemPerfil() != null && !dto.getImagemPerfil().isEmpty()) {
            String perfilNome = "perfil_" + System.currentTimeMillis() + ".png";
            Path destino = perfilDir.resolve(perfilNome);
            dto.getImagemPerfil().transferTo(destino.toFile());
            salvo.setImagemPerfil("/imagens-usuarios/" + salvo.getIdUsuario() + "/imagem-perfil/" + perfilNome);
        } else {
            salvo.setImagemPerfil("/imagens-usuarios/defaults/imagem-perfil/perfil.png");
        }

        // Upload imagem capa
        if (dto.getImagemCapa() != null && !dto.getImagemCapa().isEmpty()) {
            String capaNome = "capa_" + System.currentTimeMillis() + ".png";
            Path destino = capaDir.resolve(capaNome);
            dto.getImagemCapa().transferTo(destino.toFile());
            salvo.setImagemCapa("/imagens-usuarios/" + salvo.getIdUsuario() + "/imagem-capa/" + capaNome);
        } else {
            salvo.setImagemCapa("/imagens-usuarios/defaults/imagem-capa/capa.webp");
        }

        // Criar entidade DocumentosProdutor
        DocumentosProdutor doc = new DocumentosProdutor();
        doc.setIdProdutor(salvo.getIdUsuario());

        if (documentos.getDocIdentidade() != null && !documentos.getDocIdentidade().isEmpty()) {
            String fileName = "identidade_" + System.currentTimeMillis() + ".pdf";
            documentos.getDocIdentidade().transferTo(docsDir.resolve(fileName).toFile());
            doc.setDocumentoIdentidade("/documentos-produtores/" + salvo.getIdUsuario() + "/" + fileName);
        }
        if (documentos.getComprovanteResidencia() != null && !documentos.getComprovanteResidencia().isEmpty()) {
            String fileName = "residencia_" + System.currentTimeMillis() + ".pdf";
            documentos.getComprovanteResidencia().transferTo(docsDir.resolve(fileName).toFile());
            doc.setComprovanteResidencia("/documentos-produtores/" + salvo.getIdUsuario() + "/" + fileName);
        }
        if (documentos.getCadastroAgriculturaFamiliar() != null && !documentos.getCadastroAgriculturaFamiliar().isEmpty()) {
            String fileName = "pronaf_" + System.currentTimeMillis() + ".pdf";
            documentos.getCadastroAgriculturaFamiliar().transferTo(docsDir.resolve(fileName).toFile());
            doc.setDeclaracaoPronaf("/documentos-produtores/" + salvo.getIdUsuario() + "/" + fileName);
        }
        if (documentos.getCertificadoOrganico() != null && !documentos.getCertificadoOrganico().isEmpty()) {
            String fileName = "organico_" + System.currentTimeMillis() + ".pdf";
            documentos.getCertificadoOrganico().transferTo(docsDir.resolve(fileName).toFile());
            doc.setCertificadoOrganico("/documentos-produtores/" + salvo.getIdUsuario() + "/" + fileName);
        }
        if (documentos.getCodigoRastreabilidade() != null && !documentos.getCodigoRastreabilidade().isEmpty()) {
            String fileName = "rastreabilidade_" + System.currentTimeMillis() + ".pdf";
            documentos.getCodigoRastreabilidade().transferTo(docsDir.resolve(fileName).toFile());
            doc.setCodigoRastreabilidade("/documentos-produtores/" + salvo.getIdUsuario() + "/" + fileName);
        }
        if (documentos.getInscricaoEstadual() != null && !documentos.getInscricaoEstadual().isEmpty()) {
            String fileName = "inscricao_" + System.currentTimeMillis() + ".pdf";
            documentos.getInscricaoEstadual().transferTo(docsDir.resolve(fileName).toFile());
            doc.setNumeroInscricaoEstadual("/documentos-produtores/" + salvo.getIdUsuario() + "/" + fileName);
        }
        if (documentos.getAlvaraSanitario() != null && !documentos.getAlvaraSanitario().isEmpty()) {
            String fileName = "alvara_" + System.currentTimeMillis() + ".pdf";
            documentos.getAlvaraSanitario().transferTo(docsDir.resolve(fileName).toFile());
            doc.setAlvaraSanitario("/documentos-produtores/" + salvo.getIdUsuario() + "/" + fileName);
        }

        documentosProdutorRepository.save(doc);
        
        //	Envia o email de boas-vindas ao produtor cadastrado
        emailBoasVindasService.enviarEmailBoasVindas(salvo);

        // Atualiza usuário com imagens e retorna
        return usuarioRepository.save(salvo);
    }
    
    public Usuario cadastrarModerador(CadastroUsuarioDTO dto) throws IOException {
        validarDuplicidade(dto);

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setSobrenome(dto.getSobrenome());
        usuario.setCpf(dto.getCpf());
        usuario.setDataNascimento(dto.getDataNascimento());
        usuario.setSexo(dto.getSexo());
        usuario.setTelefone(dto.getTelefone());
        usuario.setEmail(dto.getEmail());
        usuario.setNomeLogin(dto.getNomeLogin());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));

        // Diferença principal
        usuario.setTipoUsuario(TipoUsuario.MODERADOR);
        usuario.setCidade(dto.getCidade());
        usuario.setEstado(dto.getEstado());
        usuario.setStatusConta(StatusConta.ATIVO);

        // Salvar primeiro para gerar ID
        Usuario salvo = usuarioRepository.save(usuario);

        // Configurar imagens como consumidor
        configurarImagens(salvo, dto);

        return usuarioRepository.save(salvo);
    }

    private void configurarImagens(Usuario salvo, CadastroUsuarioDTO dto) throws IOException {
        Path baseUsuario = BASE_DIR.resolve(String.valueOf(salvo.getIdUsuario()));
        Path perfilDir = baseUsuario.resolve("imagem-perfil");
        Path capaDir = baseUsuario.resolve("imagem-capa");

        Files.createDirectories(perfilDir);
        Files.createDirectories(capaDir);

        if (dto.getImagemPerfil() != null && !dto.getImagemPerfil().isEmpty()) {
            String perfilNome = "perfil_" + System.currentTimeMillis() + ".png";
            Path destino = perfilDir.resolve(perfilNome);
            dto.getImagemPerfil().transferTo(destino.toFile());
            salvo.setImagemPerfil("/imagens-usuarios/" + salvo.getIdUsuario() + "/imagem-perfil/" + perfilNome);
        } else {
            salvo.setImagemPerfil("/imagens-usuarios/defaults/imagem-perfil/perfil.png");
        }

        if (dto.getImagemCapa() != null && !dto.getImagemCapa().isEmpty()) {
            String capaNome = "capa_" + System.currentTimeMillis() + ".png";
            Path destino = capaDir.resolve(capaNome);
            dto.getImagemCapa().transferTo(destino.toFile());
            salvo.setImagemCapa("/imagens-usuarios/" + salvo.getIdUsuario() + "/imagem-capa/" + capaNome);
        } else {
            salvo.setImagemCapa("/imagens-usuarios/defaults/imagem-capa/capa.webp");
        }
    }

    // Login via OAuth2
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Usuario processOAuthPostLogin(String provider, Map<String, Object> attributes) {
        String oauthId = extractProviderId(provider, attributes);
        if (oauthId == null) {
            throw new IllegalArgumentException("Não foi possível identificar o usuário pelo provedor OAuth2.");
        }

        return usuarioRepository.findByOauthProviderAndOauthId(provider, oauthId)
                .orElseGet(() -> {
                    String nomeCompleto = (String) attributes.get("name");
                    String email = (String) attributes.get("email");

                    if (email == null || email.isBlank()) {
                        email = provider + "_" + oauthId + "@noemail.com";
                    }

                    String nome = null;
                    String sobrenome = null;
                    if (nomeCompleto != null) {
                        String[] partes = nomeCompleto.split(" ", 2);
                        nome = partes[0];
                        if (partes.length > 1) sobrenome = partes[1];
                    }

                    Usuario u = new Usuario();
                    u.setNome(nome);
                    u.setSobrenome(sobrenome);
                    u.setEmail(email);
                    u.setOauthProvider(provider);
                    u.setOauthId(oauthId);
                    u.setTipoUsuario(TipoUsuario.CONSUMIDOR);
                    u.setNome(provider + "_" + oauthId);
                    try {
                        u = usuarioRepository.save(u);
                        Path baseUsuario = BASE_DIR.resolve(String.valueOf(u.getIdUsuario()));
                        Path perfilDir = baseUsuario.resolve("imagem-perfil");
                        Path capaDir = baseUsuario.resolve("imagem-capa");
                        Files.createDirectories(perfilDir);
                        Files.createDirectories(capaDir);

                        String pictureUrl = null;
                        if ("google".equals(provider) && attributes.containsKey("picture")) {
                            pictureUrl = (String) attributes.get("picture");
                        } else if ("facebook".equals(provider) && attributes.containsKey("picture")) {
                            Object pictureObj = attributes.get("picture");
                            if (pictureObj instanceof Map<?, ?> pictureMap) {
                                Object dataObj = pictureMap.get("data");
                                if (dataObj instanceof Map<?, ?> dataMap) {
                                    pictureUrl = (String) dataMap.get("url");
                                }
                            }
                        }

                        if (pictureUrl != null) {
                            try (var in = URI.create(pictureUrl).toURL().openStream()) {
                                Files.copy(in, perfilDir.resolve("perfil.png"), StandardCopyOption.REPLACE_EXISTING);
                                u.setImagemPerfil("/imagens-usuarios/" + u.getIdUsuario() + "/imagem-perfil/perfil.png");
                            }
                        }

                        if (u.getImagemPerfil() == null) {
                            u.setImagemPerfil("/imagens-usuarios/defaults/imagem-perfil/perfil.png");
                        }
                        u.setImagemCapa("/imagens-usuarios/defaults/imagem-capa/capa.webp");

                    } catch (Exception e) {
                        u.setImagemPerfil("/imagens-usuarios/defaults/imagem-perfil/perfil.png");
                        u.setImagemCapa("/imagens-usuarios/defaults/imagem-capa/capa.webp");
                    }
                    
                    u = usuarioRepository.saveAndFlush(u);

                 // Envia e-mail de boas-vindas após cadastro via OAuth2
                 emailBoasVindasService.enviarEmailBoasVindas(u);

                 return u;
                });
    }
    
    public Optional<Usuario> buscarUsuarioPorAuthentication(Authentication auth) {
        Object principal = auth.getPrincipal();

        if (principal instanceof org.springframework.security.core.userdetails.User) {
            // Login tradicional
            String nomeLogin = auth.getName();
            return usuarioRepository.findByNomeLogin(nomeLogin);
        } else if (principal instanceof org.springframework.security.oauth2.core.user.DefaultOAuth2User oAuthUser) {
            // OAuth login
            String provider = "google";
            String oauthId = (String) oAuthUser.getAttributes().get("sub"); // para Google
            if (oauthId == null) {
                oauthId = (String) oAuthUser.getAttributes().get("id"); // para Facebook
                provider = "facebook";
            }

            return usuarioRepository.findByOauthProviderAndOauthId(provider, oauthId);
        }

        return Optional.empty();
    }

    private String extractProviderId(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider) && attributes.containsKey("sub")) {
            return (String) attributes.get("sub");
        } else if ("facebook".equals(provider) && attributes.containsKey("id")) {
            return (String) attributes.get("id");
        }
        return null;
    }
    
    public Optional<Usuario> buscarPorOauthProviderAndOauthId(String provider, String oauthId) {
        return usuarioRepository.findByOauthProviderAndOauthId(provider, oauthId);
    }
}
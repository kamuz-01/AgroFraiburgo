package org.main.services;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.main.DTOs.CadastroUsuarioDTO;
import org.main.DTOs.DocumentosProdutorDTO;
import org.main.exceptions.UsuarioNaoEncontradoException;
import org.main.enums.StatusConta;
import org.main.enums.TipoUsuario;
import org.main.models.DocumentosProdutor;
import org.main.models.Usuario;
import org.main.repository.DocumentosProdutorRepository;
import org.main.repository.UsuarioRepository;
import org.main.utils.UploadFileValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

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
    private static final int TELEFONE_MIN_DIGITOS = 10;
    private static final int TELEFONE_MAX_DIGITOS = 11;

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

    @Transactional
    public Usuario atualizarPerfilUsuario(Integer idUsuario,
                                          String nome,
                                          String sobrenome,
                                          LocalDate dataNascimento,
                                          String sexo,
                                          String telefone,
                                          String email,
                                          String cidade,
                                          String estado,
                                          MultipartFile imagemPerfil,
                                          boolean removerImagemPerfil,
                                          MultipartFile imagemCapa,
                                          boolean removerImagemCapa) throws IOException {
        if (idUsuario == null) {
            throw new IllegalArgumentException("Usuário inválido");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(UsuarioNaoEncontradoException::new);

        if (email != null && !email.isBlank()) {
            String emailNormalizado = email.trim();
            boolean emailEmUso = usuarioRepository.existsByEmailAndIdUsuarioNot(emailNormalizado, idUsuario);
            if (emailEmUso) {
                throw new IllegalArgumentException("E-mail já cadastrado por outro usuário.");
            }
            usuario.setEmail(emailNormalizado);
        }

        if (nome != null && !nome.isBlank()) {
            usuario.setNome(nome.trim());
        }
        if (sobrenome != null && !sobrenome.isBlank()) {
            usuario.setSobrenome(sobrenome.trim());
        }
        if (dataNascimento != null) {
            usuario.setDataNascimento(dataNascimento);
        }
        if (sexo != null && !sexo.isBlank()) {
            usuario.setSexo(sexo.trim());
        }
        if (telefone != null && !telefone.isBlank()) {
            usuario.setTelefone(normalizarTelefone(telefone));
        }
        if (cidade != null && !cidade.isBlank()) {
            usuario.setCidade(cidade.trim());
        }
        if (estado != null && !estado.isBlank()) {
            usuario.setEstado(estado.trim());
        }

        Path baseUsuario = BASE_DIR.resolve(String.valueOf(idUsuario));
        Path perfilDir = baseUsuario.resolve("imagem-perfil");
        Path capaDir = baseUsuario.resolve("imagem-capa");
        Files.createDirectories(perfilDir);
        Files.createDirectories(capaDir);

        if (imagemPerfil != null && !imagemPerfil.isEmpty()) {
            UploadFileValidator.FileTypeInfo perfilInfo = UploadFileValidator.validarImagem(imagemPerfil, "A imagem de perfil");
            String perfilNome = gerarNomeArquivoSeguro("perfil", perfilInfo.extension());
            Path destino = perfilDir.resolve(perfilNome);
            Files.write(destino, imagemPerfil.getBytes());
            usuario.setImagemPerfil("/imagens-usuarios/" + idUsuario + "/imagem-perfil/" + perfilNome);
        } else if (removerImagemPerfil) {
            usuario.setImagemPerfil("/imagens-usuarios/defaults/imagem-perfil/perfil.png");
        }

        if (imagemCapa != null && !imagemCapa.isEmpty()) {
            UploadFileValidator.FileTypeInfo capaInfo = UploadFileValidator.validarImagem(imagemCapa, "A imagem de capa");
            String capaNome = gerarNomeArquivoSeguro("capa", capaInfo.extension());
            Path destino = capaDir.resolve(capaNome);
            Files.write(destino, imagemCapa.getBytes());
            usuario.setImagemCapa("/imagens-usuarios/" + idUsuario + "/imagem-capa/" + capaNome);
        } else if (removerImagemCapa) {
            usuario.setImagemCapa("/imagens-usuarios/defaults/imagem-capa/capa.webp");
        }

        return usuarioRepository.save(usuario);
    }

    private String normalizarTelefone(String telefone) {
        String telefoneNormalizado = apenasDigitos(telefone);
        if (telefoneNormalizado.length() < TELEFONE_MIN_DIGITOS || telefoneNormalizado.length() > TELEFONE_MAX_DIGITOS) {
            throw new IllegalArgumentException("Telefone deve conter 10 ou 11 dígitos numéricos.");
        }
        return telefoneNormalizado;
    }

    private String apenasDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
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
        validarDuplicidade(dto);

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

        validarDocumentoObrigatorio(documentos.getDocIdentidade(), "Documento de identidade é obrigatório");
        validarDocumentoObrigatorio(documentos.getComprovanteResidencia(), "Comprovante de residência é obrigatório");
        validarDocumentoObrigatorio(documentos.getCadastroAgriculturaFamiliar(), "CAF/DAP é obrigatório");
        validarDocumentoObrigatorio(documentos.getCertificadoOrganico(), "Certificado de orgânicos é obrigatório");
        validarDocumentoObrigatorio(documentos.getCodigoRastreabilidade(), "Código de rastreabilidade é obrigatório");
        validarDocumentoObrigatorio(documentos.getInscricaoEstadual(), "Inscrição estadual é obrigatório");
        validarDocumentoObrigatorio(documentos.getAlvaraSanitario(), "Alvará sanitário é obrigatório");

        // Upload imagem perfil
        if (dto.getImagemPerfil() != null && !dto.getImagemPerfil().isEmpty()) {
            String perfilNome = gerarNomeArquivoSeguro("perfil", "png");
            Path destino = perfilDir.resolve(perfilNome);
            dto.getImagemPerfil().transferTo(destino.toFile());
            salvo.setImagemPerfil("/imagens-usuarios/" + salvo.getIdUsuario() + "/imagem-perfil/" + perfilNome);
        } else {
            salvo.setImagemPerfil("/imagens-usuarios/defaults/imagem-perfil/perfil.png");
        }

        // Upload imagem capa
        if (dto.getImagemCapa() != null && !dto.getImagemCapa().isEmpty()) {
            String capaNome = gerarNomeArquivoSeguro("capa", "png");
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
            UploadFileValidator.validarPdf(documentos.getDocIdentidade(), "O documento de identidade");
            String fileName = gerarNomeArquivoSeguro("identidade", "pdf");
            Files.write(docsDir.resolve(fileName), documentos.getDocIdentidade().getBytes());
            doc.setDocumentoIdentidade(caminhoDocumentoBanco(salvo.getIdUsuario(), fileName));
        }
        if (documentos.getComprovanteResidencia() != null && !documentos.getComprovanteResidencia().isEmpty()) {
            UploadFileValidator.validarPdf(documentos.getComprovanteResidencia(), "O comprovante de residência");
            String fileName = gerarNomeArquivoSeguro("residencia", "pdf");
            Files.write(docsDir.resolve(fileName), documentos.getComprovanteResidencia().getBytes());
            doc.setComprovanteResidencia(caminhoDocumentoBanco(salvo.getIdUsuario(), fileName));
        }
        if (documentos.getCadastroAgriculturaFamiliar() != null && !documentos.getCadastroAgriculturaFamiliar().isEmpty()) {
            UploadFileValidator.validarPdf(documentos.getCadastroAgriculturaFamiliar(), "O cadastro de agricultura familiar");
            String fileName = gerarNomeArquivoSeguro("pronaf", "pdf");
            Files.write(docsDir.resolve(fileName), documentos.getCadastroAgriculturaFamiliar().getBytes());
            doc.setDeclaracaoPronaf(caminhoDocumentoBanco(salvo.getIdUsuario(), fileName));
        }
        if (documentos.getCertificadoOrganico() != null && !documentos.getCertificadoOrganico().isEmpty()) {
            UploadFileValidator.validarPdf(documentos.getCertificadoOrganico(), "O certificado orgânico");
            String fileName = gerarNomeArquivoSeguro("organico", "pdf");
            Files.write(docsDir.resolve(fileName), documentos.getCertificadoOrganico().getBytes());
            doc.setCertificadoOrganico(caminhoDocumentoBanco(salvo.getIdUsuario(), fileName));
        }
        if (documentos.getCodigoRastreabilidade() != null && !documentos.getCodigoRastreabilidade().isEmpty()) {
            UploadFileValidator.validarPdf(documentos.getCodigoRastreabilidade(), "O código de rastreabilidade");
            String fileName = gerarNomeArquivoSeguro("rastreabilidade", "pdf");
            Files.write(docsDir.resolve(fileName), documentos.getCodigoRastreabilidade().getBytes());
            doc.setCodigoRastreabilidade(caminhoDocumentoBanco(salvo.getIdUsuario(), fileName));
        }
        if (documentos.getInscricaoEstadual() != null && !documentos.getInscricaoEstadual().isEmpty()) {
            UploadFileValidator.validarPdf(documentos.getInscricaoEstadual(), "A inscrição estadual");
            String fileName = gerarNomeArquivoSeguro("inscricao", "pdf");
            Files.write(docsDir.resolve(fileName), documentos.getInscricaoEstadual().getBytes());
            doc.setNumeroInscricaoEstadual(caminhoDocumentoBanco(salvo.getIdUsuario(), fileName));
        }
        if (documentos.getAlvaraSanitario() != null && !documentos.getAlvaraSanitario().isEmpty()) {
            UploadFileValidator.validarPdf(documentos.getAlvaraSanitario(), "O alvará sanitário");
            String fileName = gerarNomeArquivoSeguro("alvara", "pdf");
            Files.write(docsDir.resolve(fileName), documentos.getAlvaraSanitario().getBytes());
            doc.setAlvaraSanitario(caminhoDocumentoBanco(salvo.getIdUsuario(), fileName));
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

        // Envio de email de boas vindas ao moderador
        emailBoasVindasService.enviarEmailBoasVindas(salvo);

        return usuarioRepository.save(salvo);
    }

    private void configurarImagens(Usuario salvo, CadastroUsuarioDTO dto) throws IOException {
        Path baseUsuario = BASE_DIR.resolve(String.valueOf(salvo.getIdUsuario()));
        Path perfilDir = baseUsuario.resolve("imagem-perfil");
        Path capaDir = baseUsuario.resolve("imagem-capa");

        Files.createDirectories(perfilDir);
        Files.createDirectories(capaDir);

        if (dto.getImagemPerfil() != null && !dto.getImagemPerfil().isEmpty()) {
            UploadFileValidator.FileTypeInfo perfilInfo = UploadFileValidator.validarImagem(dto.getImagemPerfil(), "A imagem de perfil");
            String perfilNome = gerarNomeArquivoSeguro("perfil", perfilInfo.extension());
            Path destino = perfilDir.resolve(perfilNome);
            Files.write(destino, dto.getImagemPerfil().getBytes());
            salvo.setImagemPerfil("/imagens-usuarios/" + salvo.getIdUsuario() + "/imagem-perfil/" + perfilNome);
        } else {
            salvo.setImagemPerfil("/imagens-usuarios/defaults/imagem-perfil/perfil.png");
        }

        if (dto.getImagemCapa() != null && !dto.getImagemCapa().isEmpty()) {
            UploadFileValidator.FileTypeInfo capaInfo = UploadFileValidator.validarImagem(dto.getImagemCapa(), "A imagem de capa");
            String capaNome = gerarNomeArquivoSeguro("capa", capaInfo.extension());
            Path destino = capaDir.resolve(capaNome);
            Files.write(destino, dto.getImagemCapa().getBytes());
            salvo.setImagemCapa("/imagens-usuarios/" + salvo.getIdUsuario() + "/imagem-capa/" + capaNome);
        } else {
            salvo.setImagemCapa("/imagens-usuarios/defaults/imagem-capa/capa.webp");
        }
    }

    private String gerarNomeArquivoSeguro(String prefixo, String extensao) {
        String sufixo = extensao.startsWith(".") ? extensao : "." + extensao;
        return prefixo + "_" + UUID.randomUUID().toString().replace("-", "") + sufixo;
    }

    private String caminhoDocumentoBanco(Integer idUsuario, String nomeArquivo) {
        String caminho = "/documentos-produtores/" + idUsuario + "/" + nomeArquivo;
        DocumentosProdutor.validarCaminhoDocumento(caminho);
        return caminho;
    }

    private void validarDocumentoObrigatorio(MultipartFile documento, String mensagem) {
        if (documento == null || documento.isEmpty()) {
            throw new IllegalArgumentException(mensagem);
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

                    if (email != null) {
                        email = email.trim();
                    }

                    String nome = null;
                    String sobrenome = null;
                    if (nomeCompleto != null && !nomeCompleto.isBlank()) {
                        String[] partes = nomeCompleto.trim().split(" ", 2);
                        nome = partes[0];
                        if (partes.length > 1) sobrenome = partes[1];
                    }

                    Usuario usuarioExistente = null;
                    if (email != null && !email.isBlank()) {
                        usuarioExistente = usuarioRepository.findByEmail(email).orElse(null);
                    }

                    Usuario u = usuarioExistente != null ? usuarioExistente : new Usuario();
                    if (nome != null && (u.getNome() == null || u.getNome().isBlank())) {
                        u.setNome(nome);
                    } else if (u.getNome() == null || u.getNome().isBlank()) {
                        u.setNome(provider + "_" + oauthId);
                    }
                    if (sobrenome != null && (u.getSobrenome() == null || u.getSobrenome().isBlank())) {
                        u.setSobrenome(sobrenome);
                    }
                    if (email == null || email.isBlank()) {
                        email = provider + "_" + oauthId + "@noemail.com";
                    }
                    u.setEmail(email);
                    u.setOauthProvider(provider);
                    u.setOauthId(oauthId);
                    if (u.getTipoUsuario() == null) {
                        u.setTipoUsuario(TipoUsuario.CONSUMIDOR);
                    }

                    try {
                        u = usuarioRepository.saveAndFlush(u);
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

                    if (usuarioExistente == null) {
                        // Envia e-mail de boas-vindas apenas no primeiro cadastro via OAuth2
                        emailBoasVindasService.enviarEmailBoasVindas(u);
                    }

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

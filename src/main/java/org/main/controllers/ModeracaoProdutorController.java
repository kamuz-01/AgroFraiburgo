package org.main.controllers;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.main.DTOs.ProdutorPendenteDTO;
import org.main.DTOs.AlterarStatusRequest;
import org.main.DTOs.ProdutorDTO;
import org.main.enums.StatusConta;
import org.main.enums.TipoUsuario;
import org.main.models.DocumentosProdutor;
import org.main.models.Produtor;
import org.main.models.Usuario;
import org.main.repository.DocumentosProdutorRepository;
import org.main.repository.ProdutorRepository;
import org.main.repository.UsuarioRepository;
import org.main.services.EmailService;
import org.main.services.JwtService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping
public class ModeracaoProdutorController {

    private final UsuarioRepository usuarioRepository;
    private final DocumentosProdutorRepository documentosProdutorRepository;
	private final ProdutorRepository produtorRepository;
	private final EmailService emailService;
    private final JwtService jwtService;

    public ModeracaoProdutorController(UsuarioRepository usuarioRepository,
                                       DocumentosProdutorRepository documentosProdutorRepository,
                                       ProdutorRepository produtorRepository,
                                       EmailService emailService,
                                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
		this.documentosProdutorRepository = documentosProdutorRepository;
		this.produtorRepository = produtorRepository;
		this.emailService = emailService;
        this.jwtService = jwtService;
    }

    // Endpoint usado pelo DataTables para listar produtores pendentes de aprovação
    @PostMapping("/moderador/produtores/listar")
    @ResponseBody
    public Map<String, Object> listarPendentes(@RequestBody Map<String, Object> params) {
        int draw = (int) params.get("draw");
        int start = (int) params.get("start");
        int length = (int) params.get("length");

        // O DataTables usa start/length para paginação
        int page = start / length;

        Pageable pageable = PageRequest.of(page, length);

        // Buscar apenas PRODUTORES com paginação
        Page<Usuario> produtoresPage = usuarioRepository.findByTipoUsuario(TipoUsuario.PRODUTOR, pageable);

        List<ProdutorDTO> dtos = produtoresPage.getContent().stream().map(u -> {
            DocumentosProdutor doc = documentosProdutorRepository.findByIdProdutor(u.getIdUsuario())
                    .stream()
                    .findFirst()
                    .orElse(null);
            Integer avaliacoesRecebidas = produtorRepository.findById(u.getIdUsuario())
                .map(Produtor::getAvaliacoesRecebidas)
                .orElse(0);

            return new ProdutorDTO(
                    u.getIdUsuario(),
                    u.getNome() + " " + u.getSobrenome(),
                    u.getCpf(),
                avaliacoesRecebidas,
                    doc != null ? doc.getIdDocumento() : null,
                    doc != null ? doc.getDocumentoIdentidade() : null,
                    doc != null ? doc.getComprovanteResidencia() : null,
                    doc != null ? doc.getDeclaracaoPronaf() : null,
                    doc != null ? doc.getCertificadoOrganico() : null,
                    doc != null ? doc.getCodigoRastreabilidade() : null,
                    doc != null ? doc.getNumeroInscricaoEstadual() : null,
                    doc != null ? doc.getAlvaraSanitario() : null,
                    u.getStatusConta()
            );
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("draw", draw);
        response.put("recordsTotal", produtoresPage.getTotalElements()); // total de produtores
        response.put("recordsFiltered", produtoresPage.getTotalElements()); // total filtrado
        response.put("data", dtos);

        return response;
    }


    @PostMapping("/api/moderador/produtores/alterar-status")
    @ResponseBody
    public ResponseEntity<?> alterarStatus(@RequestBody AlterarStatusRequest request) {

        Usuario produtor = usuarioRepository.findById(request.getIdProdutor())
                .orElseThrow(() -> new RuntimeException("Produtor não encontrado"));

        if ((request.getNovoStatus() == StatusConta.REJEITADO || request.getNovoStatus() == StatusConta.BLOQUEADO)
                && (request.getObservacao() == null || request.getObservacao().isBlank())) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Observação obrigatória"));
        }

        produtor.setStatusConta(request.getNovoStatus());
        usuarioRepository.save(produtor);
        
        String estiloMensagem;
        String textoMensagem;

        switch (request.getNovoStatus()) {
            case BLOQUEADO -> {
                estiloMensagem = "background-color:#ffe6e6; color:#a94442; border:1px solid #ebccd1; padding:15px; border-radius:5px;";
                textoMensagem = "Sua conta foi <strong>bloqueada</strong>. Verifique o motivo abaixo e entre em contato com o suporte, se necessário.";
            }
            case REJEITADO -> {
                estiloMensagem = "background-color:#ffe6e6; color:#a94442; border:1px solid #ebccd1; padding:15px; border-radius:5px;";
                textoMensagem = "Sua conta foi <strong>rejeitado</strong>. Verifique o motivo abaixo e entre em contato com o suporte, se necessário.";
            }
            case PENDENTE -> {
                estiloMensagem = "background-color:#fff3cd; color:#856404; border:1px solid #ffeeba; padding:15px; border-radius:5px;";
                textoMensagem = "Seu cadastro está <strong>pendente</strong>. Veja os detalhes abaixo.";
            }
            case ATIVO -> {
                estiloMensagem = "background-color:#e6ffed; color:#155724; border:1px solid #c3e6cb; padding:15px; border-radius:5px;";
                textoMensagem = "Parabéns! Sua conta foi <strong>ativada</strong> com sucesso.";
            }
            default -> {
                estiloMensagem = "padding:15px;";
                textoMensagem = "O status da sua conta foi alterado.";
            }
        }


        // Notificação por email
        String subject = "Atualização do status da sua conta";
        String body = String.format("""
        	    <div style="font-family: Arial, sans-serif; font-size: 14px; color: #333;">
        	        <p>Olá <strong>%s</strong>,</p>

        	        <div style="%s">%s</div>

        	        %s

        	        <p style="margin-top: 20px;">
        	            Caso tenha dúvidas, entre em contato com nossa equipe de suporte suporte@agrofraiburgo.com.br.
        	        </p>

        	        <p style="margin-top: 30px;">
        	            Atenciosamente,<br>
        	            <span style="font-weight: bold;">Equipe de Moderação AgroFraiburgo</span>
        	        </p>
        	    </div>
        	    """,
        	    produtor.getNome(),
        	    estiloMensagem,
        	    textoMensagem,
        	    request.getObservacao() != null && !request.getObservacao().isBlank()
        	        ? String.format("<p><strong>Motivo:</strong> %s</p>", request.getObservacao())
        	        : "<p>Nenhuma observação adicional foi fornecida.</p>"
        	);
        emailService.enqueueEmail(produtor.getEmail(), subject, body);

        return ResponseEntity.ok(Map.of("mensagem", "Status alterado com sucesso"));
    }

    // API usada pela campainha de notificações
    @GetMapping("/api/moderador/novos-produtores")
    @ResponseBody
    public Map<String, Object> novosProdutores() {
        var pendentes = usuarioRepository.findByTipoUsuarioAndStatusConta(
                TipoUsuario.PRODUTOR, StatusConta.PENDENTE);

        return Map.of(
            "total", pendentes.size(),
            "produtores", pendentes.stream().map(u -> Map.of(
                "id", u.getIdUsuario(),
                "nome", u.getNome() + " " + u.getSobrenome()
            )).toList()
        );
    }

    // API alternativa (lista completa em JSON)
    @GetMapping("/api/moderador/produtores/pendentes")
    @ResponseBody
    public List<ProdutorPendenteDTO> listarPendentesGet() {
        return usuarioRepository.findByTipoUsuarioAndStatusConta(
                TipoUsuario.PRODUTOR, StatusConta.PENDENTE
        ).stream().map(ProdutorPendenteDTO::fromEntity).toList();
    }

        @GetMapping("/produtores_pendentes/{id}")
        @PreAuthorize("hasRole('MODERADOR')")
        public String detalheProdutorPendente(@PathVariable Integer id,
                          Authentication auth,
                          Model model) {
        Integer idUsuario = currentUserId(auth);
        if (idUsuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
        }

        Usuario moderador = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login"));

        Usuario produtor = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produtor não encontrado"));

        if (produtor.getTipoUsuario() != TipoUsuario.PRODUTOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não é produtor");
        }

        DocumentosProdutor documentos = documentosProdutorRepository.findByIdProdutor(id)
            .orElse(null);

        List<DocumentoAnaliseView> documentosAnalise = new ArrayList<>();
        documentosAnalise.add(new DocumentoAnaliseView(
            "Documento de identidade",
            documentos != null ? documentos.getDocumentoIdentidade() : null,
            "/api/moderador/produtores/" + id + "/documento/documento_identidade/download"
        ));
        documentosAnalise.add(new DocumentoAnaliseView(
            "Comprovante de residência",
            documentos != null ? documentos.getComprovanteResidencia() : null,
            "/api/moderador/produtores/" + id + "/documento/comprovante_residencia/download"
        ));
        documentosAnalise.add(new DocumentoAnaliseView(
            "Declaração PRONAF",
            documentos != null ? documentos.getDeclaracaoPronaf() : null,
            "/api/moderador/produtores/" + id + "/documento/declaracao_pronaf/download"
        ));
        documentosAnalise.add(new DocumentoAnaliseView(
            "Certificado de produção orgânica",
            documentos != null ? documentos.getCertificadoOrganico() : null,
            "/api/moderador/produtores/" + id + "/documento/certificado_producao_organica/download"
        ));
        documentosAnalise.add(new DocumentoAnaliseView(
            "Código de rastreabilidade",
            documentos != null ? documentos.getCodigoRastreabilidade() : null,
            "/api/moderador/produtores/" + id + "/documento/codigo_rastreabilidade/download"
        ));
        documentosAnalise.add(new DocumentoAnaliseView(
            "Inscrição estadual",
            documentos != null ? documentos.getNumeroInscricaoEstadual() : null,
            "/api/moderador/produtores/" + id + "/documento/numero_inscricao_estadual/download"
        ));
        documentosAnalise.add(new DocumentoAnaliseView(
            "Alvará sanitário",
            documentos != null ? documentos.getAlvaraSanitario() : null,
            "/api/moderador/produtores/" + id + "/documento/alvara_sanitario/download"
        ));

        Integer avaliacoesRecebidas = produtorRepository.findById(id)
            .map(Produtor::getAvaliacoesRecebidas)
            .orElse(0);

        model.addAttribute("nome", moderador.getNome());
        model.addAttribute("tipoUsuario", moderador.getTipoUsuario());
        model.addAttribute("imagemPerfil", moderador.getImagemPerfil());
        model.addAttribute("imagemCapa", moderador.getImagemCapa());
        model.addAttribute("homeUrl", "/home_moderador");
        model.addAttribute("produtor", produtor);
        model.addAttribute("documentos", documentos);
        model.addAttribute("documentosAnalise", documentosAnalise);
        model.addAttribute("avaliacoesRecebidas", avaliacoesRecebidas);
        model.addAttribute("statusOpcoes", StatusConta.values());

        return "produtor_pendente_detalhe";
        }
    
    @GetMapping("/api/moderador/produtores/{produtorId}/documento/{tipo}/download")
    @PreAuthorize("hasRole('MODERADOR')")
    public ResponseEntity<Resource> downloadDocumento(
            @PathVariable Integer produtorId,
            @PathVariable String tipo) {

        DocumentosProdutor doc = documentosProdutorRepository.findByIdProdutor(produtorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Documentos não encontrados"));

        String caminhoBanco = switch (tipo.toLowerCase()) {
            case "documento_identidade" -> doc.getDocumentoIdentidade();
            case "comprovante_residencia" -> doc.getComprovanteResidencia();
            case "declaracao_pronaf" -> doc.getDeclaracaoPronaf();
            case "certificado_producao_organica" -> doc.getCertificadoOrganico();
            case "codigo_rastreabilidade" -> doc.getCodigoRastreabilidade();
            case "numero_inscricao_estadual" -> doc.getNumeroInscricaoEstadual();
            case "alvara_sanitario" -> doc.getAlvaraSanitario();
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de documento inválido.");
        };

        if (caminhoBanco == null || caminhoBanco.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento não enviado.");
        }

        // 🔹 Remove a "/" inicial para evitar duplicação do diretório
        String relativo = caminhoBanco.startsWith("/") ? caminhoBanco.substring(1) : caminhoBanco;

        // 🔹 Monta o caminho absoluto real
        Path caminhoArquivo = Path.of(System.getProperty("user.dir")).resolve(relativo).normalize();

        if (!Files.exists(caminhoArquivo)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo não encontrado.");
        }

        try {
            Resource resource = new UrlResource(caminhoArquivo.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + caminhoArquivo.getFileName() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao carregar o arquivo.");
        }
    }

    private Integer currentUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        if (auth instanceof UsernamePasswordAuthenticationToken) {
            String principal = auth.getName();
            if (principal != null && principal.matches("\\d+")) {
                return Integer.valueOf(principal);
            }

            return usuarioRepository.findByNomeLogin(principal)
                    .map(Usuario::getIdUsuario)
                    .orElse(null);
        }

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            String token = jwtAuth.getToken().getTokenValue();
            Long uidLong = jwtService.extractUserId(token);
            return uidLong != null ? uidLong.intValue() : null;
        }

        return null;
    }

    public record DocumentoAnaliseView(String titulo, String caminhoArquivo, String downloadUrl) {
        public boolean disponivel() {
            return caminhoArquivo != null && !caminhoArquivo.isBlank();
        }

        public String nomeArquivo() {
            if (!disponivel()) {
                return null;
            }

            Path arquivo = Path.of(caminhoArquivo);
            Path nome = arquivo.getFileName();
            return nome != null ? nome.toString() : caminhoArquivo;
        }
    }
}
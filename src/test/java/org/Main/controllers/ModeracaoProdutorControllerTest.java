package org.Main.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.main.controllers.ModeracaoProdutorController;
import org.main.models.DocumentosProdutor;
import org.main.repository.DocumentosProdutorRepository;
import org.main.repository.ProdutorRepository;
import org.main.repository.UsuarioRepository;
import org.main.services.EmailService;
import org.main.services.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

class ModeracaoProdutorControllerTest {

    private UsuarioRepository usuarioRepository;
    private DocumentosProdutorRepository documentosProdutorRepository;
    private ProdutorRepository produtorRepository;
    private EmailService emailService;
    private JwtService jwtService;
    private ModeracaoProdutorController controller;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        documentosProdutorRepository = mock(DocumentosProdutorRepository.class);
        produtorRepository = mock(ProdutorRepository.class);
        emailService = mock(EmailService.class);
        jwtService = mock(JwtService.class);
        controller = new ModeracaoProdutorController(usuarioRepository, documentosProdutorRepository, produtorRepository, emailService, jwtService);
    }

    @Test
    void downloadDocumentoDeveBloquearCaminhoForaDaPastaPrivada() {
        DocumentosProdutor documentos = new DocumentosProdutor();
        documentos.setDocumentoIdentidade("/../segredo.pdf");

        when(documentosProdutorRepository.findByIdProdutor(77)).thenReturn(Optional.of(documentos));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> controller.downloadDocumento(77, "documento_identidade"));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void downloadDocumentoDeveRejeitarDocumentoAusente() {
        when(documentosProdutorRepository.findByIdProdutor(77)).thenReturn(Optional.of(new DocumentosProdutor()));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> controller.downloadDocumento(77, "documento_identidade"));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
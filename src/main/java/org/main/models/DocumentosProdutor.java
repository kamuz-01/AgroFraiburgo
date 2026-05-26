package org.main.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.nio.file.Path;
import java.time.LocalDateTime;

@Entity
@Table(name = "documentos_produtor")
@Getter @Setter
public class DocumentosProdutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento")
    private Integer idDocumento;

    @Column(name = "id_produtor", nullable = false)
    private Integer idProdutor; // chave estrangeira para Usuario (Produtor)

    @Column(name = "documento_identidade", nullable = false)
    private String documentoIdentidade;

    @Column(name = "comprovante_residencia", nullable = false)
    private String comprovanteResidencia;

    @Column(name = "declaracao_pronaf", nullable = false)
    private String declaracaoPronaf;

    @Column(name = "certificado_producao_organica", nullable = false)
    private String certificadoOrganico;

    @Column(name = "codigo_rastreabilidade", nullable = false)
    private String codigoRastreabilidade;

    @Column(name = "numero_inscricao_estadual", nullable = false)
    private String numeroInscricaoEstadual;

    @Column(name = "alvara_sanitario", nullable = false)
    private String alvaraSanitario;

    @Column(name = "data_envio", nullable = false, updatable = false)
    private LocalDateTime dataEnvio = LocalDateTime.now();

    @PrePersist
    @PreUpdate
    private void validarCaminhosDocumentos() {
        validarCaminhoDocumentoObrigatorio(documentoIdentidade);
        validarCaminhoDocumentoObrigatorio(comprovanteResidencia);
        validarCaminhoDocumentoObrigatorio(declaracaoPronaf);
        validarCaminhoDocumentoObrigatorio(certificadoOrganico);
        validarCaminhoDocumentoObrigatorio(codigoRastreabilidade);
        validarCaminhoDocumentoObrigatorio(numeroInscricaoEstadual);
        validarCaminhoDocumentoObrigatorio(alvaraSanitario);
    }

    public static void validarCaminhoDocumentoObrigatorio(String caminhoBanco) {
        if (caminhoBanco == null || caminhoBanco.isBlank()) {
            throw new IllegalArgumentException("Documento obrigatório não informado.");
        }
        validarCaminhoDocumento(caminhoBanco);
    }

    public static void validarCaminhoDocumento(String caminhoBanco) {
        if (caminhoBanco == null || caminhoBanco.isBlank()) {
            return;
        }

        String relativo = caminhoBanco.startsWith("/") ? caminhoBanco.substring(1) : caminhoBanco;
        if (Path.of(relativo).isAbsolute()) {
            throw new IllegalArgumentException("Caminho de documento inválido.");
        }

        Path raizProjeto = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path docsDir = raizProjeto.resolve("documentos-produtores").normalize();
        Path caminhoArquivo = raizProjeto.resolve(relativo).normalize();

        if (!caminhoArquivo.startsWith(docsDir)) {
            throw new IllegalArgumentException("Caminho de documento inválido.");
        }
    }
}

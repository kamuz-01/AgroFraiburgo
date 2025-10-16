package org.main.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
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

    @Column(name = "codigo_rastreabilidade")
    private String codigoRastreabilidade;

    @Column(name = "numero_inscricao_estadual", nullable = false)
    private String numeroInscricaoEstadual;

    @Column(name = "alvara_sanitario", nullable = false)
    private String alvaraSanitario;

    @Column(name = "data_envio", nullable = false, updatable = false)
    private LocalDateTime dataEnvio = LocalDateTime.now();
}
package org.main.DTOs;

import java.time.LocalDateTime;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentosProdutorDTO {

    @NotNull(message = "Documento de identidade é obrigatório")
    private MultipartFile docIdentidade;

    @NotNull(message = "Comprovante de residência é obrigatório")
    private MultipartFile comprovanteResidencia;

    @NotNull(message = "CAF/DAP é obrigatório")
    private MultipartFile cadastroAgriculturaFamiliar;

    @NotNull(message = "Certificado de orgânicos é obrigatório")   
    private MultipartFile certificadoOrganico;
    
    private MultipartFile codigoRastreabilidade;
    
    @NotNull(message = "Inscrição estadual é obrigatório")
    private MultipartFile inscricaoEstadual;
    
    @NotNull(message = "Alvará sanitário é obrigatório")
    private MultipartFile alvaraSanitario;

    private LocalDateTime dataEnvio = LocalDateTime.now();
}
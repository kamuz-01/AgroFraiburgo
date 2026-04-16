package org.main.DTOs;

import org.main.enums.StatusFeira;
import org.main.models.Feira;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeiraDTO {
    private Integer idFeira;

    @NotBlank(message = "O nome do local é obrigatório")
    private String nomeLocal;

    @NotBlank(message = "O logradouro é obrigatório")
    private String logradouro;

    @NotNull(message = "O número é obrigatório")
    private Integer numero;

    @NotBlank(message = "O bairro é obrigatório")
    private String bairro;

    private String complemento;

    @NotNull(message = "O status da feira é obrigatório")
    private StatusFeira statusFeira;

    public static FeiraDTO fromEntity(Feira feira) {
        if (feira == null) {
            return null;
        }

        return new FeiraDTO(
                feira.getIdFeira(),
                feira.getNomeLocal(),
                feira.getLogradouro(),
                feira.getNumero(),
                feira.getBairro(),
                feira.getComplemento(),
                feira.getStatusFeira()
        );
    }
}
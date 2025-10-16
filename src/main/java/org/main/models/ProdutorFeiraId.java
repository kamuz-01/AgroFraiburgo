package org.main.models;

import java.io.Serializable;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class ProdutorFeiraId implements Serializable {

	private static final long serialVersionUID = 1L;
	private Integer idProdutor;
    private Integer idFeira;
}
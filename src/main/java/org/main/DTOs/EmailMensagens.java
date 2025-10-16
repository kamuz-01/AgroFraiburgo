package org.main.DTOs;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailMensagens implements Serializable {
	private static final long serialVersionUID = 1L;
	private String to;
    private String subject;
    private String body; 
}
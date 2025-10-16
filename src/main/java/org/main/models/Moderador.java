package org.main.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "moderadores")
public class Moderador {
	
	@Id
	@Column(name = "id_moderador")
	private Integer idModerador;
}
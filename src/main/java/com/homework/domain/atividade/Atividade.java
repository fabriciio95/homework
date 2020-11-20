package com.homework.domain.atividade;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.homework.domain.curso.Curso;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Atividade implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public enum StatusAtividade {
		EM_ABERTO, FINALIZADA;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;
	
	@NotNull(message = "Data final não pode ser vazia")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dataFinal;
	
	@NotBlank(message = "Descrição não pode estar em branco")
	private String descricao;
	
	@NotBlank(message = "Título não pode estar em branco")
	private String titulo;
	
	@NotNull
	private boolean permiteEntregaAtrasada;
	
	@Size(max = 60)
	private String nomeArquivo;
	
	@Enumerated(EnumType.STRING)
	private StatusAtividade status;
	
	@ManyToOne
	@JoinColumn(name = "curso_id")
	private Curso curso;
	
	private transient MultipartFile atividadeFile;
	
	public void definirNomeArquivo() {
		if(atividadeFile != null) {	
			String type = atividadeFile.getContentType().split("\\/")[1];
			
			if(type == null) {
				this.nomeArquivo = String.format("%04d", id);
			} else {
				this.nomeArquivo = String.format("%04d.%s", id, type);
			}
		}
	}
}

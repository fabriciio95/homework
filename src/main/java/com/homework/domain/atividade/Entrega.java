package com.homework.domain.atividade;

import java.time.LocalDate;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.web.multipart.MultipartFile;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "entrega_atividade")
public class Entrega {

	@EqualsAndHashCode.Include
	@EmbeddedId
	private EntregaPK id;
	
	private LocalDate dataEntrega;
	
	private String nomeArquivoEntrega;
	
	private String comentario;
	
	private Double nota;
	
	private boolean corrigido = false;
	
	private String nomeArquivoCorrecao;
	
	private transient MultipartFile arquivoEntrega;
	
	private transient MultipartFile arquivoCorrecao;
	
	public void definirNomeArquivoEntrega() {
		if(arquivoEntrega != null) {
			String type = this.arquivoEntrega.getContentType().split("\\/")[1];
			if(type == null) {
				this.nomeArquivoEntrega = String.format("%d-%d", id.getAluno().getId(), id.getAtividade().getId());
			} else {
				this.nomeArquivoEntrega = String.format("%d-%d.%s", id.getAluno().getId(), id.getAtividade().getId(), type);
			}
		}
	}
	
	public void definirNomeArquivoCorrecao() {
		if(arquivoCorrecao != null) {
			String type = this.arquivoCorrecao.getContentType().split("\\/")[1];
			if(type == null) {
				this.nomeArquivoCorrecao = String.format("%d-%d", id.getAluno().getId(), id.getAtividade().getId());
			} else {
				this.nomeArquivoCorrecao = String.format("%d-%d.%s", id.getAluno().getId(), id.getAtividade().getId(), type);
			}
		}
	}
}

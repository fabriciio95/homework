package com.homework.domain.atividade;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtividadeFilter {
	
	public enum StatusAtividadeFilter {
		EM_ABERTO, ENTREGUE, FINALIZADA;
	}
	
	private Long idCurso;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dataInicio;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dataFinal;
	
	private StatusAtividadeFilter statusAtividade;
	
}

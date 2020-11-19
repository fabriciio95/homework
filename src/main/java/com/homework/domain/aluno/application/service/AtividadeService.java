package com.homework.domain.aluno.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.atividade.Atividade;
import com.homework.domain.atividade.Atividade.StatusAtividade;
import com.homework.domain.atividade.AtividadeFilter;
import com.homework.domain.atividade.AtividadeFilter.StatusAtividadeFilter;
import com.homework.domain.atividade.AtividadeRepository;
import com.homework.domain.atividade.Entrega;
import com.homework.domain.atividade.EntregaPK;
import com.homework.domain.atividade.EntregaRepository;
import com.homework.domain.curso.Curso;
import com.homework.utils.SecurityUtils;

@Service
public class AtividadeService {
	
	@Autowired
	private AtividadeRepository atividadeRepository;
	
	@Autowired
	private ArquivoService arquivoService;
	
	@Autowired
	private EntregaRepository entregaRepository;

	public List<Atividade> getAtividadesFiltradas(AtividadeFilter filter){
		StatusAtividade status;
		if(filter.getStatusAtividade().equals(StatusAtividadeFilter.FINALIZADA)) {
			status = StatusAtividade.FINALIZADA;
			List<Atividade> atividades = atividadeRepository.findByCursoDatasEStatus(filter.getIdCurso(), status, filter.getDataInicio(), filter.getDataFinal());
			return atividades.stream().sorted((a1, a2) -> a1.getDataFinal().compareTo(a2.getDataFinal()))
					.collect(Collectors.toList());
		} else {
			List<Atividade> atividadesPorData = atividadeRepository.findByDatas(filter.getIdCurso(), filter.getDataInicio(), filter.getDataFinal());
			return filtrarAtividadesPendentes(atividadesPorData);
		}
		
	}
	
	public List<Atividade> filtrarAtividadesPendentes(List<Atividade> atividades){
		List<Atividade> atividadesPendentes = new ArrayList<>();
		Aluno aluno = SecurityUtils.getAlunoLogado();
		for (Atividade atividade : atividades) {
			EntregaPK entregaPK = new EntregaPK(atividade, aluno);
			Entrega entrega = entregaRepository.findById(entregaPK).orElse(null);
			if(entrega == null) {
				atividadesPendentes.add(atividade);
			}
		}
		
		return atividadesPendentes.stream().sorted((a1, a2) -> a1.getDataFinal().compareTo(a2.getDataFinal()))
				.collect(Collectors.toList());
	}
	
	public void novaAtividade(Atividade atividade, Curso curso) throws ArquivoException {
		atividade.setStatus(StatusAtividade.EM_ABERTO);
		atividade.setCurso(curso);
		atividade = atividadeRepository.save(atividade);
		atividade.definirNomeArquivo();
		try {
			arquivoService.uploadArquivoAtividade(atividade.getAtividadeFile(), atividade.getNomeArquivo());
		} catch(ApplicationException e) {
			throw new ArquivoException(e.getMessage());
		}
		
	}
	
}

package com.homework.domain.aluno.application.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.atividade.Atividade;
import com.homework.domain.atividade.AtividadeFilter;
import com.homework.domain.atividade.AtividadeFilter.StatusAtividadeFilter;
import com.homework.domain.atividade.AtividadeRepository;
import com.homework.domain.atividade.Entrega;
import com.homework.domain.atividade.EntregaPK;
import com.homework.domain.atividade.EntregaRepository;
import com.homework.domain.curso.Curso;
import com.homework.utils.SecurityUtils;

@Service
public class EntregaService {

	@Autowired
	private EntregaRepository entregaRepository;
	
	@Autowired
	private ArquivoService arquivoService;
	
	@Autowired
	private AtividadeRepository atividadeRepository;
	
	
	public List<Entrega> getAtividadesEntreguesFiltradas(AtividadeFilter filter, Curso curso){
		List<Entrega> atividadesEntregues = new ArrayList<Entrega>();
		Aluno aluno = SecurityUtils.getAlunoLogado(); 
		if(filter.getStatusAtividade().equals(StatusAtividadeFilter.ENTREGUE)) {
			atividadesEntregues = entregaRepository.findByDatas(curso.getId(), aluno.getId(), filter.getDataInicio(), filter.getDataFinal());
		}
		return atividadesEntregues;
	}
	
	public boolean isAtividadeEntregue(Atividade atividade, Aluno aluno) {
		Entrega atividadeEntregue = entregaRepository.findById(new EntregaPK(atividade, aluno)).orElse(null);
		if(atividadeEntregue == null) {
			return false;
		}
		return true;
	}
	
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
	public void entregarAtividade(Long idAtividade, Entrega entrega) throws ArquivoException {
		Atividade atividade = atividadeRepository.findById(idAtividade).orElseThrow(NoSuchElementException::new);
		Aluno aluno = SecurityUtils.getAlunoLogado();
		entrega.setId(new EntregaPK(atividade, aluno));
		entrega.setDataEntrega(LocalDate.now());
		entregaRepository.save(entrega);
		entrega.definirNomeArquivoEntrega();
		entregaRepository.save(entrega);
		try {	
			arquivoService.uploadEntrega(entrega.getArquivoEntrega(), entrega.getNomeArquivo());
		} catch(Exception e) {
			throw new ArquivoException(e.getMessage());
		}
	}
}

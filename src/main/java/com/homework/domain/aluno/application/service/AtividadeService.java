package com.homework.domain.aluno.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.aluno.application.service.ArquivoService.Dir;
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

	public List<Atividade> getAtividadesFiltradasAluno(AtividadeFilter filter){
		if(filter.getDataInicio() == null && filter.getDataFinal() == null && filter.getStatusAtividade().equals(StatusAtividadeFilter.FINALIZADA)) {
			StatusAtividade status = StatusAtividade.FINALIZADA;
			List<Atividade> atividades = atividadeRepository.findByCursoEStatus(filter.getIdCurso(), status);
			return atividades.stream().sorted((a1, a2) -> a1.getDataFinal().compareTo(a2.getDataFinal()))
					.collect(Collectors.toList());
		} else if(filter.getDataInicio() == null && filter.getDataFinal() == null && filter.getStatusAtividade().equals(StatusAtividadeFilter.EM_ABERTO)) {
			List<Atividade> atividades = atividadeRepository.findByCurso_Id(filter.getIdCurso());
			return filtrarAtividadesPendentes(atividades);
		}
		
		if(filter.getStatusAtividade().equals(StatusAtividadeFilter.FINALIZADA)) {
			StatusAtividade status = StatusAtividade.FINALIZADA;
			List<Atividade> atividades = atividadeRepository.findByCursoDatasEStatus(filter.getIdCurso(), status, filter.getDataInicio(), filter.getDataFinal());
			return atividades.stream().sorted((a1, a2) -> a1.getDataFinal().compareTo(a2.getDataFinal()))
					.collect(Collectors.toList());
		} else {
			List<Atividade> atividadesPorData = atividadeRepository.findByDatas(filter.getIdCurso(), filter.getDataInicio(), filter.getDataFinal());
			return filtrarAtividadesPendentes(atividadesPorData);
		}
		
	}
	
	public List<Atividade> getAtividadesFiltradasProfessor(AtividadeFilter filter){
		if(filter.getTituloAtividade() != null && !filter.getTituloAtividade().isBlank()) {
			return atividadeRepository.getAtividadesPorTitulo(filter.getTituloAtividade(),filter.getIdCurso())
					.stream().sorted((n1, n2) -> n1.getTitulo().compareTo(n2.getTitulo()))
					.collect(Collectors.toList());
		}
		
		if(filter.getDataInicio() == null && filter.getDataFinal() == null && filter.getStatusAtividade().equals(StatusAtividadeFilter.CORRECOES_PENDENTES)) {
			return atividadeRepository.getAtividadesComCorrecoesPendentes(filter.getIdCurso())
					.stream().sorted((a1, a2) -> a1.getDataFinal().compareTo(a2.getDataFinal()))
					.collect(Collectors.toList());
		} else if(filter.getStatusAtividade().equals(StatusAtividadeFilter.CORRECOES_PENDENTES)) {
			return atividadeRepository.getAtividadesComCorrecoesPendentes(filter.getIdCurso(), filter.getDataInicio(), filter.getDataFinal())
					.stream().sorted((n1, n2) -> n1.getDataFinal().compareTo(n2.getDataFinal())).collect(Collectors.toList());
		 }
		
		if(filter.getDataInicio() == null && filter.getDataFinal() == null && filter.getStatusAtividade().equals(StatusAtividadeFilter.TODAS)) {
			return atividadeRepository.findByCurso_Id(filter.getIdCurso())
					.stream().sorted((n1, n2) -> n1.getDataFinal().compareTo(n2.getDataFinal())).collect(Collectors.toList());
		} else if(filter.getStatusAtividade().equals(StatusAtividadeFilter.TODAS)) {
			return atividadeRepository.findByDatas(filter.getIdCurso(), filter.getDataInicio(), filter.getDataFinal())
				.stream().sorted((a1, a2) -> a1.getDataFinal().compareTo(a2.getDataFinal()))
				.collect(Collectors.toList());
		 }
		
		StatusAtividade status;
		if(filter.getStatusAtividade().equals(StatusAtividadeFilter.FINALIZADA)) {
			status = StatusAtividade.FINALIZADA;
		} else {
			status = StatusAtividade.EM_ABERTO;
		}
		
		if(filter.getDataInicio() == null && filter.getDataFinal() == null) {
			return atividadeRepository.findByCursoEStatus(filter.getIdCurso(), status)
					.stream().sorted((a1, a2) -> -a1.getDataFinal().compareTo(a2.getDataFinal()))
							.collect(Collectors.toList());
		} else {
			List<Atividade> atividades = atividadeRepository.findByCursoDatasEStatus(filter.getIdCurso(), status, filter.getDataInicio(), filter.getDataFinal());
			return atividades.stream().sorted((a1, a2) -> a1.getDataFinal().compareTo(a2.getDataFinal()))
					.collect(Collectors.toList());
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
		if(!atividade.getAtividadeFile().isEmpty() && atividade.getAtividadeFile().getSize() > 0) {
			atividade.definirNomeArquivo();
			try {
				arquivoService.uploadArquivo(atividade.getAtividadeFile(), atividade.getNomeArquivo(), Dir.ATIVIDADE);
			} catch(ApplicationException e) {
				throw new ArquivoException(e.getMessage());
			}
		}
	}
	
	public void atualizarAtividade(Atividade atividade) throws ApplicationException{
		if(!atividade.getAtividadeFile().isEmpty()) {
			if(atividade.getNomeArquivo().isBlank()) {
				atividade.definirNomeArquivo();
			}
			arquivoService.uploadArquivo(atividade.getAtividadeFile(), atividade.getNomeArquivo(), Dir.ATIVIDADE);
		}
		
		atividadeRepository.save(atividade);
	}
	
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
	public void excluirAtividade(Atividade atividade) throws Exception {
		excluirArquivosAtividade(atividade);
		entregaRepository.excluirEntregasDaAtividade(atividade.getId());
		atividadeRepository.delete(atividade);
	}
	
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
	public void excluirArquivosAtividade(Atividade atividade) {
		if(atividade.getNomeArquivo() != null && !atividade.getNomeArquivo().isBlank()) {
			arquivoService.excluirArquivo(atividade.getNomeArquivo(), Dir.ATIVIDADE);
		}
		List<Entrega> entregas = entregaRepository.findById_Atividade(atividade);
		for(Entrega entrega : entregas) {
			if(entrega.getNomeArquivoEntrega() != null && !entrega.getNomeArquivoEntrega().isBlank()) {
				arquivoService.excluirArquivo(entrega.getNomeArquivoEntrega(), Dir.ENTREGA);
			}
			
			if(entrega.getNomeArquivoCorrecao() != null && !entrega.getNomeArquivoCorrecao().isBlank()) {
				arquivoService.excluirArquivo(entrega.getNomeArquivoCorrecao(), Dir.CORRECAO);
			}
		}
	}
	
}

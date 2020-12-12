package com.homework.domain.aluno.application.service;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.aluno.application.service.ArquivoService.Dir;
import com.homework.domain.atividade.Atividade;
import com.homework.domain.atividade.AtividadeEntregaDTO;
import com.homework.domain.atividade.AtividadeFilter;
import com.homework.domain.atividade.AtividadeFilter.StatusAtividadeFilter;
import com.homework.domain.atividade.AtividadeRepository;
import com.homework.domain.atividade.Entrega;
import com.homework.domain.atividade.EntregaFilterDTO;
import com.homework.domain.atividade.EntregaFilterDTO.SituacaoAlunoFilter;
import com.homework.domain.atividade.EntregaPK;
import com.homework.domain.atividade.EntregaRepository;
import com.homework.domain.curso.Curso;
import com.homework.domain.curso.CursoAluno;
import com.homework.domain.curso.CursoAluno.StatusMatricula;
import com.homework.domain.curso.CursoAlunoPK;
import com.homework.domain.curso.CursoAlunoRepository;
import com.homework.domain.curso.CursoRepository;
import com.homework.utils.SecurityUtils;

@Service
public class EntregaService {

	@Autowired
	private EntregaRepository entregaRepository;
	
	@Autowired
	private ArquivoService arquivoService;
	
	@Autowired
	private AtividadeRepository atividadeRepository;
	
	@Autowired
	private AlunoService alunoService;
	
	@Autowired
	private CursoAlunoRepository matriculaRepository;
	
	@Autowired
	private CursoRepository cursoRepository;
	
	
	public List<Entrega> getAtividadesEntreguesFiltradas(AtividadeFilter filter, Curso curso){
		List<Entrega> atividadesEntregues = new ArrayList<Entrega>();
		Aluno aluno = SecurityUtils.getAlunoLogado(); 
		
		if(filter.getDataInicio() == null && filter.getDataFinal() == null && filter.getStatusAtividade().equals(StatusAtividadeFilter.ENTREGUE)) {
			atividadesEntregues = entregaRepository.findEntregasAlunoPorCurso(filter.getIdCurso(), aluno.getId());
		} else if(filter.getStatusAtividade().equals(StatusAtividadeFilter.ENTREGUE)) {
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
	public void entregarAtividade(Long idAtividade, Entrega entrega) throws ArquivoException, EntregaAtrasadaException {
		Atividade atividade = atividadeRepository.findById(idAtividade).orElseThrow(NoSuchElementException::new);
		LocalDate dataEntrega = LocalDate.now();
		if(dataEntrega.isAfter(atividade.getDataFinal()) && !atividade.isPermiteEntregaAtrasada()) {
			throw new EntregaAtrasadaException("A data final para entrega da atividade já passou!");
		}
		Aluno aluno = SecurityUtils.getAlunoLogado();
		entrega.setId(new EntregaPK(atividade, aluno));
		entrega.setDataEntrega(dataEntrega);
		entrega.setNota(0.0);
		entregaRepository.save(entrega);
		if(!entrega.getArquivoEntrega().isEmpty() && entrega.getArquivoEntrega().getSize() > 0) {
			entrega.definirNomeArquivoEntrega();
			try {	
				arquivoService.uploadArquivo(entrega.getArquivoEntrega(), entrega.getNomeArquivoEntrega(), Dir.ENTREGA);
			} catch(Exception e) {
				throw new ArquivoException(e.getMessage());
			}
		}
		entregaRepository.save(entrega);
	}
	
	public List<AtividadeEntregaDTO> getTodasNotasAluno(Aluno aluno){
		List<AtividadeEntregaDTO> atividadesEntregues = new ArrayList<>();
		List<Curso> cursos = alunoService.getCursosMatriculados(aluno);
		cursos.forEach(c -> atividadesEntregues.add(getNotasAlunoPorCurso(aluno, c)));
		atividadesEntregues.forEach(a -> a.getNotas().sort((n1, n2) -> -n1.compareTo(n2)));
		return atividadesEntregues;
	}
	
	public AtividadeEntregaDTO getNotasAlunoPorCurso(Aluno aluno, Curso curso){
		List<Double> notas = getNotasAluno(aluno, curso);
		AtividadeEntregaDTO atividadeEntregueDTO = new AtividadeEntregaDTO();
		atividadeEntregueDTO.setIdAluno(aluno.getId());
		atividadeEntregueDTO.setIdCurso(curso.getId());
		atividadeEntregueDTO.setNomeAluno(aluno.getNome());
		atividadeEntregueDTO.setNomeCurso(curso.getNome());
		atividadeEntregueDTO.setNotas(notas);
		atividadeEntregueDTO.setMedia(calcularMediaAluno(aluno, curso));
		CursoAluno cursoAluno = matriculaRepository.findById(new CursoAlunoPK(curso, aluno)).orElseThrow(NoSuchElementException::new);
		atividadeEntregueDTO.setSituacaoAluno(cursoAluno.getSituacaoAluno());
		return atividadeEntregueDTO;
	}
	
	public AtividadeEntregaDTO getEntregaNotasAlunoPorCurso(Aluno aluno, Curso curso) {
		AtividadeEntregaDTO atividadeEntregueDTO = getNotasAlunoPorCurso(aluno, curso);
		List<Entrega> entregas = entregaRepository.findEntregasAlunoPorCurso(curso.getId(), aluno.getId());
		entregas.sort((e1, e2) -> -e1.getNota().compareTo(e2.getNota()));
		atividadeEntregueDTO.setEntregasNotas(entregas);
		return atividadeEntregueDTO;
	}
	
	public Double calcularMediaAluno(Aluno aluno, Curso curso) {
		List<Double> notas = getNotasAluno(aluno, curso);
		DoubleSummaryStatistics sumario = notas.stream()
				.limit(5)
				.collect(Collectors.summarizingDouble(n -> n));
		return arredondarMedia(sumario.getAverage());
	}

	public List<Double> getNotasAluno(Aluno aluno,  Curso curso) {
		List<Double> notas = new ArrayList<>();
		List<Atividade> atividadesCurso = atividadeRepository.findByCurso_Id(curso.getId());
		for(Atividade atividade : atividadesCurso) {
			Entrega entrega = entregaRepository.findById(new EntregaPK(atividade, aluno)).orElse(null);
			if(entrega == null) {
				notas.add(0.0);
			} else {
				notas.add(entrega.getNota() != null ? entrega.getNota() : 0.0);
			}
		}
		return notas.stream().sorted((n1, n2) -> -n1.compareTo(n2)).collect(Collectors.toList());
	}
	
	public List<Entrega> entregasSemCorrecaoAtividade(Atividade atividade){
		return entregaRepository.findEntregasSemCorrecao(atividade.getId());
	}
	
	public List<AtividadeEntregaDTO> buscarAlunoPor(EntregaFilterDTO filter){
		List<AtividadeEntregaDTO> atividadesEntregues = new ArrayList<>();
		Curso curso = cursoRepository.findById(filter.getIdCurso()).orElseThrow(NoSuchElementException::new);
		List<CursoAluno> matriculas = matriculaRepository.findByStatusMatriculaAndId_Curso(StatusMatricula.CONFIRMADA, curso);
		if(filter.getTodos() != null && filter.getTodos()) { 
		atividadesEntregues = matriculas.stream()
					.map(m -> getNotasAlunoPorCurso(m.getId().getAluno(), curso)).collect(Collectors.toList());
		} else if(filter.getIdAluno() != null && !filter.getNomeAluno().isBlank() && filter.getSituacaoAluno() != null) {
			List<CursoAluno> matriculasPorNomeAproximadoAluno = matriculaRepository.findByNomeAlunoAproximado(StatusMatricula.CONFIRMADA, curso, filter.getNomeAluno());
			matriculasPorNomeAproximadoAluno = matriculasPorNomeAproximadoAluno.stream()
					.filter(m -> m.getId().getAluno().getId().equals(filter.getIdAluno())).collect(Collectors.toList());
			atividadesEntregues = filtrarPorSituacaoAlunoFilter(matriculasPorNomeAproximadoAluno, filter.getSituacaoAluno());
		} else if(!filter.getNomeAluno().isBlank() && filter.getSituacaoAluno() != null) {
			List<CursoAluno> matriculasPorNomeAproximadoAluno = matriculaRepository.findByNomeAlunoAproximado(StatusMatricula.CONFIRMADA, curso, filter.getNomeAluno());
			atividadesEntregues = filtrarPorSituacaoAlunoFilter(matriculasPorNomeAproximadoAluno, filter.getSituacaoAluno());
		} else if(filter.getIdAluno() != null && filter.getSituacaoAluno() != null) {
			List<CursoAluno> matriculasPorId = matriculas.stream()
					.filter(m -> m.getId().getAluno().getId().equals(filter.getIdAluno())).collect(Collectors.toList());
			atividadesEntregues = filtrarPorSituacaoAlunoFilter(matriculasPorId, filter.getSituacaoAluno());
		} else {
			atividadesEntregues = filtrarPorSituacaoAlunoFilter(matriculas, filter.getSituacaoAluno());
		}
		return atividadesEntregues;
	}
	
	public void excluirTodasEntregaAlunoPorCurso(Aluno aluno, Curso curso) {
		entregaRepository.findEntregasAlunoPorCurso(curso.getId(), aluno.getId()).forEach(e -> entregaRepository.delete(e));
	}
	
	private List<AtividadeEntregaDTO> filtrarPorSituacaoAlunoFilter(List<CursoAluno> matriculas, SituacaoAlunoFilter situacaoAlunoFilter){
		List<AtividadeEntregaDTO> atividadesEntregues = new ArrayList<>();
		if(situacaoAlunoFilter.equals(SituacaoAlunoFilter.TODAS)) {
			atividadesEntregues = matriculas.stream()
					.map(m -> getNotasAlunoPorCurso(m.getId().getAluno(), m.getId().getCurso())).collect(Collectors.toList());
		} else {
			atividadesEntregues = matriculas.stream().filter(m -> m.getSituacaoAluno().toString().equalsIgnoreCase(situacaoAlunoFilter.toString()))
					.map(m -> getNotasAlunoPorCurso(m.getId().getAluno(), m.getId().getCurso())).collect(Collectors.toList());
		}
		return atividadesEntregues;
	}
	
	private Double arredondarMedia(Double media) {
		DecimalFormat df = new DecimalFormat("0.00");
		df.setRoundingMode(RoundingMode.HALF_UP);
		Double mediaArredondada = Math.round(media * 100.0)/100.0;
		return Double.parseDouble(df.format(mediaArredondada).replace(",", "."));
	}
}

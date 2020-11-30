package com.homework.domain.aluno.application.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.aluno.AlunoRepository;
import com.homework.domain.aluno.application.service.ArquivoService.Dir;
import com.homework.domain.atividade.Atividade;
import com.homework.domain.atividade.Atividade.StatusAtividade;
import com.homework.domain.coordenador.Coordenador;
import com.homework.domain.coordenador.CoordenadorRepository;
import com.homework.domain.atividade.AtividadeEntregaDTO;
import com.homework.domain.atividade.AtividadeRepository;
import com.homework.domain.atividade.Entrega;
import com.homework.domain.atividade.EntregaPK;
import com.homework.domain.atividade.EntregaRepository;
import com.homework.domain.curso.Curso;
import com.homework.domain.curso.CursoAluno;
import com.homework.domain.curso.CursoAluno.SituacaoAluno;
import com.homework.domain.curso.CursoAluno.StatusMatricula;
import com.homework.domain.curso.CursoAlunoPK;
import com.homework.domain.curso.CursoAlunoRepository;
import com.homework.domain.curso.CursoRepository;
import com.homework.domain.professor.Professor;
import com.homework.domain.professor.ProfessorRepository;

@Service
public class ProfessorService {

	@Autowired
	private EntregaRepository entregaRepository;
	
	@Autowired
	private ArquivoService arquivoService;
	
	@Autowired
	private CursoRepository cursoRepository;
	
	@Autowired
	private CursoAlunoRepository matriculaRepository;
	
	@Autowired
	private EntregaService entregaService;
	
	@Autowired
	private AlunoRepository alunoRepository;
	
	@Autowired
	private AtividadeRepository atividadeRepository;
	
	@Autowired
	private CoordenadorRepository coordenadorRepository;
	
	@Autowired
	private ProfessorRepository professorRepository;
	
	public void corrigirAtividade(Entrega entrega) throws ValidationException, ApplicationException {
		if(entrega.getNota() < 0.0 || entrega.getNota() > 10.0) {
			throw new ValidationException("A nota precisa estar entre 0.0 e 10.0");
		}
		if(entrega.getArquivoCorrecao() != null) {
			entrega.definirNomeArquivoCorrecao();
			arquivoService.uploadArquivo(entrega.getArquivoCorrecao(), entrega.getNomeArquivoCorrecao(), Dir.CORRECAO);
		}
		entrega.setCorrigido(true);
		entregaRepository.save(entrega);
	}
	
	public boolean fecharNotas(Long idCurso) {
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		List<Atividade> atividades = atividadeRepository.findByCurso_Id(idCurso);
		for(Atividade atividade : atividades) {
			List<Entrega> entregas = entregaService.entregasSemCorrecaoAtividade(atividade);
			if(!entregas.isEmpty()) {
				return false;
			}
		}
		List<CursoAluno> matriculas =  matriculaRepository.findByStatusMatriculaAndId_Curso(StatusMatricula.CONFIRMADA, curso);
		Map<Long, Double> medias = new HashMap<>();
		for (CursoAluno matricula : matriculas) {
			 Double media = entregaService.getNotasAlunoPorCurso(matricula.getId().getAluno(), curso).getMedia();
			 medias.put(matricula.getId().getAluno().getId(), media);
		}
		
		Set<Map.Entry<Long, Double>> conjuntoDeMedias = medias.entrySet();
		List<CursoAluno> matriculasComSituacaoDefinida = new ArrayList<>();
		for(Entry<Long, Double> alunoMedia : conjuntoDeMedias) {
			Aluno aluno = alunoRepository.findById(alunoMedia.getKey()).orElseThrow(NoSuchElementException::new);
			CursoAluno matricula = matriculaRepository.findById(new CursoAlunoPK(curso, aluno)).orElseThrow(NoSuchElementException::new);
			if(alunoMedia.getValue() >= 6.0) {
				matricula.setSituacaoAluno(SituacaoAluno.APROVADO);
			} else {
				matricula.setSituacaoAluno(SituacaoAluno.REPROVADO);
			}
			matricula.setMedia(alunoMedia.getValue());
			matriculasComSituacaoDefinida.add(matricula);
		}
		matriculaRepository.saveAll(matriculasComSituacaoDefinida);
		return true;
	}
	
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
	public void atualizarNotasAluno(AtividadeEntregaDTO alunoNotas) {
		Curso curso = cursoRepository.findById(alunoNotas.getIdCurso()).orElseThrow(NoSuchElementException::new);
		Aluno aluno = alunoRepository.findById(alunoNotas.getIdAluno()).orElseThrow(NoSuchElementException::new);
		CursoAluno matricula = matriculaRepository.findById(new CursoAlunoPK(curso, aluno)).orElseThrow(NoSuchElementException::new);
		matricula.setMedia(alunoNotas.getMedia());
		matricula.setSituacaoAluno(alunoNotas.getSituacaoAluno());
		for(Entrega entrega : alunoNotas.getEntregasNotas()) {
			Atividade atividade = atividadeRepository.findById(entrega.getId().getAtividade().getId()).orElseThrow(NoSuchElementException::new);
			Entrega entregaBD = entregaRepository.findById(new EntregaPK(atividade, aluno)).orElseThrow(NoSuchElementException::new);
			if(!entregaBD.isCorrigido() && !entrega.getNota().equals(entregaBD.getNota())) {
				entregaBD.setCorrigido(true);
				entrega.setCorrigido(true);
			}
			entregaBD.setNota(entrega.getNota());
		}
	}
	
	public List<Atividade> buscarAtividadesEmAbertoEmCursoProfessor(Professor professor) {
		List<Curso> cursosEmAndamento = cursoRepository.findCursosEmAndamentoDoProfessor(professor.getId());
		List<Atividade> atividadesEmAberto = new ArrayList<>();
		cursosEmAndamento.forEach(c -> {
			List<Atividade> atividades = atividadeRepository.findByCursoEStatus(c.getId(), StatusAtividade.EM_ABERTO);
			atividadesEmAberto.addAll(atividades);
		});
		return atividadesEmAberto;
	}
	
	public Professor save(Professor professor) throws ValidationException {
		if(!isValidEmail(professor)) {
			throw new ValidationException("E-mail já cadastrado!");
		}

		if(professor.getId() == null) {
			professor.criptografarSenha();
		} else {
			String senha = professorRepository.findById(professor.getId()).get().getSenha();
			professor.setSenha(senha);
		}
		return professorRepository.save(professor);
	}
	
	private boolean isValidEmail(Professor professor) {
			Aluno aluno = alunoRepository.findByEmail(professor.getEmail());
			
			if(aluno != null) {
				return false;
			}
			
			Coordenador coordenador = coordenadorRepository.findByEmail(professor.getEmail());
			
			if(coordenador != null) {
				return false;
			}
			
			Professor professorDB = professorRepository.findByEmail(professor.getEmail());
			
			if(professorDB != null) {
				if(professorDB.getId().equals(professor.getId())) {
					return true;
				}
				return false;
			}
			return true;
		}
}

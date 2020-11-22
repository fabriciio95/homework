package com.homework.domain.aluno.application.service;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.aluno.AlunoRepository;
import com.homework.domain.aluno.CertificadoAlunoDTO;
import com.homework.domain.atividade.Atividade;
import com.homework.domain.atividade.AtividadeRepository;
import com.homework.domain.coordenador.Coordenador;
import com.homework.domain.coordenador.CoordenadorRepository;
import com.homework.domain.curso.Curso;
import com.homework.domain.curso.CursoAluno;
import com.homework.domain.curso.CursoAluno.StatusMatricula;
import com.homework.domain.curso.CursoAlunoPK;
import com.homework.domain.curso.CursoAlunoRepository;
import com.homework.domain.curso.CursoRepository;
import com.homework.domain.curso.StatusCurso;
import com.homework.domain.professor.Professor;
import com.homework.domain.professor.ProfessorRepository;
import com.homework.utils.SecurityUtils;

@Service
public class AlunoService {

	@Autowired
	private AlunoRepository alunoRepository;
	
	@Autowired
	private ProfessorRepository professorRepository;
	
	@Autowired
	private CoordenadorRepository coordenadorRepository;
	
	@Autowired
	private CoordenadorService coordenadorService;
	
	@Autowired
	private CursoRepository cursoRepository;
	
	@Autowired
	private CursoAlunoRepository cursoAlunoRepository;
	
	@Autowired
	private AtividadeRepository atividadeRepository;
	
	@Autowired
	private AtividadeService atividadeService;
	
	@Autowired
	private ArquivoService arquivoService;
	
	public Aluno save(Aluno aluno) throws ValidationException{
		if(!isValidEmail(aluno)) {
			throw new ValidationException("E-mail já cadastrado!");
		} 
		
		if(aluno.getId() == null) {
			aluno.criptografarSenha();
		} else {
			String senha = alunoRepository.findById(aluno.getId()).get().getSenha();
			aluno.setSenha(senha);
		}
		return alunoRepository.save(aluno);
	}
	
	private boolean isValidEmail(Aluno aluno) {
		Professor professor = professorRepository.findByEmail(aluno.getEmail());
		
		if(professor != null) {
			return false;
		}
		
		Coordenador coordenador = coordenadorRepository.findByEmail(aluno.getEmail());
		
		if(coordenador != null) {
			return false;
		}
		
		Aluno alunoDB = alunoRepository.findByEmail(aluno.getEmail());
		
		if(alunoDB != null) {
			if(alunoDB.getId().equals(aluno.getId())) {
				return true;
			}
			return false;
		}
		return true;
	}
	
	public Boolean confirmarMatricula(Long idCurso, String chave) throws MatriculaNaoEncontradaException{
		Aluno aluno = SecurityUtils.getAlunoLogado();
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		CursoAluno matricula = cursoAlunoRepository.findById(new CursoAlunoPK(curso, aluno)).orElseThrow(() -> new MatriculaNaoEncontradaException("Pedido de matrícula negado ou não realizado!"));
		String chaveCorreta = coordenadorService.getChaveMatricula(curso, aluno);
		if(matricula.getPermissaoVisualizada()) {
			if(chaveCorreta.equals(chave)) {
				matricula.setStatusMatricula(StatusMatricula.CONFIRMADA);
				cursoAlunoRepository.save(matricula);
				return true;
			} else {
				return false;
			}
		}
		return null;
	}
	
	public List<Curso> getCursosMatriculados(Aluno aluno){
		List<CursoAluno> matriculas = cursoAlunoRepository.findById_AlunoAndStatusMatricula(aluno, StatusMatricula.CONFIRMADA);
		return matriculas.stream().filter(m -> !m.getId().getCurso().getStatus().equals(StatusCurso.CONCLUIDO))
				.map(m ->   m.getId().getCurso())
				.collect(Collectors.toList());
	}
	
	public List<CursoAluno> getCursosConcluidos(Aluno aluno){
		return cursoAlunoRepository.findCursosConcluidosPeloAluno(aluno.getId());
	}
	
	public void baixarCertificado(HttpServletResponse response, CursoAluno matricula) throws Exception {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		CertificadoAlunoDTO certificado = new CertificadoAlunoDTO();
		certificado.setNomeAluno(matricula.getId().getAluno().getNome());
		certificado.setNomeCurso(matricula.getId().getCurso().getNome());
		certificado.setNomeProfessor(matricula.getId().getCurso().getProfessor().getNome());
		certificado.setDataConclusao(matricula.getId().getCurso().getDataConclusao().format(formatter).toString());
		certificado.setDataMatricula(matricula.getDataMatricula().format(formatter).toString());
		List<CertificadoAlunoDTO> certificados = new ArrayList<CertificadoAlunoDTO>();
		certificados.add(certificado);
		String caminhoCertificado = arquivoService.gerarRelatorio(certificados, new HashMap<>(),
				"certificado", "certificado" + matricula.getId().getAluno().getNome());
		arquivoService.downloadArquivo(response, new File(caminhoCertificado), "certificado" +  matricula.getId().getAluno().getNome() + ".pdf");
	}
	
	public List<Atividade> getTodasAtividadesPendentesAlunoLogado(){
		Aluno aluno = SecurityUtils.getAlunoLogado();
		List<Curso> cursosMatriculados = getCursosMatriculados(aluno);
		List<Atividade> atividades = new ArrayList<>();
		for (Curso curso : cursosMatriculados) {
			 atividades.addAll(atividadeRepository.findByCurso_Id(curso.getId()));
		}
		return atividadeService.filtrarAtividadesPendentes(atividades);
	}
}

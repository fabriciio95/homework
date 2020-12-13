package com.homework.domain.aluno.application.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.aluno.AlunoFilter;
import com.homework.domain.aluno.AlunoRepository;
import com.homework.domain.aluno.application.service.ArquivoService.Dir;
import com.homework.domain.atividade.Entrega;
import com.homework.domain.atividade.EntregaRepository;
import com.homework.domain.comunicado.Comunicado;
import com.homework.domain.comunicado.Comunicado.TipoEnvio;
import com.homework.domain.coordenador.Coordenador;
import com.homework.domain.coordenador.CoordenadorRepository;
import com.homework.domain.coordenador.RecadoCoordenador;
import com.homework.domain.coordenador.RecadoCoordenadorRepository;
import com.homework.domain.curso.Curso;
import com.homework.domain.curso.CursoAluno;
import com.homework.domain.curso.CursoAluno.StatusMatricula;
import com.homework.domain.curso.CursoAlunoPK;
import com.homework.domain.curso.CursoAlunoRepository;
import com.homework.domain.curso.CursoFilter;
import com.homework.domain.curso.CursoRepository;
import com.homework.domain.curso.StatusCurso;
import com.homework.domain.professor.Professor;
import com.homework.domain.professor.ProfessorFilter;
import com.homework.domain.professor.ProfessorRepository;
import com.homework.domain.professor.RecadoProfessor;
import com.homework.domain.professor.RecadoProfessorRepository;
import com.homework.domain.recado.Recado;
import com.homework.domain.usuario.Usuario;
import com.homework.utils.EnviarEmail;
import com.homework.utils.SecurityUtils;

@Service
public class CoordenadorService {

	@Autowired
	private CursoAlunoRepository matriculaRepository;
	
	@Autowired
	private CursoRepository cursoRepository;
	
	@Autowired
	private AlunoRepository alunoRepository;
	
	@Autowired
	private EntregaRepository entregaRepository;
	
	@Autowired
	private ArquivoService arquivoService;
	
	@Autowired
	private EntregaService entregaService;
	
	@Autowired
	private AlunoService alunoService;
	
	@Autowired
	private CursoService cursoService;
	
	@Autowired
	private RecadoCoordenadorRepository recadoCoordenadorRepository;
	
	@Autowired
	private RecadoProfessorRepository recadoProfessorRepository;
	
	@Autowired
	private ProfessorRepository professorRepository;
	
	@Autowired
	private CoordenadorRepository coordenadorRepository;
	
	public List<CursoAluno> solicitacoesDeMatriculasPendentes(){
		return matriculaRepository.findByPermissaoVisualizada(false);
	}
	
	public void permitirMatricula(Long idCurso, Long idAluno) {
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		Aluno aluno = alunoRepository.findById(idAluno).orElseThrow(NoSuchElementException::new);
		CursoAluno matricula = matriculaRepository.findById(new CursoAlunoPK(curso, aluno)).orElseThrow(NoSuchElementException::new);
		matricula.setPermissaoVisualizada(true);
		matricula.setStatusMatricula(StatusMatricula.NAO_CONFIRMADA);
		matriculaRepository.save(matricula);
	}
	
	public void enviarEmailDePermissaoDeMatricula(Long idCurso, Long idAluno) throws EmailException {
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		Aluno aluno = alunoRepository.findById(idAluno).orElseThrow(NoSuchElementException::new);
		try {
			EnviarEmail.enviarEmail(aluno.getEmail(), "Sua matrícula no curso de " + curso.getNome() + " foi liberada! ",
					getConteudoEmailMatriculaPermitida(curso, aluno));
		} catch(Exception e) {
			e.printStackTrace();
			throw new EmailException("Houve um erro ao enviar e-mail"); 
		}
	}
	
	public void proibirMatricula(Long idCurso, Long idAluno) {
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		curso = cursoService.atualizarVagasEQtdAlunosDoCurso(curso, false);
		cursoRepository.save(curso);
		Aluno aluno = alunoRepository.findById(idAluno).orElseThrow(NoSuchElementException::new);
		CursoAluno matricula = matriculaRepository.findById(new CursoAlunoPK(curso, aluno)).orElseThrow(NoSuchElementException::new);
		matriculaRepository.delete(matricula);
	}
	
	public void enviarEmailDeProibicaoDeMatricula(Long idCurso, Long idAluno) throws EmailException {
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		Aluno aluno = alunoRepository.findById(idAluno).orElseThrow(NoSuchElementException::new);
		try {
			EnviarEmail.enviarEmail(aluno.getEmail(), "Infelizmente sa matrícula no curso de " + curso.getNome() + " foi negada! ",
					getConteudoEmailMatriculaNegada(curso, aluno));
		} catch(Exception e) {
			e.printStackTrace();
			throw new EmailException("Houve um erro ao enviar e-mail"); 
		}
	}
	
	private String getConteudoEmailMatriculaPermitida(Curso curso, Aluno aluno) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div style=\" font-weight: bolder; font-family: cursive; background-color: black; color: #ffbf00; width: 400px;\">");
		sb.append("<span>Olá ");
		sb.append(aluno.getNome());
		sb.append(" </span> <br/>");
		sb.append("<span>Sua matrícula no curso de \"");
		sb.append(curso.getNome());
		sb.append("\" foi liberada,</span><br/>");
		sb.append("<span>e a partir de agora você pode confirmar a sua</span><br/>");
		sb.append("<span>matrícula utilizando a chave:</span><br/>");
		sb.append("<span style=\"background-color:white; color: black; font-weight: bolder; margin-left: 20%; padding: 2px; \">");
		sb.append(getChaveMatricula(curso, aluno));
		sb.append("</span><br/>");
		sb.append("<span>Seja muito bem vindo.</span>");
		sb.append("</div>");
		return sb.toString();
	}
	
	private String getConteudoEmailMatriculaNegada(Curso curso, Aluno aluno) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div style=\" font-weight: bolder; font-family: cursive; background-color: black; color: #ffbf00; width: 400px;\">");
		sb.append("<span>Olá ");
		sb.append(aluno.getNome());
		sb.append(" </span> <br/>");
		sb.append("<span>Infelizmente sua matrícula no curso de \"");
		sb.append(curso.getNome());
		sb.append("\" foi negada,</span><br/>");
		sb.append("<span>mas você ainda pode entrar em contato</span><br/>");
		sb.append("<span>com o coordenador e/ou solicitar uma nova permissão</span><br/>");
		sb.append("<span>de matrícula. Obrigado pela compreensão! </span>");
		sb.append("</div>");
		return sb.toString();
	}

	public String getChaveMatricula(Curso curso, Aluno aluno) {
		int code = (int) ((aluno.getId() + curso.getId() + curso.getProfessor().getId() + curso.getCoordenador().getId()) * 3);
		StringBuilder sb = new StringBuilder();
		sb.append(aluno.getId());
		sb.append(code);
		sb.append(curso.getCoordenador().getId());
		sb.append(curso.getId());
		return sb.toString();
	}
	
	@Transactional(rollbackFor = Exception.class)
	public void excluirAluno(Aluno aluno) throws Exception {
		List<Entrega> entregas = entregaRepository.findById_Aluno(aluno);
		excluirArquivosAluno(aluno, entregas);
		entregaRepository.excluirTodasEntregaAluno(aluno);
		atualizarVagasEQtdAlunosDosCursosMatriculados(aluno);
		matriculaRepository.excluirMatriculasAluno(aluno);
		alunoRepository.delete(aluno);
	}
	
	private void atualizarVagasEQtdAlunosDosCursosMatriculados(Aluno aluno) {
		List<Curso> cursosMatriculados = alunoService.getCursosMatriculados(aluno);
		for (Curso curso : cursosMatriculados) {
			curso = cursoService.atualizarVagasEQtdAlunosDoCurso(curso, false);
			cursoRepository.save(curso);
		}
	}
	
	@Transactional(rollbackFor = Exception.class)
	public void excluirArquivosAluno(Aluno aluno, List<Entrega> entregas) throws Exception {
		for(Entrega entrega : entregas) {
			if(entrega.getNomeArquivoEntrega() != null && !entrega.getNomeArquivoEntrega().isBlank()) {
				arquivoService.excluirArquivo(entrega.getNomeArquivoEntrega(), Dir.ENTREGA);
			}
			
			if(entrega.getNomeArquivoCorrecao() != null && !entrega.getNomeArquivoCorrecao().isBlank()) {
				arquivoService.excluirArquivo(entrega.getNomeArquivoCorrecao(), Dir.CORRECAO);
			}
		}
	}
	
	@Transactional(rollbackFor = Exception.class)
	public void desmatricularAluno(Long idAluno, Long idCurso) throws Exception {
		List<Entrega> entregas = entregaRepository.findEntregasAlunoPorCurso(idCurso, idAluno);
		Aluno aluno = alunoRepository.findById(idAluno).orElseThrow(NoSuchElementException::new);
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		curso = cursoService.atualizarVagasEQtdAlunosDoCurso(curso, false);
		cursoRepository.save(curso);
		excluirArquivosAluno(aluno, entregas);
		entregaService.excluirTodasEntregaAlunoPorCurso(aluno, curso);
		matriculaRepository.deleteById(new CursoAlunoPK(curso, aluno));
	}
	
	public List<CursoAluno> filtrarCursosConcluidosAluno(Long idAluno, CursoFilter filter) {
		Aluno aluno = alunoRepository.findById(idAluno).orElseThrow(NoSuchElementException::new);
		
		boolean isApenasPorId = filter.getIdCurso() != null && filter.getNome().isBlank() && filter.getDataInicio() == null && filter.getDataFinal() == null;
		boolean isApenasPorNome = !filter.getNome().isBlank() && filter.getIdCurso() == null && filter.getDataInicio() == null && filter.getDataFinal() == null;
		boolean isApenasPorDatas = filter.getDataInicio() != null && filter.getDataFinal() != null && filter.getNome().isBlank() && filter.getIdCurso() == null;
		boolean isPorIdENome = filter.getIdCurso() != null && !filter.getNome().isBlank() && filter.getDataInicio() == null && filter.getDataFinal() == null;
		boolean isPorIdEDatas = filter.getDataInicio() != null && filter.getDataFinal() != null && filter.getNome().isBlank() && filter.getIdCurso() != null;
		boolean isPorNomeEDatas =  filter.getDataInicio() != null && filter.getDataFinal() != null && !filter.getNome().isBlank() && filter.getIdCurso() == null;
		boolean isTodosCriterios = filter.getIdCurso() != null && !filter.getNome().isBlank() && filter.getDataInicio() != null && filter.getDataFinal() != null;;
		List<CursoAluno> cursosConcluidosFiltrados = new ArrayList<>();
		
		if(isApenasPorId) {
			cursosConcluidosFiltrados = matriculaRepository.findCursosConcluidoPeloIdCurso(aluno, filter.getIdCurso());
		} else if(filter.isTodos()) {
			cursosConcluidosFiltrados = matriculaRepository.findCursosConcluidosPeloAluno(aluno.getId());
		} else if(isApenasPorNome) {
			cursosConcluidosFiltrados = matriculaRepository.findCursosConcluidosPorNomeCurso(aluno, filter.getNome());
		} else if(isApenasPorDatas) {
			cursosConcluidosFiltrados = matriculaRepository.findCursosConcluidosPorDatas(aluno, filter.getDataInicio(), filter.getDataFinal());
		} else if(isPorIdENome) {
			cursosConcluidosFiltrados = matriculaRepository.findCursosConcluidoPeloIdENomeCurso(aluno, filter.getIdCurso(), filter.getNome());
		} else if(isPorIdEDatas) {
			cursosConcluidosFiltrados = matriculaRepository.findCursosConcluidosPorDatasEIdCurso(aluno, filter.getDataInicio(), filter.getDataFinal(), filter.getIdCurso());
		} else if(isPorNomeEDatas) {
			cursosConcluidosFiltrados = matriculaRepository.findCursosConcluidosPorDatasENomeCurso(aluno, filter.getDataInicio(), filter.getDataFinal(), filter.getNome());
		} else if(isTodosCriterios) {
			cursosConcluidosFiltrados = matriculaRepository.findCursosConcluidosPorIdDatasENomeCurso(aluno, filter.getIdCurso(), filter.getDataInicio(), filter.getDataFinal(), filter.getNome());
		}
		
		return cursosConcluidosFiltrados;
	}
	
	public List<Aluno> filtrarAlunos(AlunoFilter filter){
		List<Aluno> alunos = new ArrayList<>();
		boolean isApenasPorId = filter.getIdAluno() != null && filter.getNomeAluno().isBlank() && filter.getNomeCurso().isBlank();
		boolean isApenasPorNomeAluno = !filter.getNomeAluno().isBlank() && filter.getNomeCurso().isBlank() && filter.getIdAluno() == null;
		boolean isApenasPorNomeCurso = filter.getNomeAluno().isBlank() && !filter.getNomeCurso().isBlank() && filter.getIdAluno() == null;
		boolean isPorIdENomeAluno = filter.getIdAluno() != null && !filter.getNomeAluno().isBlank() && filter.getNomeCurso().isBlank();
		boolean isPorIdENomeCurso = filter.getIdAluno() != null && filter.getNomeAluno().isBlank() && !filter.getNomeCurso().isBlank();
		boolean isPorNomeCursoENomeAluno = !filter.getNomeAluno().isBlank() && !filter.getNomeCurso().isBlank() && filter.getIdAluno() == null;
		boolean isTodosCriterios = !filter.getNomeAluno().isBlank() && !filter.getNomeCurso().isBlank() && filter.getIdAluno() != null;
		
		if(isApenasPorId) {
			Optional<Aluno> aluno = alunoRepository.findById(filter.getIdAluno());
			if(aluno.isPresent()) {
				alunos.add(aluno.get());
			}
		} else if(filter.isTodos()) {
			alunos = alunoRepository.findAll();
		} else if(isApenasPorNomeAluno) {
			alunos = alunoRepository.findByNomeIgnoreCaseContaining(filter.getNomeAluno());
		} else if(isApenasPorNomeCurso) {
			alunos = matriculaRepository.findByNomeCurso(filter.getNomeCurso());
		} else if(isPorIdENomeAluno) {
			alunos = alunoRepository.findByIdAndNome(filter.getIdAluno(), filter.getNomeAluno());
		} else if(isPorIdENomeCurso) {
			alunos = matriculaRepository.findByIdAlunoENomeCurso(filter.getIdAluno(), filter.getNomeCurso());
		} else if(isPorNomeCursoENomeAluno) {
			alunos = matriculaRepository.findByNomeAlunoENomeCurso(filter.getNomeAluno(), filter.getNomeCurso());
		} else if(isTodosCriterios) {
			alunos = matriculaRepository.findByNomeEIdAlunoENomeCurso(filter.getIdAluno(),
					filter.getNomeAluno(), filter.getNomeCurso());
		}
		
		return alunos;
	}
	
	public Recado deletarRecado(Long idRecado, boolean isAutorCoordenador) {
		Recado recado = null;
		if(isAutorCoordenador) {
			recado = recadoCoordenadorRepository.findById(idRecado).orElseThrow(NoSuchElementException::new);
			recadoCoordenadorRepository.delete((RecadoCoordenador) recado);
		} else {
			recado = recadoProfessorRepository.findById(idRecado).orElseThrow(NoSuchElementException::new);
			recadoProfessorRepository.delete((RecadoProfessor) recado);
		}
		recado.getCurso().getRecados().remove(recado);
		return recado;
	}
	
	public boolean encerrarCurso(Long idCurso) {
		Long alunosIndefinidos = matriculaRepository.qtdAlunosSituacaoIndefinidoDoCurso(idCurso);
		if(alunosIndefinidos > 0) {
			return false;
		}
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		curso.setDataConclusao(LocalDate.now());
		curso.setStatus(StatusCurso.CONCLUIDO);
		cursoRepository.save(curso);
		return true;
	}

	public boolean isIdProfessorValido(Curso curso, Long idProfessor) {
		Professor professor = professorRepository.findById(idProfessor).orElse(null);
		if(professor != null) {
			curso.setProfessor(professor);
			return true;
		} else {
			if(curso.getId() != null) {
				Curso cursoDB = cursoRepository.findById(curso.getId()).orElseThrow(NoSuchElementException::new);
				curso.setProfessor(cursoDB.getProfessor());
			} else {
				curso.setProfessor(null);
			}
			return false;
		}
	}

	public boolean excluirProfessor(Professor professor) {
		List<Curso> cursosEmAndamento = cursoRepository.findCursosEmAndamentoDoProfessor(professor.getId());
		if(cursosEmAndamento.size() == 0) {
			professorRepository.delete(professor);
			return true;
		} 
 		return false;
	}
	
	public List<Curso> filtrarCursosLecionadoProfessor(CursoFilter filter){
		List<Curso> cursos = new ArrayList<Curso>();
		boolean isPorIdESituacao = filter.getIdCurso() != null && filter.getSituacaoCurso() != null && filter.getNome().isBlank() && filter.getDataInicio() == null && filter.getDataFinal() == null;
		boolean isPorIdDatasESituacao = filter.getIdCurso() != null && filter.getSituacaoCurso() != null && filter.getNome().isBlank() && filter.getDataInicio() != null && filter.getDataFinal() != null;
		boolean isPorIdNomeCursoESituacao = filter.getIdCurso() != null && filter.getSituacaoCurso() != null && !filter.getNome().isBlank() && filter.getDataInicio() == null && filter.getDataFinal() == null;
		boolean isPorNomeCursoDatasESituacao = !filter.getNome().isBlank() && filter.getSituacaoCurso() != null && filter.getDataInicio() != null && filter.getDataFinal() != null && filter.getIdCurso() == null;
		boolean isApenasPorSituacao = filter.getIdCurso() == null && filter.getSituacaoCurso() != null && filter.getNome().isBlank() && filter.getDataInicio() == null && filter.getDataFinal() == null;
		boolean isTodosCriterios = !filter.getNome().isBlank() && filter.getSituacaoCurso() != null && filter.getDataInicio() != null && filter.getDataFinal() != null && filter.getIdCurso() != null;
		boolean isPorNomeCursoESituacao = !filter.getNome().isBlank() && filter.getSituacaoCurso() != null && filter.getDataInicio() == null && filter.getDataFinal() == null && filter.getIdCurso() == null;
		boolean isPorDatasESituacao = filter.getIdCurso() == null && filter.getSituacaoCurso() != null && filter.getNome().isBlank() && filter.getDataInicio() != null && filter.getDataFinal() != null;
		
		if(filter.isTodos()) {
			cursos = cursoRepository.findByProfessor_Id(filter.getIdProfessor());
		} else if(isApenasPorSituacao) {
			cursos = cursoRepository.findByProfessor_IdAndStatus(filter.getIdProfessor(), filter.getSituacaoCurso());
		} else if(isPorIdESituacao) {
			cursos = cursoRepository.findbyProfessorCursoAndSituacao(filter.getIdProfessor(), filter.getIdCurso(), filter.getSituacaoCurso());
		} else if(isPorIdDatasESituacao) {
			cursos = cursoRepository.findbyProfessorCursoDatasAndSituacao(filter.getIdProfessor(), filter.getIdCurso(),
					filter.getDataInicio(), filter.getDataFinal(), filter.getSituacaoCurso());
		} else if(isPorIdNomeCursoESituacao) {
			cursos = cursoRepository.findbyProfessorCursoNomeAndSituacao(filter.getIdProfessor(), filter.getIdCurso(),
					filter.getNome(), filter.getSituacaoCurso());
		} else if(isPorNomeCursoDatasESituacao) {
			cursos = cursoRepository.findbyProfessorNomeCursoDatasAndSituacao(filter.getIdProfessor(), filter.getNome(),
					filter.getDataInicio(), filter.getDataFinal(), filter.getSituacaoCurso());
		} else if(isPorNomeCursoESituacao) {
			cursos = cursoRepository.findbyProfessorNomeCursoAndSituacao(filter.getIdProfessor(), filter.getNome(), filter.getSituacaoCurso());
		} else if(isPorDatasESituacao) {
			cursos = cursoRepository.findbyProfessorDatasAndSituacao(filter.getIdProfessor(), filter.getDataInicio(),
					filter.getDataFinal(), filter.getSituacaoCurso());
		} else if(isTodosCriterios) {
			cursos = cursoRepository.findbyProfessorCursoNomeDatasAndSituacao(filter.getIdProfessor(), filter.getIdCurso(),
					filter.getNome(), filter.getDataInicio(), filter.getDataFinal(), filter.getSituacaoCurso());
		}
		
		return cursos;
	}

	public ProfessorFilter getChaveCadastroProfessor(ProfessorFilter professorChave) throws ValidationException {
		Professor professor = professorRepository.findByEmail(professorChave.getEmail());
		if(professor == null) {
			throw new ValidationException("Não encontramos nenhum professor com esse e-mail");
		}
		professorChave.setChave(gerarChaveCadastroProfessor(professor));
		return professorChave;
	}
	
	public String gerarChaveCadastroProfessor(Professor professor) {
		return String.format("%s-%02d%d%d", String.valueOf(professor.getNome().charAt(0)).toUpperCase(),
				professor.getId() * 3, professor.getId() * 4, professor.getId() * 3 + professor.getId() * 4);
	}

	public List<Professor> filtrarProfessores(ProfessorFilter filter) {
		List<Professor> professores = new ArrayList<>();
		boolean isApenasPorId = filter.getId() != null && filter.getNome().isBlank() && filter.getNomeCurso().isBlank();
		boolean isApenasPorNomeProfessor = filter.getId() == null && !filter.getNome().isBlank() && filter.getNomeCurso().isBlank();
		boolean isApenasPorNomeCurso = filter.getId() == null && filter.getNome().isBlank() && !filter.getNomeCurso().isBlank();
		boolean isPorIdENomeProfessor = filter.getId() != null && !filter.getNome().isBlank() && filter.getNomeCurso().isBlank();
		boolean isPorIdENomeCurso = filter.getId() != null && filter.getNome().isBlank() && !filter.getNomeCurso().isBlank();
		boolean isPorNomeProfessorENomeCurso = filter.getId() == null && !filter.getNome().isBlank() && !filter.getNomeCurso().isBlank();
		boolean isTodosCriterios = filter.getId() != null && !filter.getNome().isBlank() && !filter.getNomeCurso().isBlank();
		
		if(filter.isTodos()) {
			professores = professorRepository.findAll();
		} else if(isApenasPorId) {
			Professor professor = professorRepository.findById(filter.getId()).orElse(null);
			if(professor != null) {
				professores.add(professor);
			}
		} else if(isApenasPorNomeProfessor) {
			professores = professorRepository.findByNomeIgnoreCaseContaining(filter.getNome());
		} else if(isApenasPorNomeCurso) {
			professores = professorRepository.findByCursoLecionado(filter.getNomeCurso());
		} else if(isPorIdENomeProfessor) {
			professores = professorRepository.findByIdAndNomeIgnoreCaseContaining(filter.getId(), filter.getNome());
		} else if(isPorIdENomeCurso) {
			professores = professorRepository.findByIdENomeCurso(filter.getId(), filter.getNomeCurso());
		} else if(isPorNomeProfessorENomeCurso) {
			professores = professorRepository.findByNomeProfessorENomeCurso(filter.getNome(), filter.getNomeCurso());
		} else if(isTodosCriterios) {
			professores = professorRepository.findByIdNomeProfessorENomeCurso(filter.getId(), filter.getNome(), filter.getNomeCurso());
		}
		
		return professores;
	}
	
	
	public Coordenador save(Coordenador coordenador) throws ValidationException {
		if(!isValidEmail(coordenador)) {
			throw new ValidationException("E-mail já cadastrado!");
		}

		if(coordenador.getId() == null) {
			coordenador.criptografarSenha();
		} else {
			String senha = professorRepository.findById(coordenador.getId()).get().getSenha();
			coordenador.setSenha(senha);
		}
		return coordenadorRepository.save(coordenador);
	}
	
	private boolean isValidEmail(Coordenador coordenador) {
			Professor professor = professorRepository.findByEmail(coordenador.getEmail());
			
			if(professor != null) {
				return false;
			}
			
			Aluno aluno = alunoRepository.findByEmail(coordenador.getEmail());
			
			if(aluno != null) {
				return false;
			}
			
			Coordenador coordenadorDB = coordenadorRepository.findByEmail(coordenador.getEmail());
			
			if(coordenadorDB != null) {
				if(coordenadorDB.getId().equals(coordenador.getId())) {
					return true;
				}
				return false;
			}
			return true;
		}
	
	public void cadastrarNovoCurso(Curso curso) throws ValidationException{
		if(curso.getDataInicial().isBefore(LocalDate.now())) {
			throw new ValidationException("Você não pode criar um curso com uma data que já passou");
		}
		
		Coordenador coordenador = SecurityUtils.getCoordenadorLogado();
		curso.setCoordenador(coordenador);
		cursoRepository.save(curso);
	}
	
	public List<Curso> filtrarCursos(CursoFilter filter){
		List<Curso> cursos = new ArrayList<>();
		
		boolean isPorIdESituacao = filter.getIdCurso() != null && filter.getSituacaoCurso() != null && filter.getNome().isBlank();
		boolean isPorIdNomeCursoESituacao = filter.getIdCurso() != null && filter.getSituacaoCurso() != null && !filter.getNome().isBlank();
		boolean isPorNomeCursoESituacao = !filter.getNome().isBlank() && filter.getSituacaoCurso() != null && filter.getIdCurso() == null;
		boolean isApenasPorSituacao = filter.getIdCurso() == null && filter.getSituacaoCurso() != null && filter.getNome().isBlank();
		
		if(filter.isTodos()) {
			cursos = cursoRepository.findAll(Sort.by(Order.desc("dataConclusao")));
		} else if(isPorIdESituacao) {
			cursos = cursoRepository.findByIdAndStatus(filter.getIdCurso(), filter.getSituacaoCurso());
		} else if(isPorIdNomeCursoESituacao) {
			cursos = cursoRepository.findByIdNomeCursoESituacao(filter.getIdCurso(), filter.getNome(), filter.getSituacaoCurso());
		} else if(isPorNomeCursoESituacao) {
			cursos = cursoRepository.findByNomeCursoESituacao(filter.getNome(), filter.getSituacaoCurso());
		} else if(isApenasPorSituacao) {
			cursos = cursoRepository.findByStatusOrdered(filter.getSituacaoCurso());
		}
		
		return cursos;
	}

	public void enviarComunicado(Comunicado comunicado) throws Exception {
		List<Usuario> destinatarios = new ArrayList<>();
		if(comunicado.getTipoEnvio().equals(TipoEnvio.PROFESSOR)) {
			if(comunicado.getIdTipoEnvio() == 0) {
				destinatarios.addAll(professorRepository.findAll());
			} else if(comunicado.getIdTipoEnvio() > 0){
				Professor professor = professorRepository.findById(comunicado.getIdTipoEnvio())
						.orElseThrow(() -> new IllegalStateException("Não encontramos nenhum professor com este ID"));
				destinatarios.add(professor);
			} else {
				throw new IllegalStateException("Opção inválida, digite 0 ou um ID válido");
			}
		} else if(comunicado.getTipoEnvio().equals(TipoEnvio.ALUNO)) {
			if(comunicado.getIdTipoEnvio() == 0) {
				destinatarios.addAll(alunoRepository.findAll());
			} else if(comunicado.getIdTipoEnvio() > 0){
				Aluno aluno = alunoRepository.findById(comunicado.getIdTipoEnvio())
						.orElseThrow(() -> new IllegalStateException("Não encontramos nenhum aluno com este ID"));
				destinatarios.add(aluno);
			} else {
				throw new IllegalStateException("Opção inválida, digite 0 ou um ID válido");
			}
		} else if(comunicado.getTipoEnvio().equals(TipoEnvio.CURSO_PROFESSOR_ALUNOS)) {
			if(comunicado.getIdTipoEnvio() <= 0) {
				throw new IllegalStateException("OPCAO INVÁLIDA! Digite o ID correto do curso que deseja enviar o comunicado");
			}
			
			Curso curso = cursoRepository.findById(comunicado.getIdTipoEnvio())
					.orElseThrow(() -> new IllegalStateException("Não encontramos nenhum curso com este ID"));
			destinatarios.add(curso.getProfessor());
			List<Aluno> alunos = matriculaRepository.findByStatusMatriculaAndId_Curso(StatusMatricula.CONFIRMADA, curso)
												.stream().map(m -> m.getId().getAluno()).collect(Collectors.toList());
			destinatarios.addAll(alunos);
		} else if(comunicado.getTipoEnvio().equals(TipoEnvio.CURSO_ALUNOS)) {
			if(comunicado.getIdTipoEnvio() <= 0) {
				throw new IllegalStateException("OPCAO INVÁLIDA! Digite o ID correto do curso que deseja enviar o comunicado");
			}
			
			Curso curso = cursoRepository.findById(comunicado.getIdTipoEnvio())
						.orElseThrow(() -> new IllegalStateException("Não encontramos nenhum curso com este ID"));
			List<Aluno> alunos = matriculaRepository.findByStatusMatriculaAndId_Curso(StatusMatricula.CONFIRMADA, curso)
						.stream().map(m -> m.getId().getAluno()).collect(Collectors.toList());
			destinatarios.addAll(alunos);
		} else if(comunicado.getTipoEnvio().equals(TipoEnvio.CURSO_PROFESSOR)) {
			if(comunicado.getIdTipoEnvio() <= 0) {
				throw new IllegalStateException("OPCAO INVÁLIDA! Digite o ID correto do curso que deseja enviar o comunicado");
			}
			Curso curso = cursoRepository.findById(comunicado.getIdTipoEnvio())
					.orElseThrow(() -> new IllegalStateException("Não encontramos nenhum curso com este ID"));
			destinatarios.add(curso.getProfessor());
		}
		
	
		for (Usuario usuario : destinatarios) {
			new Thread(() -> {
				try {
					EnviarEmail.enviarEmail(usuario.getEmail(), comunicado.getAssunto(),
							getConteudoEmailComunicado(usuario.getNome(), comunicado.getCorpoEmail()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();
		}
	}
	
	private String getConteudoEmailComunicado(String nomeUsuario, String conteudoEmail) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div style=\" font-weight: bolder; font-family: cursive; background-color: black; color: #ffbf00; width: 400px;\">");
		sb.append("<span>Olá ");
		sb.append(nomeUsuario);
		sb.append(" </span> <br/>");
		sb.append("<span>");
		sb.append(conteudoEmail);
		sb.append("</span>");
		sb.append("</div>");
		return sb.toString();
	}
}

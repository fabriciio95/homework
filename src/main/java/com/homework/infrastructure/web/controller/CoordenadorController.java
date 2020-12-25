package com.homework.infrastructure.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.aluno.AlunoFilter;
import com.homework.domain.aluno.AlunoRepository;
import com.homework.domain.aluno.application.service.AlunoService;
import com.homework.domain.aluno.application.service.CoordenadorService;
import com.homework.domain.aluno.application.service.EmailException;
import com.homework.domain.aluno.application.service.EntregaService;
import com.homework.domain.aluno.application.service.ProfessorService;
import com.homework.domain.aluno.application.service.RecadoService;
import com.homework.domain.aluno.application.service.ValidationException;
import com.homework.domain.atividade.AtividadeEntregaDTO;
import com.homework.domain.atividade.EntregaFilterDTO;
import com.homework.domain.comunicado.Comunicado;
import com.homework.domain.coordenador.Coordenador;
import com.homework.domain.curso.Curso;
import com.homework.domain.curso.Curso.CategoriaCurso;
import com.homework.domain.curso.CursoAluno;
import com.homework.domain.curso.CursoAluno.SituacaoAluno;
import com.homework.domain.curso.CursoAlunoPK;
import com.homework.domain.curso.CursoAlunoRepository;
import com.homework.domain.curso.CursoFilter;
import com.homework.domain.curso.CursoRepository;
import com.homework.domain.curso.StatusCurso;
import com.homework.domain.professor.Professor;
import com.homework.domain.professor.ProfessorFilter;
import com.homework.domain.professor.ProfessorRepository;
import com.homework.domain.recado.Recado;
import com.homework.utils.SecurityUtils;

@Controller
@RequestMapping("/coordenador")
@SessionAttributes("matriculas")
public class CoordenadorController {

	@Autowired
	private CoordenadorService coordenadorService;
	
	@Autowired
	private AlunoRepository alunoRepository;
	
	@Autowired
	private EntregaService entregaService;
	
	@Autowired
	private AlunoService alunoService; 
	
	@Autowired
	private CursoRepository cursoRepository;
	
	@Autowired
	private CursoAlunoRepository matriculaRepository;
	
	@Autowired
	private RecadoService recadoService;
	
	@Autowired
	private ProfessorRepository professorRepository;
	
	@Autowired
	private ProfessorService professorService;
	
	@GetMapping("/home")
	public String home(Model model) {
		colocarDependenciasCoordenadorHomeNoModel(model);
		return "coordenador-home";
	}
	
	@GetMapping("/permissoes")
	public String permissoes(Model model) {
		colocarMatriculasNoModel(model);
		return "coordenador-permissoes";
	}
	
	@GetMapping("/permissoes/permitir")
	public String permitirMatricula(@RequestParam("curso") Long idCurso, @RequestParam("aluno") Long idAluno, Model model) {
		try {
			coordenadorService.enviarEmailDePermissaoDeMatricula(idCurso, idAluno);
		} catch(EmailException e) {
			model.addAttribute("msgErro", e.getMessage());
			colocarMatriculasNoModel(model);
			return "coordenador-permissoes";
		}
		coordenadorService.permitirMatricula(idCurso, idAluno);
		model.addAttribute("msgSucesso", "Matrícula liberada com sucesso!");
		colocarMatriculasNoModel(model);
		return "coordenador-permissoes";
	}
	
	@GetMapping("/permissoes/proibir")
	public String proibirMatricula(@RequestParam("curso") Long idCurso, @RequestParam("aluno") Long idAluno, Model model) {
		try {
			coordenadorService.enviarEmailDeProibicaoDeMatricula(idCurso, idAluno);
		} catch(EmailException e) {
			model.addAttribute("msgErro", e.getMessage());
			colocarMatriculasNoModel(model);
			return "coordenador-permissoes";
		}
		coordenadorService.proibirMatricula(idCurso, idAluno);
		model.addAttribute("msgSucesso", "Solicitação de matrícula negada com sucesso!");
		colocarMatriculasNoModel(model);
		return "coordenador-permissoes";
	}
	
	@GetMapping("/aluno")
	public String verAluno(@RequestParam("id") Long idAluno, Model model) {
		putDependenciesCoordenadorAlunoOnModel(idAluno, model, true);
		return "coordenador-aluno";
	}
	
	@GetMapping("/aluno/excluir")
	public String excluirAluno(@RequestParam("id") Long idAluno, Model model) {
		try {
			Aluno aluno = alunoRepository.findById(idAluno).orElseThrow(NoSuchElementException::new);
			coordenadorService.excluirAluno(aluno);
			model.addAttribute("msgSucesso", "Aluno excluído com sucesso");
		} catch(Exception e) {
			model.addAttribute("msgErroExcluir", "Houve um erro ao excluir aluno: " + e.getMessage());
			putDependenciesCoordenadorAlunoOnModel(idAluno, model, true);
			return "coordenador-aluno";
		}
		colocarDependenciasCoordenadorHomeNoModel(model);
		return "coordenador-home";
	}
	
	@GetMapping("/aluno/desmatricular")
	public String desmatricularAluno(@RequestParam("aluno") Long idAluno, @RequestParam("curso") Long idCurso, Model model) {
		try {
			coordenadorService.desmatricularAluno(idAluno, idCurso);
			model.addAttribute("msgSucessoDesmatricular", "Aluno desmatriculado com sucesso");
		} catch(Exception e) {
			model.addAttribute("msgErroDesmatricular", "Houve um erro ao desmatricular aluno: " + e.getMessage());
		}
		putDependenciesCoordenadorAlunoOnModel(idAluno, model, true);
		return "coordenador-aluno";
	}
	
	@PostMapping("/aluno/buscarCursoConcluido")
	public String buscarCursoConcluidoAluno(@ModelAttribute("cursoFilter") CursoFilter filter, @RequestParam("idAluno") Long idAluno, 
			Model model) {
		List<CursoAluno> cursosFiltrados = coordenadorService.filtrarCursosConcluidosAluno(idAluno, filter);
		model.addAttribute("cursos", cursosFiltrados);
		putDependenciesCoordenadorAlunoOnModel(idAluno, model, false);
		return "coordenador-aluno";
	}
	
	@GetMapping("/aluno/download-certificado")
	public String baixarCertificadoAluno(@RequestParam("aluno") Long idAluno, @RequestParam("curso") Long idCurso,
			HttpServletResponse response, Model model) {
		Aluno aluno = alunoRepository.findById(idAluno).orElseThrow(NoSuchElementException::new);
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		CursoAluno matricula = matriculaRepository.findById(new CursoAlunoPK(curso, aluno)).orElse(null);
		putDependenciesCoordenadorAlunoOnModel(idAluno, model, true);
		try { 
			if(matricula != null && matricula.getSituacaoAluno().equals(SituacaoAluno.APROVADO)) {
				alunoService.baixarCertificado(response, matricula);
			} else {
				throw new IllegalStateException("Matricula não encontrada ou você não foi aprovado nesse curso");
			}
		} catch(Exception e) {
			List<CursoAluno> cursosConcluidos = alunoService.getCursosConcluidos(aluno);
			model.addAttribute("cursos", cursosConcluidos);
			model.addAttribute("msgErroCertificado", "Houve um erro ao fazer download do certificado: " + e.getMessage());
			return "coordenador-aluno";
		}
		return null;
	}
	
	@GetMapping("/alunos")
	public String viewAlunos(Model model) {
		model.addAttribute("alunoFilter", new AlunoFilter());
		return "coordenador-alunos";
	}
	
	@PostMapping("/alunos/buscarAluno")
	public String buscarAlunos(@ModelAttribute("alunoFilter") AlunoFilter filter, Model model) {
		List<Aluno> alunos = coordenadorService.filtrarAlunos(filter);
		model.addAttribute("alunos", alunos);
		return "coordenador-alunos";
	}
	
	@GetMapping("/curso")
	public String viewCurso(@RequestParam("id") Long id,@ModelAttribute("matriculas") List<AtividadeEntregaDTO> matriculas, 
			Model model ) {
		putDependenciesCoordenadorCursoOnModel(id, model, true, true);
		model.addAttribute("matriculas", null);
		return "coordenador-curso";
	}
	
	@GetMapping("/curso/mural")
	public String viewMuralCurso(@RequestParam("curso") Long idCurso, Model model) {
		putDependenciesCoordenadorRecadosOnModel(idCurso, model);
		return "coordenador-curso-mural";
	}
	
	@PostMapping("/curso/recados/adicionar")
	public String novoRecado(@RequestParam("idCurso") Long idCurso, @RequestParam("corpo") String corpo, Model model) {
		Coordenador coordenador = SecurityUtils.getCoordenadorLogado();
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		recadoService.saveRecado(corpo, coordenador, curso);
		putDependenciesCoordenadorRecadosOnModel(idCurso, model);
		return "coordenador-curso-mural";
	}
	
	@GetMapping("/curso/recados/excluir")
	public String excluirRecado(@RequestParam("recado") Long idRecado, 
			@RequestParam("autorCoordenador") boolean isAutorCoordenador,  Model model) {
		Recado recado = coordenadorService.deletarRecado(idRecado, isAutorCoordenador);
		putDependenciesCoordenadorRecadosOnModel(recado.getCurso().getId(), model);
		return "coordenador-curso-mural";
	}
	
	@GetMapping("/curso/encerrar")
	public String encerrarCurso(@RequestParam("curso") Long idCurso,
			@ModelAttribute("matriculas") List<AtividadeEntregaDTO> matriculas, Model model) {
		boolean encerrou = coordenadorService.encerrarCurso(idCurso);
		if(encerrou) {
			model.addAttribute("msgSucesso", "Curso encerrado com sucesso!");
		} else {
			model.addAttribute("msgErroEncerramento", "Existe ainda alunos com situação indefinida, e por isso não é possível encerrar o curso!");
		}
		 putDependenciesCoordenadorCursoOnModel(idCurso,model, true, true);
		 model.addAttribute("matriculas", null);
		return "coordenador-curso";
	}
	
	@PostMapping("/curso/atualizar")
	public String atualizarCurso(@ModelAttribute("cursoForm") @Valid Curso curso, Errors errors,
			@RequestParam("idProfessor") Long idProfessor,
			@ModelAttribute("matriculas") List<AtividadeEntregaDTO> matriculas, Model model) {
		boolean isIdProfessorValido = coordenadorService.isIdProfessorValido(curso, idProfessor);
		if(!errors.hasErrors() && isIdProfessorValido) {
			Curso cursoBD = cursoRepository.findById(curso.getId()).orElseThrow();
			curso.setQtdAlunosMatriculados(cursoBD.getQtdAlunosMatriculados());
			cursoRepository.save(curso);
			model.addAttribute("msgSucesso", "Curso atualizado com sucesso");
			putDependenciesCoordenadorCursoOnModel(curso.getId(), model, true, true);
		} else {
			model.addAttribute("hasError", true);
			putDependenciesCoordenadorCursoOnModel(curso.getId(), model, false, true);
		} 
		
		if(!isIdProfessorValido) {
			errors.rejectValue("professor", null, "Não encontramos nenhum professor com o id passado anteriormente");
		} 
		model.addAttribute("matriculas", null);
		return "coordenador-curso";
	}
	
	@PostMapping("/curso/buscarAluno")
	public String buscarAlunoMatriculado(@ModelAttribute("entregaFilter") EntregaFilterDTO filter,
			@RequestParam("idCurso") Long idCurso,
			@ModelAttribute("matriculas") List<AtividadeEntregaDTO> matriculas, Model model) {
		filter.setIdCurso(idCurso);
		matriculas = entregaService.buscarAlunoPor(filter);
		model.addAttribute("matriculas", matriculas);
		putDependenciesCoordenadorCursoOnModel(idCurso, model, true, false);
		return "coordenador-curso";
	}
	
	@GetMapping("/curso/buscarAluno/desmatricular")
	public String desmatricularAlunoNaPaginaCurso(@RequestParam("aluno") Long idAluno,
			@RequestParam("curso") Long idCurso,
			@ModelAttribute("matriculas") List<AtividadeEntregaDTO> matriculas, 
			@ModelAttribute("entregaFilter") EntregaFilterDTO filter, Model model) {
		try {
			coordenadorService.desmatricularAluno(idAluno, idCurso);
			model.addAttribute("msgSucessoDesmatricular", "Aluno desmatriculado com sucesso");
			model.addAttribute("matriculas", atualizarListaMatriculas(matriculas, idAluno, idCurso));
		} catch(Exception e) {
			model.addAttribute("msgErroDesmatricular", "Houve um erro ao desmatricular aluno: " + e.getMessage());
		}
		putDependenciesCoordenadorCursoOnModel(idCurso, model, true, false);
		return "coordenador-curso";
	}
	
	@GetMapping("/professor")
	public String viewProfessor(@RequestParam("id") Long idProfessor, Model model) {
		putDependenciesCoordenadorProfessorOnModel(idProfessor, model, true);
		return "coordenador-professor";
	}
	
	@GetMapping("/professor/excluirProfessor")
	public String excluirProfessor(@RequestParam("id") Long idProfessor, Model model) {
		Professor professor = professorRepository.findById(idProfessor).orElseThrow(NoSuchElementException::new);
		boolean excluiu = coordenadorService.excluirProfessor(professor);
		if(excluiu) {
			model.addAttribute("msgSucesso", "Professor excluído com sucesso");
			colocarDependenciasCoordenadorHomeNoModel(model);
			return "coordenador-home";
		} else {
			model.addAttribute("msgErro", "O professor está atualmente lecionando um curso que não foi concluído e por isso não pode ser excluído");
			putDependenciesCoordenadorProfessorOnModel(professor.getId(), model, true);
			return "coordenador-professor";
		}
	}
	
	@PostMapping("/professor/buscarCurso")
	public String buscarCursoLecionadoProfessor(@ModelAttribute("cursoFilter") CursoFilter filter,
			@RequestParam("idProfessor") Long idProfessor, Model model) {
		filter.setIdProfessor(idProfessor);
		List<Curso> cursosFiltrados = coordenadorService.filtrarCursosLecionadoProfessor(filter);
		model.addAttribute("cursos", cursosFiltrados);
		putDependenciesCoordenadorProfessorOnModel(filter.getIdProfessor(), model, false);
		return "coordenador-professor";
	}
	
	
	@GetMapping("/professores")
	public String viewProfessores(Model model) {
		colocarDependenciasCoordenadorProfessoresNoModel(true, true, true, model);
		return "coordenador-professores";
	}
	
	@PostMapping("/professores/cadastrarProfessor")
	public String cadastrarProfessor(@ModelAttribute("professor") @Valid Professor professor, Errors errors, Model model) {
		if(!errors.hasErrors()) {
			try {
				professorService.save(professor);
				model.addAttribute("msgSucesso", "Professor cadastrado com sucesso e chave de autenticação liberada");
				colocarDependenciasCoordenadorProfessoresNoModel(true, true, true, model);
			} catch (ValidationException e) {
				errors.rejectValue("email", null, "Este e-mail já está cadastrado");
				colocarDependenciasCoordenadorProfessoresNoModel(false, true, true, model);
				model.addAttribute("hasError1", true);
			} catch(Exception e) {
				model.addAttribute("msgErro", "Houve um erro ao cadastrar professor: " + e.getMessage());
				colocarDependenciasCoordenadorProfessoresNoModel(false, true, true, model);
				model.addAttribute("hasError1", true);
			}
		} else {
			colocarDependenciasCoordenadorProfessoresNoModel(false, true, true, model);
			model.addAttribute("hasError1", true);
		}
		return "coordenador-professores";
	}
	
	@PostMapping("/professores/gerarChave")
	public String gerarChaveProfessor(@ModelAttribute("professorChave") ProfessorFilter professorChave, Model model) {
		try {
			professorChave = coordenadorService.getChaveCadastroProfessor(professorChave);
		} catch(ValidationException e) {
			model.addAttribute("msgErroChave", e.getMessage());
			professorChave.setChave("");
			model.addAttribute("professorChave", professorChave);
		}
		model.addAttribute("open", true);
		colocarDependenciasCoordenadorProfessoresNoModel(true, false, true, model);
		return "coordenador-professores";
	}
	
	@PostMapping("/professores/buscarProfessor")
	public String buscarProfessor(@ModelAttribute("professorFilter") ProfessorFilter filter, Model model) {
		List<Professor> professores = coordenadorService.filtrarProfessores(filter);
		model.addAttribute("professores", professores);
		colocarDependenciasCoordenadorProfessoresNoModel(true, true, false, model);
		return "coordenador-professores";
	}
	
	@GetMapping("/cursos")
	public String viewCursos(Model model) {
		colocarDependendenciasCoordenadorCursosNoModel(true, true, model);
		return "coordenador-cursos";
	}
	
	@PostMapping("/cursos/novoCurso")
	public String cadastrarNovoCurso(@ModelAttribute("curso") @Valid Curso curso, Errors errors, 
			@RequestParam("idProfessor") Long idProfessor, Model model) {
		boolean isIdProfessorValido = coordenadorService.isIdProfessorValido(curso, idProfessor);
		if(!errors.hasErrors() && isIdProfessorValido) {
			try {
				coordenadorService.cadastrarNovoCurso(curso);
			} catch(ValidationException e) {
				errors.rejectValue("dataInicial", null, e.getMessage());
				model.addAttribute("hasError", true);
				colocarDependendenciasCoordenadorCursosNoModel(false, true, model);
				return "coordenador-cursos"; 
			}
			model.addAttribute("msgSucesso", "Curso cadastrado com sucesso");
			colocarDependendenciasCoordenadorCursosNoModel(true, true, model);
		} else {
			model.addAttribute("hasError", true);
			colocarDependendenciasCoordenadorCursosNoModel(false, true, model);
		} 
		
		if(!isIdProfessorValido) {
			errors.rejectValue("professor", null, "Não encontramos nenhum professor com o id passado anteriormente");
		} 
		return "coordenador-cursos";
	}
	
	@PostMapping("/cursos/buscarCurso")
	public String buscarCurso(@ModelAttribute("cursoFilter") CursoFilter filter, Model model) {
		List<Curso> cursos = coordenadorService.filtrarCursos(filter);
		model.addAttribute("cursos", cursos);
		colocarDependendenciasCoordenadorCursosNoModel(true, false, model);
		return "coordenador-cursos";
	}
	
	@GetMapping("/comunicados")
	public String viewComunicados(Model model) {
		model.addAttribute("comunicado", new Comunicado());
		return "coordenador-comunicados";
	}
	
	@PostMapping("/comunicados/enviar")
	public String enviarComunicado(@ModelAttribute("comunicado") Comunicado comunicado, Model model) {
		try { 
			coordenadorService.enviarComunicado(comunicado);
			model.addAttribute("msgSucesso", "Comunicado enviado com sucesso!");
			comunicado.setIdTipoEnvio(null);
			model.addAttribute("comunicado", comunicado);
		} catch(Exception e) {
			model.addAttribute("msgErro", "Houve um erro ao enviar comunicado: " + e.getMessage());
		}
		return "coordenador-comunicados";
	}
	
	@GetMapping("/atualizar")
	public String atualizarCoordenador(Model model) {
		Coordenador coordenador = SecurityUtils.getCoordenadorLogado();
		model.addAttribute("funcionario", coordenador);
		model.addAttribute("isCoordenador", true);
		return "funcionario-atualizacao";
	}
	
	@PostMapping("/save")
	public String atualizarDadosCoordenador(@ModelAttribute("funcionario") @Valid Coordenador coordenador, Errors errors, Model model) {
		if(!errors.hasErrors()) {
			try {
				coordenadorService.save(coordenador);
				SecurityUtils.getLoggedUser().atualizarLoggedUser(coordenador);
				model.addAttribute("msgSucesso", "Coordenador atualizado com sucesso");
			} catch(ValidationException e) {
				errors.rejectValue("email", null, e.getMessage());
			}
		}
		model.addAttribute("isCoordenador", true);
		return "funcionario-atualizacao";
	}
	
	
	
	public void colocarDependendenciasCoordenadorCursosNoModel(boolean isComNovoCurso, boolean isComNewCursoFilter, Model model) {
		if(isComNovoCurso) {
			model.addAttribute("curso", new Curso());
		}
		
		List<CategoriaCurso> categorias = new ArrayList<>(Arrays.asList(CategoriaCurso.values()));
		categorias.remove(CategoriaCurso.NENHUMA);
		model.addAttribute("categoriasCurso", categorias);
		
		if(isComNewCursoFilter) {
			model.addAttribute("cursoFilter", new CursoFilter());
		}
	}
	
	public void colocarDependenciasCoordenadorProfessoresNoModel(boolean isComNovoProfessor,
			boolean isComNovoProfessorChave, boolean isComNovoProfessorFilter, Model model) {
		if(isComNovoProfessor) {
			model.addAttribute("professor", new Professor());
		}
		
		if(isComNovoProfessorChave) {
			model.addAttribute("professorChave", new ProfessorFilter());
		}
		
		if(isComNovoProfessorFilter) {
			model.addAttribute("professorFilter", new ProfessorFilter());
		}
	}
	
	public void putDependenciesCoordenadorProfessorOnModel(Long idProfessor, Model model, boolean isWithNewCursoFilter) {
		Professor professor = professorRepository.findById(idProfessor).orElseThrow(NoSuchElementException::new);
		model.addAttribute("professor", professor);
		if(isWithNewCursoFilter) {
			model.addAttribute("cursoFilter", new CursoFilter());
		}
	}
	
	private List<AtividadeEntregaDTO> atualizarListaMatriculas(List<AtividadeEntregaDTO> matriculas, Long idAluno, Long idCurso) {
		matriculas.removeIf(a -> a.getIdAluno().equals(idAluno) && a.getIdCurso().equals(idCurso));
		return matriculas;
	}

	@ModelAttribute("matriculas")
	public List<AtividadeEntregaDTO> matriculas(){
		return new ArrayList<>();
	}
	
	private void putDependenciesCoordenadorRecadosOnModel(Long idCurso, Model model) {
		colocarCursoNoModel(idCurso, model);
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		ControllerHelper.getRecadosAndPutOnModel(curso, model, false);
	}

	private void putDependenciesCoordenadorAlunoOnModel(Long idAluno, Model model, boolean isWithNewFilterCursoFilter) {
		Aluno aluno = alunoRepository.findById(idAluno).orElseThrow(NoSuchElementException::new);
		List<AtividadeEntregaDTO> atividades = entregaService.getTodasNotasAluno(aluno);
		model.addAttribute("atividadesEntregues", atividades);
		model.addAttribute("aluno", aluno);
		if(isWithNewFilterCursoFilter) {
			model.addAttribute("cursoFilter", new CursoFilter());
		}
	}
	
	private void colocarMatriculasNoModel(Model model) {
		List<CursoAluno> matriculas = coordenadorService.solicitacoesDeMatriculasPendentes();
		model.addAttribute("matriculas", matriculas);
		if(matriculas.isEmpty()) {
			model.addAttribute("msgPermissoesMatricula", "Não há novas solicitações de matrículas");
		}
	}
	
	private void colocarCursoNoModel(Long idCurso, Model model) {
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		model.addAttribute("curso", curso);
	}
	
	private void putDependenciesCoordenadorCursoOnModel(Long idCurso, Model model, boolean withCursoForm, boolean withNewAlunoFilter) {
		if(withCursoForm) {
			Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
			model.addAttribute("cursoForm", curso);
		}
		
		if(withNewAlunoFilter) {
			model.addAttribute("entregaFilter", new EntregaFilterDTO());
		}
		colocarCursoNoModel(idCurso, model);
		List<CategoriaCurso> categorias = new ArrayList<>(Arrays.asList(CategoriaCurso.values()));
		categorias.remove(CategoriaCurso.NENHUMA);
		model.addAttribute("categoriasCurso", categorias);
		List<StatusCurso> status = new ArrayList<>(Arrays.asList(StatusCurso.values()));
		status.remove(StatusCurso.CONCLUIDO);
		model.addAttribute("statusCurso", status);
	}
	
	public void colocarDependenciasCoordenadorHomeNoModel(Model model) {
		List<Curso> cursosEmAndamento = cursoRepository.findByStatusOrdered(StatusCurso.EM_ANDAMENTO);
		model.addAttribute("cursosEmAndamento", cursosEmAndamento);
	}
}


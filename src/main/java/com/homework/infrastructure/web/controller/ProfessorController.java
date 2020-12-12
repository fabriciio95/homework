package com.homework.infrastructure.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.aluno.AlunoRepository;
import com.homework.domain.aluno.application.service.ApplicationException;
import com.homework.domain.aluno.application.service.ArquivoException;
import com.homework.domain.aluno.application.service.ArquivoService;
import com.homework.domain.aluno.application.service.AtividadeService;
import com.homework.domain.aluno.application.service.EntregaService;
import com.homework.domain.aluno.application.service.ProfessorService;
import com.homework.domain.aluno.application.service.RecadoService;
import com.homework.domain.aluno.application.service.ValidationException;
import com.homework.domain.atividade.Atividade;
import com.homework.domain.atividade.AtividadeEntregaDTO;
import com.homework.domain.atividade.AtividadeFilter;
import com.homework.domain.atividade.AtividadeRepository;
import com.homework.domain.atividade.Entrega;
import com.homework.domain.atividade.EntregaFilterDTO;
import com.homework.domain.atividade.EntregaPK;
import com.homework.domain.atividade.EntregaRepository;
import com.homework.domain.curso.Curso;
import com.homework.domain.curso.CursoRepository;
import com.homework.domain.professor.Professor;
import com.homework.domain.professor.RecadoProfessor;
import com.homework.domain.professor.RecadoProfessorRepository;
import com.homework.utils.SecurityUtils;

@Controller
@RequestMapping(path = "/professor")
@SessionAttributes("atividadesEntregues")
public class ProfessorController {
	
	@Autowired
	private CursoRepository cursoRepository;
	
	@Autowired
	private RecadoService recadoService;
	
	@Autowired
	private AtividadeService atividadeService;
	
	@Autowired
	private AtividadeRepository atividadeRepository;
	
	@Autowired
	private EntregaService entregaService;
	
	@Autowired
	private ArquivoService arquivoService;
	
	@Autowired
	private EntregaRepository entregaRepository;
	
	@Autowired
	private AlunoRepository alunoRepository;
	
	@Autowired
	private ProfessorService professorService;
	
	@Autowired
	private RecadoProfessorRepository recadoRepository;
	
	@GetMapping("/home")
	public String home(Model model) {
		Professor professor = SecurityUtils.getProfessorLogado();
		List<Curso> cursos = cursoRepository.findCursosEmAndamentoDoProfessor(professor.getId());
		List<Atividade> atividadesEmAberto = professorService.buscarAtividadesEmAbertoEmCursoProfessor(professor);
		model.addAttribute("cursos", cursos);
		model.addAttribute("atividadesEmAberto", atividadesEmAberto);
		return "professor-home";
	}
	
	@GetMapping("/atualizar")
	public String viewAtualizarProfessor(Model model) {
		Professor professor = SecurityUtils.getProfessorLogado();
		model.addAttribute("funcionario", professor);
		model.addAttribute("isProfessor", true);
		return "funcionario-atualizacao";
	}
	
	@PostMapping("/save")
	public String atualizarDadosProfessor(@ModelAttribute("funcionario") @Valid Professor professor, Errors errors, Model model) {
		if(!errors.hasErrors()) {
			try {
				professorService.save(professor);
				SecurityUtils.getLoggedUser().atualizarLoggedUser(professor);
				model.addAttribute("msgSucesso", "Professor atualizado com sucesso");
			} catch(ValidationException e) {
				errors.rejectValue("email", null, e.getMessage());
			}
		}
		model.addAttribute("isProfessor", true);
		return "funcionario-atualizacao";
	}
	
	@GetMapping("/curso")
	public String viewCurso(@RequestParam("curso") Long idCurso, Model model) {
		putDependenciesProfessorCursoOnModel(idCurso, model);
		return "professor-curso";
	}
	
	@PostMapping("/curso/atividades/nova")
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
	public String novaAtividade(@ModelAttribute("atividade") @Valid Atividade atividade, Errors errors, 
			@RequestParam(name = "recado",required = false) String recado,
			@RequestParam("idCurso") Long idCurso, Model model) {
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		putDependenciesProfessorCursoOnModel(curso.getId(), model);
		if(!errors.hasErrors()) {
			try { 
				atividadeService.novaAtividade(atividade, curso);
			} catch(ArquivoException e) {
				errors.rejectValue("atividadeFile", null, "Houve um erro ao salvar arquivo: " + e.getMessage());
				model.addAttribute("hasError", true);
				return "professor-curso";
			}
			if(recado != null && !recado.isBlank()) {
				recadoService.saveRecado(recado, SecurityUtils.getProfessorLogado(), curso);
			}
			model.addAttribute("msgSucesso", "Atividade " + atividade.getTitulo() + " adicionada com sucesso!");
			model.addAttribute("atividade", new Atividade());
			} else {
				model.addAttribute("hasError", true);
			}
		return "professor-curso";
	}
	
	@PostMapping("/curso/atividades/buscarAtividades")
	public String buscarAtividades(@ModelAttribute("atividadeFilter") AtividadeFilter atividadeFilter,
			@RequestParam("idCurso") Long idCurso, Model model) {
		if(model.containsAttribute("correcoesPendentes")) {
			model.addAttribute("correcoesPendentes", null);
		}
		List<Atividade> atividades = atividadeService.getAtividadesFiltradasProfessor(atividadeFilter);
		model.addAttribute("atividades", atividades);
		putCursoEAtividadeOnModel(model, idCurso);
		return "professor-curso";
	}
	
	@GetMapping("/curso/atividades/verAtividade")
	public String viewAtividade(@RequestParam("idAtividade") Long idAtividade, Model model) {
		Atividade atividade = atividadeRepository.findById(idAtividade).orElseThrow(NoSuchElementException::new);
		putDependenciesProfessorAtividadeOnModel(atividade, model);
		return "professor-atividade";
	}
	
	@PostMapping("/curso/atividades/verAtividade/atualizar")
	public String atualizarAtividade(@ModelAttribute("atividade") @Valid Atividade atividade,
			Errors errors,
			@RequestParam(name = "recado", required = false) String recado, Model model) {
		putDependenciesProfessorAtividadeOnModel(atividade, model);
		if(!errors.hasErrors()) {
			try {
				atividadeService.atualizarAtividade(atividade);
				model.addAttribute("msgSucesso", "Atividade atualizada com sucesso!");
				if(recado != null && !recado.isBlank()) {
					Professor professor = SecurityUtils.getProfessorLogado();
					recadoService.saveRecado(recado, professor, atividade.getCurso());
				}
			} catch(ApplicationException e) {
				errors.rejectValue("atividadeFile", null, "Houve um erro ao fazer upload do arquivo: " + e.getMessage());
			}
		}
		return "professor-atividade";
	}
	
	@PostMapping("/curso/atividades/verAtividade/corrigir")
	public String corrigirAtividade(@RequestParam("idAluno") Long idAluno, @RequestParam("idAtividade") Long idAtividade,
			@RequestParam("nota") Double nota, @RequestParam(name = "arquivoCorrecao", required = false) MultipartFile arquivoCorrecao,
			@RequestParam(name = "pageCurso", required = false) Boolean isReturnPageCurso, Model model) {
		Atividade atividade = atividadeRepository.findById(idAtividade).orElseThrow(NoSuchElementException::new);
		Aluno aluno = alunoRepository.findById(idAluno).orElseThrow(NoSuchElementException::new);
		Entrega entrega = entregaRepository.findById(new EntregaPK(atividade, aluno)).orElseThrow(NoSuchElementException::new);
		entrega.setNota(nota);
		if(!arquivoCorrecao.isEmpty() && arquivoCorrecao.getSize() > 0) {
			entrega.setArquivoCorrecao(arquivoCorrecao);
		}
		try {
			professorService.corrigirAtividade(entrega);
			model.addAttribute("msgSucesso", "Atividade corrigida com sucesso!");
		} catch(ValidationException | ApplicationException e) {
			model.addAttribute("msgErro", e.getMessage());
		}
		putDependenciesProfessorAtividadeOnModel(atividade, model);
		if(isReturnPageCurso != null && isReturnPageCurso) {
			putDependenciesProfessorCursoOnModel(entrega.getId().getAtividade().getCurso().getId(), model);
			return "professor-curso";
		} else {
			return "professor-atividade";
		}
	}
	
	@GetMapping("/curso/atividades/verAtividade/baixarAtividadeAluno")
	public String baixarEntregaAtividadeAluno(@RequestParam("idAluno") Long idAluno,
			@RequestParam("idAtividade") Long idAtividade, HttpServletResponse response, Model model) {
		Atividade atividade = atividadeRepository.findById(idAtividade).orElseThrow(NoSuchElementException::new);
		Aluno aluno = alunoRepository.findById(idAluno).orElseThrow(NoSuchElementException::new);
		Entrega entrega = entregaRepository.findById(new EntregaPK(atividade, aluno)).orElseThrow(NoSuchElementException::new);
		try {
			if( entrega.getNomeArquivoEntrega() == null || entrega.getNomeArquivoEntrega().isBlank()) {
				throw new ArquivoException("Nenhum arquivo foi entregado!");
			}
			arquivoService.downloadArquivoEntrega(response, entrega);
		} catch(IOException  | ArquivoException e) {
			model.addAttribute("atividade", atividade);
			model.addAttribute("msgErro", "Houve um erro ao baixar arquivo: " + e.getMessage());
			putDependenciesProfessorAtividadeOnModel(atividade, model);
			return "professor-atividade";
		}
		return null;
	}
	
	@GetMapping("/curso/recados")
	public String viewRecados(@RequestParam("curso") Long idCurso, Model model) {
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		putDependenciesProfessorRecadosOnModel(curso, model);
		return "professor-recados";
	}
	
	@PostMapping("/curso/recados/adicionar")
	public String novoRecado(@RequestParam("idCurso") Long idCurso, @RequestParam("corpo") String corpo, Model model) {
		Professor professor = SecurityUtils.getProfessorLogado();
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		recadoService.saveRecado(corpo, professor, curso);
		putDependenciesProfessorRecadosOnModel(curso, model);
		return "professor-recados";
	}
	
	@GetMapping("curso/recados/excluir")
	public String excluirRecado(@RequestParam("recado") Long idRecado, Model model) {
		RecadoProfessor recado = recadoRepository.findById(idRecado).orElseThrow(NoSuchElementException::new);
		recadoRepository.delete(recado);
		recado.getCurso().getRecados().remove(recado);
		putDependenciesProfessorRecadosOnModel(recado.getCurso(), model);
		return "professor-recados";
	}
	
	@GetMapping("/curso/alunos")
	public String viewAlunos(@RequestParam("curso") Long idCurso,
			@ModelAttribute("atividadesEntregues") List<AtividadeEntregaDTO> atividadesEntregues, Model model) {
		putCursoOnModel(idCurso, model);
		model.addAttribute("entregaFilter", new EntregaFilterDTO());
		atividadesEntregues = null;
		model.addAttribute("atividadesEntregues", atividadesEntregues);
		return "professor-alunos";
	}
	
	@GetMapping("/curso/fecharNotas")
	public String fecharNotas(@RequestParam("curso") Long idCurso, 
			@ModelAttribute("atividadesEntregues") List<AtividadeEntregaDTO> atividadesEntregues, Model model) {
		boolean fechou = professorService.fecharNotas(idCurso);
		if(!fechou) {
			model.addAttribute("msgErro", "Ainda há atividades para serem corrigidas");
		} else {
			model.addAttribute("msgSucesso", "Médias fechadas com sucesso! A situação dos alunos foram definidas.");
		}
		putCursoOnModel(idCurso, model);
		model.addAttribute("entregaFilter", new EntregaFilterDTO());
		atividadesEntregues = null;
		model.addAttribute("atividadesEntregues", atividadesEntregues);
		return "professor-alunos";
	}
	
	@PostMapping("/curso/alunos/buscarAluno")
	public String buscarAluno(@ModelAttribute("entregaFilter") EntregaFilterDTO filter,
			@RequestParam("idCurso") Long idCurso, 
			@ModelAttribute("atividadesEntregues") List<AtividadeEntregaDTO> atividadesEntregues, Model model) {
		filter.setIdCurso(idCurso);
		atividadesEntregues = entregaService.buscarAlunoPor(filter);
		model.addAttribute("atividadesEntregues", atividadesEntregues);
		putCursoOnModel(filter.getIdCurso(), model);
		return "professor-alunos";
	}
	
	@GetMapping("/curso/alunos/editar")
	public String editarMatricula(@RequestParam("aluno") Long idAluno, @RequestParam("curso") Long idCurso, Model model) {
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		Aluno aluno = alunoRepository.findById(idAluno).orElseThrow(NoSuchElementException::new);
		AtividadeEntregaDTO alunoNotas = entregaService.getEntregaNotasAlunoPorCurso(aluno, curso);
		model.addAttribute("editMode", true);
		putCursoOnModel(idCurso, model);
		model.addAttribute("entregaFilter", new EntregaFilterDTO());
		model.addAttribute("alunoNotas", alunoNotas);
		return "professor-alunos";
	}
	
	@PostMapping("/curso/alunos/editar/atualizar")
	public String atualizarNotasAluno(@ModelAttribute("alunoNotas") AtividadeEntregaDTO alunoNotas,
			@ModelAttribute("atividadesEntregues") List<AtividadeEntregaDTO> atividadesEntregues, Model model) {
		professorService.atualizarNotasAluno(alunoNotas);
		model.addAttribute("msgSucessoAtualizar", "Dados atualizados com sucesso");
		putCursoOnModel(alunoNotas.getIdCurso(), model);
		model.addAttribute("entregaFilter", new EntregaFilterDTO());
		model.addAttribute("atividadesEntregues", atualizarListaAtividadesEntregues(atividadesEntregues, alunoNotas));
		return "professor-alunos";
	}
	
	@GetMapping("/curso/atividades/verAtividade/excluir")
	public String excluirAtividade(@RequestParam("idAtividade") Long idAtividade, Model model) {
		Atividade atividade = atividadeRepository.findById(idAtividade).orElseThrow(NoSuchElementException::new);
		try {
			atividadeService.excluirAtividade(atividade);
		} catch(Exception e) {
			model.addAttribute("msgErro", "Houve um erro ao excluir atividade: " + e.getMessage());
			putDependenciesProfessorAtividadeOnModel(atividade, model);
			return "professor-atividade";
		}
		putDependenciesProfessorCursoOnModel(atividade.getCurso().getId(), model);
		model.addAttribute("msgSucesso", "A atividade foi excluída com sucesso!");
		return "professor-curso";
	}
	
	@ModelAttribute("atividadesEntregues")
	public List<AtividadeEntregaDTO> atividadesEntregues(){
		return new ArrayList<>();
	}
	
	private void putCursoOnModel(Long idCurso, Model model) {
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		model.addAttribute("curso", curso);
	}
	
	private void putDependenciesProfessorRecadosOnModel(Curso curso, Model model) {
		putCursoOnModel(curso.getId(), model);
		ControllerHelper.getRecadosAndPutOnModel(curso, model, false);
	}
	
	private void putCursoEAtividadeOnModel(Model model, Long idCurso) {
		putCursoOnModel(idCurso, model);
		model.addAttribute("atividade", new Atividade());
	}
	
	private void putDependenciesProfessorCursoOnModel(Long idCurso, Model model) {
		putCursoEAtividadeOnModel(model, idCurso);
		model.addAttribute("atividade", new Atividade());
		model.addAttribute("atividadeFilter", new AtividadeFilter());
		List<Entrega> atividadesEntregues = entregaRepository.findEntregasSemCorrecaoPorCurso(idCurso);
		model.addAttribute("correcoesPendentes", atividadesEntregues);
	}
	
	private void putDependenciesProfessorAtividadeOnModel(Atividade atividade, Model model) {
		model.addAttribute("atividade", atividade);
		List<Entrega> atividadesEntregues = entregaService.entregasSemCorrecaoAtividade(atividade);
		model.addAttribute("atividadesEntregues", atividadesEntregues);
	}
	
	private List<AtividadeEntregaDTO> atualizarListaAtividadesEntregues(List<AtividadeEntregaDTO> atividadesEntregues, AtividadeEntregaDTO alunoNota){
		List<Double> notas = alunoNota.getEntregasNotas().stream()
				.map(e -> e.getNota())
				.sorted((n1, n2) -> -n1.compareTo(n2)).collect(Collectors.toList());
		Aluno aluno = alunoRepository.findById(alunoNota.getIdAluno()).orElseThrow(NoSuchElementException::new);
		Curso curso = cursoRepository.findById(alunoNota.getIdCurso()).orElseThrow(NoSuchElementException::new);
		atividadesEntregues.stream().filter(n -> n.getIdAluno().equals(alunoNota.getIdAluno()))
									.forEach(n -> {
										n.getNotas().clear();
										n.getNotas().addAll(notas);
										n.setSituacaoAluno(alunoNota.getSituacaoAluno());
										n.setMedia(entregaService.calcularMediaAluno(aluno, curso));
									});
		return atividadesEntregues;
	}

}

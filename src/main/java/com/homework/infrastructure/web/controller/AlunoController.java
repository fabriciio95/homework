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
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.aluno.application.service.AlunoService;
import com.homework.domain.aluno.application.service.ArquivoException;
import com.homework.domain.aluno.application.service.ArquivoService;
import com.homework.domain.aluno.application.service.AtividadeService;
import com.homework.domain.aluno.application.service.CursoService;
import com.homework.domain.aluno.application.service.EntregaService;
import com.homework.domain.aluno.application.service.MatriculaNaoEncontradaException;
import com.homework.domain.aluno.application.service.ValidationException;
import com.homework.domain.atividade.Atividade;
import com.homework.domain.atividade.AtividadeFilter;
import com.homework.domain.atividade.AtividadeFilter.StatusAtividadeFilter;
import com.homework.domain.atividade.AtividadeRepository;
import com.homework.domain.atividade.Entrega;
import com.homework.domain.atividade.EntregaPK;
import com.homework.domain.atividade.EntregaRepository;
import com.homework.domain.curso.Curso;
import com.homework.domain.curso.CursoFilter;
import com.homework.domain.curso.CursoRepository;
import com.homework.domain.recado.Recado;
import com.homework.utils.SecurityUtils;

@Controller
@RequestMapping("/aluno")
public class AlunoController {

	@Autowired
	private AlunoService alunoService;

	@Autowired
	private CursoService cursoService;

	@Autowired
	private CursoRepository cursoRepository;

	@Autowired
	private AtividadeService atividadeService;

	@Autowired
	private EntregaService entregaService;

	@Autowired
	private AtividadeRepository atividadeRepository;

	@Autowired
	private ArquivoService arquivoService;
	
	@Autowired
	private EntregaRepository entregaRepository;

	@GetMapping("/home")
	public String home() {
		return "aluno-home";
	}

	@GetMapping("/atualizar")
	public String atualizar(Model model) {
		Aluno aluno = SecurityUtils.getAlunoLogado();
		model.addAttribute("aluno", aluno);
		return "aluno-cadastro";
	}

	@PostMapping("/save")
	public String atualizar(@ModelAttribute("aluno") @Valid Aluno aluno, Errors errors, Model model) {
		try {
			if (!errors.hasErrors()) {
				alunoService.save(aluno);
				SecurityUtils.getLoggedUser().atualizarLoggedUser(aluno);
				model.addAttribute("msgSucesso", "Aluno atualizado com sucesso!");
			}
		} catch (ValidationException e) {
			errors.rejectValue("email", null, e.getMessage());
		}
		return "aluno-cadastro";
	}

	@GetMapping("/cursos/novos")
	public String viewNovosCursos(Model model) {
		model.addAttribute("cursoFilter", new CursoFilter());
		return "aluno-novos-cursos";
	}

	@GetMapping("/curso/buscar")
	public String buscarCursos(@ModelAttribute("cursoFilter") CursoFilter filter, Model model) {
		model.addAttribute("cursos", cursoService.buscarCursos(filter));
		return "aluno-novos-cursos";
	}

	@GetMapping("/matricula/permissao")
	public String permissaoMatricula(@RequestParam("idCurso") Long idCurso, Model model) {
		Boolean solicitacaoConfirmada = cursoService.solicitarMatricula(idCurso);
		if (solicitacaoConfirmada == null) {
			model.addAttribute("msgPermissaoMatricula", "Infelizmente já acabou as vagas de matrículas neste curso!");
		} else if (solicitacaoConfirmada) {
			model.addAttribute("msgPermissaoMatricula",
					"Seu pedido foi para análise. Você receberá uma resposta por e-mail assim que algum coordenador permitir a sua matrícula!");
		} else {
			model.addAttribute("msgPermissaoMatricula",
					"Você já solicitou uma permissão para se matricular neste curso! Verifique seu e-mail ou entre em contato com o coordenador!");
		}
		model.addAttribute("cursoFilter", new CursoFilter());
		return "aluno-novos-cursos";
	}

	@PostMapping("/matricula/confirmar")
	public String confirmarMatricula(@RequestParam("idCurso") Long idCurso, @RequestParam("chave") String chave,
			Model model) {
		try {
			Boolean matriculou = alunoService.confirmarMatricula(idCurso, chave);
			if (matriculou == null) {
				model.addAttribute("msgErro", "Seu pedido ainda está em análise!");
			} else if (matriculou) {
				model.addAttribute("msgSucesso", "Matricula realizada com sucesso!");
			} else {
				model.addAttribute("msgErro", "Chave incorreta!");

			}
		} catch (MatriculaNaoEncontradaException e) {
			model.addAttribute("msgErro", e.getMessage());
		}
		model.addAttribute("cursoFilter", new CursoFilter());
		return "aluno-novos-cursos";
	}

	@GetMapping("/cursos")
	public String meusCursos(Model model) {
		List<Curso> cursos = alunoService.getCursosMatriculados();
		model.addAttribute("cursos", cursos);
		return "aluno-meus-cursos";
	}

	@GetMapping("/cursos/verCurso")
	public String viewCurso(@RequestParam("curso") Long idCurso, Model model) {
		putDependenciesOnPageAlunoCurso(model, idCurso);
		return "aluno-curso";
	}

	@GetMapping("/cursos/verCurso/recados")
	public String viewCursoRecados(@RequestParam("curso") Long idCurso, Model model) {
		Curso curso = cursoRepository.findById(idCurso).orElse(null);
		model.addAttribute("curso", curso);
		getRecadosAndPutOnModel(curso, model, false);
		return "aluno-recados";
	}

	@PostMapping("/cursos/verCurso/buscarAtividades")
	private String buscarAtividades(@ModelAttribute("atividadeFilter") AtividadeFilter filter,
			@RequestParam("idCurso") Long idCurso, Model model) {
		List<?> atividades = new ArrayList<>();
		String nameAtributeModel = "";
		filter.setIdCurso(idCurso);
		Curso curso = cursoRepository.findById(filter.getIdCurso()).orElse(null);
		if (!filter.getStatusAtividade().equals(StatusAtividadeFilter.ENTREGUE)) {
			atividades = atividadeService.getAtividadesFiltradas(filter);
			nameAtributeModel = "atividades";
		} else if (filter.getStatusAtividade().equals(StatusAtividadeFilter.ENTREGUE)) {
			atividades = entregaService.getAtividadesEntreguesFiltradas(filter, curso);
			nameAtributeModel = "atividadesEntregues";
		}
		model.addAttribute(nameAtributeModel, atividades);
		model.addAttribute("curso", curso);
		model.addAttribute("firstTime", false);
		getRecadosAndPutOnModel(curso, model, true);
		return "aluno-curso";
	}

	@PostMapping("/cursos/verCurso/verAtividade")
	public String viewAtividade(@RequestParam("idAtividade") Long idAtividade, Model model) {
		Atividade atividade = atividadeRepository.findById(idAtividade).orElseThrow(NoSuchElementException::new);
		Aluno aluno = SecurityUtils.getAlunoLogado();
		if (entregaService.isAtividadeEntregue(atividade, aluno)) {
			model.addAttribute("msgAlerta",
					"Você já entregou essa atividade, e não é possível visualizar uma atividade já entregue!");
			putDependenciesOnPageAlunoCurso(model, atividade.getCurso().getId());
			return "aluno-curso";
		}
		model.addAttribute("atividade", atividade);
		model.addAttribute("entrega", new Entrega());
		return "aluno-atividade";
	}

	@GetMapping("/cursos/verCurso/verAtividade/baixar")
	public String baixarArquivoAtividade(@RequestParam("idAtividade") Long idAtividade,
			@RequestParam(name = "pageCurso", required = false) Boolean isPageCurso,
			HttpServletResponse response,
			Model model) throws IOException {
			Atividade atividade = atividadeRepository.findById(idAtividade).orElseThrow(NoSuchElementException::new);
			try {
			model.addAttribute("atividade", atividade);
			model.addAttribute("entrega", new Entrega());
			if (atividade.getNomeArquivo() == null) {
				model.addAttribute("msgErro", "Essa atividade não possui nenhum arquivo para ser baixado!");
				if(isPageCurso != null && isPageCurso) {
					putDependenciesOnPageAlunoCurso(model, atividade.getCurso().getId());
					return "aluno-curso";
				} else {
					return "aluno-atividade";
				}
			}
			arquivoService.downloadArquivoAtividade(response, atividade);
		} catch (Exception e) {
			model.addAttribute("msgErro", "Houve um erro ao fazer download do arquivo: " + e.getMessage());
			if(isPageCurso != null && isPageCurso) {
				putDependenciesOnPageAlunoCurso(model, atividade.getCurso().getId());
				return "aluno-curso";
			} else {
				return "aluno-atividade";
			}
		}
		return null;
	}
	
	@GetMapping("/cursos/verCurso/verAtividade/baixarCorrecao")
	public String baixarArquivoCorrecao(@RequestParam("idAtividade") Long idAtividade,
			HttpServletResponse response,
			Model model) {
		Atividade atividade = atividadeRepository.findById(idAtividade).orElseThrow(NoSuchElementException::new);
		Aluno aluno = SecurityUtils.getAlunoLogado();
		Entrega entrega = entregaRepository.findById(new EntregaPK(atividade, aluno)).orElse(null);
		if(entrega != null) {
			try {
				model.addAttribute("atividade", atividade);
				model.addAttribute("entrega", new Entrega());
				putDependenciesOnPageAlunoCurso(model, atividade.getCurso().getId());
				if (entrega.getNomeArquivoCorrecao() == null) {
					model.addAttribute("msgErro", "Essa atividade não possui nenhum arquivo de correção para ser baixado!");
					return "aluno-curso";
				}
				arquivoService.downloadArquivoCorrecao(response, entrega);
			} catch (Exception e) {
				model.addAttribute("msgErro", "Houve um erro ao fazer download do arquivo: " + e.getMessage());
				return "aluno-curso";
			}
		} else {
			model.addAttribute("msgErro", "Essa atividade ainda não foi entregue!");
			return "aluno-curso";
		}
		return null;
	}
	
	
	@PostMapping("/cursos/verCurso/verAtividade/entregar")
	public String entregarAtividade(@ModelAttribute("entrega") Entrega entrega,
			@RequestParam("idAtividade") Long idAtividade, Model model) {
		Atividade atividade = atividadeRepository.findById(idAtividade).orElseThrow(NoSuchElementException::new);
		try {
			entregaService.entregarAtividade(idAtividade, entrega);
		} catch(ArquivoException e) {
			model.addAttribute("msgErro", "Houve um erro ao entregar atividade: " + e.getMessage());
			model.addAttribute("atividade", atividade);
			return "aluno-atividade";
		}
		model.addAttribute("msgSucesso", "Atividade foi entregue com sucesso!");
		putDependenciesOnPageAlunoCurso(model, atividade.getCurso().getId());
	   return "aluno-curso";
	}
	
	private void putDependenciesOnPageAlunoCurso(Model model, Long idCurso) {
		Curso curso = cursoRepository.findById(idCurso).orElse(null);
		model.addAttribute("curso", curso);
		getRecadosAndPutOnModel(curso, model, true);
		model.addAttribute("atividadeFilter", new AtividadeFilter());
		List<Atividade> atividades = atividadeRepository.findByCurso_Id(curso.getId());
		List<Atividade> atividadesPendentes = atividadeService.filtrarAtividadesPendentes(atividades);
		model.addAttribute("titleAtividadesPendentes", "Atividades Pendentes");
		model.addAttribute("firstTime", true);
		model.addAttribute("atividades", atividadesPendentes);
	}

	private List<Recado> getRecadosAndPutOnModel(Curso curso, Model model, boolean isWithLimit) {
		List<Recado> recados;
		if (isWithLimit) {
			recados = curso.getRecados().stream().sorted((r1, r2) -> -r1.getData().compareTo(r2.getData())).limit(3)
					.collect(Collectors.toList());
		} else {
			recados = curso.getRecados().stream().sorted((r1, r2) -> -r1.getData().compareTo(r2.getData()))
					.collect(Collectors.toList());
		}
		model.addAttribute("recados", recados);
		return recados;
	}
}

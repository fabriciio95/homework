package com.homework.infrastructure.web.controller;

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
import com.homework.domain.aluno.application.service.CursoService;
import com.homework.domain.aluno.application.service.MatriculaNaoEncontradaException;
import com.homework.domain.aluno.application.service.ValidationException;
import com.homework.domain.curso.CursoFilter;
import com.homework.utils.SecurityUtils;

@Controller
@RequestMapping("/aluno")
public class AlunoController {
	
	@Autowired
	private AlunoService alunoService;
	
	@Autowired
	private CursoService cursoService;

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
			if(!errors.hasErrors()) {
				alunoService.save(aluno);
				SecurityUtils.getLoggedUser().atualizarLoggedUser(aluno);
				model.addAttribute("msgSucesso", "Aluno atualizado com sucesso!");
			}
		} catch (ValidationException e) {
			errors.rejectValue("email", null, e.getMessage());
		}
		return "aluno-cadastro";
	}
	
	@GetMapping("/curso/novos")
	public String viewNovosCursos(Model model) {
		model.addAttribute("cursoFilter", new CursoFilter());
		return "aluno-novos-cursos";
	}
	
	@GetMapping("/curso/buscar")
	public String buscarCursos(@ModelAttribute("cursoFilter") CursoFilter filter,Model model) {
		model.addAttribute("cursos", cursoService.buscarCursos(filter));
		return "aluno-novos-cursos";
	}
	
	@GetMapping("/matricula/permissao")
	public String permissaoMatricula(@RequestParam("idCurso") Long idCurso,Model model) {
		Boolean solicitacaoConfirmada = cursoService.solicitarMatricula(idCurso);
		if(solicitacaoConfirmada == null) {
			model.addAttribute("msgPermissaoMatricula", "Infelizmente já acabou as vagas de matrículas neste curso!");
		} else if(solicitacaoConfirmada) {
			model.addAttribute("msgPermissaoMatricula", "Seu pedido foi para análise. Você receberá uma resposta por e-mail assim que algum coordenador permitir a sua matrícula!");
		} else {
			model.addAttribute("msgPermissaoMatricula", "Você já solicitou uma permissão para se matricular neste curso! Verifique seu e-mail ou entre em contato com o coordenador!");
		}
		model.addAttribute("cursoFilter", new CursoFilter());
		return "aluno-novos-cursos";
	}
	
	@PostMapping("/matricula/confirmar")
	public String confirmarMatricula(@RequestParam("idCurso") Long idCurso, @RequestParam("chave") String chave, Model model) {
		try {
			Boolean matriculou = alunoService.confirmarMatricula(idCurso, chave);
			if(matriculou == null) {
				model.addAttribute("msgErro", "Seu pedido ainda está em análise!");
			} else if(matriculou) { 
				model.addAttribute("msgSucesso", "Matricula realizada com sucesso!");
			} else {
				model.addAttribute("msgErro", "Chave incorreta!");
				
			} 
		} catch(MatriculaNaoEncontradaException e) {
			model.addAttribute("msgErro", e.getMessage());
		}	
		model.addAttribute("cursoFilter", new CursoFilter());
		return "aluno-novos-cursos";
	}
}

package com.homework.infrastructure.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.homework.domain.aluno.application.service.CoordenadorService;
import com.homework.domain.aluno.application.service.EmailException;
import com.homework.domain.curso.CursoAluno;

@Controller
@RequestMapping("/coordenador")
public class CoordenadorController {

	@Autowired
	private CoordenadorService coordenadorService;
	
	@GetMapping("/home")
	public String home() {
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
	
	private void colocarMatriculasNoModel(Model model) {
		List<CursoAluno> matriculas = coordenadorService.solicitacoesDeMatriculasPendentes();
		model.addAttribute("matriculas", matriculas);
		if(matriculas.isEmpty()) {
			model.addAttribute("msgPermissoesMatricula", "Não há novas solicitações de matrículas");
		}
	}
}

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

import com.homework.domain.aluno.Aluno;
import com.homework.domain.aluno.application.service.AlunoService;
import com.homework.domain.aluno.application.service.ValidationException;
import com.homework.utils.SecurityUtils;

@Controller
@RequestMapping("/aluno")
public class AlunoController {
	
	@Autowired
	private AlunoService alunoService;
	

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
}

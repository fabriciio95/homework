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

@Controller
@RequestMapping("/public")
public class PublicController {
	
	@Autowired
	private AlunoService alunoService;

	@GetMapping("/aluno/cadastro")
	public String cadastroAluno(Model model) {
		model.addAttribute("aluno", new Aluno());
		return "aluno-cadastro";
	}
	
	@PostMapping("/aluno/cadastrar")
	public String cadastrarAluno(@ModelAttribute("aluno") @Valid Aluno aluno, Errors errors, Model model) {
		try {
			if(!errors.hasErrors()) {
				alunoService.save(aluno);
				model.addAttribute("msgSucesso", "Aluno cadastrado com sucesso!");
				return "login";
			}
		} catch (ValidationException e) {
			errors.rejectValue("email", null, e.getMessage());
		}
		return "aluno-cadastro";
	}
}

package com.homework.infrastructure.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.homework.domain.aluno.Aluno;

@Controller
@RequestMapping("/aluno")
public class AlunoController {

	@GetMapping("/cadastro")
	public String cadastro(Model model) {
		model.addAttribute("aluno", new Aluno());
		return "aluno-cadastro";
	}
	
	@PostMapping("/save")
	public String save(@ModelAttribute("aluno") Aluno aluno, Model model) {
		model.addAttribute("msgSucesso", "Aluno cadastrado com sucesso!");
		return "login";
	}
}

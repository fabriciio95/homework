package com.homework.infrastructure.web.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ui.Model;

import com.homework.domain.curso.Curso;
import com.homework.domain.recado.Recado;

public class ControllerHelper {

	public static List<Recado> getRecadosAndPutOnModel(Curso curso, Model model, boolean isWithLimit) {
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

package com.homework.utils;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.coordenador.Coordenador;
import com.homework.domain.professor.Professor;
import com.homework.infrastructure.web.security.LoggedUser;

public class SecurityUtils {

	public static LoggedUser getLoggedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if(authentication instanceof AnonymousAuthenticationToken) {
			return null;
		}
		
		return (LoggedUser) authentication.getPrincipal();
	}
	
	public static Aluno getAlunoLogado() {
		LoggedUser loggedUser = getLoggedUser();
		if(loggedUser == null) {
			throw new IllegalStateException("Não existe um usuário logado!");
		}
		
		if(!(loggedUser.getUsuario() instanceof Aluno)) {
			throw new IllegalStateException("O usuário logado não é um aluno");
		}
		
		return (Aluno) loggedUser.getUsuario();
	}
	
	
	public static Professor getProfessorLogado() {
		LoggedUser loggedUser = getLoggedUser();
		
		if(loggedUser == null) {
			throw new IllegalStateException("Não existe um usuário logado");
		}
		
		if(!(loggedUser.getUsuario() instanceof Professor)) {
			throw new IllegalStateException("O usuário logado não é um professor");
		}
		
		return (Professor) loggedUser.getUsuario();
	}
	
	public static Coordenador getCoordenadorLogado() {
		LoggedUser loggedUser = getLoggedUser();
		
		if(loggedUser == null) {
			throw new IllegalStateException("Não existe um usuário logado");
		}
		
		if(!(loggedUser.getUsuario() instanceof Coordenador)) {
			throw new IllegalStateException("O usuário logado não é um coordenador");
		}
		
		return (Coordenador) loggedUser.getUsuario();
	}
}

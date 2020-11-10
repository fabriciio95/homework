package com.homework.infrastructure.web.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.homework.utils.SecurityUtils;

public class AuthenticationSucessHandlerImpl implements AuthenticationSuccessHandler{

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		Role role = SecurityUtils.getLoggedUser().getRole();
		
		if(role == Role.ALUNO) {
			response.sendRedirect("aluno/home");
		} else if(role == Role.PROFESSOR) {
			response.sendRedirect("professor/home");
		} else if(role == Role.COORDENADOR) {
			response.sendRedirect("coordenador/home");
		} else {
			throw new IllegalStateException("Erro na autenticação");
		}
	}

}

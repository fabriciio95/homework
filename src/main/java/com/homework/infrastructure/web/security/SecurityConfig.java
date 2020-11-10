package com.homework.infrastructure.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter{
	
	@Bean
	public AuthenticationSuccessHandler getAuthenticationSucessHandler() {
		return new AuthenticationSucessHandlerImpl();
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.csrf().disable()
		.authorizeRequests()
		.antMatchers("/images/**", "/css/**", "/javascript/**", "/public/**").permitAll()
		.antMatchers("/aluno/**").hasRole(Role.ALUNO.toString())
		.antMatchers("/professor/**").hasRole(Role.PROFESSOR.toString())
		.antMatchers("/coordenador/**").hasRole(Role.COORDENADOR.toString())
		.anyRequest().authenticated()
		.and()
			.formLogin()
			.loginPage("/login")
			.failureUrl("/login-error")
			.successHandler(getAuthenticationSucessHandler())
			.permitAll()
		.and()
			.logout()
			.logoutUrl("/logout")
			.permitAll();
		
	}
}

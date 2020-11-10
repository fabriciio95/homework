package com.homework.infrastructure.web.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.coordenador.Coordenador;
import com.homework.domain.professor.Professor;
import com.homework.domain.usuario.Usuario;

public class LoggedUser implements UserDetails{
	private static final long serialVersionUID = 1L;
	
	private Usuario usuario;
	private Role role;
	private Collection<? extends GrantedAuthority> roles;
	
	public LoggedUser(Usuario usuario) {
		this.usuario = usuario;
		if(usuario instanceof Aluno) {
			role = Role.ALUNO;
		} else if(usuario instanceof Professor) {
			role = Role.PROFESSOR;
		} else if(usuario instanceof Coordenador) {
			role = Role.COORDENADOR;
		} else {
			throw new IllegalStateException("O usuário não tem um perfil válido!");
		}
		
		this.roles = List.of(new SimpleGrantedAuthority("ROLE_" + role));
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return roles;
	}

	@Override
	public String getPassword() {
		return usuario.getSenha();
	}

	@Override
	public String getUsername() {
		return usuario.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	public Role getRole() {
		return role;
	}
	
	public Usuario getUsuario() {
		return usuario;
	}
	
	public void atualizarLoggedUser(Usuario usuario) {
		this.usuario.setNome(usuario.getNome());
		this.usuario.setEmail(usuario.getEmail());
	}
}

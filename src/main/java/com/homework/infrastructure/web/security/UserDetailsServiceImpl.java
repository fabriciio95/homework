package com.homework.infrastructure.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.homework.domain.aluno.AlunoRepository;
import com.homework.domain.coordenador.CoordenadorRepository;
import com.homework.domain.professor.ProfessorRepository;
import com.homework.domain.usuario.Usuario;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{
	
	@Autowired
	private AlunoRepository alunoRepository;
	
	@Autowired
	private ProfessorRepository professorRepository;
	
	@Autowired
	private CoordenadorRepository coordenadorRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario usuario = alunoRepository.findByEmail(username);
		
		if(usuario == null) {
			usuario = professorRepository.findByEmail(username);
			
			if(usuario == null) {
				usuario = coordenadorRepository.findByEmail(username);
			}
			
			if(usuario == null) {
				throw new UsernameNotFoundException("Usuário inválido!");
			}
		}
		
		return new LoggedUser(usuario);
	}

}

package com.homework.arquivo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.homework.domain.aluno.CertificadoAlunoDTO;
import com.homework.domain.aluno.application.service.ArquivoService;

@SpringBootTest
@ActiveProfiles("test")
public class TestArquivoService {

	@Autowired
	private ArquivoService arquivoService;
	
	@Test
	public void gerarCertificadoTest() throws Exception{
		CertificadoAlunoDTO certificado = new CertificadoAlunoDTO();
		certificado.setNomeAluno("Fabricio Siqueira Macedo");
		certificado.setNomeCurso("Lógica de Programação");
		certificado.setNomeProfessor("Alex Edgio");
		certificado.setDataConclusao("14/07/2020");
		certificado.setDataMatricula("10/03/2020");
		List<CertificadoAlunoDTO> certificados = new ArrayList<CertificadoAlunoDTO>();
		certificados.add(certificado);
	    arquivoService.gerarRelatorio(certificados, new HashMap<>(),
				"certificado", "certificado" + "Fabricio");
	}
}

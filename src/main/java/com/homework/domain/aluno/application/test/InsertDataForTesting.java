package com.homework.domain.aluno.application.test;


import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.aluno.AlunoRepository;
import com.homework.domain.aluno.application.service.ArquivoService;
import com.homework.domain.aluno.application.service.ProfessorService;
import com.homework.domain.aluno.application.service.ArquivoService.Dir;
import com.homework.domain.atividade.Atividade;
import com.homework.domain.atividade.Atividade.StatusAtividade;
import com.homework.domain.atividade.AtividadeRepository;
import com.homework.domain.atividade.Entrega;
import com.homework.domain.atividade.EntregaPK;
import com.homework.domain.atividade.EntregaRepository;
import com.homework.domain.coordenador.Coordenador;
import com.homework.domain.coordenador.CoordenadorRepository;
import com.homework.domain.coordenador.RecadoCoordenador;
import com.homework.domain.coordenador.RecadoCoordenadorRepository;
import com.homework.domain.curso.Curso;
import com.homework.domain.curso.Curso.CategoriaCurso;
import com.homework.domain.curso.CursoAluno;
import com.homework.domain.curso.CursoAluno.SituacaoAluno;
import com.homework.domain.curso.CursoAluno.StatusMatricula;
import com.homework.domain.curso.CursoAlunoPK;
import com.homework.domain.curso.CursoAlunoRepository;
import com.homework.domain.curso.CursoRepository;
import com.homework.domain.curso.StatusCurso;
import com.homework.domain.professor.Professor;
import com.homework.domain.professor.ProfessorRepository;
import com.homework.domain.professor.RecadoProfessor;
import com.homework.domain.professor.RecadoProfessorRepository;
import com.homework.domain.recado.Recado;
import com.homework.utils.RandomUtils;

@Component
public class InsertDataForTesting {


	@Autowired
	private AlunoRepository alunoRepository;
	
	@Autowired
	private ProfessorRepository professorRepository;
	
	@Autowired
	private CursoRepository cursoRepository;
	
	@Autowired
	private CoordenadorRepository coordenadorRepository;
	
	@Autowired
	private CursoAlunoRepository cursoAlunoRepository;
	
	@Autowired
	private RecadoProfessorRepository recadoProfessorRepository;
	
	@Autowired
	private RecadoCoordenadorRepository recadoCoordenadorRepository;
	
	@Autowired
	private AtividadeRepository atividadeRepository;
	
	@Autowired
	private EntregaRepository entregaRepository;
	
	@Autowired
	private ProfessorService professorService;
	
	@Autowired
	private ArquivoService arquivoService;
	
	
	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) throws IOException {
		Environment environment = event.getApplicationContext().getEnvironment();
		if(environment.acceptsProfiles(Profiles.of("dev")) || environment.acceptsProfiles(Profiles.of("prod"))){
			List<Aluno> alunos = getAlunos();
			List<Professor> professores = getProfessores();
			Coordenador coordenador = getCoordenador();
			List<Curso> cursos = getCursos(professores, coordenador);
			salvarRecados(cursos, professores, coordenador);
			salvarMatriculas(cursos, alunos);
			getAtividades(cursos);
			salvarEntregaDeAtividades();
		}
	}
	
	private void salvarRecados(List<Curso> cursos, List<Professor> professores, Coordenador coordenador) {
		for(Curso curso : cursos) {
			List<String> dados = getRecadosDoCurso(curso);
			int totalDeRecados = RandomUtils.random(0, 6);
			for(int i = 0; i <= totalDeRecados; i++) {
				if(dados.size() > 0) {
					int indexDado = RandomUtils.random(0, dados.size() - 1);
					String dado = dados.get(indexDado);
					String[] d = dado.split("/");
					Recado recado = null;
					boolean isCoordenador = Boolean.parseBoolean(d[2]);
					if(isCoordenador) {
						recado = new RecadoCoordenador();
						((RecadoCoordenador) recado).setAutor(cursos.get(2).getCoordenador());
					} else {
						recado = new RecadoProfessor();
						((RecadoProfessor) recado).setAutor(curso.getProfessor());
					}
					 
					recado.setCorpo(d[0]);
					recado.setCurso(curso);
					recado.setData(LocalDateTime.parse(d[1]));
					
					if(recado instanceof RecadoProfessor) {
						recadoProfessorRepository.save((RecadoProfessor) recado);
					} else {
						recadoCoordenadorRepository.save((RecadoCoordenador) recado);
					}
					dados.remove(indexDado);
				}
			}
		}
	}
	
	private List<String> getRecadosDoCurso(Curso curso){
		List<String> dados = new ArrayList<>();
		LocalDate dataInicioCurso = curso.getDataInicial();
		LocalDate dataFinalCurso = curso.getDataConclusao();
		String d1 = String.format("%s%s/%s/%s", "Sejam Bem vindos ao nosso curso de ", curso.getNome(), LocalDateTime.of(dataInicioCurso.getYear(), dataInicioCurso.getMonth(), dataInicioCurso.getDayOfMonth(), 10, 29).toString(), "true");
		String d2 = String.format("%s%s%s/%s/%s", "Olá pessoal, meu nome é  ", curso.getProfessor().getNome(), " e eu serei o instrutor de vocês neste curso, sejam todos muito bem-vindos.", LocalDateTime.of(dataInicioCurso.getYear(), dataInicioCurso.getMonth(), dataInicioCurso.getDayOfMonth(), 15, 12).toString(), "false");
		
		String d3 = String.format("%s/%s/%s", "Olá pessoal, fiquem ligados nas datas finais de entrega das atividades.", LocalDateTime.of(dataInicioCurso.getYear(), dataInicioCurso.getMonth(), dataInicioCurso.getDayOfMonth(), 20, 29).plusDays(25).toString(), "false");
		dados.add(d3);
		String d4 = String.format("%s/%s/%s", "Logo estarei adicionando novas atividades.", LocalDateTime.of(dataInicioCurso.getYear(), dataInicioCurso.getMonth(), dataInicioCurso.getDayOfMonth(), 9, 01).plusDays(3).toString(), "false");
		dados.add(d4);
		String d5 = String.format("%s/%s/%s", "Algumas atividades poderão ser entregue após a data final, porém será tirado alguns pontos dependendo da quantidade de dias de atraso.", LocalDateTime.of(dataInicioCurso.getYear(), dataInicioCurso.getMonth(), dataInicioCurso.getDayOfMonth(), 13, 03).plusDays(5).toString(), "false");
		dados.add(d5);
		String d6 = String.format("%s/%s/%s", "Ao entregar uma atividade, certifique-se de que foi anexado o arquivo pois uma vez que a atividade foi entregue, não poderá mais entregar novamente.", LocalDateTime.of(dataInicioCurso.getYear(), dataInicioCurso.getMonth(), dataInicioCurso.getDayOfMonth(), 18, 02).plusDays(27).toString(), "false");
		dados.add(d6);
		String d7 = String.format("%s/%s/%s", "Caprichem nas atividades pessoal, cada ponto lá na frente será importante.", LocalDateTime.of(dataInicioCurso.getYear(), dataInicioCurso.getMonth(), dataInicioCurso.getDayOfMonth(), 15, 29).plusDays(35).toString(), "false");
		dados.add(d7);
		String d8 = String.format("%s/%s/%s", "Para visualizar suas notas, acesse o menu \"Minhas notas\" disponível na página home", LocalDateTime.of(dataInicioCurso.getYear(), dataInicioCurso.getMonth(), dataInicioCurso.getDayOfMonth(), 16, 19).plusDays(9).toString(), "false");
		dados.add(d8);
		String d9 = String.format("%s/%s/%s", "Irei nos próximos dias disponibilizar as correções pendentes.", LocalDateTime.of(dataInicioCurso.getYear(), dataInicioCurso.getMonth(), dataInicioCurso.getDayOfMonth(), 17, 20).plusDays(40).toString(), "false");
		dados.add(d9);
		String d10 = String.format("%s/%s/%s", "Bom dia pessoal, dentro de alguns dias será liberado uma nova atividade.", LocalDateTime.of(dataInicioCurso.getYear(), dataInicioCurso.getMonth(), dataInicioCurso.getDayOfMonth(), 8, 01).plusDays(15).toString(), "false");
		dados.add(d10);
		String d11 = String.format("%s/%s/%s", "Lembrando que na próxima semana haverá live para tirar as dúvidas de vocês.", LocalDateTime.of(dataInicioCurso.getYear(), dataInicioCurso.getMonth(), dataInicioCurso.getDayOfMonth(), 17, 20).plusDays(50).toString(), "false");
		dados.add(d11);
		String d12 = String.format("%s/%s/%s", "Vamos lá pessoal, caprichem nas atividade pois já estamos entrando na reta final do curso.", LocalDateTime.of(dataFinalCurso.getYear(), dataFinalCurso.getMonth(), dataFinalCurso.getDayOfMonth(), 14, 20).minusDays(22).toString(), "false");
		dados.add(d12);
		String d13 = String.format("%s/%s/%s", "A partir de agora estamos entrando na parte final do curso e com isso o nível de dificuldade das atividades irão aumentar.", LocalDateTime.of(dataFinalCurso.getYear(), dataFinalCurso.getMonth(), dataFinalCurso.getDayOfMonth(), 18, 06).minusDays(35).toString(), "false");
		dados.add(d13);
		String d14 = String.format("%s/%s/%s", "Amanhã teremos uma live as 20h para tirar dúvidas sobre a matéria ou atividades.", LocalDateTime.of(dataFinalCurso.getYear(), dataFinalCurso.getMonth(), dataFinalCurso.getDayOfMonth(), 21, 10).minusDays(25).toString(), "false");
		dados.add(d14);
		String d15 = String.format("%s/%s/%s", "Não deixem de fazer as atividades pessoal, pois cada uma delas tem um peso importante para a média final.", LocalDateTime.of(dataFinalCurso.getYear(), dataFinalCurso.getMonth(), dataFinalCurso.getDayOfMonth(), 20, 10).minusDays(55).toString(), "false");
		dados.add(d15);
		String d16 = String.format("%s/%s/%s", "Estou liberando aos poucos as correções das ultimas atividades.", LocalDateTime.of(dataFinalCurso.getYear(), dataFinalCurso.getMonth(), dataFinalCurso.getDayOfMonth(), 10, 10).minusDays(41).toString(), "false");
		dados.add(d16);
		String d17 = String.format("%s/%s/%s", "A próxima atividade será liberada em breve.", LocalDateTime.of(dataFinalCurso.getYear(), dataFinalCurso.getMonth(), dataFinalCurso.getDayOfMonth(), 11, 15).minusDays(60).toString(), "false");
		dados.add(d17);
		String d18 = String.format("%s/%s/%s", "Curso está quase em seu final, vamos lá pessoal mantenham o foco!", LocalDateTime.of(dataFinalCurso.getYear(), dataFinalCurso.getMonth(), dataFinalCurso.getDayOfMonth(), 12, 13).minusDays(13).toString(), "false");
		dados.add(d18);
		String d19 = String.format("%s/%s/%s", "\"A vontade de se preparar tem que ser maior do que a vontade de vencer, e a vitória é a consequência de uma boa preparação\"", LocalDateTime.of(dataFinalCurso.getYear(), dataFinalCurso.getMonth(), dataFinalCurso.getDayOfMonth(), 7, 10).minusDays(30).toString(), "false");
		dados.add(d19);
		String d20 = String.format("%s/%s/%s", "Com o nosso curso chegando ao seu final, gostaria de agradecer a todos por todo empenho e dedicação. Desejo a vocês muito sucesso no futuro, até a próxima!!!", LocalDateTime.of(dataFinalCurso.getYear(), dataFinalCurso.getMonth(), dataFinalCurso.getDayOfMonth(), 16, 16).toString(), "false");
		dados.add(d20);
		
		
		String d21 = String.format("%s/%s/%s", "Para calcular a média final, irá ser somada a notas de todas as atividades e então será feita a média simples, ou seja, a soma de todas as notas das atividades será dividida pela quantidade de atividades.", LocalDateTime.of(dataInicioCurso.getYear(), dataInicioCurso.getMonth(), dataInicioCurso.getDayOfMonth(), 11, 10).plusDays(2).toString(), "true");
		dados.add(d21);
		String d22 = String.format("%s/%s/%s", "A definição da situação de cada aluno será disponibilizada após a correção de todas as atividades e fechamento das notas pelo professor.", LocalDateTime.of(dataFinalCurso.getYear(), dataFinalCurso.getMonth(), dataFinalCurso.getDayOfMonth(), 13, 21).minusDays(5).toString(), "true");
		dados.add(d22);
		String d23 = String.format("%s/%s/%s", "Ao final do curso se, e somente se, o aluno for aprovado o certificado será disponibilizado de forma imediata para download.", LocalDateTime.of(dataFinalCurso.getYear(), dataFinalCurso.getMonth(), dataFinalCurso.getDayOfMonth(), 15, 21).minusDays(2).toString(), "true");
		dados.add(d23);
		String d24 = String.format("%s/%s/%s", "Dúvida sobre as notas ou a situação do aluno, por favor entrar em contato por e-mail mcursos12@gmail.com e informar seu nome, curso matriculado e a dúvida.", LocalDateTime.of(dataFinalCurso.getYear(), dataFinalCurso.getMonth(), dataFinalCurso.getDayOfMonth(), 22, 21).minusDays(2).toString(), "true");
		dados.add(d24);
		String d25 = String.format("%s/%s/%s", "Curso acabando com a certeza de que evoluímos com nosso conhecimento, queria agradecer a todos alunos e professor por esse tempo passado. Com certeza os alunos irão colher os frutos no futuro, muito obrigado a todos desejo uma boa sorte, até mais!", LocalDateTime.of(dataFinalCurso.getYear(), dataFinalCurso.getMonth(), dataFinalCurso.getDayOfMonth(), 20, 21).toString(), "true");
		dados.add(d25);
		dados = dados.stream().filter(dado -> LocalDateTime.parse(dado.split("/")[1]).isBefore(LocalDateTime.now())
				|| LocalDateTime.parse(dado.split("/")[1]).equals(LocalDateTime.now())).collect(Collectors.toList());
		
		
		if(curso.getDataInicial().isBefore(LocalDate.now()) || curso.getDataInicial().equals(LocalDate.now())) {
			dados.add(d1);
			dados.add(d2);
		}
		return dados;
	}

	private List<Aluno> getAlunos() {
		List<Aluno> alunos = new ArrayList<>();
		List<String> dados = getDadosAlunos();
		for (String dado : dados) {
			String[] d = dado.split("/");
			Aluno aluno = new Aluno();
			aluno.setNome(d[0]);
			aluno.setEmail(d[1]);
			aluno.setSenha("a");
			aluno.criptografarSenha();
			alunoRepository.save(aluno);
			alunos.add(aluno);
		}
		return alunos;
	}
	
	private List<String> getDadosAlunos(){
		List<String> dados = new ArrayList<>();
		dados.add("Claudia Monteiro/claudia.monteiro@gmail.com");
		dados.add("Rogerio Silva/rogerio.silva@gmail.com");
		dados.add("Matheus Costa/matheus.costa@gmail.com");
		dados.add("Maria Madalena/maria.madalena@gmail.com");
		dados.add("Anastácia Costa/anastacia.costa@hotmail.com");
		dados.add("Diego Ribas/diego.ribas@bol.com.br");
		dados.add("Michael Oliver/michael.oliver@yahoo.com");
		dados.add("Pedro Felipe/pedro.felipe@hotmail.com");
		dados.add("Douglas Fernandez/douglas.fernandez@gmail.com");
		dados.add("Karina Lima/karina.lima@gmail.com");
		dados.add("Crisógono Pereira/crisogono.pereira@gmail.com");
		dados.add("Benedita Amada/benedita.amada@hotmail.com");
		dados.add("Leonardo Oliveira/leo.oliveira@gmail.com");
		dados.add("Leonardo Michael/leonardo.michael@hotmail.com");
		dados.add("Beatriz Lobo/beatriz.lobo@gmail.com");
		dados.add("Oliver Renato/oliver.renato@gmail.com");
		dados.add("Antonio Lima/antonio.lima@gmail.com");
		dados.add("Éverson Marques /everson.marques@hotmail.com");
		dados.add("Éder Miguel/eder.miguel@gmail.com");
		dados.add("Miguel Silva/miguel.silva@gmail.com");
		dados.add("Sofia Silva/sofia.silva@hotmail.com");
		dados.add("Talita Marques/talita.marques@gmail.com");
		dados.add("Marcela Liberato/marcela.liberato@gmail.com");
		dados.add("Nélio Faro/nelio.faro@hotmail.com");
		dados.add("Elisandra Silva/elisandra.silva@gmail.com");
		dados.add("Giselle Pereira/giselle.pereira@gmail.com");
		dados.add("Adelly Macedo/adelly.macedo@hotmail.com");
		dados.add("Janaina Utiama/janaina.utiama@hotmail.com");
		dados.add("Alisson Junior/alisson.junior@gmail.com");
		dados.add("Enrick Mereiles/enrick.meireles@gmail.com");
		dados.add("Victor Inácio/victor.inacio@hotmail.com");
		
		dados.add("Milton Cruz/milton.cruz@gmail.com");
		dados.add("Rafael Moura/rafael.moura@gmail.com");
		dados.add("Yago Felipe/yago.felipe@gmail.com");
		dados.add("Richard Alan/richard.alan@gmail.com");
		dados.add("Heitor Torres/heitor.torres@hotmail.com");
		dados.add("Gabriel Barbosa/gabriel.barbosa@bol.com.br");
		dados.add("Hugo Silva/hugo.silva@yahoo.com");
		dados.add("Rhuan Menezes/rhuan.menezes@hotmail.com");
		dados.add("Douglas Costa/douglas.costa@gmail.com");
		dados.add("Marta Sofia/marta.sofia@gmail.com");
		dados.add("Tiago Benedito/tiago.benedito@gmail.com");
		dados.add("Juliana Gonçalves/juliana.goncalvez@hotmail.com");
		dados.add("Leonardo Moura/leonardo.moura@gmail.com");
		dados.add("Leonardo Gonçalvez/leonardo.goncalves@hotmail.com");
		dados.add("Beatriz Lima/beatriz.lima@gmail.com");
		dados.add("Oliver Silvestre/oliver.silvestre@gmail.com");
		dados.add("Antonio Miguel/antonio.miguel@gmail.com");
		dados.add("Miguel Marques /miguel.marques@hotmail.com");
		dados.add("Fabio Nazário/fabio.nazario@gmail.com");
		dados.add("Ronaldo Cruz/ronaldo.cruz@gmail.com");
		dados.add("Michelli Martins/michelli.martins@hotmail.com");
		dados.add("Débora Macedo/debora.macedo@gmail.com");
		dados.add("Rafaella Gewieviski/rafaella.gewieviski@gmail.com");
		dados.add("Phelipe Fonseca/phelipe.fonseca@hotmail.com");
		dados.add("Daniel Medeiros/daniel.medeiros@gmail.com");
		dados.add("Aline Gabrielli/aline.gabrielli@gmail.com");
		dados.add("Jessica Matta/jessica.matta@hotmail.com");
		dados.add("Amanda Silva/amanda.silva@hotmail.com");
		dados.add("Breno Muriel/breno.muriel@gmail.com");
		dados.add("Luiz Inácio/luiz.inacio@gmail.com");
		dados.add("Fabricio Queiroz/fabricio.queiroz@hotmail.com");
		
		dados.add("Fabricio Macedo/fabriciusiqueira@gmail.com");
		dados.add("Erick Moraes/erick.moraes@gmail.com");
		dados.add("Thiago Luiz/thiago.luiz@gmail.com");
		dados.add("Lucio Alves/lucio.alves@gmail.com");
		dados.add("Barbara Lima/barbara.lima@hotmail.com");
		dados.add("Alisson Barbosa/alisson.barbosa@bol.com.br");
		dados.add("Nathan Silva/nathan.silva@yahoo.com");
		dados.add("Nathan Peres/nathan.peres@hotmail.com");
		dados.add("Douglas Alves/douglas.alves@gmail.com");
		dados.add("Eduarda Candido/eduarda.candido@gmail.com");
		dados.add("Leonardo Elero/leonardo.elero@gmail.com");
		dados.add("Stefany Prado/stefany.prado@hotmail.com");
		dados.add("Lucas Macanhan/lucas.macanhan@gmail.com");
		dados.add("Saulo Ferreira/saulo.ferreira@hotmail.com");
		dados.add("Jhennifer Duarte/jhennifer.duarte@gmail.com");
		dados.add("Danilo Ferdinand/danilo.ferdinand@gmail.com");
		dados.add("Thiago Mehl/thiago.mehl@gmail.com");
		dados.add("Bernardo Gomez/bernardo.gomez@hotmail.com");
		dados.add("Joseval Silveira/joseval.silveira@gmail.com");
		dados.add("Raissa Pereira/raissa.pereira@gmail.com");
		dados.add("Amanda Bueno/amanda.bueno@hotmail.com");
		dados.add("Isabelle Costa/isabelle.costa@gmail.com");
		dados.add("Vânia Ratto/vania.ratto@gmail.com");
		dados.add("Roberval Lima/roberval.lima@hotmail.com");
		dados.add("Thayná Cunha/thayna.cunha@gmail.com");
		dados.add("Leticia Fernanda/leticia.fernanda@gmail.com");
		dados.add("Leticia Silva/leticia.silva@hotmail.com");
		dados.add("Cristina Pereira/cristina.pereira@hotmail.com");
		dados.add("Sergio Roberto/sergio.roberto@gmail.com");
		dados.add("Jean Motta/jean.motta@gmail.com");
		dados.add("Fabiano Rodrigues/fabiano.rodrigues@hotmail.com");
		
		dados.add("Mauricio Ricardo/mauricio.ricardo@gmail.com");
		dados.add("Mauricio Souza/mauricio.souza@gmail.com");
		dados.add("Matheus Luiz/matheus.luiz@gmail.com");
		dados.add("Mateus Alves/mateus.alves@gmail.com");
		dados.add("Luana Siqueira/luana.siqueira@hotmail.com");
		dados.add("Daniel Alencar/daniel.alencar@bol.com.br");
		dados.add("Caio Abreu Silva/caio.abreu@yahoo.com");
		dados.add("Michel Villa/michel.villa@hotmail.com");
		dados.add("Fernando Fernandes/fernando.fernandes@gmail.com");
		dados.add("Eduarda Conceição/eduarda.conceicao@gmail.com");
		dados.add("Leonardo Pereira/leo_pereira@gmail.com");
		dados.add("Josiane Ferraz/josiane.ferraz@hotmail.com");
		dados.add("José Almeida/jose.almeida@gmail.com");
		dados.add("Josué Vinicius/josue.vinicius@hotmail.com");
		dados.add("Jenifer Silvar/jenifer.silva@gmail.com");
		dados.add("Danilo Mendes/danilo.mendes@gmail.com");
		dados.add("Tiago Ricardo/tiago.ricardo@gmail.com");
		dados.add("Vinicius Volpi/vinicius.volpi@hotmail.com");
		dados.add("William Silva/william.silva@gmail.com");
		dados.add("Manoela Pires/manoela.pires@gmail.com");
		dados.add("Renata Motta/renata.motta@hotmail.com");
		dados.add("Miriã Silva/miria.silva@gmail.com");
		dados.add("Edna Meirelles/edna.meirelles@gmail.com");
		dados.add("Muriel Ferraz/muriel.ferraz@hotmail.com");
		dados.add("Sônia Abreu/sonia.abreu@gmail.com");
		dados.add("Eliza Martis/eliza.martins@gmail.com");
		dados.add("Estela Silva/estela.silva@hotmail.com");
		dados.add("Darci Macedo/darci.macedo@hotmail.com");
		dados.add("Josenildo Mota/josenildo.mota@gmail.com");
		dados.add("Henrique Queiroga/henrique.queiroga@gmail.com");
		dados.add("Bernardo Franco/bernado.franco@hotmail.com");
		
		dados.add("Ricardo Drubski/ricardo.drubski@gmail.com");
		dados.add("André Leite/andre.leite@gmail.com");
		dados.add("Gustavo Silva/gustavo.Silva@gmail.com");
		dados.add("Rafael Zucki/rafael.zucki@gmail.com");
		dados.add("Andréia Carvalho/andreia.carvalho@hotmail.com");
		dados.add("Cléber Augusto/cleber.augusto@bol.com.br");
		dados.add("Maycon Lucas/maycon.lucas@yahoo.com");
		dados.add("Matheus Albino/matheus.albino@hotmail.com");
		dados.add("Layo Silva/layo.silva@gmail.com");
		dados.add("Rhuan Franscisco/rhuan.franscisco@gmail.com");
		dados.add("Francisco Cunha/francisco_cunha@gmail.com");
		dados.add("Julia Rodilha/julia.rodilha@hotmail.com");
		dados.add("Cristiano Araujo/cristiano.araujo@gmail.com");
		dados.add("Vitor Lima/vitor.lima@hotmail.com");
		dados.add("Aline Machado/aline.machado@gmail.com");
		dados.add("Vincius Vasconcellos/vinicius.vasconcellos@gmail.com");
		dados.add("Adriano Cardoso/adriano.cardoso@gmail.com");
		dados.add("Cristian Amorin/cristian.amorin@hotmail.com");
		dados.add("Sidney Alves/sidney.alves@gmail.com");
		dados.add("Adriana Cardoso/adriana.cardoso@gmail.com");
		dados.add("Gabriela Martins/gabriela.martins@hotmail.com");
		dados.add("Bianca Fagundes/bianca.fagundes@gmail.com");
		dados.add("Milena Alves/minela.alves@gmail.com");
		dados.add("Marcos Lima/marcos.lima@hotmail.com");
		dados.add("Regina Ferreira/regina.ferreira@gmail.com");
		dados.add("Alessanda Araujo/alessandra.araujo@gmail.com");
		dados.add("Karina Siqueira/karina.siqueira@hotmail.com");
		dados.add("Cintia Macedo/cintia.macedo@hotmail.com");
		dados.add("Marcos Paulo/marcos.paulo@gmail.com");
		dados.add("Milton Fonseca/milton.fonseca@gmail.com");
		dados.add("Reginaldo Rossi/reginaldo.rossi@hotmail.com");
		return dados;
	}
	
	private Coordenador getCoordenador(){
		Coordenador coordenador = new Coordenador();
		coordenador.setEmail("admin@admin");
		coordenador.setNome("Admin");
		coordenador.setSenha("admin");
		coordenador.criptografarSenha();
		return coordenadorRepository.saveAndFlush(coordenador);
	}
	
	private List<Professor> getProfessores(){
		List<Professor> professores = new ArrayList<>();
		List<String> dados = getDadosProfessores();
		for (String dado : dados) {
			Professor professor = new Professor();
			String[] d = dado.split("/");
			professor.setNome(d[0]);
			professor.setEmail(d[1]);
			professor.setSenha("p");
			professor.criptografarSenha();
			professor = professorRepository.save(professor);
			professores.add(professor);
		}
		
		return professores;
	}
	
	private List<String> getDadosProfessores(){
		List<String> dados = new ArrayList<>();
		dados.add("Claudia Regina/claudia@gmail.com");
		dados.add("Rogerio Cunha/rogerio@gmail.com");
		dados.add("Matheus Ferreira/matheus@gmail.com");
		dados.add("Maria Monte/maria@gmail.com");
		dados.add("Anastácia Silva/anastacia@hotmail.com");
		dados.add("Diego Alves/diego@bol.com.br");
		dados.add("Michael Oliver/michael@yahoo.com");
		dados.add("Pedro Inácio/pedro@hotmail.com");
		dados.add("Douglas Ribeiro/douglas@gmail.com");
		dados.add("Karina Alves/karina@gmail.com");
		dados.add("Crisógono Silva/crisogono@gmail.com");
		dados.add("Benedita Silva/benedita@hotmail.com");
		dados.add("Leonardo Matheus/leo@gmail.com");
		dados.add("Leonardo Cunha/leonardo@hotmail.com");
		dados.add("Beatriz Silvestre/beatriz@gmail.com");
		dados.add("Oliver Fagundes/oliver@gmail.com");
		dados.add("Antonio Fagundes/antonio@gmail.com");
		dados.add("Éverson Rocha/everson@hotmail.com");
		dados.add("Éder Ribeiro/eder@gmail.com");
		dados.add("Miguel Costa/miguel@gmail.com");
		dados.add("Sofia Matilde/sofia@hotmail.com");
		dados.add("Talita Siqueira/talita@gmail.com");
		dados.add("Marcela Macedo/marcela@gmail.com");
		dados.add("Nélio Alves/nelio@hotmail.com");
		dados.add("Elisandra Lima/elisandra@gmail.com");
		dados.add("Giselle Moraes/giselle@gmail.com");
		dados.add("Adelly Siqueira/adelly@hotmail.com");
		dados.add("Janaina Lima/janaina@hotmail.com");
		dados.add("Alisson Barbosa/alisson@gmail.com");
		dados.add("Enrick Pedro/enrick@gmail.com");
		dados.add("Victor Feitosa/victor_feitosa@hotmail.com");
		return dados;
	}
	
	private List<Curso> getCursos(List<Professor> professores, Coordenador coordenador) {
		List<Curso> cursos = new ArrayList<Curso>();
		List<String> dados = getDadosCursos();
		Random r = new Random();
		for (String dado : dados) {
			String[] d = dado.split("/");
			Curso curso = new Curso();
			curso.setNome(d[0]);
			curso.setDescricao(d[1]);
			curso.setCategoriaCurso(CategoriaCurso.valueOf(d[2]));
			curso.setVagas(Integer.parseInt(d[3]));
			curso.setDataInicial(LocalDate.parse(d[4]));
			curso.setDataConclusao(LocalDate.parse(d[5]));
			curso.setStatus(StatusCurso.valueOf(d[6]));
			curso.setProfessor(professores.get(r.nextInt(professores.size() - 1)));
			curso.setCoordenador(coordenador);
			curso = cursoRepository.save(curso);
			cursos.add(curso);
		}
		
		return cursos;
	}
	
	private List<String> getDadosCursos() {
		List<String> dados = new ArrayList<>();
		LocalDate now = LocalDate.now();
		String d1 = String.format("%s/%s/%s/%s/%s/%s/%s", "DevOps", "Vamos aprender os principais conceitos de DevOps, e ao fim do curso você sairá com bons conhecimentos sobre o assunto", CategoriaCurso.INFORMATICA, "5", now.minusMonths(11).minusDays(13).toString(), now.minusMonths(7).minusDays(27).toString(), StatusCurso.CONCLUIDO.toString());
		dados.add(d1);
		String d2 = String.format("%s/%s/%s/%s/%s/%s/%s", "Lógica de Programação", "Aprenda sobre algoritimos e lógica de programação", CategoriaCurso.INFORMATICA, "3", now.minusMonths(11).minusDays(15).toString(), now.minusMonths(6).minusDays(15).toString(), StatusCurso.CONCLUIDO.toString());
		dados.add(d2);
		String d3 =String.format("%s/%s/%s/%s/%s/%s/%s", "Matemática Básica", "Com esse curso você irá construir uma base sólida em matemática, e a partir desse curso você terá o conhecimento necessário para evoluir na matemática", CategoriaCurso.MATEMATICA, "2", now.minusMonths(11).minusDays(20).toString(), now.minusMonths(8).minusDays(18).toString(), StatusCurso.CONCLUIDO.toString());
		dados.add(d3);
		String d4 = String.format("%s/%s/%s/%s/%s/%s/%s", "Teoria dos Conjuntos", "Aprenda sobre a teoria dos conjuntos", CategoriaCurso.MATEMATICA, "0", now.minusMonths(11).minusDays(27).toString(), now.minusMonths(5).minusDays(22).toString(), StatusCurso.CONCLUIDO.toString());
		dados.add(d4);
		String d5 = String.format("%s/%s/%s/%s/%s/%s/%s", "Gramática", "Aprende gramática de uma forma descomplicada.", CategoriaCurso.PORTUGUES, "0", now.minusMonths(10).toString(), now.minusMonths(4).minusDays(3).toString(), StatusCurso.CONCLUIDO.toString());
		dados.add(d5);
		String d6 = String.format("%s/%s/%s/%s/%s/%s/%s", "Redação", "Aprenda a escrever redações da maneira correta", CategoriaCurso.PORTUGUES, "5", now.minusMonths(9).minusDays(1).toString(), now.minusMonths(4).minusDays(3).toString(), StatusCurso.CONCLUIDO.toString());
		dados.add(d6);
		String d7 = String.format("%s/%s/%s/%s/%s/%s/%s", "Inglês", "Aprenda inglês de uma forma diferente, com um método inovador", CategoriaCurso.IDIOMAS, "0", now.minusMonths(8).minusDays(2).toString(), now.minusMonths(3).minusDays(4).toString(), StatusCurso.CONCLUIDO.toString());
		dados.add(d7);
		String d8 = String.format("%s/%s/%s/%s/%s/%s/%s", "Espanhol", "Aprenda espanhol com um método inovador", CategoriaCurso.IDIOMAS, "2", now.minusMonths(7).minusDays(1).toString(), now.minusMonths(2).minusDays(2).toString(), StatusCurso.CONCLUIDO.toString());
		dados.add(d8);
		String d9 = String.format("%s/%s/%s/%s/%s/%s/%s", "Desenho", "Aprenda a desenhar como um profissional", CategoriaCurso.OUTROS, "10",  now.minusMonths(6).toString(), now.minusMonths(1).minusDays(5), StatusCurso.CONCLUIDO.toString());
		dados.add(d9);
		String d10 = String.format("%s/%s/%s/%s/%s/%s/%s", "Sustentabilidade", "Aprenda sobre sustentabilidade", CategoriaCurso.OUTROS, "22", now.minusMonths(5).toString(), now.minusDays(3).toString(), StatusCurso.CONCLUIDO.toString());
		dados.add(d10);
		
		String d11 = String.format("%s/%s/%s/%s/%s/%s/%s", "Java", "Aprenda uma das maiores linguagens de programação", CategoriaCurso.INFORMATICA, "19", now.minusMonths(5).minusDays(5).toString(), now.plusDays(5).toString(), StatusCurso.EM_ANDAMENTO.toString());
		dados.add(d11);
		String d12 = String.format("%s/%s/%s/%s/%s/%s/%s", "Python", "Aprenda a linguagem do futuro", CategoriaCurso.INFORMATICA, "20",  now.minusMonths(4).minusDays(4).toString(), now.plusMonths(1).plusDays(2).toString(), StatusCurso.EM_ANDAMENTO.toString());
		dados.add(d12);
		String d13 = String.format("%s/%s/%s/%s/%s/%s/%s", "Inglês", "Aprenda inglês de um jeito diferente e eficaz", CategoriaCurso.IDIOMAS, "25", now.minusMonths(3).minusDays(3).toString(), now.plusMonths(2).plusDays(10).toString(), StatusCurso.EM_ANDAMENTO.toString());
		dados.add(d13);
		String d14 = String.format("%s/%s/%s/%s/%s/%s/%s", "Francês", "Aprenda o básico de francês", CategoriaCurso.IDIOMAS, "15", now.minusMonths(2).minusDays(2).toString(), now.plusMonths(3).toString(), StatusCurso.EM_ANDAMENTO.toString());
		dados.add(d14);
		String d15 = String.format("%s/%s/%s/%s/%s/%s/%s", "Informática Básica", "Aprenda o básico sobre informática", CategoriaCurso.INFORMATICA, "30", now.minusMonths(1).minusDays(1).toString(), now.plusMonths(4).plusDays(1).toString(), StatusCurso.EM_ANDAMENTO.toString());
		dados.add(d15);
		String d16 = String.format("%s/%s/%s/%s/%s/%s/%s", "Probabilidade", "Aprenda sobre probabilidade", CategoriaCurso.MATEMATICA, "5", now.minusDays(1).toString(), now.plusMonths(5).plusDays(9).toString(), StatusCurso.EM_ANDAMENTO.toString());
		dados.add(d16);
		String d17 = String.format("%s/%s/%s/%s/%s/%s/%s", "Análise Combinatória", "Aprenda análise combinatória", CategoriaCurso.MATEMATICA, "6", now.minusMonths(1).plusDays(3).toString(), now.plusMonths(5).plusDays(9).toString(), StatusCurso.EM_ANDAMENTO.toString());
		dados.add(d17);
		String d18 = String.format("%s/%s/%s/%s/%s/%s/%s", "Porcentagem", "Aprenda sobre porcentagem", CategoriaCurso.MATEMATICA, "10", now.minusMonths(2).plusDays(2).toString(), now.plusMonths(6).plusDays(15).toString(), StatusCurso.EM_ANDAMENTO.toString());
		dados.add(d18);
		String d19 = String.format("%s/%s/%s/%s/%s/%s/%s", "Desenho", "Aprenda a desenhar de maneira profissional", CategoriaCurso.OUTROS, "2", now.minusMonths(3).plusDays(4).toString(), now.plusMonths(3).plusDays(29).toString(), StatusCurso.EM_ANDAMENTO.toString());
		dados.add(d19);
		String d20 = String.format("%s/%s/%s/%s/%s/%s/%s", "Banco de Dados", "Aprenda os conceitos principais na modelagem e criação de um banco de dados", CategoriaCurso.INFORMATICA, "0", now.minusMonths(1).plusDays(15).toString(), now.plusMonths(5).plusDays(25).toString(), StatusCurso.EM_ANDAMENTO.toString());
		dados.add(d20);
		
		String d21 = String.format("%s/%s/%s/%s/%s/%s/%s", "Inglês", "Venha aprender a língua mais falada no mundo", CategoriaCurso.IDIOMAS, "30", now.plusMonths(1).plusDays(5).toString(), now.plusMonths(6).plusDays(5).toString(), StatusCurso.NAO_INICIADO.toString());
		dados.add(d21);
		String d22 = String.format("%s/%s/%s/%s/%s/%s/%s", "Espanhol", "Aprenda espanhol de maneira prática e eficiente", CategoriaCurso.IDIOMAS, "20", now.plusMonths(1).plusDays(5).toString(), now.plusMonths(6).plusDays(4).toString(), StatusCurso.NAO_INICIADO.toString());
		dados.add(d22);
		String d23 = String.format("%s/%s/%s/%s/%s/%s/%s", "Big Data", "Aprenda Big Data com Python", CategoriaCurso.INFORMATICA, "8", now.plusMonths(1).plusDays(8).toString(), now.plusMonths(5).plusDays(4).toString(), StatusCurso.NAO_INICIADO.toString());
		dados.add(d23);
		String d24 = String.format("%s/%s/%s/%s/%s/%s/%s", "Javascript", "Aprenda os conceitos básicos de javascript.", CategoriaCurso.INFORMATICA, "5", now.plusMonths(1).plusDays(2).toString(), now.plusMonths(7).minusDays(1).toString(), StatusCurso.NAO_INICIADO.toString());
		dados.add(d24);
		String d25 = String.format("%s/%s/%s/%s/%s/%s/%s", "HTML & CSS", "Aprenda HTML e CSS", CategoriaCurso.INFORMATICA, "28", now.plusMonths(1).toString(), now.plusMonths(4).plusDays(17).toString(), StatusCurso.NAO_INICIADO.toString());
		dados.add(d25);
		String d26 = String.format("%s/%s/%s/%s/%s/%s/%s", "Matemática Básica", "Aprenda os pilares da matemática", CategoriaCurso.MATEMATICA, "24", now.plusDays(5).toString(), now.plusMonths(5).plusDays(17).toString(), StatusCurso.NAO_INICIADO.toString());
		dados.add(d26);
		String d27 = String.format("%s/%s/%s/%s/%s/%s/%s", "Gramática", "Aprenda sobre gramática", CategoriaCurso.PORTUGUES, "20", now.plusDays(3).toString(), now.plusMonths(4).plusDays(10).toString(), StatusCurso.NAO_INICIADO.toString());
		dados.add(d27);
		String d28 = String.format("%s/%s/%s/%s/%s/%s/%s", "React", "Aprenda o framework React", CategoriaCurso.INFORMATICA, "3", now.plusDays(2).toString(), now.plusMonths(5).plusDays(20).toString(), StatusCurso.NAO_INICIADO.toString());
		dados.add(d28);
		String d29 = String.format("%s/%s/%s/%s/%s/%s/%s", "Angular 8", "Aprenda o framework Angular", CategoriaCurso.INFORMATICA, "5", now.plusDays(10).toString(), now.plusMonths(6).plusDays(5).toString(), StatusCurso.NAO_INICIADO.toString());
		dados.add(d29);
		String d30 = String.format("%s/%s/%s/%s/%s/%s/%s", "IOT", "Aprenda sobre a internet das coisas", CategoriaCurso.INFORMATICA, "12", now.plusWeeks(2).toString(), now.plusMonths(6).plusDays(11).toString(), StatusCurso.NAO_INICIADO.toString());
		dados.add(d30);
		
		return dados;
	}
	
	private void atualizarQtdEVagasCursoESalvarNoBD(Curso curso) {
		curso.setQtdAlunosMatriculados(curso.getQtdAlunosMatriculados() + 1);
		cursoRepository.save(curso);
	}
	
	private void salvarMatriculas(List<Curso> cursos, List<Aluno> alunos) {
		List<Curso> cursosConcluidos = cursos.stream().filter(c -> c.getStatus().equals(StatusCurso.CONCLUIDO)).collect(Collectors.toList());
		for (Curso curso : cursosConcluidos) {
			List<Aluno> turma = montarTurma(alunos, RandomUtils.random(30, 45));
			int cont = 0;
			for (Aluno aluno : turma) {
				CursoAluno cursoAluno = new CursoAluno();
				cursoAluno.setId(new CursoAlunoPK(curso, aluno));
				String dado = getDadosMatriculasCursoConcluido(curso.getDataInicial()).get(cont);
				String[] d = dado.split("/");
				cursoAluno.setDataMatricula(LocalDate.parse(d[0]));
				cursoAluno.setPermissaoVisualizada(Boolean.parseBoolean(d[1]));
				cursoAluno.setStatusMatricula(StatusMatricula.valueOf(d[2]));
				cursoAluno.setSituacaoAluno(SituacaoAluno.valueOf(d[3]));
				cursoAlunoRepository.saveAndFlush(cursoAluno);
				atualizarQtdEVagasCursoESalvarNoBD(curso);
				cont += 1;
			}
		}
			
		
		List<Curso> cursosEmAndamento = cursos.stream().filter(c -> c.getStatus().equals(StatusCurso.EM_ANDAMENTO)).collect(Collectors.toList());
		for (Curso curso : cursosEmAndamento) {
			List<Aluno> turma = montarTurma(alunos, RandomUtils.random(25, 35));
			int cont = 0;
			for (Aluno aluno : turma) {
				CursoAluno cursoAluno = new CursoAluno();
				cursoAluno.setId(new CursoAlunoPK(curso, aluno));
				String dado = getDadosMatriculasCursoEmAndamento(curso.getDataInicial()).get(cont);
				String[] d = dado.split("/");
				cursoAluno.setDataMatricula(LocalDate.parse(d[0]));
				cursoAluno.setPermissaoVisualizada(Boolean.parseBoolean(d[1]));
				cursoAluno.setStatusMatricula(StatusMatricula.valueOf(d[2]));
				cursoAluno.setSituacaoAluno(SituacaoAluno.INDEFINIDO);
				cursoAlunoRepository.saveAndFlush(cursoAluno);
				atualizarQtdEVagasCursoESalvarNoBD(curso);
				cont +=1;
			}
		}
		
		List<Curso> cursosNaoIniciado = cursos.stream().filter(c -> c.getStatus().equals(StatusCurso.NAO_INICIADO)).collect(Collectors.toList());
		for (Curso curso : cursosNaoIniciado) {
			List<Aluno> turma = montarTurma(alunos, RandomUtils.random(0, 25));
			int cont = 0;
			for (Aluno aluno : turma) {
				CursoAluno cursoAluno = new CursoAluno();
				cursoAluno.setId(new CursoAlunoPK(curso, aluno));
				String dado = getDadosMatriculasCursosNaoIniciado().get(cont);
				String[] d = dado.split("/");
				cursoAluno.setDataMatricula(LocalDate.parse(d[0]));
				cursoAluno.setPermissaoVisualizada(Boolean.parseBoolean(d[1]));
				cursoAluno.setStatusMatricula(StatusMatricula.valueOf(d[2]));
				cursoAluno.setSituacaoAluno(SituacaoAluno.INDEFINIDO);
				cursoAlunoRepository.saveAndFlush(cursoAluno);
				atualizarQtdEVagasCursoESalvarNoBD(curso);
				cont += 1;
			}
		}
	}
	
	private List<String> getDadosMatriculasCursosNaoIniciado() {
		List<String> dados = new ArrayList<>();
		LocalDate now = LocalDate.now();
		String d1 = String.format("%s/%s/%s", now.minusWeeks(1).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d1);
		String d2 = String.format("%s/%s/%s", now.minusDays(3).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d2);
		String d3 = String.format("%s/%s/%s", now.minusDays(2).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d3);
		String d4 = String.format("%s/%s/%s", now.minusDays(2).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d4);
		String d5 = String.format("%s/%s/%s", now.minusDays(2).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d5);
		String d6 = String.format("%s/%s/%s", now.minusDays(2).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d6);
		String d7 = String.format("%s/%s/%s", now.minusDays(2).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d7);
		String d8 = String.format("%s/%s/%s", now.minusDays(3).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d8);
		String d9 = String.format("%s/%s/%s", now.minusDays(3).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d9);
		String d10 = String.format("%s/%s/%s", now.minusDays(1).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d10);
		String d11 = String.format("%s/%s/%s", now.minusDays(1).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d11);
		String d12 = String.format("%s/%s/%s", now.minusDays(8).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d12);
		String d13 = String.format("%s/%s/%s", now.minusDays(12).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d13);
		String d14 = String.format("%s/%s/%s", now.minusDays(10).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d14);
		String d15 = String.format("%s/%s/%s", now.minusDays(9).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d15);
		String d16 = String.format("%s/%s/%s", now.minusDays(8).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d16);
		String d17 = String.format("%s/%s/%s", now.minusDays(7).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d17);
		String d18 = String.format("%s/%s/%s", now.minusDays(6).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d18);
		String d19 = String.format("%s/%s/%s", now.minusDays(5).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d19);
		String d20 = String.format("%s/%s/%s", now.minusDays(15).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d20);
		String d21 = String.format("%s/%s/%s", now.minusDays(22).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d21);
		String d22 = String.format("%s/%s/%s", now.minusDays(3).toString(), "true", StatusMatricula.NAO_CONFIRMADA.toString());
		dados.add(d22);
		String d23 = String.format("%s/%s/%s", now.toString(), "false", StatusMatricula.NAO_CONFIRMADA.toString());
		dados.add(d23);
		String d24 = String.format("%s/%s/%s", now.toString(), "false", StatusMatricula.NAO_CONFIRMADA.toString());
		dados.add(d24);
		String d25 = String.format("%s/%s/%s", now.minusDays(5).toString(), "true", StatusMatricula.NAO_CONFIRMADA.toString());
		dados.add(d25);
		return dados;
	}

	private List<String> getDadosMatriculasCursoEmAndamento(LocalDate dataInicioCurso) {
		List<String> dados = new ArrayList<>();
		String d1 = String.format("%s/%s/%s", dataInicioCurso.minusWeeks(2).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d1);
		String d2 = String.format("%s/%s/%s", dataInicioCurso.minusDays(3).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d2);
		String d3 = String.format("%s/%s/%s", dataInicioCurso.minusDays(21).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d3);
		String d4 = String.format("%s/%s/%s", dataInicioCurso.minusDays(3).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d4);
		String d5 = String.format("%s/%s/%s", dataInicioCurso.minusDays(3).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d5);
		String d6 = String.format("%s/%s/%s", dataInicioCurso.minusDays(1).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d6);
		String d7 = String.format("%s/%s/%s", dataInicioCurso.minusDays(2).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d7);
		String d8 = String.format("%s/%s/%s", dataInicioCurso.minusDays(4).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d8);
		String d9 = String.format("%s/%s/%s", dataInicioCurso.minusDays(4).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d9);
		String d10 = String.format("%s/%s/%s", dataInicioCurso.minusDays(1).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d10);
		String d11 = String.format("%s/%s/%s", dataInicioCurso.minusDays(1).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d11);
		String d12 = String.format("%s/%s/%s", dataInicioCurso.minusDays(9).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d12);
		String d13 = String.format("%s/%s/%s", dataInicioCurso.toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d13);
		String d14 = String.format("%s/%s/%s", dataInicioCurso.toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d14);
		String d15 = String.format("%s/%s/%s", dataInicioCurso.minusDays(10).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d15);
		String d16 = String.format("%s/%s/%s", dataInicioCurso.minusDays(11).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d16);
		String d17 = String.format("%s/%s/%s", dataInicioCurso.minusDays(7).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d17);
		String d18 = String.format("%s/%s/%s", dataInicioCurso.minusDays(6).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d18);
		String d19 = String.format("%s/%s/%s", dataInicioCurso.minusDays(4).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d19);
		String d20 = String.format("%s/%s/%s", dataInicioCurso.minusDays(16).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d20);
		String d21 = String.format("%s/%s/%s", dataInicioCurso.minusDays(17).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d21);
		String d22 = String.format("%s/%s/%s", dataInicioCurso.minusDays(3).toString(), "true", StatusMatricula.NAO_CONFIRMADA.toString());
		dados.add(d22);
		String d23 = String.format("%s/%s/%s", dataInicioCurso.plusDays(1).toString(), "false", StatusMatricula.NAO_CONFIRMADA.toString());
		dados.add(d23);
		String d24 = String.format("%s/%s/%s", dataInicioCurso.toString(), "false", StatusMatricula.NAO_CONFIRMADA.toString());
		dados.add(d24);
		String d25 = String.format("%s/%s/%s", dataInicioCurso.minusDays(5).toString(), "true", StatusMatricula.NAO_CONFIRMADA.toString());
		dados.add(d25);
		String d26 = String.format("%s/%s/%s", dataInicioCurso.minusDays(8).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d26);
		String d27 = String.format("%s/%s/%s", dataInicioCurso.minusDays(2).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d27);
		String d28 = String.format("%s/%s/%s", dataInicioCurso.minusDays(6).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d28);
		String d29 = String.format("%s/%s/%s", dataInicioCurso.minusDays(12).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d29);
		String d30 = String.format("%s/%s/%s", dataInicioCurso.minusDays(16).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d30);
		String d31 = String.format("%s/%s/%s", dataInicioCurso.minusDays(1).toString(), "true", StatusMatricula.CONFIRMADA.toString());
		dados.add(d31);
		String d32 = String.format("%s/%s/%s", dataInicioCurso.minusDays(4).toString(), "true", StatusMatricula.NAO_CONFIRMADA.toString());
		dados.add(d32);
		String d33 = String.format("%s/%s/%s", dataInicioCurso.plusDays(2).toString(), "false", StatusMatricula.NAO_CONFIRMADA.toString());
		dados.add(d33);
		String d34 = String.format("%s/%s/%s", dataInicioCurso.minusDays(1).toString(), "false", StatusMatricula.NAO_CONFIRMADA.toString());
		dados.add(d34);
		String d35 = String.format("%s/%s/%s", dataInicioCurso.minusDays(6).toString(), "true", StatusMatricula.NAO_CONFIRMADA.toString());
		dados.add(d35);
		return dados;
	}

	private List<String> getDadosMatriculasCursoConcluido(LocalDate dataInicioCurso){
		List<String> dados = new ArrayList<>();
		String d1 = String.format("%s/%s/%s/%s", dataInicioCurso.minusWeeks(2).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d1);
		String d2 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(3).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d2);
		String d3 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(21).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d3);
		String d4 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(3).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d4);
		String d5 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(3).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d5);
		String d6 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(1).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d6);
		String d7 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(2).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d7);
		String d8 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(4).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d8);
		String d9 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(4).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d9);
		String d10 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(1).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d10);
		String d11 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(1).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d11);
		String d12 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(9).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d12);
		String d13 = String.format("%s/%s/%s/%s", dataInicioCurso.toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d13);
		String d14 = String.format("%s/%s/%s/%s", dataInicioCurso.toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d14);
		String d15 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(10).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d15);
		String d16 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(11).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d16);
		String d17 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(7).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d17);
		String d18 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(6).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d18);
		String d19 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(4).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d19);
		String d20 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(16).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d20);
		String d21 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(17).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.REPROVADO.toString());
		dados.add(d21);
		String d22 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(3).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.REPROVADO.toString());
		dados.add(d22);
		String d23 = String.format("%s/%s/%s/%s", dataInicioCurso.plusDays(1).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.REPROVADO.toString());
		dados.add(d23);
		String d24 = String.format("%s/%s/%s/%s", dataInicioCurso.toString(), "true", StatusMatricula.NAO_CONFIRMADA.toString(), SituacaoAluno.REPROVADO.toString());
		dados.add(d24);
		String d25 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(5).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.REPROVADO.toString());
		dados.add(d25);
		
		String d26 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(9).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d26);
		String d27 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(1).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d27);
		String d28 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(5).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d28);
		String d29 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(6).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d29);
		String d30 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(22).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d30);
		String d31 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(18).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d31);
		String d32 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(14).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d32);
		String d33 = String.format("%s/%s/%s/%s", dataInicioCurso.plusDays(3).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d33);
		String d34 = String.format("%s/%s/%s/%s", dataInicioCurso.plusDays(2).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d34);
		String d35 = String.format("%s/%s/%s/%s", dataInicioCurso.plusDays(1).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d35);
		String d36 = String.format("%s/%s/%s/%s", dataInicioCurso.toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d36);
		String d37 = String.format("%s/%s/%s/%s", dataInicioCurso.toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d37);
		String d38 = String.format("%s/%s/%s/%s", dataInicioCurso.toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d38);
		String d39 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(11).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d39);
		String d40 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(16).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.APROVADO.toString());
		dados.add(d40);
		String d41 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(15).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.REPROVADO.toString());
		dados.add(d41);
		String d42 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(23).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.REPROVADO.toString());
		dados.add(d42);
		String d43 = String.format("%s/%s/%s/%s", dataInicioCurso.plusDays(1).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.REPROVADO.toString());
		dados.add(d43);
		String d44 = String.format("%s/%s/%s/%s", dataInicioCurso.plusDays(2).toString(), "true", StatusMatricula.NAO_CONFIRMADA.toString(), SituacaoAluno.REPROVADO.toString());
		dados.add(d44);
		String d45 = String.format("%s/%s/%s/%s", dataInicioCurso.minusDays(5).toString(), "true", StatusMatricula.CONFIRMADA.toString(), SituacaoAluno.REPROVADO.toString());
		dados.add(d45);
		return dados;
	}
	
	private List<Aluno> montarTurma(List<Aluno> alunos, int maxAlunos){
		List<Aluno> turma = new ArrayList<>();
		List<Aluno> alunosDisponiveis = new ArrayList<>();
		alunosDisponiveis.addAll(alunos);
		for(int i = 1; i <= maxAlunos; i++) {
				Random random = new Random();
				int id = random.nextInt(alunosDisponiveis.size() - 1);
				Aluno aluno = alunosDisponiveis.get(id);
				turma.add(aluno);
				alunosDisponiveis.remove(aluno);
		}
		
		return turma;
	}
	
	private List<Atividade> getAtividades(List<Curso> cursos) {
		List<Atividade> atividades = new ArrayList<>();
		
		for(Curso curso : cursos) {
			List<String> dados = new ArrayList<>();
			if(curso.getNome().equals("DevOps")) {
				dados = getDadosAtividadesDevOps();
			} else if(curso.getNome().equals("Lógica de Programação")) {
				dados = getDadosAtividadesLogicaProgramacao();
			} else if(curso.getNome().equals("Matemática Básica")) {
				dados = getDadosAtividadesMatemáticaBasica();
			} else if(curso.getNome().equals("Teoria dos Conjuntos")) {
				dados = getDadosAtividadesTeoriaDosConjuntos();
			} else if(curso.getNome().equals("Gramática")) {
				dados = getDadosAtividadesGramatica();
			} else if(curso.getNome().equals("Redação")) {
				dados = getDadosAtividadesRedacao();
			} else if(curso.getNome().equals("Inglês")) {
				dados = getDadosAtividadesIngles();
			} else if(curso.getNome().equals("Espanhol")) {
				dados = getDadosAtividadesEspanhol();
			} else if(curso.getNome().equals("Desenho")) {
				dados = getDadosAtividadesDesenho();
			} else if(curso.getNome().equals("Sustentabilidade")) {
				dados = getDadosAtividadesSustentabilidade();
			} else if(curso.getNome().equals("Java")) {
				dados = getDadosAtividadesJava();
			} else if(curso.getNome().equals("Python")) {
				dados = getDadosAtividadesPython();
			} else if(curso.getNome().equals("Francês")) {
				dados = getDadosAtividadesFrances();
			} else if(curso.getNome().equals("Informática Básica")) {
				dados = getDadosAtividadesInformaticaBasica();
			} else if(curso.getNome().equals("Probabilidade")) {
				dados = getDadosAtividadesProbabilidade();
			} else if(curso.getNome().equals("Análise Combinatória")) {
				dados = getDadosAtividadesAnaliseCombinatoria();
			} else if(curso.getNome().equals("Porcentagem")) {
				dados = getDadosAtividadesPorcentagem();
			} else if(curso.getNome().equals("Banco de Dados")) {
				dados = getDadosAtividadesBancoDeDados();
			} 
			
			LocalDate now = LocalDate.now();
			if(curso.getStatus().equals(StatusCurso.EM_ANDAMENTO) || curso.getStatus().equals(StatusCurso.CONCLUIDO)) {
				for(String dado : dados) {
					String[] d = dado.split("/");
					LocalDate dataFinal = curso.getDataInicial().plusDays(Integer.parseInt(d[1]));
					if(dataFinal.isBefore(curso.getDataConclusao())) {
						Atividade at = new Atividade();
						at.setDataFinal(dataFinal);
						at.setDescricao(d[0]);
						at.setPermiteEntregaAtrasada(Boolean.parseBoolean(d[2]));
						if(now.isAfter(at.getDataFinal())) {
							at.setStatus(StatusAtividade.FINALIZADA);
						} else {
							at.setStatus(StatusAtividade.EM_ABERTO);
						}
						at.setTitulo(d[3]);
						at.setCurso(curso);
						at = atividadeRepository.save(at);
						at.setNomeArquivo(String.format("%04d.pdf", at.getId()));
						atividadeRepository.save(at);
						atividades.add(at);
						}
					}
				}
		}
		return atividades;
	}
	
	private List<String> getDadosAtividadesBancoDeDados() {
		List<String> dados = new ArrayList<>();
		String d1 = String.format("%s/%s/%s/%s", "Exercícios sobre modelagem ", "7", "true", "Modelagem");
		dados.add(d1);
		String d2 = String.format("%s/%s/%s/%s", "Exercícios sobre modelo conceitual", "20", "true", "Modelo Conceitual");
		dados.add(d2);
		String d3 = String.format("%s/%s/%s/%s", "Exercícios sobre modelo lógico", "40", "true", "Modelo Lógico");
		dados.add(d3);
		String d4 = String.format("%s/%s/%s/%s", "Exercícios sobre formas normais", "60", "false", "Formas Normais");
		dados.add(d4);
		String d5 = String.format("%s/%s/%s/%s", "Exercícios sobre SQL", "80", "false", "SQL");
		dados.add(d5);
		return dados;
	}

	private List<String> getDadosAtividadesPorcentagem() {
		List<String> dados = new ArrayList<>();
		String d1 = String.format("%s/%s/%s/%s", "Exercícios sobre proporção ", "7", "true", "Proporção");
		dados.add(d1);
		String d2 = String.format("%s/%s/%s/%s", "Exercícios sobre regra de três", "20", "true", "Regra de três");
		dados.add(d2);
		String d3 = String.format("%s/%s/%s/%s", "Exercícios sobre funções", "40", "true", "Funções");
		dados.add(d3);
		String d4 = String.format("%s/%s/%s/%s", "Exercícios sobre variação percentual", "60", "false", "Variação Percentual");
		dados.add(d4);
		String d5 = String.format("%s/%s/%s/%s", "Exercícios sobre fator de aumento e desconto", "80", "false", "Fator de Aumento e Desconto");
		dados.add(d5);
		return dados;
	}

	private List<String> getDadosAtividadesAnaliseCombinatoria() {
		List<String> dados = new ArrayList<>();
		String d1 = String.format("%s/%s/%s/%s", "Exercícios sobre o princípio fundamental da contagem - PFC ", "7", "true", "Princípio Fundamental da Contagem");
		dados.add(d1);
		String d2 = String.format("%s/%s/%s/%s", "Exercícios sobre anagramas", "20", "true", "Anagramas");
		dados.add(d2);
		String d3 = String.format("%s/%s/%s/%s", "Exercícios sobre tipos de agrupamentos", "40", "true", "Tipos de agrupamentos");
		dados.add(d3);
		String d4 = String.format("%s/%s/%s/%s", "Exercícios sobre combinação simples", "60", "false", "Combinação simples");
		dados.add(d4);
		String d5 = String.format("%s/%s/%s/%s", "Exercícios sobre distinção entre permutações e combinações", "80", "false", "Permutações e Combinações");
		dados.add(d5);
		return dados;
	}

	private List<String> getDadosAtividadesProbabilidade() {
		List<String> dados = new ArrayList<>();
		String d1 = String.format("%s/%s/%s/%s", "Exercícios sobre espaços amostrais ", "7", "true", "Espaços Amostrais");
		dados.add(d1);
		String d2 = String.format("%s/%s/%s/%s", "Exercícios sobre eventos", "20", "true", "Eventos");
		dados.add(d2);
		String d3 = String.format("%s/%s/%s/%s", "Exercícios sobre intervalo de probabilidades", "40", "true", "Intervalo de Probabilidades");
		dados.add(d3);
		String d4 = String.format("%s/%s/%s/%s", "Exercícios sobre probabilidade do evento complementar", "60", "false", "Probabilidade do Evento Complementar");
		dados.add(d4);
		String d5 = String.format("%s/%s/%s/%s", "Exercícios sobre eventos dependentes e independentes", "80", "false", "Eventos Dependentes e Independentes");
		dados.add(d5);
		return dados;
	}

	private List<String> getDadosAtividadesInformaticaBasica() {
		List<String> dados = new ArrayList<>();
		String d1 = String.format("%s/%s/%s/%s", "Exercícios sobre hardware ", "7", "true", "Hardware");
		dados.add(d1);
		String d2 = String.format("%s/%s/%s/%s", "Exercícios sobre software", "20", "true", "Software");
		dados.add(d2);
		String d3 = String.format("%s/%s/%s/%s", "Exercícios sobre sistemas operacionais", "40", "true", "Sistemas Operacionais");
		dados.add(d3);
		String d4 = String.format("%s/%s/%s/%s", "Exercícios sobre redes e conectividade", "60", "false", "Redes e Conectividade");
		dados.add(d4);
		String d5 = String.format("%s/%s/%s/%s", "Exercícios sobre segurança da informação", "80", "false", "Segurança da Informação");
		dados.add(d5);
		return dados;
	}

	private List<String> getDadosAtividadesFrances() {
		List<String> dados = new ArrayList<>();
		String d1 = String.format("%s/%s/%s/%s", "Exercícios o alfabeto francês ", "7", "true", "Alfabeto");
		dados.add(d1);
		String d2 = String.format("%s/%s/%s/%s", "Exercícios sobre artigos (definido e indefinido)", "20", "true", "Artigos");
		dados.add(d2);
		String d3 = String.format("%s/%s/%s/%s", "Exercícios sobre adjetivos e adverbios", "40", "true", "Adjetivos e Advérbios");
		dados.add(d3);
		String d4 = String.format("%s/%s/%s/%s", "Exercícios sobre pronomes", "60", "false", "Pronomes");
		dados.add(d4);
		String d5 = String.format("%s/%s/%s/%s", "Exercícios sobre gramática em geral", "80", "false", "Gramática");
		dados.add(d5);
		return dados;
	}

	private List<String> getDadosAtividadesPython() {
		List<String> dados = new ArrayList<>();
		String d1 = String.format("%s/%s/%s/%s", "Exercícios sobre estruturas de decisão ", "7", "true", "Estruturas de Decisão");
		dados.add(d1);
		String d2 = String.format("%s/%s/%s/%s", "Exercícios sobre estruturas de repetição", "20", "true", "Estruturas de Repetição");
		dados.add(d2);
		String d3 = String.format("%s/%s/%s/%s", "Exercícios sobre paradigmas de programação", "40", "true", "Paradigmas");
		dados.add(d3);
		String d4 = String.format("%s/%s/%s/%s", "Exercícios sobre programação orientada a objetos", "60", "false", "Orientação a objetos");
		dados.add(d4);
		String d5 = String.format("%s/%s/%s/%s", "Exercícios sobre programação funcional", "80", "false", "Programação Funcional");
		dados.add(d5);
		return dados;
	}

	private List<String> getDadosAtividadesJava() {
		List<String> dados = new ArrayList<>();
		String d1 = String.format("%s/%s/%s/%s", "Exercícios sobre estruturas de decisão ", "7", "true", "Estruturas de Decisão");
		dados.add(d1);
		String d2 = String.format("%s/%s/%s/%s", "Exercícios sobre estruturas de repetição", "20", "true", "Estruturas de Repetição");
		dados.add(d2);
		String d3 = String.format("%s/%s/%s/%s", "Exercícios sobre os pilares da orientação a objetos", "40", "true", "Orientação a objetos");
		dados.add(d3);
		String d4 = String.format("%s/%s/%s/%s", "Criando um jogo de perguntas e respostas", "60", "false", "Show do Milhão");
		dados.add(d4);
		String d5 = String.format("%s/%s/%s/%s", "Criando o jogo da cobrinha", "80", "false", "Snake");
		dados.add(d5);
		String d6 = String.format("%s/%s/%s/%s", "Exercícios sobre estrutura de dados", "130", "false", "Estruturas de Dados");
		dados.add(d6);
		return dados;
	}

	private List<String> getDadosAtividadesSustentabilidade() {
		List<String> dados = new ArrayList<>();
		String d1 = String.format("%s/%s/%s/%s", "Fale mais sobre o que é sustentabilidade? ", "7", "true", "O que é sustentabilidade?");
		dados.add(d1);
		String d2 = String.format("%s/%s/%s/%s", "Exercícios sobre desenvolvimento sustentável.", "20", "true", "Desenvolvimento sustentável");
		dados.add(d2);
		String d3 = String.format("%s/%s/%s/%s", " Questionário sobre sustentabilidade ambiental", "40", "true", "Sustentabilidade Ambiental");
		dados.add(d3);
		String d4 = String.format("%s/%s/%s/%s", "Exercicíos sobre sustentabilidade social", "60", "false", "Sustentabilidade Social");
		dados.add(d4);
		String d5 = String.format("%s/%s/%s/%s", "Exercicíos sobre sustentabilidade econômica", "80", "false", "Sustentabilidade Econômica");
		dados.add(d5);
		String d6 = String.format("%s/%s/%s/%s", "Exercicíos sobre sustentabilidade empresarial", "130", "false", "Sustentabilidade Empresarial");
		dados.add(d6);
		return dados;
	}

	private List<String> getDadosAtividadesDesenho() {
		List<String> dados = new ArrayList<>();
		String d1 = String.format("%s/%s/%s/%s", "Etapas de um desenho", "7", "true", "Etapas");
		dados.add(d1);
		String d2 = String.format("%s/%s/%s/%s", "Exercícios sobre anatomia.", "20", "true", "Anatomia");
		dados.add(d2);
		String d3 = String.format("%s/%s/%s/%s", " Questionário sobre perspectiva", "40", "true", "Perspectiva");
		dados.add(d3);
		String d4 = String.format("%s/%s/%s/%s", "Exercicíos sobre luz", "60", "false", "Luz");
		dados.add(d4);
		String d5 = String.format("%s/%s/%s/%s", "Exercicíos sobre sombra", "80", "false", "Sombra");
		dados.add(d5);
		return dados;
	}

	private List<String> getDadosAtividadesEspanhol() {
		List<String> dados = new ArrayList<>();
		String d1 = String.format("%s/%s/%s/%s", "Exercícios sobre o alfabeto", "7", "true", "Alfabeto");
		dados.add(d1);
		String d2 = String.format("%s/%s/%s/%s", "Exercícios sobre pronúncia e a acentuação tônica", "20", "true", "Acentuação Tônica");
		dados.add(d2);
		String d3 = String.format("%s/%s/%s/%s", "Responder o questionário sobre os artigos e os adjetivos", "40", "true", "Artigos e Adjetivos");
		dados.add(d3);
		String d4 = String.format("%s/%s/%s/%s", "Exercicíos sobre vocabulário básico", "60", "false", "Vocabulário básico");
		dados.add(d4);
		String d5 = String.format("%s/%s/%s/%s", "Exercicíos sobre gramática", "80", "false", "Gramática");
		dados.add(d5);
		return dados;
	}

	private List<String> getDadosAtividadesIngles() {
		List<String> dados = new ArrayList<>();
		String d1 = String.format("%s/%s/%s/%s", "Exercícios sobre verbo to be", "7", "true", "Verbo to be");
		dados.add(d1);
		String d2 = String.format("%s/%s/%s/%s", "Mais exercícios sobre verbo to be", "20", "true", "Verbo to be 2");
		dados.add(d2);
		String d3 = String.format("%s/%s/%s/%s", "Responder o questionário", "40", "true", "Questionário");
		dados.add(d3);
		String d4 = String.format("%s/%s/%s/%s", "Exercicíos sobre phrasal verbs", "60", "false", "Phrasal Verbs");
		dados.add(d4);
		String d5 = String.format("%s/%s/%s/%s", "Mais exercicíos sobre phrasal verbs", "80", "false", "Phrasal Verbs 2");
		dados.add(d5);
		return dados;
	}

	private List<String> getDadosAtividadesRedacao() {
		List<String> dados = new ArrayList<>();
		String d1 = String.format("%s/%s/%s/%s", "Exercícios sobre os conceitos de uma redação do tipo descritivo", "7", "true", "Redação Descritiva");
		dados.add(d1);
		String d2 = String.format("%s/%s/%s/%s", "Exercícios sobre os conceitos de uma redação do tipo descritivo", "20", "true", "Redação Narrativa");
		dados.add(d2);
		String d3 = String.format("%s/%s/%s/%s", "Exercícios sobre os conceitos de uma redação do tipo descritivo", "40", "true", "Redação Argumentativa");
		dados.add(d3);
		String d4 = String.format("%s/%s/%s/%s", "Exercícios sobre os conceitos de uma redação do tipo descritivo", "60", "false", "Redação Injuntiva");
		dados.add(d4);
		String d5 = String.format("%s/%s/%s/%s", "Faça uma redação argumentativa de no minimo 20 linhas.", "80", "false", "Redação Argumentativa 2");
		dados.add(d5);
		return dados;
	}

	private List<String> getDadosAtividadesGramatica() {
		List<String> dados = new ArrayList<>();
		String d1 = String.format("%s/%s/%s/%s", "Exercícios sobre gramática normativa", "7", "true", "Gramática Normativa");
		dados.add(d1);
		String d2 = String.format("%s/%s/%s/%s", "Exercícios sobre gramática descritiva", "20", "true", "Gramática Descritiva");
		dados.add(d2);
		String d3 = String.format("%s/%s/%s/%s", "Exercícios sobre gramática histórica", "40", "true", "Gramática Histórica");
		dados.add(d3);
		String d4 = String.format("%s/%s/%s/%s", "Exercícios sobre gramática comparativa", "60", "false", "Gramática Comparativa");
		dados.add(d4);
		String d5 = String.format("%s/%s/%s/%s", "Exercícios sobre gramática reflexiva", "80", "false", "Gramática Reflexiva");
		dados.add(d5);
		return dados;
	}

	private List<String> getDadosAtividadesTeoriaDosConjuntos() {
		List<String> dados = new ArrayList<>();
		String d1 = String.format("%s/%s/%s/%s", "Resolvendo problemas com o diagrama de Euler-Venn", "7", "true", "Diagrama de Euler-Venn");
		dados.add(d1);
		String d2 = String.format("%s/%s/%s/%s", "Relação de inclusão e pertinência", "20", "true", "Inclusão e Pertinência");
		dados.add(d2);
		String d3 = String.format("%s/%s/%s/%s", "União, Intersecção e Diferença entre Conjuntos", "40", "true", "Relação entre conjuntos");
		dados.add(d3);
		String d4 = String.format("%s/%s/%s/%s", "Exercícios sobre os conjuntos numéricos", "60", "false", "Conjuntos Númericos");
		dados.add(d4);
		String d5 = String.format("%s/%s/%s/%s", "Operações com conjuntos", "80", "false", "Operações");
		dados.add(d5);
		return dados;
	}

	private List<String> getDadosAtividadesMatemáticaBasica() {
		List<String> dados = new ArrayList<>();
		String d1 = String.format("%s/%s/%s/%s", "Exercícios com números inteiros", "7", "true", "Números Inteiros");
		dados.add(d1);
		String d2 = String.format("%s/%s/%s/%s", "Exercícios com números racionais", "20", "true", "Números Racionais");
		dados.add(d2);
		String d3 = String.format("%s/%s/%s/%s", "Exercicios sobre potenciação", "40", "true", "Potenciação");
		dados.add(d3);
		String d4 = String.format("%s/%s/%s/%s", "Exercícios sobre equações", "60", "false", "Equações");
		dados.add(d4);
		String d5 = String.format("%s/%s/%s/%s", "Exercício diversos", "80", "false", "Diversos");
		dados.add(d5);
		return dados;
	}

	private List<String> getDadosAtividadesLogicaProgramacao() {
		List<String> dados = new ArrayList<>();
		String d1 = String.format("%s/%s/%s/%s", "Calculando a sequência de Fibonacci", "7", "true", "Fibonacci");
		dados.add(d1);
		String d2 = String.format("%s/%s/%s/%s", "Criando uma calculadora de operações básicas", "20", "true", "Calculadora");
		dados.add(d2);
		String d3 = String.format("%s/%s/%s/%s", "Criando um jogo de adivinhação de um número sorteado pela máquina", "40", "true", "Adivinhe o número");
		dados.add(d3);
		String d4 = String.format("%s/%s/%s/%s", "Questionário sobre estruturas de repetição", "60", "false", "Estruturas de repetição");
		dados.add(d4);
		String d5 = String.format("%s/%s/%s/%s", "Criando um jogo da velha", "80", "false", "Jogo da velha");
		dados.add(d5);
		return dados;
	}

	private List<String> getDadosAtividadesDevOps(){
		List<String> dadosAtividadesDevOps = new ArrayList<>();
		String d1 = String.format("%s/%s/%s/%s", "Esta atividade comtempla a escrita de uma redação sobre o que realmente é DevOps e porque você implementaria DevOps na sua empresa.", "7", "true", "O que é DevOps?");
		dadosAtividadesDevOps.add(d1);
		String d2 = String.format("%s/%s/%s/%s", "Responder questionário sobre integração contínua.", "20", "true", "Integração Contínua");
		dadosAtividadesDevOps.add(d2);
		String d3 = String.format("%s/%s/%s/%s", "Responder questionário sobre versionamento de código", "40", "true", "Versionamento de código");
		dadosAtividadesDevOps.add(d3);
		String d4 = String.format("%s/%s/%s/%s", "Essa atividade é simples, apenas crie um repositório no Github e faça a simulação de um versionamento de código de um sistema, criando branches e tudo mais.", "60", "false", "GitHub");
		dadosAtividadesDevOps.add(d4);
		String d5 = String.format("%s/%s/%s/%s", "Fale mais sobre o TDD", "80", "false", "TDD");
		dadosAtividadesDevOps.add(d5);
		String d6 = String.format("%s/%s/%s/%s", "Scrum e o desenvolvimento ágil, faça uma redação explicando o que é, como funciona e qual o impacto do uso dessas metodologias no processo de desenvolvimento de um software", "130", "true", "Desenvolvimento Ágil");
		dadosAtividadesDevOps.add(d6);
		return dadosAtividadesDevOps;
	}
	
	private void salvarEntregaDeAtividades() throws IOException {
		List<Curso> cursos = cursoRepository.findConcluidosEEmAndamento();
		for(Curso curso : cursos) {
				List<Aluno> alunosMatriculados = cursoAlunoRepository.findByIdCurso(curso.getId());
				List<Atividade> atividades = atividadeRepository.findByCurso_Id(curso.getId());
				for(Aluno aluno : alunosMatriculados) {
					for(Atividade atividade : atividades) {
						List<String> dados = new ArrayList<>();
							String[] dado = null;
							Entrega e = new Entrega();
							e.setId(new EntregaPK(atividade, aluno));
							if(curso.getStatus().equals(StatusCurso.CONCLUIDO)) {
								dados =  getDadosAtividadeEntregueConcluida(atividade);
								if(dados.size() > 0) {
									dado = dados.get(RandomUtils.random(0, dados.size() - 1)).split("/");
									e.setNomeArquivoCorrecao(String.format("%d-%d.%s", e.getId().getAluno().getId(), e.getId().getAtividade().getId(), "pdf"));
									arquivoService.salvarArquivo(Dir.CORRECAO, e.getNomeArquivoCorrecao());
									e.setCorrigido(true);
								}
								
							} else if(curso.getStatus().equals(StatusCurso.EM_ANDAMENTO)) {
								dados =  getDadosAtividadeEntregueEmAndamento(atividade);
								if(dados.size() > 0) {
									dado = dados.get(RandomUtils.random(0, dados.size() - 1)).split("/");
									boolean isCorrigido = Boolean.parseBoolean(dado[3]);
									if(isCorrigido) {
										e.setCorrigido(true);
										e.setNomeArquivoCorrecao(String.format("%d-%d.%s", e.getId().getAluno().getId(), e.getId().getAtividade().getId(), "pdf"));
										arquivoService.salvarArquivo(Dir.CORRECAO, e.getNomeArquivoCorrecao());
									} else {
										e.setCorrigido(false);
									}
								}
							}
							if(dado != null) {
								e.setDataEntrega(LocalDate.parse(dado[1]));
								e.setComentario(dado[2]);
								e.setNota(Double.parseDouble(dado[0]));
								e.setNomeArquivoEntrega(String.format("%d-%d.%s", e.getId().getAluno().getId(), e.getId().getAtividade().getId(), "pdf"));
								arquivoService.salvarArquivo(Dir.ENTREGA, e.getNomeArquivoEntrega());
								entregaRepository.save(e);
							}
				}
			}
				if(curso.getStatus().equals(StatusCurso.CONCLUIDO)){
					professorService.fecharNotas(curso.getId());
				}
		}
	}
	
	private List<String> getDadosAtividadeEntregueEmAndamento(Atividade atividade) {
		List<String> dados = new ArrayList<>();
		int x = RandomUtils.random(1, 10);
		if(x > 5) {
			LocalDate dataFinal = atividade.getDataFinal();
			LocalDate now = LocalDate.now();
			if(atividade.getDataFinal().isAfter(now) || atividade.getDataFinal().equals(now)) {
				String d1 = String.format("%s/%s/%s/%s", "0.0", now.minusDays(2).toString(), "Consegui fazer de forma tranquila, percebi que entendi muito bem o conteúdo!", "false");
				dados.add(d1);
				String d2 = String.format("%s/%s/%s/%s", "0.0", now.minusDays(2).toString(), " ", "false");
				dados.add(d2);
				String d3 = String.format("%s/%s/%s/%s", "0.0", now.minusDays(2).toString(), " ", "false");
				dados.add(d3);
				String d4 = String.format("%s/%s/%s/%s", "0.0", now.minusDays(2).toString(), "Foi bem fácil de fazer", "false");
				dados.add(d4);
				String d5 = String.format("%s/%s/%s/%s", "0.0", now.minusDays(2).toString(), " ", "false");
				dados.add(d5);
				String d6 = String.format("%s/%s/%s/%s", "7.0", now.minusDays(3).toString(), "Me esforcei bastante pra fazer, mas no fim consegui terminar", "true");
				dados.add(d6);
				String d7 = String.format("%s/%s/%s/%s", "8.3", now.minusDays(3).toString(), " ", "true");
				dados.add(d7);
				String d8 = String.format("%s/%s/%s/%s", "7.9", now.minusDays(3).toString(), "Achei bastante interessante essa atividade, com aquela tirei algumas dúvidas que tinha", "true");
				dados.add(d8);
				String d9 = String.format("%s/%s/%s/%s", "9.8", now.minusDays(3).toString(), " ", "true");
				dados.add(d9);
				String d10 = String.format("%s/%s/%s/%s", "6.7", now.minusDays(3).toString(), " ", "true");
				dados.add(d10);
				String d11 = String.format("%s/%s/%s/%s", "0.0", now.minusDays(1).toString(), "Gostei de fazer essa atividade, acredito que aprendi bastante", "false");
				dados.add(d11);
				String d12 = String.format("%s/%s/%s/%s", "0.0", now.minusDays(1).toString(), " ", "false");
				dados.add(d12);
				String d13 = String.format("%s/%s/%s/%s", "0.0", now.minusDays(1).toString(), " ", "false");
				dados.add(d13);
				String d14 = String.format("%s/%s/%s/%s", "0.0", now.minusDays(1).toString(), "Depois de muito esforço consegui fazer", "false");
				dados.add(d14);
				String d15 = String.format("%s/%s/%s/%s", "0.0", now.minusDays(1).toString(), " ", "false");
				dados.add(d15);
				String d16 = String.format("%s/%s/%s/%s", "5.4", now.minusDays(3).toString(), "Precisei fazer bastante pesquisar para conseguir fazer, estou com dificuldades nesse conteúdo", "true");
				dados.add(d16);
				String d17 = String.format("%s/%s/%s/%s", "4.5", now.minusDays(3).toString(), " ", "true");
				dados.add(d17);
				String d18 = String.format("%s/%s/%s/%s", "4.6", now.minusDays(2).toString(), " ", "true");
				dados.add(d18);
				String d19 = String.format("%s/%s/%s/%s", "5.5", now.minusDays(2).toString(), " ", "true");
				dados.add(d19);
				String d20 = String.format("%s/%s/%s/%s", "3.7", now.minusDays(2).toString(), "Preciso rever todo o conteúdo, pois acredito que fui mal nessa atividade porque realmente parece que não aprendi quase nada", "true");
				dados.add(d20);
				String d21 = String.format("%s/%s/%s/%s", "0.0", now.toString(), " ", "false");
				dados.add(d21);
				String d22 = String.format("%s/%s/%s/%s", "0.0", now.toString(), "Deu bastante trabalho para fazer, mas acredito que fui bem", "false");
				dados.add(d22);
				String d23 = String.format("%s/%s/%s/%s", "0.0", now.toString(), " ", "false");
				dados.add(d23);
			} else {
				String d1 = String.format("%s/%s/%s/%s", "0.0", dataFinal.minusDays(2).toString(), "Acho que entendi muito bem o conteúdo!", "false");
				dados.add(d1);
				String d2 = String.format("%s/%s/%s/%s", "0.0", dataFinal.minusDays(2).toString(), " ", "false");
				dados.add(d2);
				String d3 = String.format("%s/%s/%s/%s", "0.0", dataFinal.minusDays(2).toString(), " ", "false");
				dados.add(d3);
				String d4 = String.format("%s/%s/%s/%s", "0.0", dataFinal.minusDays(2).toString(), "Essa foi fácil de fazer", "false");
				dados.add(d4);
				String d5 = String.format("%s/%s/%s/%s", "0.0", dataFinal.minusDays(2).toString(), " ", "false");
				dados.add(d5);
				String d6 = String.format("%s/%s/%s/%s", "6.5", dataFinal.minusDays(3).toString(), "Acho que entendi e fui bem, mas não tenho certeza", "true");
				dados.add(d6);
				String d7 = String.format("%s/%s/%s/%s", "7.3", dataFinal.minusDays(3).toString(), " ", "true");
				dados.add(d7);
				String d8 = String.format("%s/%s/%s/%s", "7.9", dataFinal.minusDays(3).toString(), " ", "true");
				dados.add(d8);
				String d9 = String.format("%s/%s/%s/%s", "9.8", dataFinal.minusDays(3).toString(), " ", "true");
				dados.add(d9);
				String d10 = String.format("%s/%s/%s/%s", "10.0", dataFinal.minusDays(3).toString(), "Acredito que dominei o conteúdo passado até aqui e foi bem fácil de fazer essa atividade", "true");
				dados.add(d10);
				String d11 = String.format("%s/%s/%s/%s", "9.1", dataFinal.minusDays(2).toString(), "Aprendi bastante ao fazer essa atividade", "true");
				dados.add(d11);
				String d12 = String.format("%s/%s/%s/%s", "8.0", dataFinal.minusDays(2).toString(), " ", "true");
				dados.add(d12);
				String d13 = String.format("%s/%s/%s/%s", "5.0", dataFinal.minusDays(3).toString(), " ", "true");
				dados.add(d13);
				String d14 = String.format("%s/%s/%s/%s", "0.0", dataFinal.toString(), "Depois de muitas tentativas consegui fazer", "false");
				dados.add(d14);
				String d15 = String.format("%s/%s/%s/%s", "0.0", dataFinal.toString(), " ", "false");
				dados.add(d15);
			}
			
			if(atividade.isPermiteEntregaAtrasada()) {
				Period period = Period.between(dataFinal, now);
				int diasDeDiferenca = period.getDays();
				String d16 = String.format("%s/%s/%s/%s", "6.5", dataFinal.plusDays(RandomUtils.random(0, diasDeDiferenca - 1)).toString(), "Tive alguns problemas pessoais e acabei perdendo a data final de entrega", "true");
				dados.add(d16);
				String d17 = String.format("%s/%s/%s/%s", "0.0", dataFinal.plusDays(RandomUtils.random(0, diasDeDiferenca - 1)).toString(), " ", "false");
				dados.add(d17);
				String d18 = String.format("%s/%s/%s/%s", "0.0", dataFinal.plusDays(RandomUtils.random(0, diasDeDiferenca - 1)).toString(), "Meu computador deu problema bem nessa semana e por isso não consegui fazer e entregar antes", "false");
				dados.add(d18);
				String d19 = String.format("%s/%s/%s/%s", "7.1", dataFinal.plusDays(RandomUtils.random(0, diasDeDiferenca - 1)).toString(), " ", "true");
				dados.add(d19);
				String d20 = String.format("%s/%s/%s/%s", "0.0", dataFinal.plusDays(RandomUtils.random(0, diasDeDiferenca - 1)).toString(), "Atrasei por dificuldade de entender, mas consegui terminar", "false");
				dados.add(d20);
				String d21 = String.format("%s/%s/%s/%s", "0.0", dataFinal.plusDays(RandomUtils.random(0, diasDeDiferenca - 1)).toString(), " ", "false");
				dados.add(d21);
				String d22 = String.format("%s/%s/%s/%s", "0.0", dataFinal.plusDays(RandomUtils.random(0, diasDeDiferenca - 1)).toString(), "Atrasei por que esta muito dificil de fazer", "false");
				dados.add(d22);
				String d23 = String.format("%s/%s/%s/%s", "4.7", dataFinal.plusDays(RandomUtils.random(0, diasDeDiferenca - 1)).toString(), " ", "true");
				dados.add(d23);
				String d24 = String.format("%s/%s/%s/%s", "3.6", dataFinal.plusDays(RandomUtils.random(0, diasDeDiferenca - 1)).toString(), "Acho que não entendi quase nada, pois não tava conseguindo fazer mas após muita insistência consegui terminar mas acabei me atrasando", "true");
				dados.add(d24);
				String d25 = String.format("%s/%s/%s/%s", "7.8", dataFinal.plusDays(RandomUtils.random(0, diasDeDiferenca - 1)).toString(), " ", "true");
				dados.add(d25);
				String d26 = String.format("%s/%s/%s/%s", "6.7", dataFinal.plusDays(RandomUtils.random(0, diasDeDiferenca - 1)).toString(), " ", "true");
				dados.add(d26);
			}
		}
		return dados;
	}

	private List<String> getDadosAtividadeEntregueConcluida(Atividade atividade){
		List<String> dados = new ArrayList<>();
		LocalDate dataFinal = atividade.getDataFinal();
		String d1 = String.format("%s/%s/%s", "7.9", dataFinal.minusDays(3).toString(), "Consegui fazer de forma tranquila, percebi que entendi muito bem o conteúdo!");
		dados.add(d1);
		String d2 = String.format("%s/%s/%s", "8.1", dataFinal.minusDays(3).toString(), " ");
		dados.add(d2);
		String d3 = String.format("%s/%s/%s", "7.5", dataFinal.minusDays(3).toString(), " ");
		dados.add(d3);
		String d4 = String.format("%s/%s/%s", "9.5", dataFinal.minusDays(3).toString(), "Foi bem fácil de fazer");
		dados.add(d4);
		String d5 = String.format("%s/%s/%s", "6.5", dataFinal.minusDays(3).toString(), " ");
		dados.add(d5);
		String d6 = String.format("%s/%s/%s", "6.1", dataFinal.minusDays(2).toString(), " ");
		dados.add(d6);
		String d7 = String.format("%s/%s/%s", "10", dataFinal.minusDays(2).toString(), "Fiz sem nenhuma dificuldade professor!");
		dados.add(d7);
		String d8 = String.format("%s/%s/%s", "10.0", dataFinal.minusDays(2).toString(), " ");
		dados.add(d8);
		String d9 = String.format("%s/%s/%s", "4.5", dataFinal.minusDays(2).toString(), " ");
		dados.add(d9);
		String d10 = String.format("%s/%s/%s", "3.5", dataFinal.minusDays(2).toString(), "Tive muita dificuldade para fazer, e sinto que não aprendi quase nada");
		dados.add(d10);
		String d11 = String.format("%s/%s/%s", "4.6", dataFinal.minusDays(1).toString(), " ");
		dados.add(d11);
		String d12 = String.format("%s/%s/%s", "5.6", dataFinal.minusDays(1).toString(), "Preciso tirar algumas dúvidas sobre o conteúdo, pois foi muito díficil pra mim fazer");
		dados.add(d12);
		String d13 = String.format("%s/%s/%s", "7.7", dataFinal.minusDays(1).toString(), " ");
		dados.add(d13);
		String d14 = String.format("%s/%s/%s", "8", dataFinal.minusDays(1).toString(), " ");
		dados.add(d14);
		String d15 = String.format("%s/%s/%s", "8.2", dataFinal.minusDays(1).toString(), "Tive um pouco de dificuldade para fazer, mas acredito que tive um bom entendimento do conteúdo");
		dados.add(d15);
		String d16 = String.format("%s/%s/%s", "6", dataFinal.toString(), "Achei difícil essa atividade, porém acredito que não eu não deva ir tão mal");
		dados.add(d16);
		String d17 = String.format("%s/%s/%s", "6.7", dataFinal.toString(), " ");
		dados.add(d17);
		String d18 = String.format("%s/%s/%s", "7.9", dataFinal.toString(), " ");
		dados.add(d18);
		String d19 = String.format("%s/%s/%s", "8.9", dataFinal.toString(), " ");
		dados.add(d19);
		String d20 = String.format("%s/%s/%s", "9.5", dataFinal.toString(), " ");
		dados.add(d20);
		String d21 = String.format("%s/%s/%s", "4.1", dataFinal.toString(), "Quase não consegui fazer, preciso rever o conteúdo");
		dados.add(d21);
		String d22 = String.format("%s/%s/%s", "5.4", dataFinal.toString(), " ");
		dados.add(d22);
		String d23 = String.format("%s/%s/%s", "3.4", dataFinal.toString(), "Sinto que fui muito mal nessa atividade, pois realmente não entendi quase nada do conteúdo");
		dados.add(d23);
		String d24 = String.format("%s/%s/%s", "5.5", dataFinal.toString(), " ");
		dados.add(d24);
		String d25 = String.format("%s/%s/%s", "4.9", dataFinal.toString(), "Acredito que fui bem nessa atividade, foi tranquilo de fazer");
		dados.add(d25);
		if(atividade.isPermiteEntregaAtrasada()) {
			String d26 = String.format("%s/%s/%s", "6.5", dataFinal.plusDays(1).toString(), "Tive alguns problemas e acabei perdendo a data final de entrega");
			dados.add(d26);
			String d27 = String.format("%s/%s/%s", "6.6", dataFinal.plusDays(1).toString(), " ");
			dados.add(d27);
			String d28 = String.format("%s/%s/%s", "6.2", dataFinal.plusDays(2).toString(), "Meu computador pifou bem nessa semana e por isso não consegui fazer e entregar antes");
			dados.add(d28);
			String d29 = String.format("%s/%s/%s", "7.1", dataFinal.plusDays(1).toString(), " ");
			dados.add(d29);
			String d30 = String.format("%s/%s/%s", "7.3", dataFinal.plusDays(1).toString(), "Atrasei alguns probleminhas pessoais que tive");
			dados.add(d30);
			String d31 = String.format("%s/%s/%s", "4.5", dataFinal.plusDays(3).toString(), " ");
			dados.add(d31);
			String d32 = String.format("%s/%s/%s", "4.6", dataFinal.plusDays(1).toString(), "Não estava conseguindo fazer, por isso acabei atrasando");
			dados.add(d32);
			String d33 = String.format("%s/%s/%s", "4.7", dataFinal.plusDays(3).toString(), " ");
			dados.add(d33);
			String d34 = String.format("%s/%s/%s", "3.6", dataFinal.plusDays(5).toString(), "Atrasei a entrega porque estava com dificuldade e por vezes eu pensei em nem entregar, realmente não entendi quase nada.");
			dados.add(d34);
			String d35 = String.format("%s/%s/%s", "5.8", dataFinal.plusDays(3).toString(), " ");
			dados.add(d35);
			String d36 = String.format("%s/%s/%s", "5.7", dataFinal.plusDays(4).toString(), " ");
			dados.add(d36);
		}
		
		return dados;
	}
	
}

package com.homework.domain.aluno.application.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.homework.domain.atividade.Atividade;
import com.homework.domain.atividade.Entrega;
import com.homework.utils.IOUtils;

import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;

@SuppressWarnings("deprecation")
@Service
public class ArquivoService {

	@Value("${homework.files.arquivoAtividade}")
	private String arquivoAtividadeDir;
	
	@Value("${homework.files.entregaAtividade}")
	private String arquivoEntregaDir;
	
	@Value("${homework.files.correcaoAtividade}")
	private String arquivoCorrecaoDir;
	
	@Value("${homework.files.relatorios}")
	private String arquivoRelatoriosDir;
	
	
	public enum Dir {
		ATIVIDADE, ENTREGA, CORRECAO, RELATORIOS;
	}
	
	public void uploadArquivo(MultipartFile multipartFile, String nomeArquivo, Dir diretorio) throws ApplicationException {
		try {
			String dir;
			if(diretorio.equals(Dir.ATIVIDADE)) {
				dir = arquivoAtividadeDir;
			} else if(diretorio.equals(Dir.ENTREGA)) {
				dir = arquivoEntregaDir;
			} else if(diretorio.equals(Dir.CORRECAO)) {
				dir = arquivoCorrecaoDir;
			} else {
				dir = arquivoRelatoriosDir;
			}
			IOUtils.copy(multipartFile.getInputStream(), nomeArquivo, dir);
		} catch(Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	public void downloadArquivoAtividade(HttpServletResponse response, Atividade atividade) throws IOException {
		File file = new File(arquivoAtividadeDir + "//" + atividade.getNomeArquivo());
		String nomeDoArquivo = String.format("%s.%s", atividade.getTitulo().replace(" ", ""), atividade.getNomeArquivo().split("\\.")[1]);
		downloadArquivo(response, file, nomeDoArquivo);
	}
	
	public void downloadArquivoCorrecao(HttpServletResponse response, Entrega entrega) throws IOException {
		File file = new File(arquivoCorrecaoDir + "//" + entrega.getNomeArquivoCorrecao());
		String nomeDoArquivo = String.format("%sCorrecao.%s", entrega.getId().getAtividade().getTitulo().replace(" ", ""), entrega.getNomeArquivoCorrecao().split("\\.")[1]);
		downloadArquivo(response, file, nomeDoArquivo);
	}
	
	public void downloadArquivoEntrega(HttpServletResponse response, Entrega entrega) throws IOException {
		File file = new File(arquivoEntregaDir + "//" + entrega.getNomeArquivoEntrega());
		String nomeDoArquivo = String.format("%s.%s", entrega.getId().getAluno().getNome(), entrega.getNomeArquivoEntrega().split("\\.")[1]);
		downloadArquivo(response, file, nomeDoArquivo);
	}
	
	public void downloadArquivo(HttpServletResponse response, File arquivo, String nomeArquivoDeSaida) throws IOException {
		if(arquivo.exists()) {
			response.setContentType("application/octet-stream");
			response.setContentLength((int) arquivo.length());
			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"", nomeArquivoDeSaida);
			response.setHeader(headerKey, headerValue);
			OutputStream outputStream = response.getOutputStream();
			FileInputStream fileInputStream = new FileInputStream(arquivo);
			byte[] bytes = new byte[4096];
			int read = 0;
			while((read = fileInputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			
			fileInputStream.close();
			outputStream.flush();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public String gerarRelatorio(List<?> listaBeanDataCollection, HashMap<String, Object> parametrosRelatorio,
			String nomeRelatorioJasper, String nomeRelatorioSaida) throws Exception {
		JRBeanCollectionDataSource jrbcds = new JRBeanCollectionDataSource(listaBeanDataCollection);
		String caminhoArquivoJasper = arquivoRelatoriosDir + File.separator + nomeRelatorioJasper + ".jasper";
		parametrosRelatorio.put("REPORT_PARAMETERS_IMG", arquivoRelatoriosDir);
		JasperReport relatorioJasper = (JasperReport) JRLoader.loadObjectFromFile(caminhoArquivoJasper);
		parametrosRelatorio.put("SUBREPORT_DIR", arquivoRelatoriosDir + File.separator);
		JasperPrint impressoraJasper = JasperFillManager.fillReport(relatorioJasper, parametrosRelatorio, jrbcds);
		JRExporter exporter = new JRPdfExporter();
		String caminhoArquivoRelatorio = arquivoRelatoriosDir + File.separator + nomeRelatorioSaida + ".pdf";
		File arquivoGerado = new File(caminhoArquivoRelatorio);
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, impressoraJasper);
		exporter.setParameter(JRExporterParameter.OUTPUT_FILE, arquivoGerado);
		exporter.exportReport();
		arquivoGerado.deleteOnExit();
		return caminhoArquivoRelatorio;
	}
	
	public void excluirArquivo(String nomeArquivo, Dir dir) {
		String diretorio = ""; 
		if(dir.equals(Dir.ATIVIDADE)) {
			diretorio = arquivoAtividadeDir;
		} else if(dir.equals(Dir.CORRECAO)) {
			diretorio = arquivoCorrecaoDir;
		} else if(dir.equals(Dir.ENTREGA)) {
			diretorio = arquivoEntregaDir;
		} else if(dir.equals(Dir.RELATORIOS)) {
			diretorio = arquivoRelatoriosDir;
		}
		File file = new File(diretorio + "//" + nomeArquivo);
		if(file.exists()) {
			file.delete();
		}
	}
}

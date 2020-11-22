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
	
	public void uploadArquivoAtividade(MultipartFile multipartFile, String nomeArquivo) throws ApplicationException {
		try {
			IOUtils.copy(multipartFile.getInputStream(), nomeArquivo, arquivoAtividadeDir);
		} catch(Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	public void uploadEntrega(MultipartFile multipartFile, String nomeArquivo) throws ApplicationException {
		try {
			IOUtils.copy(multipartFile.getInputStream(), nomeArquivo, arquivoEntregaDir);
		} catch (Exception e) {
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
		String nomeDoArquivo = String.format("%sCorrecao.%s", entrega.getId().getAtividade().getTitulo().replace(" ", ""), entrega.getId().getAtividade().getNomeArquivo().split("\\.")[1]);
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
		//TODO descomentar linha abaixo
		//arquivoGerado.deleteOnExit();
		return caminhoArquivoRelatorio;
	}
}

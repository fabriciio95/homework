package com.homework.domain.aluno.application.service;


public class ArquivoException extends Exception {
	private static final long serialVersionUID = 1L;

	public ArquivoException(String msg) {
		super(msg);
	}

	public ArquivoException(Throwable cause) {
		super(cause);
	}
}

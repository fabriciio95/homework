package com.homework.domain.aluno.application.service;


public class EntregaAtrasadaException extends Exception {
	private static final long serialVersionUID = 1L;

	public EntregaAtrasadaException(String msg) {
		super(msg);
	}

	public EntregaAtrasadaException(Throwable cause) {
		super(cause);
	}
}

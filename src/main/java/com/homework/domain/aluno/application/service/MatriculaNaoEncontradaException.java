package com.homework.domain.aluno.application.service;


public class MatriculaNaoEncontradaException extends Exception {
	private static final long serialVersionUID = 1L;

	public MatriculaNaoEncontradaException(String msg) {
		super(msg);
	}

	public MatriculaNaoEncontradaException(Throwable cause) {
		super(cause);
	}
}

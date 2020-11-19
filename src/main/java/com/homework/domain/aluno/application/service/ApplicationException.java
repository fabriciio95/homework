package com.homework.domain.aluno.application.service;


public class ApplicationException extends Exception {
	private static final long serialVersionUID = 1L;

	public ApplicationException(String msg) {
		super(msg);
	}

	public ApplicationException(Throwable cause) {
		super(cause);
	}
}

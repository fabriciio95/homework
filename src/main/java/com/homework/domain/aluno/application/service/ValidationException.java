package com.homework.domain.aluno.application.service;


public class ValidationException extends Exception {
	private static final long serialVersionUID = 1L;

	public ValidationException(String msg) {
		super(msg);
	}

	public ValidationException(Throwable cause) {
		super(cause);
	}
}

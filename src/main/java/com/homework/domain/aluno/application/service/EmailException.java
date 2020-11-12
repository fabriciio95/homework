package com.homework.domain.aluno.application.service;


public class EmailException extends Exception {
	private static final long serialVersionUID = 1L;

	public EmailException(String msg) {
		super(msg);
	}

	public EmailException(Throwable cause) {
		super(cause);
	}
}

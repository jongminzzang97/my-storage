package com.jongmin.mystorage.exception;

public class FileNotInDatabaseException extends RuntimeException {
	public FileNotInDatabaseException(String message) {
		super(message);
	}
}

package com.jongmin.mystorage.exception;

public class FileAlreadyExistException extends RuntimeException {
	public FileAlreadyExistException(String message) {
		super(message);
	}
}

package com.jongmin.mystorage.exception;

public class FileNotInFileSystemException extends RuntimeException {
	public FileNotInFileSystemException(String message) {
		super(message);
	}
}

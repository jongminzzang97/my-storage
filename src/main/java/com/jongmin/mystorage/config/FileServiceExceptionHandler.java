package com.jongmin.mystorage.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jongmin.mystorage.exception.ErrorResult;
import com.jongmin.mystorage.exception.FileAlreadyExistException;
import com.jongmin.mystorage.exception.FileStorageException;
import com.jongmin.mystorage.exception.OwnerNameException;

@RestControllerAdvice
public class FileServiceExceptionHandler {
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(FileAlreadyExistException.class)
	public ErrorResult handleFileAlreadyExistsException(FileAlreadyExistException ex) {
		return new ErrorResult("0001", ex.getMessage());
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(FileStorageException.class)
	public ErrorResult handleFileStorageException(FileStorageException ex) {
		return new ErrorResult("0002", ex.getMessage());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(OwnerNameException.class)
	public ErrorResult handleOwnerNameException(OwnerNameException ex) {
		return new ErrorResult("0003", ex.getMessage());
	}
}

package com.jongmin.mystorage.config;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jongmin.mystorage.exception.ErrorResult;

public class SpringExceptionHandler {

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(BindException.class)
	public ErrorResult handleFileAlreadyExistsException(BindException ex) {
		String defaultMessage = ex.getAllErrors().get(0).getDefaultMessage();
		return new ErrorResult("0101", defaultMessage);
	}
}

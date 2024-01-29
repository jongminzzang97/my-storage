package com.jongmin.mystorage.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ErrorResult {
	private String code;
	private String message;
}

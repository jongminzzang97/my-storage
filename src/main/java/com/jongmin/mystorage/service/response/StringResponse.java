package com.jongmin.mystorage.service.response;

import lombok.Getter;

@Getter
public class StringResponse {
	private String response;

	public StringResponse(String response) {
		this.response = response;
	}
}

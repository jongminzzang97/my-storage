package com.jongmin.mystorage.service.request;

import com.jongmin.mystorage.model.MyFile;

import lombok.Builder;
import lombok.Getter;

@Getter
public class DefaultFileRequest {

	private String fileName;
	private String owner;

	@Builder
	private DefaultFileRequest(String fileName, String owner) {
		this.fileName = fileName;
		this.owner = owner;
	}

	public static DefaultFileRequest defaultFileRequestFromFileNameAndOwner(String fileName, String owner) {
		return DefaultFileRequest.builder()
			.fileName(fileName)
			.owner(owner)
			.build();
	}

	public MyFile toMyFileEntity() {
		return MyFile.builder()
			.name(fileName)
			.owner(owner)
			.build();
	}
}

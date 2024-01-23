package com.jongmin.mystorage.service.request;

import com.jongmin.mystorage.model.MyFile;

import lombok.Builder;
import lombok.Getter;

@Getter
public class FileDownloadRequest {

	private String fileName;
	private String owner;

	@Builder
	private FileDownloadRequest(String fileName, String owner) {
		this.fileName = fileName;
		this.owner = owner;
	}

	public static FileDownloadRequest fileDownloadRequestFromFileNameAndOwner(String fileName, String owner) {
		return FileDownloadRequest.builder()
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

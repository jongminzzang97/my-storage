package com.jongmin.mystorage.service.file;

import com.jongmin.mystorage.model.MyFile;

import lombok.Builder;
import lombok.Getter;

@Getter
public class FileServiceResponse {
	private String owner;
	private String fileName;

	@Builder
	private FileServiceResponse(String owner, String fileName) {
		this.owner = owner;
		this.fileName = fileName;
	}

	public static FileServiceResponse of(MyFile myFile) {
		return FileServiceResponse.builder()
			.fileName(myFile.getFileName())
			.owner(myFile.getOwnerName())
			.build();
	}
}

package com.jongmin.mystorage.service.request;

import org.springframework.web.multipart.MultipartFile;

import com.jongmin.mystorage.model.MyFile;

import lombok.Builder;
import lombok.Getter;

@Getter
public class FileUploadRequest {

	private String fileName;
	private String owner;
	private MultipartFile multipartFile;

	@Builder
	public FileUploadRequest(String fileName, String owner, MultipartFile multipartFile) {
		this.fileName = fileName;
		this.owner = owner;
		this.multipartFile = multipartFile;
	}

	public MyFile toMyFileEntity() {
		return MyFile.builder()
			.name(fileName)
			.owner(owner)
			.size(multipartFile.getSize())
			.build();
	}
}

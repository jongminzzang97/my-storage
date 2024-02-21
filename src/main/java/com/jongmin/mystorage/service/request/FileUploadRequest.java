package com.jongmin.mystorage.service.request;

import org.springframework.web.multipart.MultipartFile;

import com.jongmin.mystorage.model.MyFile;

import lombok.Builder;
import lombok.Getter;

@Getter
public class FileUploadRequest {

	private String fileName;
	private String ownerName;
	private MultipartFile multipartFile;

	@Builder
	public FileUploadRequest(String fileName, String ownerName, MultipartFile multipartFile) {
		this.fileName = fileName;
		this.ownerName = ownerName;
		this.multipartFile = multipartFile;
	}

	public MyFile toMyFileEntity() {
		return MyFile.builder()
			.fileName(fileName)
			.ownerName(ownerName)
			.size(multipartFile.getSize())
			.build();
	}
}

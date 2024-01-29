package com.jongmin.mystorage.controller.api.dto;

import org.springframework.web.multipart.MultipartFile;

import com.jongmin.mystorage.service.request.FileUploadRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UploadFileRequestDto {

	private String fileName;

	@NotBlank(message = "owner는 Empty이면 안됩니다.")
	private String owner;

	@NotNull(message = "파일을 올려주세요.")
	private MultipartFile multipartFile;

	public FileUploadRequest toFileUploadRequest() {
		if (this.fileName.isBlank()) {
			this.fileName = multipartFile.getOriginalFilename();
		}

		return FileUploadRequest.builder()
			.fileName(fileName)
			.owner(owner)
			.multipartFile(multipartFile)
			.build();
	}
}

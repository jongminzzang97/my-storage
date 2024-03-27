package com.jongmin.mystorage.controller.api.dto;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadSharedFileRequestDto {
	@NotNull(message = "파일을 올려주세요.")
	private MultipartFile multipartFile;
	private UUID folderUuid;
	private String relativePath;
}

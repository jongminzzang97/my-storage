package com.jongmin.mystorage.controller.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jongmin.mystorage.controller.api.dto.UploadFileRequestDto;
import com.jongmin.mystorage.service.file.FileService;
import com.jongmin.mystorage.service.file.FileServiceResponse;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class FileApiController {

	private final FileService fileService;
	@PostMapping("/api/upload")
	public FileServiceResponse uploadFile(@Valid UploadFileRequestDto requestDto) {
		return fileService.uploadFile(requestDto.toFileUploadRequest());
	}
}

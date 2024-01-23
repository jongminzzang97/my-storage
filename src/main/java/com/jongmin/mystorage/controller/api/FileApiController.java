package com.jongmin.mystorage.controller.api;

import static com.jongmin.mystorage.service.request.FileDownloadRequest.*;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jongmin.mystorage.controller.api.dto.UploadFileRequestDto;
import com.jongmin.mystorage.service.file.FileService;
import com.jongmin.mystorage.service.file.FileServiceResponse;
import com.jongmin.mystorage.service.request.FileDownloadRequest;

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

	@GetMapping("/api/download/{owner}/{fileName}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String owner, @PathVariable String fileName) {

		FileDownloadRequest request = fileDownloadRequestFromFileNameAndOwner(fileName, owner);
		Resource fileResource = fileService.downloadFile(request);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDispositionFormData("attachment", fileName);

		return ResponseEntity.ok()
			.headers(headers)
			.body(fileResource);
	}
}

package com.jongmin.mystorage.controller.api;

import static com.jongmin.mystorage.service.request.DefaultFileRequest.*;

import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.jongmin.mystorage.controller.api.dto.UploadFileRequestDto;
import com.jongmin.mystorage.service.file.FileService;
import com.jongmin.mystorage.service.file.FileServiceResponse;
import com.jongmin.mystorage.service.request.DefaultFileRequest;
import com.jongmin.mystorage.service.response.FileResponse;
import com.jongmin.mystorage.service.response.StringResponse;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class FileApiController {

	private final FileService fileService;

	@PostMapping("/api/files/upload")
	public FileResponse uploadFile(@RequestHeader("ownerName") String ownerName,
									@ModelAttribute UploadFileRequestDto requestDto) {
		return fileService.uploadFile(ownerName, requestDto);
	}

	@GetMapping("/api/files/{fileUuid}/download")
	public ResponseEntity<Resource> downloadFile(@RequestHeader("ownerName") String ownerName,
												@PathVariable(name = "fileUuid", required = true) UUID fileUuid) {
		Resource fileResource = fileService.downloadFile(ownerName, fileUuid);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDispositionFormData("attachment", fileResource.getFilename());
		return ResponseEntity.ok()
			.headers(headers)
			.body(fileResource);
	}
	
	@GetMapping("/api/files/{fileUuid}")
	public FileResponse readFile(@RequestHeader("ownerName") String ownerName,
								@PathVariable(name = "fileUuid", required = true) UUID fileUuid) {
		return fileService.readFile(ownerName, fileUuid);
	}

	@DeleteMapping("/api/files/{fileUuid}")
	public void deleteFile(@RequestHeader("ownerName") String ownerName,
		@PathVariable(name = "fileUuid", required = true) UUID fileUuid) {
		fileService.deleteFile(ownerName, fileUuid);
	}
}

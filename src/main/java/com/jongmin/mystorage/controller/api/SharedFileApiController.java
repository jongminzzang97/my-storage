package com.jongmin.mystorage.controller.api;

import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.jongmin.mystorage.service.SharedFileService;
import com.jongmin.mystorage.service.response.SharedFileResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SharedFileApiController {

	private final SharedFileService sharedFileService;

	@GetMapping("/api/share/files/{shareFileUuid}")
	public SharedFileResponse readSharedFile(
		@PathVariable(name = "shareFileUuid", required = true) UUID sharedFileUuid) {
		return sharedFileService.readSharedFile(sharedFileUuid);
	}

	@GetMapping("/api/share/files/{shareFileUuid}/download")
	public ResponseEntity<Resource> downloadSharedFile(
		@PathVariable(name = "shareFileUuid", required = true) UUID sharedFileUuid) {

		Resource fileResource = sharedFileService.downloadSharedFile(sharedFileUuid);
		String attachment = fileResource.getFilename().substring(37);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

		headers.setContentDispositionFormData("attachment", attachment);

		return ResponseEntity.ok()
			.headers(headers)
			.body(fileResource);
	}
}

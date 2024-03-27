package com.jongmin.mystorage.controller.api;

import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jongmin.mystorage.controller.api.dto.UploadSharedFileRequestDto;
import com.jongmin.mystorage.service.SharedFolderService;
import com.jongmin.mystorage.service.response.SharedFolderInfoResponse;
import com.jongmin.mystorage.service.response.SharedFolderItem;
import com.jongmin.mystorage.service.response.StringResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class SharedFolderApiController {

	private final SharedFolderService sharedFolderService;

	@GetMapping({"/api/share/folders/{sharedFolderId}", "/api/share/folders/{sharedFolderId}/**"})
	public SharedFolderInfoResponse readFolder(
		@PathVariable(name = "sharedFolderId", required = true) UUID sharedFolderId,
		HttpServletRequest request) {
		if (request.getRequestURI().length() == 54) {
			return sharedFolderService.readFolder(sharedFolderId, null);
		} else {
			String relativePath = request.getRequestURI().substring(55);
			return sharedFolderService.readFolder(sharedFolderId, relativePath);
		}
	}

	@GetMapping("/api/share/folders/download/{sharedFolderId}/**")
	public ResponseEntity<Resource> downloadFileInSharedFolder(
		@PathVariable(name = "sharedFolderId", required = true) UUID sharedFolderId,
		HttpServletRequest request) {
		Resource fileResource = null;
		if (request.getRequestURI().length() <= 63) {
			throw new RuntimeException("공유된 폴더에 자체에 대해선 다운로드 요청을 보낼 수 없습니다. 파일을 다운로드 요청해주세요.");
		} else {
			String relativePath = request.getRequestURI().substring(64);
			System.out.println("relativePath = " + relativePath);
			fileResource = sharedFolderService.downloadFile(sharedFolderId, relativePath);
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

		String attachment = fileResource.getFilename().substring(37);
		headers.setContentDispositionFormData("attachment", attachment);
		return ResponseEntity.ok()
			.headers(headers)
			.body(fileResource);
	}

	@PostMapping("/api/share/folders/upload")
	public SharedFolderItem uploadFileInSharedFolder(UploadSharedFileRequestDto requestDto) {
		return sharedFolderService.uploadFile(requestDto);
	}

	@DeleteMapping("/api/share/folders/{sharedFolderId}/**")
	public StringResponse deleteFileInSharedFolder(
		@PathVariable(name = "sharedFolderId", required = true) UUID sharedFolderId,
		HttpServletRequest request) {
		String relativePath = request.getRequestURI().substring(55);
		return sharedFolderService.deleteFile(sharedFolderId, relativePath);
	}
}

package com.jongmin.mystorage.controller.api;

import java.util.HashMap;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jongmin.mystorage.controller.api.dto.FolderUpdateDto;
import com.jongmin.mystorage.controller.api.dto.MoveRequestDto;
import com.jongmin.mystorage.service.SharedFolderService;
import com.jongmin.mystorage.service.folder.FolderService;
import com.jongmin.mystorage.service.response.FolderInfoResponse;
import com.jongmin.mystorage.service.response.FolderResponse;
import com.jongmin.mystorage.service.response.SharedFolderResponse;
import com.jongmin.mystorage.service.response.StringResponse;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class FolderApiController {

	private final FolderService folderService;
	private final SharedFolderService sharedFolderService;

	@PostMapping({"/api/folders", "/api/folders/{parentFolderId}"})
	public FolderResponse createFolder(@RequestBody HashMap<String, String> map,
		@PathVariable(name = "parentFolderId", required = false) UUID parentFolderId,
		@RequestHeader("ownerName") String ownerName) {
		return folderService.createFolder(ownerName, map.get("folderName"), parentFolderId);
	}

	@GetMapping({"/api/folders", "/api/folders/{folderId}"})
	public FolderInfoResponse readFolder(@PathVariable(name = "folderId", required = false) UUID folderId,
		@RequestHeader("ownerName") String ownerName) {
		return folderService.readFolder(ownerName, folderId);
	}

	@PutMapping("/api/folders/{folderId}")
	public FolderInfoResponse updateFolder(@RequestBody FolderUpdateDto folderUpdateDto,
		@RequestParam("folderId") UUID folderId,
		@RequestHeader("ownerName") String ownerName) {
		return null;
	}

	@DeleteMapping("/api/folders/{folderId}")
	public StringResponse deleteFolder(@PathVariable(name = "folderId", required = false) UUID folderId,
		@RequestHeader("ownerName") String ownerName) {
		return folderService.deleteFolder(ownerName, folderId);
	}

	@PostMapping("/api/folders/{folderUuid}/move")
	public FolderResponse moveFile(@RequestHeader("ownerName") String ownerName,
		@PathVariable(name = "folderUuid", required = true) UUID transferFolderUuid,
		@RequestBody MoveRequestDto requestDto) {
		return folderService.moveFolder(ownerName, transferFolderUuid, requestDto.getDestFolderUuid());
	}

	@PostMapping("/api/folders/{folderUuid}/share")
	public SharedFolderResponse shareFile(@RequestHeader("ownerName") String ownerName,
		@PathVariable(name = "folderUuid", required = true) UUID folderUuid) {
		return sharedFolderService.createSharedFolder(ownerName, folderUuid);
	}
}

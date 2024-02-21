package com.jongmin.mystorage.service.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jongmin.mystorage.model.MyFolder;
import com.jongmin.mystorage.model.enums.FileItemType;

import lombok.Builder;
import lombok.Getter;

@Getter
public class FolderResponse {
	private String folderName;
	private FileItemType type;
	private UUID uuid;
	private String fullPath;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Builder
	public FolderResponse(String folderName, FileItemType type, UUID uuid, String fullPath, LocalDateTime createdAt,
		LocalDateTime updatedAt) {
		this.folderName = folderName;
		this.type = type;
		this.uuid = uuid;
		this.fullPath = fullPath;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static FolderResponse fromMyFolder(MyFolder myFolder) {
		return FolderResponse.builder()
			.uuid(myFolder.getUuid())
			.createdAt(myFolder.getCreatedAt())
			.folderName(myFolder.getFolderName())
			.fullPath(myFolder.getFullPath())
			.type(myFolder.getFileItemType())
			.updatedAt(myFolder.getUpdatedAt())
			.build();
	}

}

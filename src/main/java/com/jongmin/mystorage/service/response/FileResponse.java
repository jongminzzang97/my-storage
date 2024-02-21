package com.jongmin.mystorage.service.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.model.enums.FileItemType;

import lombok.Builder;
import lombok.Getter;

@Getter
public class FileResponse {
	private String fileName;
	private FileItemType type;
	private UUID uuid;
	private String fullPath;
	private Long size;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Builder
	public FileResponse(String fileName, FileItemType type, UUID uuid, String fullPath, Long size,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
		this.fileName = fileName;
		this.type = type;
		this.uuid = uuid;
		this.fullPath = fullPath;
		this.size = size;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static FileResponse fromMyFile(MyFile file) {
		return FileResponse.builder()
			.uuid(file.getUuid())
			.fileName(file.getFileName())
			.type(file.getFileItemType())
			.size(file.getSize())
			.updatedAt(file.getUpdatedAt())
			.createdAt(file.getCreatedAt())
			.build();
	}
}

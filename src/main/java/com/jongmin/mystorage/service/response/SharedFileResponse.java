package com.jongmin.mystorage.service.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.model.SharedFile;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SharedFileResponse {
	private String fileName;
	private UUID uuid;
	private Long size;
	private LocalDateTime expiredAt;

	@Builder
	public SharedFileResponse(String fileName, UUID uuid, Long size, LocalDateTime expiredAt) {
		this.fileName = fileName;
		this.uuid = uuid;
		this.size = size;
		this.expiredAt = expiredAt;
	}

	public static SharedFileResponse fromSharedFile(SharedFile sharedFile) {
		MyFile file = sharedFile.getMyFile();
		return SharedFileResponse.builder()
			.fileName(file.getFileName())
			.uuid(sharedFile.getUuid())
			.size(file.getSize())
			.expiredAt(sharedFile.getExpiredAt())
			.build();
	}
}

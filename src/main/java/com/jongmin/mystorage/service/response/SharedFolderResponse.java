package com.jongmin.mystorage.service.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jongmin.mystorage.model.MyFolder;
import com.jongmin.mystorage.model.SharedFolder;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SharedFolderResponse {
	private LocalDateTime expiredAt;
	private UUID sharedId;
	private String folderName;

	@Builder
	public SharedFolderResponse(String folderName, UUID sharedId, LocalDateTime expiredAt) {
		this.folderName = folderName;
		this.sharedId = sharedId;
		this.expiredAt = expiredAt;
	}

	public static SharedFolderResponse fromSharedFolder(SharedFolder sharedFolder) {
		return SharedFolderResponse.builder()
			.sharedId(sharedFolder.getSharedId())
			.expiredAt(sharedFolder.getExpiredAt())
			.folderName(sharedFolder.getMyFolder().getFolderName())
			.build();
	}
}

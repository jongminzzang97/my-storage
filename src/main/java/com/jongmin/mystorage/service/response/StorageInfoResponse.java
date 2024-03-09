package com.jongmin.mystorage.service.response;

import com.jongmin.mystorage.model.StorageInfo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorageInfoResponse {
	private Long currentSize;
	private Long maxSize;
	private Long fileCount;
	private Long folderCount;

	@Builder
	public StorageInfoResponse(Long maxSize, Long currentSize, Long fileCount, Long folderCount) {
		this.maxSize = maxSize;
		this.currentSize = currentSize;
		this.fileCount = fileCount;
		this.folderCount = folderCount;
	}

	public static StorageInfoResponse fromStorageInfo(StorageInfo storageInfo) {
		return StorageInfoResponse.builder().maxSize(storageInfo.getMaxSize())
			.currentSize(storageInfo.getSize())
			.fileCount(storageInfo.getFileCount())
			.folderCount(storageInfo.getFolderCount())
			.build();
	}
}

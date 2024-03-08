package com.jongmin.mystorage.model;

import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class StorageInfo extends BaseEntity {
	private String ownerName;
	private Long size;
	private Long fileCount;
	private Long folderCount;

	@Builder
	public StorageInfo(String ownerName, Long size, Long fileCount, Long folderCount) {
		this.ownerName = ownerName;
		this.size = size;
		this.fileCount = fileCount;
		this.folderCount = folderCount;
	}

	public StorageInfo(String ownerName) {
		this.ownerName = ownerName;
		this.size = 0L;
		this.fileCount = 0L;
		this.folderCount = 0L;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public void setFileCount(Long fileCount) {
		this.fileCount = fileCount;
	}

	public void setFolderCount(Long folderCount) {
		this.folderCount = folderCount;
	}
}

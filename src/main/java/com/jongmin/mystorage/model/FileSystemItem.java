package com.jongmin.mystorage.model;

import java.util.UUID;

import com.jongmin.mystorage.model.enums.FileItemStatus;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@MappedSuperclass
@Getter
public abstract class FileSystemItem extends BaseEntity {

	protected UUID uuid;
	protected String ownerName;
	@Enumerated(EnumType.STRING)
	protected FileItemStatus status;

	// 사용자 입장에서 보이는 파일의 경로
	protected String fullPath;
	protected String parentPath;

	@ManyToOne(fetch = FetchType.LAZY)
	protected MyFolder parentFolder;
}

package com.jongmin.mystorage.model;

import java.util.UUID;

import com.jongmin.mystorage.model.enums.FileItemStatus;
import com.jongmin.mystorage.model.enums.FileItemType;

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
	protected String fullPath;
	protected String parentPath;
	@Enumerated(EnumType.STRING)
	protected FileItemStatus status;
	@Enumerated(EnumType.STRING)
	protected FileItemType fileItemType;
	protected String accessRoute;

	@ManyToOne(fetch = FetchType.LAZY)
	protected MyFolder parentFolder;

}

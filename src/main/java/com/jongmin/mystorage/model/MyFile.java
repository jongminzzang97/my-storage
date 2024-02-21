package com.jongmin.mystorage.model;

import com.jongmin.mystorage.model.enums.FileItemStatus;
import com.jongmin.mystorage.model.enums.FileItemType;

import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class MyFile extends FileSystemItem {

	private String fileName;
	private Long size;

	@Builder
	private MyFile(String ownerName, String fileName, Long size, FileItemType fileItemType) {
		this.ownerName = ownerName;
		this.fileName = fileName;
		this.size = size;
		this.status = FileItemStatus.SAVED;
		this.fileItemType = fileItemType;
	}

}


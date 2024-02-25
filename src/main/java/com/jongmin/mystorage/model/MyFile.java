package com.jongmin.mystorage.model;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

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
	private String contentType;
	@Builder
	private MyFile(String ownerName, String fileName, Long size, FileItemType fileItemType, MyFolder parentFolder, String contentType) {
		this.uuid = UUID.randomUUID();
		this.ownerName = ownerName;
		this.size = size;
		this.fileItemType = fileItemType;
		this.contentType = contentType;
		this.parentFolder = parentFolder;

		this.fileName = fileName;
		this.parentPath = parentFolder.getFullPath();
		this.status = FileItemStatus.SAVED;
		this.fullPath = parentFolder.getFullPath() + "/" + fileName;
		this.accessRoute = parentFolder.getAccessRoute() + "/" + this.uuid + "_" +fileName;
	}

	public static MyFile createMyFileEntity(MultipartFile multipartFile, String ownerName, MyFolder parentFolder) {
		return MyFile.builder()
				.ownerName(ownerName)
				.fileName(multipartFile.getOriginalFilename())
				.size(multipartFile.getSize())
				.contentType(multipartFile.getContentType())
				.parentFolder(parentFolder)
				.build();
	}

	public MyFile deleteFile() {
		this.status = FileItemStatus.DELETED;
		return this;
	}

}


package com.jongmin.mystorage.model;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.jongmin.mystorage.model.enums.FileItemStatus;

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

	// 실제 파일에 접근하기 위한 경로
	private String accessRoute;

	@Builder
	public MyFile(UUID uuid, String ownerName, Long size, String contentType, MyFolder parentFolder,
				String fileName, String fullPath, String parentPath, String accessRoute, FileItemStatus status) {
		this.uuid = uuid;
		this.ownerName = ownerName;
		this.size = size;
		this.contentType = contentType;
		this.parentFolder = parentFolder;
		this.fileName = fileName;
		this.fullPath = fullPath;
		this.parentPath = parentPath;
		this.status = status;
		this.accessRoute = accessRoute;
	}

	public static MyFile createMyFileEntity(MultipartFile multipartFile, String ownerName,
											MyFolder parentFolder, UUID uuid) {
		Long size = multipartFile.getSize();
		String contentType = multipartFile.getContentType();
		String fileName = multipartFile.getOriginalFilename();
		FileItemStatus status = FileItemStatus.SAVED;

		String parentPath = parentFolder.getFullPath();
		String fullPath = parentPath + "/" + fileName;
		String accessRoute = ownerName + "/" + uuid + "_"  + fileName;

		return MyFile.builder()
			.uuid(uuid)
			.size(size)
			.ownerName(ownerName)
			.contentType(contentType)
			.fileName(fileName)
			.status(status)
			.parentPath(parentPath)
			.fullPath(fullPath)
			.accessRoute(accessRoute)
			.parentFolder(parentFolder)
			.build();
	}

	public static MyFile createMyFileEntity(MultipartFile multipartFile, String ownerName, MyFolder parentFolder) {
		return createMyFileEntity(multipartFile, ownerName, parentFolder, UUID.randomUUID());
	}

	public MyFile deleteFile() {
		this.status = FileItemStatus.DELETED;
		return this;
	}
}


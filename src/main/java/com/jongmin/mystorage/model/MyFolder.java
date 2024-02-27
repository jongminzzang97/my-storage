package com.jongmin.mystorage.model;

import java.util.List;
import java.util.UUID;

import com.jongmin.mystorage.model.enums.FileItemStatus;
import com.jongmin.mystorage.model.enums.FileItemType;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class MyFolder extends FileSystemItem {

	private String folderName;

	@OneToMany(mappedBy = "parentFolder", fetch = FetchType.LAZY)
	private List<MyFolder> childFolders;
	@OneToMany(mappedBy = "parentFolder", fetch = FetchType.LAZY)
	private List<MyFile> files;

	@Builder
	public MyFolder(UUID uuid, String ownerName, String parentPath, String folderName,
					FileItemStatus status, String fullPath, MyFolder parentFolder, String accessRoute) {
		this.uuid = uuid;
		this.ownerName = ownerName;
		this.parentPath = parentPath;
		this.folderName = folderName;
		this.fileItemType = FileItemType.FOLDER;
		this.fullPath = fullPath;
		this.status = FileItemStatus.SAVED;
		this.parentFolder = parentFolder;
		this.accessRoute = ownerName + "/" + uuid;
	}
}

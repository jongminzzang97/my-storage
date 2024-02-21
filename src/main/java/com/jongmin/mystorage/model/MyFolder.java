package com.jongmin.mystorage.model;

import java.util.List;

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
	public MyFolder(String ownerName, String parentPath, String folderName, FileItemStatus status,
					MyFolder parentFolder, List<MyFolder> childFolders, List<MyFile> files) {
		this.ownerName = ownerName;
		this.parentPath = parentPath;
		this.folderName = folderName;
		this.status = status;
		this.fileItemType = FileItemType.FOLDER;
		this.parentFolder = parentFolder;
		this.childFolders = childFolders;
		this.files = files;
	}
}

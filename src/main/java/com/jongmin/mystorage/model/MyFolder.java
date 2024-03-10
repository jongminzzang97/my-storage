package com.jongmin.mystorage.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.jongmin.mystorage.model.enums.FileItemStatus;

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
		String fullPath, MyFolder parentFolder, FileItemStatus fileItemStatus) {
		this.uuid = uuid;
		this.ownerName = ownerName;
		this.parentPath = parentPath;
		this.folderName = folderName;
		this.fullPath = fullPath;
		this.status = FileItemStatus.SAVED;
		this.parentFolder = parentFolder;
	}

	public static MyFolder createMyFolderEntity(String ownerName, String folderName, MyFolder parentFolder, UUID uuid) {
		String parentPath = "";
		String fullPath = "";
		if (parentFolder != null) {
			parentPath = parentFolder.getFullPath();
			fullPath = parentPath + "/" + folderName;
		}

		FileItemStatus status = FileItemStatus.SAVED;

		return MyFolder.builder()
			.uuid(uuid).ownerName(ownerName).folderName(folderName)
			.parentFolder(parentFolder).parentPath(parentPath).fullPath(fullPath)
			.fileItemStatus(status)
			.build();
	}

	public static MyFolder createMyFolderEntity(String ownerName, String folderName, MyFolder parentFolder) {
		UUID uuid = UUID.randomUUID();
		return createMyFolderEntity(ownerName, folderName, parentFolder, uuid);
	}

	public MyFolder deleteFolder() {
		this.status = FileItemStatus.DELETED;
		return this;
	}

	public MyFolder move(MyFolder destFolder) {
		this.parentFolder = destFolder;
		this.parentPath = destFolder.getFullPath();
		this.fullPath = parentPath + "/" + folderName;
		return this;
	}

	public MyFolder replacePath(String from, String to) {
		this.parentPath = this.getParentPath().replaceFirst(from, to);
		this.fullPath = this.parentPath + "/" + this.folderName;
		return this;
	}

	public MyFolder setUpdateAt(LocalDateTime time) {
		this.updatedAt = time;
		return this;
	}
}

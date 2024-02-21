package com.jongmin.mystorage.service.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.jongmin.mystorage.controller.api.dto.FileResponseDto;
import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.model.MyFolder;
import com.jongmin.mystorage.model.enums.FileItemType;

import lombok.Builder;
import lombok.Getter;

@Getter
public class FolderInfoResponse {
	private String folderName;
	private FileItemType type;
	private UUID uuid;
	private String fullPath;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<FolderResponse> folders;
	private List<FileResponse> files;

	@Builder
	public FolderInfoResponse(String folderName, FileItemType type, UUID uuid, String fullPath, LocalDateTime createdAt,
		LocalDateTime updatedAt, List<FolderResponse> folders, List<FileResponse> files) {
		this.folderName = folderName;
		this.type = type;
		this.uuid = uuid;
		this.fullPath = fullPath;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.folders = folders;
		this.files = files;
	}

	public static FolderInfoResponse fromMyFolder(MyFolder myFolder) {

		List<FolderResponse> folders = new ArrayList<>();
		List<FileResponse> files = new ArrayList<>();

		if (myFolder.getChildFolders() != null) {
			for (MyFolder folder : myFolder.getChildFolders()) {
				folders.add(FolderResponse.fromMyFolder(folder));
			}
		}
		if (myFolder.getFiles() != null) {
			for (MyFile file : myFolder.getFiles()) {
				files.add(FileResponse.fromMyFile(file));
			}
		}

		return FolderInfoResponse.builder()
			.uuid(myFolder.getUuid())
			.createdAt(myFolder.getCreatedAt())
			.folderName(myFolder.getFolderName())
			.fullPath(myFolder.getFullPath())
			.type(myFolder.getFileItemType())
			.updatedAt(myFolder.getUpdatedAt())
			.folders(folders)
			.files(files)
			.build();
	}

}

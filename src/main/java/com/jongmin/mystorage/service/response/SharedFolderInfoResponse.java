package com.jongmin.mystorage.service.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.model.MyFolder;
import com.jongmin.mystorage.model.SharedFolder;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SharedFolderInfoResponse {

	private UUID sharedId;
	private LocalDateTime expiredAt;
	private String relativePath;
	private List<SharedFolderItem> folders;
	private List<SharedFolderItem> files;

	@Builder
	public SharedFolderInfoResponse(UUID sharedId, LocalDateTime expiredAt, String relativePath,
		List<SharedFolderItem> folders, List<SharedFolderItem> files) {
		this.sharedId = sharedId;
		this.expiredAt = expiredAt;
		this.relativePath = relativePath;
		this.folders = folders;
		this.files = files;
	}

	public static SharedFolderInfoResponse createSharedFolderInfoResponse(SharedFolder sharedFolder, MyFolder myFolder,
		String relativePath) {

		List<SharedFolderItem> folderItems = new ArrayList<>();
		if (myFolder.getChildFolders() != null) {
			for (MyFolder folder :
				myFolder.getChildFolders()) {
				folderItems.add(
					new SharedFolderItem("folder", folder.getFullPath().replace(sharedFolder.getFullPath(), ""),
						folder.getFolderName(), 0L));
			}
		}

		List<SharedFolderItem> fileItems = new ArrayList<>();
		if (myFolder.getFiles() != null) {
			for (MyFile file :
				myFolder.getFiles()) {
				fileItems.add(new SharedFolderItem("file", file.getFullPath().replace(sharedFolder.getFullPath(), ""),
					file.getFileName(), file.getSize()));
			}
		}

		return SharedFolderInfoResponse.builder().sharedId(sharedFolder.getSharedId())
			.expiredAt(sharedFolder.getExpiredAt())
			.relativePath(relativePath)
			.folders(folderItems)
			.files(fileItems)
			.build();
	}
}

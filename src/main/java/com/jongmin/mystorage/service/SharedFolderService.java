package com.jongmin.mystorage.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jongmin.mystorage.controller.api.dto.UploadFileRequestDto;
import com.jongmin.mystorage.controller.api.dto.UploadSharedFileRequestDto;
import com.jongmin.mystorage.exception.FileNotInFileSystemException;
import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.model.MyFolder;
import com.jongmin.mystorage.model.SharedFolder;
import com.jongmin.mystorage.model.enums.FileItemStatus;
import com.jongmin.mystorage.repository.FileRepository;
import com.jongmin.mystorage.repository.FolderRepository;
import com.jongmin.mystorage.repository.SharedFolderRepository;
import com.jongmin.mystorage.service.file.FileService;
import com.jongmin.mystorage.service.response.FileResponse;
import com.jongmin.mystorage.service.response.SharedFolderInfoResponse;
import com.jongmin.mystorage.service.response.SharedFolderItem;
import com.jongmin.mystorage.service.response.SharedFolderResponse;
import com.jongmin.mystorage.service.response.StringResponse;
import com.jongmin.mystorage.utils.SharedFolderUtils;
import com.jongmin.mystorage.utils.ioutils.FileIoUtils;
import com.jongmin.mystorage.utils.repositorytutils.FolderRepositoryUtils;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class SharedFolderService {

	private final FolderRepositoryUtils folderRepositoryUtils;
	private final SharedFolderUtils sharedFolderUtils;
	private final SharedFolderRepository sharedFolderRepository;
	private final FolderRepository folderRepository;
	private final FileRepository fileRepository;

	private final FileIoUtils fileIoUtils;

	private final FileService fileService;

	public SharedFolderResponse createSharedFolder(String ownerName, UUID folderUuid) {
		MyFolder folder = folderRepositoryUtils.getFolderByUuidWithSavedStatus(ownerName,
			folderUuid);

		SharedFolder sharedFolder = sharedFolderUtils.createAndPersistSharedFolder(folder);
		return SharedFolderResponse.fromSharedFolder(sharedFolder);

	}

	public SharedFolderInfoResponse readFolder(UUID sharedFolderId, String relativePath) {
		if (relativePath == null) {
			relativePath = "";
		}

		Optional<SharedFolder> bySharedId = sharedFolderRepository.findBySharedId(sharedFolderId);
		if (bySharedId.isEmpty()) {
			throw new RuntimeException("잘못된 공유 폴더에 대한 접근입니다.");
		}
		SharedFolder sharedFolder = bySharedId.get();
		String fullPath = sharedFolder.getMyFolder().getFullPath() + relativePath;
		String ownerName = sharedFolder.getOwnerName();

		Optional<MyFolder> byFullPath = folderRepository.findByOwnerNameAndFullPath(ownerName, fullPath);
		if (byFullPath.isEmpty()) {
			throw new RuntimeException("잘못된 하위 폴더 대한 접근입니다.");
		}
		MyFolder folder = byFullPath.get();

		return SharedFolderInfoResponse.createSharedFolderInfoResponse(sharedFolder, folder, relativePath);
	}

	public Resource downloadFile(UUID sharedFolderId, String relativePath) {
		if (relativePath == null) {
			relativePath = "";
		}

		Optional<SharedFolder> bySharedId = sharedFolderRepository.findBySharedId(sharedFolderId);
		if (bySharedId.isEmpty()) {
			throw new RuntimeException("잘못된 공유 폴더에 대한 접근입니다.");
		}
		SharedFolder sharedFolder = bySharedId.get();
		String fullPath = sharedFolder.getMyFolder().getFullPath() + relativePath;
		String ownerName = sharedFolder.getOwnerName();

		Optional<MyFile> byFullPath = fileRepository.findByOwnerNameAndFullPathAndStatus(ownerName, fullPath,
			FileItemStatus.SAVED);
		if (byFullPath.isEmpty()) {
			throw new RuntimeException("잘못된 하위 파일에 대한 접근입니다.");
		}

		MyFile file = byFullPath.get();
		if (fileIoUtils.fileNotExists(file)) {
			throw new FileNotInFileSystemException("파일이 디스크 상에 존재하지 않습니다.");
		}
		return fileIoUtils.fileToResource(file);
	}

	public SharedFolderItem uploadFile(UploadSharedFileRequestDto requestDto) {
		UUID sharedFolderId = requestDto.getFolderUuid();
		String relativePath = requestDto.getRelativePath();
		if (relativePath == null) {
			relativePath = "";
		}

		Optional<SharedFolder> bySharedId = sharedFolderRepository.findBySharedId(sharedFolderId);
		if (bySharedId.isEmpty()) {
			throw new RuntimeException("잘못된 공유 폴더에 대한 접근입니다.");
		}
		SharedFolder sharedFolder = bySharedId.get();
		String fullPath = sharedFolder.getMyFolder().getFullPath() + relativePath;
		String ownerName = sharedFolder.getOwnerName();

		Optional<MyFolder> byFullPath = folderRepository.findByOwnerNameAndFullPath(ownerName, fullPath);
		if (byFullPath.isEmpty()) {
			throw new RuntimeException("잘못된 하위 폴더 대한 접근입니다.");
		}
		MyFolder folder = byFullPath.get();

		// fileService의 uploadFile로 실행을 넘김
		FileResponse fileResponse = fileService.uploadFile(ownerName,
			new UploadFileRequestDto(requestDto.getMultipartFile(), folder.getUuid()));

		String fileRelativePath = fileResponse.getFullPath().replace(sharedFolder.getFullPath(), "");

		return new SharedFolderItem("File", fileRelativePath, fileResponse.getFileName(), fileResponse.getSize());
	}

	public StringResponse deleteFile(UUID sharedFolderId, String relativePath) {
		if (relativePath == null) {
			relativePath = "";
		}

		Optional<SharedFolder> bySharedId = sharedFolderRepository.findBySharedId(sharedFolderId);
		if (bySharedId.isEmpty()) {
			throw new RuntimeException("잘못된 공유 폴더에 대한 접근입니다.");
		}
		SharedFolder sharedFolder = bySharedId.get();
		String fullPath = sharedFolder.getMyFolder().getFullPath() + relativePath;
		String ownerName = sharedFolder.getOwnerName();
		System.out.println("fullPath = " + fullPath);
		System.out.println("ownerName = " + ownerName);
		Optional<MyFile> byFullPath = fileRepository.findByOwnerNameAndFullPathAndStatus(ownerName, fullPath,
			FileItemStatus.SAVED);
		if (byFullPath.isEmpty()) {
			throw new RuntimeException("잘못된 하위 파일에 대한 접근입니다.");
		}
		MyFile file = byFullPath.get();

		return fileService.deleteFile(file.getOwnerName(), file.getUuid());
	}
}

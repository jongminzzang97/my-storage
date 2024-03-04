package com.jongmin.mystorage.service.folder;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.model.MyFolder;
import com.jongmin.mystorage.repository.FileRepository;
import com.jongmin.mystorage.repository.FolderRepository;
import com.jongmin.mystorage.service.response.FolderInfoResponse;
import com.jongmin.mystorage.service.response.FolderResponse;
import com.jongmin.mystorage.service.response.StringResponse;
import com.jongmin.mystorage.utils.ioutils.FileIoUtils;
import com.jongmin.mystorage.utils.repositorytutils.FolderRepositoryUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class FolderService {

	private final FolderRepository folderRepository;
	private final FolderRepositoryUtils folderRepositoryUtils;

	private final FileRepository fileRepository;
	private final FileIoUtils fileIoUtils;

	@Transactional
	public FolderResponse createFolder(String ownerName, String folderName, UUID parentFolderUuid) {
		MyFolder parentFolder;
		if (parentFolderUuid == null) {
			parentFolder = folderRepositoryUtils.getRootFolder(ownerName);
		} else {
			parentFolder = folderRepositoryUtils.getFolderByUuidWithSavedStatus(ownerName, parentFolderUuid);
		}

		List<MyFolder> childFolders = parentFolder.getChildFolders();
		if (childFolders != null) {
			for (MyFolder folder : childFolders) {
				if (folder.getFolderName().equals(folderName)) {
					throw new RuntimeException("동일한 이름의 폴더가 이미 존재합니다.");
				}
			}
		}

		MyFolder createdFolder = MyFolder.createMyFolderEntity(ownerName, folderName, parentFolder);
		folderRepository.save(createdFolder);

		return FolderResponse.fromMyFolder(createdFolder);
	}

	@Transactional
	public FolderInfoResponse readFolder(String ownerName, UUID folderId) {
		MyFolder folder;
		if (folderId == null) {
			folder = folderRepositoryUtils.getRootFolder(ownerName);
		} else {
			folder = folderRepositoryUtils.getFolderByUuidWithSavedStatus(ownerName, folderId);
		}

		return FolderInfoResponse.fromMyFolder(folder);
	}

	@Transactional
	public StringResponse deleteFolder(String ownerName, UUID folderId) {

		MyFolder myFolder = folderRepositoryUtils.getFolderByUuidWithSavedStatus(ownerName, folderId);
		String fullPath = myFolder.getFullPath();

		List<MyFile> files = fileRepository.findByOwnerNameAndFullPathStartingWith(ownerName, fullPath);
		files.stream().forEach(MyFile::deleteFile);
		files.stream().forEach(fileIoUtils::deleteFile);

		List<MyFolder> folders = folderRepository.findByOwnerNameAndFullPathStartingWith(ownerName, fullPath);
		folders.stream().forEach(MyFolder::deleteFolder);

		return new StringResponse("폴더 삭제가 완료되었습니다");
	}

	public void deleteFolder_v2(String ownerName, UUID folderId) {

		MyFolder myFolder = folderRepositoryUtils.getFolderByUuidWithSavedStatus(ownerName, folderId);
		String fullPath = myFolder.getFullPath();

		// DB에 접근하는 쿼리의 수를 줄일 수 있다.

	}

}

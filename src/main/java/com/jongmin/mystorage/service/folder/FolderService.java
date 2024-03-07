package com.jongmin.mystorage.service.folder;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jongmin.mystorage.exception.FileAlreadyExistException;
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

	public FolderResponse moveFolder(String ownerName, UUID transferFolderUuid, UUID destFolderUuid) {
		// 추후에 구현(optional)
		// 1. 이동 가능한 것만 이동, 이동 불가능한 것들은 그대로
		// 2. 이동 가능한 것들은 그대로 이동, 이동 불가능 한 파일은 (1) 붙여서
		// 3. 모든 파일 덮어 쓰기

		MyFolder transferFolder = folderRepositoryUtils.getFolderByUuidWithSavedStatus(ownerName, transferFolderUuid);
		MyFolder destFolder = folderRepositoryUtils.getFolderByUuidWithSavedStatus(ownerName, destFolderUuid);

		if (destFolder.getFullPath().startsWith(transferFolder.getFullPath())) {
			throw new RuntimeException("자신의 하위 폴더로 옮겨질 수 없습니다.");
		}

		// 대상 폴더 안에 타겟 폴더와 동일한 이름이 없으면 당연하게 폴더 하위의 대상들도 존재하지 않는 것이 보장된다.
		if (folderRepositoryUtils.sameFolderNameExistsInFolder(destFolder, transferFolder.getFolderName())) {
			throw new FileAlreadyExistException("옮기려는 폴더에 동일한 이름의 파일이 존재해 이동이 불가능 합니다.");
		}

		// transferFolder 하위의 모든 파일과 폴더를 구한다.
		List<MyFolder> folders = folderRepository.findByOwnerNameAndFullPathStartingWith(ownerName,
			transferFolder.getFullPath());
		List<MyFile> files = fileRepository.findByOwnerNameAndFullPathStartingWith(ownerName,
			transferFolder.getFullPath());

		folders.forEach(f -> f.replacePath(transferFolder.getParentPath(), destFolder.getFullPath()));
		files.forEach(MyFile::reset);

		// 부모가 바뀌는 것은 단 하나 -> 부모폴더와의 매핑이 변경되어야 하는 것은 직접 이동하는 폴더 뿐임
		// 다른 폴더들은 위에서 경로에 대한 갱신을 진행했음
		transferFolder.move(destFolder);

		return FolderResponse.fromMyFolder(destFolder);
	}
}

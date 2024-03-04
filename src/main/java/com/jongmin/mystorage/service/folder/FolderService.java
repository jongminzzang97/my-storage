package com.jongmin.mystorage.service.folder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jongmin.mystorage.exception.FileNotInDatabaseException;
import com.jongmin.mystorage.exception.FileNotInFileSystemException;
import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.model.MyFolder;
import com.jongmin.mystorage.model.enums.FileItemStatus;
import com.jongmin.mystorage.repository.FileRepository;
import com.jongmin.mystorage.repository.FolderRepository;
import com.jongmin.mystorage.service.response.FolderInfoResponse;
import com.jongmin.mystorage.service.response.FolderResponse;
import com.jongmin.mystorage.service.response.StringResponse;
import com.jongmin.mystorage.utils.ioutils.FileIoUtils;
import com.jongmin.mystorage.utils.ioutils.FolderIolUtils;
import com.jongmin.mystorage.utils.repositorytutils.FolderRepositoryUtils;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class FolderService {

	private final FolderRepository folderRepository;
	private final FolderRepositoryUtils folderRepositoryUtils;

	private final FileRepository fileRepository;
	private final FileIoUtils fileIoUtils;


	public MyFolder checkMyFolderAndGet(String ownerName, UUID folderUuid) {
		Optional<MyFolder> optional = folderRepository.findByUuid(folderUuid);
		if (optional.isEmpty()) {
			throw new FileNotInDatabaseException("폴더에 대한 정보가 DB에 존재하지 않습니다.");
		}

		MyFolder myFolder = optional.get();
		if (myFolder.getStatus() != FileItemStatus.SAVED) {
			throw new RuntimeException("폴더가 삭제되어 있는 상태입니다.");
		}
		if (!myFolder.getOwnerName().equals(ownerName)) {
			throw new RuntimeException("본인 소유의 폴더가 아닙니다.");
		}

		return myFolder;
	}

	@Transactional
	public FolderResponse createFolder(String ownerName, String folderName, UUID parentFolderUuid) {
		MyFolder parentFolder;
		if (parentFolderUuid == null) {
			parentFolder = folderRepositoryUtils.getRootFolder(ownerName);
		} else {
			parentFolder = checkMyFolderAndGet(ownerName, parentFolderUuid);
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
			folder = checkMyFolderAndGet(ownerName, folderId);
		}

		return FolderInfoResponse.fromMyFolder(folder);
	}

	@Transactional
	public StringResponse deleteFolder(String ownerName, UUID folderId) {

		MyFolder myFolder = checkMyFolderAndGet(ownerName, folderId);
		String fullPath = myFolder.getFullPath();

		List<MyFile> files = fileRepository.findByOwnerNameAndFullPathStartingWith(ownerName, fullPath);
		files.stream().forEach(MyFile::deleteFile);
		files.stream().forEach(fileIoUtils::deleteFile);

		List<MyFolder> folders = folderRepository.findByOwnerNameAndFullPathStartingWith(ownerName, fullPath);
		folders.stream().forEach(MyFolder::deleteFolder);

		return new StringResponse("폴더 삭제가 완료되었습니다");
	}

	public void deleteFolder_v2(String ownerName, UUID folderId) {

		MyFolder myFolder = checkMyFolderAndGet(ownerName, folderId);
		String fullPath = myFolder.getFullPath();

		// DB에 접근하는 쿼리의 수를 줄일 수 있다.

	}

}

package com.jongmin.mystorage.utils.repositorytutils;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jongmin.mystorage.exception.FileNotInDatabaseException;
import com.jongmin.mystorage.model.MyFolder;
import com.jongmin.mystorage.model.enums.FileItemStatus;
import com.jongmin.mystorage.repository.FolderRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class FolderRepositoryUtils {

	private final FolderRepository folderRepository;

	public MyFolder createRootFolder(String ownerName) {
		MyFolder root = MyFolder.createMyFolderEntity(ownerName, "", null);
		folderRepository.save(root);
		return root;
	}

	public MyFolder createFolder(String ownerName, String folderName, MyFolder parentFolder) {
		MyFolder folder = MyFolder.createMyFolderEntity(ownerName, folderName, parentFolder);
		folderRepository.save(folder);
		return folder;
	}

	public MyFolder createFolder(String ownerName, String folderName, MyFolder parentFolder, UUID uuid) {
		MyFolder folder = MyFolder.createMyFolderEntity(ownerName, folderName, parentFolder, uuid);
		folderRepository.save(folder);
		return folder;
	}

	public MyFolder getRootFolder(String ownerName) {
		Optional<MyFolder> optional = folderRepository.findByOwnerNameAndFullPath(ownerName, "");
		MyFolder root;
		if (optional.isEmpty()) {
			root = createRootFolder(ownerName);
		} else {
			root = optional.get();
		}
		return root;
	}

	public MyFolder getFolderByUuid(UUID folderUuid) {
		MyFolder folder;
		Optional<MyFolder> optionalFolder = folderRepository.findByUuid(folderUuid);
		if (optionalFolder.isEmpty()) {
			throw new RuntimeException("해당 UUID를 갖는 폴더가 존재하지 않습니다.");
		} else {
			folder = optionalFolder.get();
		}
		return folder;
	}

	public MyFolder getFolderAndValidate(String ownerName, UUID folderUuid) {
		MyFolder folder = null;
		Optional<MyFolder> optionalFolder = folderRepository.findByUuid(folderUuid);
		if (optionalFolder.isEmpty()) {
			throw new RuntimeException("해당 UUID를 갖는 폴더가 존재하지 않습니다.");
		} else {
			folder = optionalFolder.get();
		}

		if (!folder.getOwnerName().equals(ownerName)) {
			throw new RuntimeException("상위 폴더의 소유자가 본인이 아닙니다.");
		}
		return folder;
	}

	public MyFolder getFolderByUuidWithSavedStatus(String ownerName, UUID folderUuid) {
		Optional<MyFolder> optional = folderRepository.findByUuidAndStatus(folderUuid, FileItemStatus.SAVED);

		if (optional.isEmpty()) {
			throw new FileNotInDatabaseException("폴더를 찾을 수 없습니다.");
		}

		MyFolder myFolder = optional.get();
		if (!myFolder.getOwnerName().equals(ownerName)) {
			throw new RuntimeException("본인 소유의 폴더가 아닙니다.");
		}

		return myFolder;
	}

	public boolean sameFolderNameExistsInFolder(MyFolder myFolder, String folderName) {
		return folderRepository.findByOwnerNameAndFolderNameAndParentFolderIdAndStatus(
			myFolder.getOwnerName(),
			folderName,
			myFolder.getId(),
			FileItemStatus.SAVED
		).isPresent();
	}
}

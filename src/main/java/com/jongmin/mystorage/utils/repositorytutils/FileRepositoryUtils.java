package com.jongmin.mystorage.utils.repositorytutils;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.jongmin.mystorage.exception.FileNotInDatabaseException;
import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.model.MyFolder;
import com.jongmin.mystorage.model.enums.FileItemStatus;
import com.jongmin.mystorage.repository.FileRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FileRepositoryUtils {

	private final FileRepository fileRepository;

	public MyFile deleteFile(MyFile myFile) {
		return myFile.deleteFile();
	}

	public MyFile getFileByUuidWithSavedStatus(String ownerName, UUID fileUuid) {
		Optional<MyFile> optional = fileRepository.findByUuidAndStatus(fileUuid, FileItemStatus.SAVED);
		if (optional.isEmpty()) {
			throw new FileNotInDatabaseException("파일를 찾을 수 없습니다.");
		}

		MyFile myFile = optional.get();
		if (!myFile.getOwnerName().equals(ownerName)) {
			throw new RuntimeException("본인 소유의 파일이 아닙니다.");
		}

		return myFile;
	}

	public boolean sameFileNameExistsInFolder(MyFile myFileEntity, MyFolder myFolder) {
		return fileRepository.findByOwnerNameAndFileNameAndParentFolderIdAndStatus(
			myFileEntity.getOwnerName(),
			myFileEntity.getFileName(),
			myFolder.getId(),
			FileItemStatus.SAVED
		).isPresent();
	}

	public MyFile createFile(MultipartFile multipartFile, String ownerName, MyFolder parentFolder) {
		MyFile myFile = MyFile.createMyFileEntity(multipartFile, ownerName, parentFolder);
		fileRepository.save(myFile);
		return myFile;
	}

	public MyFile createFile(MultipartFile multipartFile, String ownerName, MyFolder parentFolder, UUID uuid) {
		MyFile myFile = MyFile.createMyFileEntity(multipartFile, ownerName, parentFolder, uuid);
		fileRepository.save(myFile);
		return myFile;
	}
}

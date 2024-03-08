package com.jongmin.mystorage.utils.repositorytutils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.model.StorageInfo;
import com.jongmin.mystorage.repository.StorageInfoRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StorageInfoRepositoryUtils {

	private final StorageInfoRepository storageInfoRepository;

	public StorageInfo getStorageInfo(String ownerName) {
		return storageInfoRepository.findByOwnerName(ownerName)
			.orElseGet(() -> storageInfoRepository.save(new StorageInfo(ownerName)));
	}

	// public setter 위험 해보임 -> 같은 패키지에서만 setter 이용 가능하도록 default(package-private) 이용?
	public StorageInfo addFile(StorageInfo storageInfo, MyFile file) {
		storageInfo.setSize(storageInfo.getSize() + file.getSize());
		storageInfo.setFileCount(storageInfo.getFileCount() + 1);
		return storageInfo;
	}

	public StorageInfo addFileCount(StorageInfo storageInfo, int fileCount) {
		storageInfo.setFileCount(storageInfo.getFileCount() + fileCount);
		return storageInfo;
	}

	public StorageInfo addSize(StorageInfo storageInfo, Long size) {
		storageInfo.setSize(storageInfo.getSize() + size);
		return storageInfo;
	}

	public StorageInfo addFolderCount(StorageInfo storageInfo, Long folderCount) {
		storageInfo.setFolderCount(storageInfo.getFolderCount() + folderCount);
		return storageInfo;
	}

	public StorageInfo addFolder(StorageInfo storageInfo) {
		storageInfo.setFolderCount(storageInfo.getFolderCount() + 1);
		return storageInfo;
	}

}

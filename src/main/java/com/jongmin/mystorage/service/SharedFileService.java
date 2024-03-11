package com.jongmin.mystorage.service;

import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.jongmin.mystorage.exception.FileNotInFileSystemException;
import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.model.SharedFile;
import com.jongmin.mystorage.repository.FileRepository;
import com.jongmin.mystorage.repository.SharedFileRepository;
import com.jongmin.mystorage.service.response.SharedFileResponse;
import com.jongmin.mystorage.utils.SharedFileUtils;
import com.jongmin.mystorage.utils.ioutils.FileIoUtils;
import com.jongmin.mystorage.utils.repositorytutils.FileRepositoryUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SharedFileService {

	private final FileRepositoryUtils fileRepositoryUtils;
	private final SharedFileUtils sharedFileUtils;
	private final FileIoUtils fileIoUtils;

	public SharedFileResponse createSharedFile(String ownerName, UUID fileUuid) {
		MyFile file = fileRepositoryUtils.getFileByUuidWithSavedStatus(ownerName, fileUuid);
		SharedFile sharedFile = sharedFileUtils.createAndPersistSharedFile(file);
		return SharedFileResponse.fromSharedFile(sharedFile);
	}


	public SharedFileResponse readSharedFile(UUID sharedFileUuid) {
		SharedFile sharedFile = sharedFileUtils.getSharedFile(sharedFileUuid);
		return SharedFileResponse.fromSharedFile(sharedFile);
	}

	public Resource downloadSharedFile(UUID sharedFileUuid) {
		SharedFile sharedFile = sharedFileUtils.getSharedFile(sharedFileUuid);
		if (fileIoUtils.fileNotExists(sharedFile.getMyFile())) {
			throw new FileNotInFileSystemException("파일이 디스크 상에 존재하지 않습니다.");
		}
		return fileIoUtils.fileToResource(sharedFile.getMyFile());
	}
}


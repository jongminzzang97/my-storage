package com.jongmin.mystorage.service.file;

import java.util.Optional;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.jongmin.mystorage.controller.api.dto.UploadFileRequestDto;
import com.jongmin.mystorage.exception.FileAlreadyExistException;
import com.jongmin.mystorage.exception.FileNotInDatabaseException;
import com.jongmin.mystorage.exception.FileNotInFileSystemException;
import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.model.MyFolder;
import com.jongmin.mystorage.model.enums.FileItemStatus;
import com.jongmin.mystorage.repository.FileRepository;
import com.jongmin.mystorage.service.response.FileResponse;
import com.jongmin.mystorage.service.response.StringResponse;
import com.jongmin.mystorage.utils.ioutils.FileIoUtils;
import com.jongmin.mystorage.utils.repositorytutils.FileRepositoryUtils;
import com.jongmin.mystorage.utils.repositorytutils.FolderRepositoryUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class FileService {

	private final FileRepository fileRepository;

	private final FolderRepositoryUtils folderRepositoryUtils;
	private final FileRepositoryUtils fileRepositoryUtils;
	private final FileIoUtils fileIoUtils;

	public FileResponse uploadFile(String ownerName, UploadFileRequestDto requestDto) {
		MyFolder parentFolder = folderRepositoryUtils.getFolderByUuid(requestDto.getFolderUuid());
		MyFile myFileEntity = MyFile.createMyFileEntity(requestDto.getMultipartFile(), ownerName, parentFolder);
		if (fileRepositoryUtils.sameFileNameExistsInFolder(myFileEntity)) {
			throw new FileAlreadyExistException("이미 동일한 이름의 파일이 존재합니다.");
		}

		fileIoUtils.save(requestDto.getMultipartFile(), myFileEntity);
		fileRepository.save(myFileEntity);
		return FileResponse.fromMyFile(myFileEntity);
	}

	public FileResponse readFile(String ownerName, UUID fileUuid) {
		MyFile checkedFile = fileRepositoryUtils.getFileByUuidWithSavedStatus(ownerName, fileUuid);
		return FileResponse.fromMyFile(checkedFile);
	}

	public StringResponse deleteFile(String ownerName, UUID fileUuid) {
		// 파일을 삭제를 위한 Lock을 얻고,
		// 파일 상태를 확인하고 삭제하는 과정이 atomic하게 진행될 수 있도록 추후에 수정이 필요합니다.
		// 파일을 다운로드 받는 사용자가 있는 경우 역시 고려되어야 합니다.
		MyFile checkedFile = fileRepositoryUtils.getFileByUuidWithSavedStatus(ownerName, fileUuid);
		fileIoUtils.deleteFile(checkedFile);

		if (fileIoUtils.fileNotExists(checkedFile)) {
			throw new FileNotInFileSystemException("파일이 디스크 상에 존재하지 않습니다.");
		}
		fileRepositoryUtils.deleteFile(checkedFile);

		return new StringResponse("요청한 파일에 대한 삭제가 성공적으로 진행되었습니다.");
	}

	public Resource downloadFile(String ownerName, UUID fileUuid) {
		// 파일을 다운로드를 위한 Lock을 얻고,
		// 파일 상태를 확인하고 다운로드 받는 과정이 atomic하게 진행될 수 있도록 추후에 수정이 필요합니다.
		MyFile checkedFile = fileRepositoryUtils.getFileByUuidWithSavedStatus(ownerName, fileUuid);
		if (fileIoUtils.fileNotExists(checkedFile)) {
			throw new FileNotInFileSystemException("파일이 디스크 상에 존재하지 않습니다.");
		}
		return fileIoUtils.fileToResource(checkedFile);
	}
}

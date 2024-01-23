package com.jongmin.mystorage.service.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.jongmin.mystorage.exception.FileAlreadyExistException;
import com.jongmin.mystorage.exception.FileNotInDatabaseException;
import com.jongmin.mystorage.exception.FileNotInFileSystemException;
import com.jongmin.mystorage.exception.FileStorageException;
import com.jongmin.mystorage.exception.OwnerNameException;
import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.repository.FileRepository;
import com.jongmin.mystorage.service.request.FileDownloadRequest;
import com.jongmin.mystorage.service.request.FileUploadRequest;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class FileService {

	private final FileSystemWrapper fileSystemWrapper;
	private final FileRepository fileRepository;

	public FileServiceResponse uploadFile(FileUploadRequest request) {
		String fileName = request.getFileName();
		String owner = request.getOwner();
		String fileDir = owner + "_" + fileName;

		if (owner.contains("_")) {
			throw new OwnerNameException("Owner는 '_' 문자를 포함 할 수 없습니다.");
		}

		if (fileSystemWrapper.fileExists(fileDir)) {
			throw new FileAlreadyExistException("이미 동일한 이름의 파일이 존재합니다.");
		}

		try (InputStream inputStream = request.getMultipartFile().getInputStream()) {
			long copy = fileSystemWrapper.copy(inputStream, fileDir);
			MyFile myFile = fileRepository.save(request.toMyFileEntity());
			return FileServiceResponse.of(myFile);
		} catch (IOException e) {
			throw new FileStorageException("파일 저장 중 오류가 발생했습니다.");
		}
	}

	public Resource downloadFile(FileDownloadRequest request) {
		// 파일을 다운로드를 위한 Lock을 얻고,
		// 파일 상태를 확인하고 다운로드 받는 과정이 atomic하게 진행될 수 있도록 추후에 수정이 필요합니다.
		String fileName = request.getFileName();
		String owner = request.getOwner();
		String fileDir = owner + "_" + fileName;
		Optional<MyFile> myFile = fileRepository.findByOwnerAndName(owner, fileName);
		if (myFile.isEmpty()) {
			throw new FileNotInDatabaseException("파일에 대한 정보가 DB에 존재하지 않습니다.");
		}
		if (fileSystemWrapper.fileNotExists(fileDir)) {
			throw new FileNotInFileSystemException("다운로드 하려는 파일이 존재하지 않습니다.");
		}
		return fileSystemWrapper.fileDirToResource(fileDir);
	}
}

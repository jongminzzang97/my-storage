package com.jongmin.mystorage.service.file;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.stereotype.Service;

import com.jongmin.mystorage.exception.FileAlreadyExistException;
import com.jongmin.mystorage.exception.FileStorageException;
import com.jongmin.mystorage.exception.OwnerNameException;
import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.repository.FileRepository;
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
}

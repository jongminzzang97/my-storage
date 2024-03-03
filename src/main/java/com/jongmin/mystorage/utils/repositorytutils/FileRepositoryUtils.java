package com.jongmin.mystorage.utils.repositorytutils;

import org.springframework.stereotype.Component;

import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.repository.FileRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FileRepositoryUtils {

	private final FileRepository fileRepository;

	public MyFile deleteFile(MyFile myFile) {
		return myFile.deleteFile();
	}

	public boolean fileExists(MyFile myFileEntity) {
		return fileRepository.findByFileName(myFileEntity.getFileName()).isPresent();
	}
}

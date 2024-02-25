package com.jongmin.mystorage.utils.ioutils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jongmin.mystorage.exception.FileStorageException;
import com.jongmin.mystorage.model.MyFile;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FileIoUtils {

	@Value("${file.storage.baseDir}")
	private String baseDir;

	public void deleteFile(MyFile myFile) {
		String accessRoute = myFile.getAccessRoute();
		Path path = Paths.get(baseDir + accessRoute);
		try {
			Files.delete(path);
		} catch (IOException e) {
			throw new FileStorageException("파일 삭제 중 오류가 발생했습니다.");
		}
	}
}

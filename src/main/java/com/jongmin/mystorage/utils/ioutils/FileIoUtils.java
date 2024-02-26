package com.jongmin.mystorage.utils.ioutils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.jongmin.mystorage.exception.FileStorageException;
import com.jongmin.mystorage.model.MyFile;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FileIoUtils {

	@Value("${file.storage.baseDir}")
	private String baseDir;

	public boolean fileExists(MyFile myFile) {
		String accessRoute = myFile.getAccessRoute();
		Path path = Paths.get(baseDir + accessRoute);
		return Files.exists(path);
	}

	public boolean fileNotExists(MyFile myFile) {
		String accessRoute = myFile.getAccessRoute();
		Path path = Paths.get(baseDir + accessRoute);
		return Files.notExists(path);
	}

	public Resource fileToResource(MyFile myFile) {
		String accessRoute = myFile.getAccessRoute();
		Path path = Paths.get(baseDir + accessRoute);
		return new FileSystemResource(path);
	}

	public void deleteFile(MyFile myFile) {
		String accessRoute = myFile.getAccessRoute();
		Path path = Paths.get(baseDir + accessRoute);
		try {
			Files.delete(path);
		} catch (IOException e) {
			throw new FileStorageException("파일 삭제 중 오류가 발생했습니다.");
		}
	}

	public long save(MultipartFile multipartFile, MyFile myFile, CopyOption... options) {
		String accessRoute = myFile.getAccessRoute();
		Path path = Paths.get(baseDir + accessRoute);
		try (InputStream inputStream = multipartFile.getInputStream()) {
			return Files.copy(inputStream, path, options);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

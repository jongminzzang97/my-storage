package com.jongmin.mystorage.service.file;

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

@Component
public class FileSystemWrapper {

	@Value("${file.storage.rootDir}")
	private String rootDir;

	public boolean fileExists(String fileDir) {
		Path path = Paths.get(rootDir + fileDir);
		return Files.exists(path);
	}

	public boolean fileNotExists(String fileDir) {
		Path path = Paths.get(rootDir + fileDir);
		return Files.notExists(path);
	}

	public long copy(InputStream in, String fileDir, CopyOption... options) throws IOException {
		Path path = Paths.get(rootDir + fileDir);
		return Files.copy(in, path, options);
	}

	public Resource fileDirToResource(String fileDir) {
		return new FileSystemResource(rootDir + fileDir);
	}

	public void fileDelete(String fileDir) throws IOException {
		Path path = Paths.get(rootDir + fileDir);
		Files.delete(path);
	}
}

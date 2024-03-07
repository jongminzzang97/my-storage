package com.jongmin.mystorage.utils.ioutils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jongmin.mystorage.model.MyFolder;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FolderIolUtils {

	@Value("${file.storage.baseDir}")
	private String baseDir;

	public void createPhysicalFolder(String ownerName, UUID folderUuid) {
		try {
			Files.createDirectories(Paths.get(baseDir + ownerName + "/" + folderUuid));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

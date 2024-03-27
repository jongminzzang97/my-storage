package com.jongmin.mystorage.service;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.model.MyFolder;
import com.jongmin.mystorage.model.SharedFile;
import com.jongmin.mystorage.repository.FileRepository;
import com.jongmin.mystorage.repository.FolderRepository;
import com.jongmin.mystorage.repository.SharedFileRepository;
import com.jongmin.mystorage.repository.StorageInfoRepository;
import com.jongmin.mystorage.service.file.FileService;
import com.jongmin.mystorage.service.response.SharedFileResponse;
import com.jongmin.mystorage.utils.SharedFileUtils;
import com.jongmin.mystorage.utils.ioutils.FileIoUtils;
import com.jongmin.mystorage.utils.ioutils.FolderIolUtils;
import com.jongmin.mystorage.utils.repositorytutils.FileRepositoryUtils;
import com.jongmin.mystorage.utils.repositorytutils.FolderRepositoryUtils;
import com.jongmin.mystorage.utils.repositorytutils.StorageInfoRepositoryUtils;

import jakarta.persistence.EntityManager;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class SharedFileServiceTest {

	@Value("${file.storage.baseDir}")
	private String baseDir;
	@Autowired
	private FolderRepository folderRepository;
	@Autowired
	private FileRepository fileRepository;
	@Autowired
	private FileRepositoryUtils fileRepositoryUtils;
	@Autowired
	private FolderRepositoryUtils folderRepositoryUtils;
	@Autowired
	private FolderIolUtils folderIolUtils;
	@Autowired
	private FileIoUtils fileIoUtils;
	@Autowired
	private FileService fileService;
	@Autowired
	private StorageInfoRepository storageInfoRepository;
	@Autowired
	private StorageInfoRepositoryUtils storageInfoRepositoryUtils;
	@Autowired
	private SharedFileService sharedFileService;
	@Autowired
	private SharedFileRepository sharedFileRepository;
	@Autowired
	private SharedFileUtils sharedFileUtils;
	@Autowired
	private EntityManager entityManager;

	@AfterEach
	public void clearFolder() {
		File file = new File(baseDir);
		deleteDirectoryAndFiles(file);
	}

	@DisplayName("createSharedFile : 정상 흐름")
	@Test
	@Transactional
	public void createSharedFile() {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);
		MockMultipartFile mockFile = new MockMultipartFile("file.txt", "file.txt", "text/plain", new byte[] {1});
		MyFile myFile = fileRepositoryUtils.createAndPersistFile(mockFile, "testOwner", hello);
		fileIoUtils.save(mockFile, myFile);

		// when
		SharedFileResponse response = sharedFileService.createSharedFile("testOwner", myFile.getUuid());

		// then
		List<SharedFile> all = sharedFileRepository.findAll();
		assertThat(all.size()).isEqualTo(1);
		assertThat(all.get(0).getMyFile().getFullPath()).isEqualTo("/hello/file.txt");
	}

	@DisplayName("readSharedFile : 정상 흐름")
	@Test
	@Transactional
	public void readSharedFile() {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);
		MockMultipartFile mockFile = new MockMultipartFile("file.txt", "file.txt", "text/plain", new byte[] {1});
		MyFile myFile = fileRepositoryUtils.createAndPersistFile(mockFile, "testOwner", hello);
		fileIoUtils.save(mockFile, myFile);
		SharedFile sharedFile = sharedFileUtils.createAndPersistSharedFile(myFile);

		// when
		SharedFileResponse readResponse = sharedFileService.readSharedFile(sharedFile.getUuid());

		// then
		assertThat(readResponse.getFileName()).isEqualTo("file.txt");
		assertThat(readResponse.getUuid()).isEqualTo(sharedFile.getUuid());
		assertThat(readResponse.getSize()).isEqualTo(1);
		assertThat(readResponse.getExpiredAt()).isEqualTo(sharedFile.getExpiredAt());
	}

	private void deleteDirectoryAndFiles(File targetFolder) {
		File[] files = targetFolder.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				deleteDirectoryAndFiles(file);
			}
			file.delete();
		}
	}
}

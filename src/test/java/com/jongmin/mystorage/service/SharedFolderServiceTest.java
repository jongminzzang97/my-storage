package com.jongmin.mystorage.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import com.jongmin.mystorage.controller.api.dto.UploadSharedFileRequestDto;
import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.model.MyFolder;
import com.jongmin.mystorage.model.SharedFolder;
import com.jongmin.mystorage.model.enums.FileItemStatus;
import com.jongmin.mystorage.repository.FileRepository;
import com.jongmin.mystorage.repository.FolderRepository;
import com.jongmin.mystorage.repository.SharedFolderRepository;
import com.jongmin.mystorage.service.response.SharedFolderInfoResponse;
import com.jongmin.mystorage.service.response.SharedFolderItem;
import com.jongmin.mystorage.service.response.SharedFolderResponse;
import com.jongmin.mystorage.service.response.StringResponse;
import com.jongmin.mystorage.utils.SharedFolderUtils;
import com.jongmin.mystorage.utils.ioutils.FileIoUtils;
import com.jongmin.mystorage.utils.repositorytutils.FileRepositoryUtils;
import com.jongmin.mystorage.utils.repositorytutils.FolderRepositoryUtils;

import jakarta.persistence.EntityManager;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class SharedFolderServiceTest {

	@Autowired
	private SharedFolderService sharedFolderService;
	@Autowired
	private SharedFolderRepository sharedFolderRepository;
	@Autowired
	private FolderRepository folderRepository;
	@Autowired
	private FileRepository fileRepository;
	@Autowired
	private FolderRepositoryUtils folderRepositoryUtils;
	@Autowired
	private SharedFolderUtils sharedFolderUtils;
	@Autowired
	private FileIoUtils fileIoUtils;
	@Autowired
	private FileRepositoryUtils fileRepositoryUtils;
	@Autowired
	private EntityManager entityManager;

	@Test
	@DisplayName("createSharedFolder: 공유 폴더 생성 정상 흐름")
	@Transactional
	void createSharedFolder() {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);

		// when
		SharedFolderResponse response = sharedFolderService.createSharedFolder("testOwner", hello.getUuid());

		// then
		assertThat(response.getExpiredAt()).isAfter(LocalDateTime.now().plusHours(2))
			.isBefore(LocalDateTime.now().plusHours(3));
		assertThat(response.getFolderName()).isEqualTo("hello");
	}

	@Test
	@DisplayName("readFolder: 공유 폴더 읽기 정상 흐름")
	@Transactional
	void readFolder() {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);
		MyFolder world1 = folderRepositoryUtils.createAndPersistFolder("testOwner", "world1", hello);
		MyFolder world2 = folderRepositoryUtils.createAndPersistFolder("testOwner", "world2", hello);
		MyFolder world3 = folderRepositoryUtils.createAndPersistFolder("testOwner", "world3", hello);

		MockMultipartFile mockFile = new MockMultipartFile("file.txt", "file.txt", "text/plain", new byte[] {1});
		MyFile myFile = fileRepositoryUtils.createAndPersistFile(mockFile, "testOwner", hello);
		fileIoUtils.save(mockFile, myFile);

		SharedFolder sharedFolder = sharedFolderUtils.createAndPersistSharedFolder(hello);

		entityManager.clear();

		// when
		SharedFolderInfoResponse response = sharedFolderService.readFolder(sharedFolder.getSharedId(), null);

		// then
		assertThat(response.getFolders().size()).isEqualTo(3);
		assertThat(response.getFiles().size()).isEqualTo(1);
		assertThat(response.getRelativePath()).isEqualTo("");
	}

	@Test
	@DisplayName("readFolder: 공유 폴더의 하위 폴더 읽기 정상 흐름")
	@Transactional
	void readFolderInSharedFolder() {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);
		MyFolder world1 = folderRepositoryUtils.createAndPersistFolder("testOwner", "world1", hello);
		MyFolder world2 = folderRepositoryUtils.createAndPersistFolder("testOwner", "world2", world1);
		MyFolder world3 = folderRepositoryUtils.createAndPersistFolder("testOwner", "world3", world1);

		MockMultipartFile mockFile = new MockMultipartFile("file.txt", "file.txt", "text/plain", new byte[] {1});
		MyFile myFile = fileRepositoryUtils.createAndPersistFile(mockFile, "testOwner", world1);
		fileIoUtils.save(mockFile, myFile);

		SharedFolder sharedFolder = sharedFolderUtils.createAndPersistSharedFolder(hello);

		entityManager.clear();

		// when
		SharedFolderInfoResponse response = sharedFolderService.readFolder(sharedFolder.getSharedId(), "/world1");

		// then
		assertThat(response.getFolders().size()).isEqualTo(2);
		assertThat(response.getFiles().size()).isEqualTo(1);
		assertThat(response.getRelativePath()).isEqualTo("/world1");
	}

	@Test
	@DisplayName("readFolder: 공유 폴더에 없는 하위 폴더에 대한 접근 오류")
	@Transactional
	void readWrongFolderInSharedFolder() {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);
		MyFolder world1 = folderRepositoryUtils.createAndPersistFolder("testOwner", "world1", hello);
		MyFolder world2 = folderRepositoryUtils.createAndPersistFolder("testOwner", "world2", world1);
		MyFolder world3 = folderRepositoryUtils.createAndPersistFolder("testOwner", "world3", world1);

		MockMultipartFile mockFile = new MockMultipartFile("file.txt", "file.txt", "text/plain", new byte[] {1});
		MyFile myFile = fileRepositoryUtils.createAndPersistFile(mockFile, "testOwner", world1);
		fileIoUtils.save(mockFile, myFile);

		SharedFolder sharedFolder = sharedFolderUtils.createAndPersistSharedFolder(hello);

		// when - then
		RuntimeException exception = assertThrows(RuntimeException.class,
			() -> sharedFolderService.readFolder(sharedFolder.getSharedId(), "/world2"));
		assertThat(exception.getMessage()).isEqualTo("잘못된 하위 폴더 대한 접근입니다.");
	}

	@Test
	@DisplayName("uploadFile: 공유 폴더 하위 폴더의 파일 업로드 정상 흐름")
	@Transactional
	void uploadFile() {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);

		SharedFolder sharedFolder = sharedFolderUtils.createAndPersistSharedFolder(hello);

		MockMultipartFile mockFile = new MockMultipartFile("file.txt", "file.txt", "text/plain", new byte[] {1});
		UploadSharedFileRequestDto requestDto = new UploadSharedFileRequestDto(mockFile, sharedFolder.getSharedId(),
			null);

		// when
		SharedFolderItem response = sharedFolderService.uploadFile(requestDto);

		// then
		assertThat(response.getSize()).isEqualTo(1);
		assertThat(response.getName()).isEqualTo("file.txt");
		assertThat(response.getType()).isEqualTo("File");
		assertThat(response.getRelativePath()).isEqualTo("/file.txt");
		MyFile file = fileRepository.findByOwnerNameAndFullPathAndStatus("testOwner",
			"/hello/file.txt", FileItemStatus.SAVED).get();
		assertThat(file).isNotNull();
		assertThat(file.getFileName()).isEqualTo("file.txt");
	}

	@Test
	@DisplayName("uploadFile: 공유 폴더 파일 업로드 정상 흐름")
	@Transactional
	void uploadFileInChildFolder() {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);
		MyFolder world1 = folderRepositoryUtils.createAndPersistFolder("testOwner", "world1", hello);

		SharedFolder sharedFolder = sharedFolderUtils.createAndPersistSharedFolder(hello);

		MockMultipartFile mockFile = new MockMultipartFile("file.txt", "file.txt", "text/plain", new byte[] {1});
		UploadSharedFileRequestDto requestDto = new UploadSharedFileRequestDto(mockFile, sharedFolder.getSharedId(),
			"/world1");

		// when
		SharedFolderItem response = sharedFolderService.uploadFile(requestDto);

		entityManager.flush();

		// then
		assertThat(response.getSize()).isEqualTo(1);
		assertThat(response.getName()).isEqualTo("file.txt");
		assertThat(response.getType()).isEqualTo("File");
		assertThat(response.getRelativePath()).isEqualTo("/world1/file.txt");

		MyFile file = fileRepository.findByOwnerNameAndFullPathAndStatus("testOwner",
			"/hello/world1/file.txt", FileItemStatus.SAVED).get();
		assertThat(file).isNotNull();
		assertThat(file.getFileName()).isEqualTo("file.txt");
	}

	@Test
	@DisplayName("deleteFile: 공유 폴더의 파일 삭제 정상 흐름")
	@Transactional
	void deleteFile() {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);

		MockMultipartFile mockFile = new MockMultipartFile("file.txt", "file.txt", "text/plain", new byte[] {1});
		MyFile myFile = fileRepositoryUtils.createAndPersistFile(mockFile, "testOwner", hello);
		fileIoUtils.save(mockFile, myFile);

		SharedFolder sharedFolder = sharedFolderUtils.createAndPersistSharedFolder(hello);

		// when
		StringResponse response = sharedFolderService.deleteFile(sharedFolder.getSharedId(), "/file.txt");

		// then
		assertThat(response.getResponse()).isEqualTo("요청한 파일에 대한 삭제가 성공적으로 진행되었습니다.");
		MyFile file = fileRepository.findByOwnerNameAndFullPathAndStatus("testOwner",
			"/hello/file.txt", FileItemStatus.DELETED).get();
		assertThat(file).isNotNull();
		assertThat(file.getFileName()).isEqualTo("file.txt");
	}

	@Test
	@DisplayName("deleteFile: 공유 폴더 하위의 폴더의 파일 삭제 정상 흐름")
	@Transactional
	void deleteFileInChildFolder() {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);
		MyFolder world1 = folderRepositoryUtils.createAndPersistFolder("testOwner", "world1", hello);

		MockMultipartFile mockFile = new MockMultipartFile("file.txt", "file.txt", "text/plain", new byte[] {1});
		MyFile myFile = fileRepositoryUtils.createAndPersistFile(mockFile, "testOwner", world1);
		fileIoUtils.save(mockFile, myFile);

		SharedFolder sharedFolder = sharedFolderUtils.createAndPersistSharedFolder(hello);

		// when
		StringResponse response = sharedFolderService.deleteFile(sharedFolder.getSharedId(), "/world1/file.txt");

		// then
		assertThat(response.getResponse()).isEqualTo("요청한 파일에 대한 삭제가 성공적으로 진행되었습니다.");
		MyFile file = fileRepository.findByOwnerNameAndFullPathAndStatus("testOwner",
			"/hello/world1/file.txt", FileItemStatus.DELETED).get();
		assertThat(file).isNotNull();
		assertThat(file.getFileName()).isEqualTo("file.txt");
	}

	@Test
	@DisplayName("downloadFile: 공유 폴더 파일 다운로드 정상흐름")
	@Transactional
	void downloadFile() throws IOException {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);

		MockMultipartFile mockFile = new MockMultipartFile("file.txt", "file.txt", "text/plain", new byte[] {1});
		MyFile myFile = fileRepositoryUtils.createAndPersistFile(mockFile, "testOwner", hello);
		fileIoUtils.save(mockFile, myFile);

		SharedFolder sharedFolder = sharedFolderUtils.createAndPersistSharedFolder(hello);

		// when
		Resource resource = sharedFolderService.downloadFile(sharedFolder.getSharedId(), "/file.txt");

		// then
		assertThat(resource.getFilename().substring(37)).isEqualTo("file.txt");
		assertThat(resource.contentLength()).isEqualTo(1L);
	}

	@Test
	@DisplayName("downloadFile: 공유 폴더 없는 파일 다운로드 오류")
	@Transactional
	void downloadFileFail() throws IOException {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);

		MockMultipartFile mockFile = new MockMultipartFile("file.txt", "file.txt", "text/plain", new byte[] {1});
		MyFile myFile = fileRepositoryUtils.createAndPersistFile(mockFile, "testOwner", hello);
		fileIoUtils.save(mockFile, myFile);

		SharedFolder sharedFolder = sharedFolderUtils.createAndPersistSharedFolder(hello);

		// when
		RuntimeException exception = assertThrows(RuntimeException.class,
			() -> sharedFolderService.downloadFile(sharedFolder.getSharedId(), "/file1.txt"));
		// then
		assertThat(exception.getMessage()).isEqualTo("잘못된 하위 파일에 대한 접근입니다.");
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

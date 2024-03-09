package com.jongmin.mystorage.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import com.jongmin.mystorage.exception.FileAlreadyExistException;
import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.model.MyFolder;
import com.jongmin.mystorage.model.StorageInfo;
import com.jongmin.mystorage.model.enums.FileItemStatus;
import com.jongmin.mystorage.repository.FileRepository;
import com.jongmin.mystorage.repository.FolderRepository;
import com.jongmin.mystorage.repository.StorageInfoRepository;
import com.jongmin.mystorage.service.folder.FolderService;
import com.jongmin.mystorage.service.response.FolderInfoResponse;
import com.jongmin.mystorage.service.response.FolderResponse;
import com.jongmin.mystorage.utils.ioutils.FileIoUtils;
import com.jongmin.mystorage.utils.repositorytutils.FileRepositoryUtils;
import com.jongmin.mystorage.utils.repositorytutils.FolderRepositoryUtils;
import com.jongmin.mystorage.utils.repositorytutils.StorageInfoRepositoryUtils;

import jakarta.persistence.EntityManager;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class FolderServiceTest {
	@Autowired
	private FolderRepository folderRepository;
	@Autowired
	private FolderRepositoryUtils folderRepositoryUtils;
	@Autowired
	private FolderService folderService;
	@Autowired
	private FileRepository fileRepository;
	@Autowired
	private FileRepositoryUtils fileRepositoryUtils;
	@Autowired
	private FileIoUtils fileIoUtils;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private StorageInfoRepository storageInfoRepository;
	@Autowired
	private StorageInfoRepositoryUtils storageInfoRepositoryUtils;

	@DisplayName("createFolder : 아무 폴더도 없는 상태에서 폴더 생성 요청시 요청한 폴더와 root폴더가 같이 생성한다.")
	@Test
	@Transactional
	void createFolderTest() {
		// when
		FolderResponse createdFolder = folderService.createFolder("testOwner", "testFolder", null);

		// then
		List<MyFolder> folders = folderRepository.findAll();
		assertThat(folders.size()).isEqualTo(2);
		assertThat(createdFolder.getFolderName()).isEqualTo("testFolder");
	}

	@DisplayName("createFolder : 폴더 생성 시 parentFolderUuid의 값이 유효하다면 해당 폴더 하위에 폴더가 생성된다.")
	@Test
	@Transactional
	void createFolderTest2() {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");

		// when
		FolderResponse createdFolder = folderService.createFolder("testOwner", "testFolder", root.getUuid());

		// then
		MyFolder folder = folderRepository.findByUuid(createdFolder.getUuid()).get();
		assertThat(createdFolder.getFullPath()).isEqualTo("/testFolder");
		assertThat(folder.getParentFolder().getUuid()).isEqualTo(root.getUuid());
	}

	@DisplayName("createFolder : 다른 사람의 폴더의 자신의 하위 폴더를 생성하려고 하면 오류가 발생한다.")
	@Test
	@Transactional
	void createFolderTestOtherOwner() {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");

		// when - then
		assertThrows(
			RuntimeException.class, () -> folderService
				.createFolder("testOwner1", "testFolder", root.getUuid())
		);
	}

	@DisplayName("createFolder : 동일한 이름의 폴더가 존재하면 폴더 생성에 실패한다.")
	@Test
	@Transactional
	void createFolderWithDuplicateName() {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);

		entityManager.flush();
		entityManager.clear();

		// then
		assertThrows(
			RuntimeException.class, () -> folderService.createFolder("testOwner", "hello", null)
		);
	}

	@DisplayName("moveFolder : 정상적인 흐름")
	@Test
	@Transactional
	void moveFolderTest() {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);
		MyFolder world1 = folderRepositoryUtils.createAndPersistFolder("testOwner", "world1", hello);
		MyFolder world2 = folderRepositoryUtils.createAndPersistFolder("testOwner", "world2", hello);

		MockMultipartFile mockFile = new MockMultipartFile("test.txt", "test.txt", "text/plain", new byte[] {123});
		MyFile file1 = fileRepositoryUtils.createAndPersistFile(mockFile, "testOwner", world1);

		// when
		folderService.moveFolder("testOwner", world1.getUuid(), world2.getUuid());

		// then
		assertThat(world1.getParentPath()).isEqualTo("/hello/world2");
		assertThat(world1.getFullPath()).isEqualTo("/hello/world2/world1");
		assertThat(world1.getParentFolder().getId()).isEqualTo(world2.getId());

		assertThat(file1.getParentPath()).isEqualTo("/hello/world2/world1");
		assertThat(file1.getFullPath()).isEqualTo("/hello/world2/world1/test.txt");
	}

	@DisplayName("moveFolder : 정상적인 흐름 2")
	@Test
	@Transactional
	void moveFolderTest2() {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);
		MyFolder world1 = folderRepositoryUtils.createAndPersistFolder("testOwner", "world1", hello);
		MyFolder world2 = folderRepositoryUtils.createAndPersistFolder("testOwner", "world2", world1);

		MockMultipartFile mockFile1 = new MockMultipartFile("file1.txt", "file1.txt", "text/plain", new byte[] {123});
		MockMultipartFile mockFile2 = new MockMultipartFile("file2.txt", "file2.txt", "text/plain", new byte[] {123});
		MyFile file1 = fileRepositoryUtils.createAndPersistFile(mockFile1, "testOwner", world1);
		MyFile file2 = fileRepositoryUtils.createAndPersistFile(mockFile2, "testOwner", world2);

		// when
		folderService.moveFolder("testOwner", world2.getUuid(), hello.getUuid());

		// then
		assertThat(world1.getFullPath()).isEqualTo("/hello/world1");
		assertThat(file1.getFullPath()).isEqualTo("/hello/world1/file1.txt");
		assertThat(world2.getFullPath()).isEqualTo("/hello/world2");
		assertThat(file2.getFullPath()).isEqualTo("/hello/world2/file2.txt");
	}

	@DisplayName("moveFolder : 자신의 하위 폴더로 옮겨질 수 없습니다.")
	@Test
	@Transactional
	void moveFolderToChild() {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);
		MyFolder world = folderRepositoryUtils.createAndPersistFolder("testOwner", "world1", hello);

		// when - then
		RuntimeException exception = assertThrows(RuntimeException.class,
			() -> folderService.moveFolder("testOwner", hello.getUuid(), world.getUuid()));
		assertThat(exception.getMessage()).isEqualTo("자신의 하위 폴더로 옮겨질 수 없습니다.");
	}

	@DisplayName("moveFolder : 옮기려는 폴더 내에 이름이 동일한 폴더가 존재s하면 폴더를 옮길 수 없습니다.")
	@Test
	@Transactional
	void moveFolderToSameNameFolderExist() {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);
		MyFolder world = folderRepositoryUtils.createAndPersistFolder("testOwner", "world", root);
		MyFolder helloWorld = folderRepositoryUtils.createAndPersistFolder("testOwner", "world", hello);

		// when - then
		RuntimeException exception = assertThrows(FileAlreadyExistException.class,
			() -> folderService.moveFolder("testOwner", world.getUuid(), hello.getUuid()));
		assertThat(exception.getMessage()).isEqualTo("옮기려는 폴더에 동일한 이름의 파일이 존재해 이동이 불가능 합니다.");
	}

	@DisplayName("deleteFolder : 정상 흐름")
	@Test
	@Transactional
	void deleteFolderTest() {
		// given
		StorageInfo storageInfo = storageInfoRepositoryUtils.getStorageInfo("testOwner");

		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);
		MyFolder world = folderRepositoryUtils.createAndPersistFolder("testOwner", "world", hello);
		storageInfoRepositoryUtils.addFolder(storageInfo, 3L);

		MockMultipartFile mockFile1 = new MockMultipartFile("file1.txt", "file1.txt", "text/plain", new byte[] {123});
		MockMultipartFile mockFile2 = new MockMultipartFile("file2.txt", "file2.txt", "text/plain", new byte[] {123});
		MyFile file1 = fileRepositoryUtils.createAndPersistFile(mockFile1, "testOwner", hello);
		MyFile file2 = fileRepositoryUtils.createAndPersistFile(mockFile2, "testOwner", world);
		fileIoUtils.save(mockFile1, file1);
		fileIoUtils.save(mockFile2, file2);

		// when
		folderService.deleteFolder("testOwner", hello.getUuid());

		// then
		List<MyFolder> folders = folderRepository.findByOwnerNameAndFullPathStartingWith("testOwner", "/hello");
		List<MyFile> files = fileRepository.findByOwnerNameAndFullPathStartingWith("testOwner", "/hello");

		assertThat(folders.size()).isEqualTo(2);
		assertThat(files.size()).isEqualTo(2);
		assertThat(folders).allMatch(folder -> folder.getStatus() == FileItemStatus.DELETED);
		assertThat(files).allMatch(file -> file.getStatus() == FileItemStatus.DELETED);
		assertThat(storageInfo.getFolderCount()).isEqualTo(2);
	}

	@DisplayName("readFolder : 최상단 폴더에서의 정상 흐름")
	@Test
	@Transactional
	void readFolder() {
		// given
		StorageInfo storageInfo = storageInfoRepositoryUtils.getStorageInfo("testOwner");

		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);
		MyFolder world = folderRepositoryUtils.createAndPersistFolder("testOwner", "world", hello);
		storageInfoRepositoryUtils.addFolder(storageInfo, 3L);

		MockMultipartFile mockFile1 = new MockMultipartFile("file1.txt", "file1.txt", "text/plain",
			new byte[] {1, 2, 3});
		MockMultipartFile mockFile2 = new MockMultipartFile("file2.txt", "file2.txt", "text/plain", new byte[] {123});
		MyFile file1 = fileRepositoryUtils.createAndPersistFile(mockFile1, "testOwner", hello);
		MyFile file2 = fileRepositoryUtils.createAndPersistFile(mockFile2, "testOwner", world);
		fileIoUtils.save(mockFile1, file1);
		fileIoUtils.save(mockFile2, file2);

		storageInfoRepositoryUtils.addFile(storageInfo, file1);
		storageInfoRepositoryUtils.addFile(storageInfo, file2);

		entityManager.flush();
		entityManager.clear();

		// when
		FolderInfoResponse response = folderService.readFolder("testOwner", null);

		// then
		assertThat(response.getFolders().size()).isEqualTo(1);
		assertThat(response.getFiles().size()).isEqualTo(0);
		assertThat(response.getFolderCount()).isEqualTo(2);
		assertThat(response.getFileCount()).isEqualTo(2);
		assertThat(response.getSize()).isEqualTo(4L);
	}

	@DisplayName("readFolder: 최상단 폴더가 아닌 폴더에서의 정상 흐름")
	@Test
	@Transactional
	void readFolder2() {
		// given
		StorageInfo storageInfo = storageInfoRepositoryUtils.getStorageInfo("testOwner");

		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);
		MyFolder world = folderRepositoryUtils.createAndPersistFolder("testOwner", "world", hello);
		storageInfoRepositoryUtils.addFolder(storageInfo, 3L);

		MockMultipartFile mockFile1 = new MockMultipartFile("file1.txt", "file1.txt", "text/plain",
			new byte[] {1, 2, 3});
		MockMultipartFile mockFile2 = new MockMultipartFile("file2.txt", "file2.txt", "text/plain", new byte[] {123});
		MyFile file1 = fileRepositoryUtils.createAndPersistFile(mockFile1, "testOwner", hello);
		MyFile file2 = fileRepositoryUtils.createAndPersistFile(mockFile2, "testOwner", world);
		fileIoUtils.save(mockFile1, file1);
		fileIoUtils.save(mockFile2, file2);

		storageInfoRepositoryUtils.addFile(storageInfo, file1);
		storageInfoRepositoryUtils.addFile(storageInfo, file2);

		entityManager.flush();
		entityManager.clear();

		// when
		FolderInfoResponse response = folderService.readFolder("testOwner", hello.getUuid());

		// then
		assertThat(response.getFolders().size()).isEqualTo(1);
		assertThat(response.getFiles().size()).isEqualTo(1);
		assertThat(response.getFolderCount()).isEqualTo(1);
		assertThat(response.getFileCount()).isEqualTo(2);
		assertThat(response.getSize()).isEqualTo(4L);
	}
}

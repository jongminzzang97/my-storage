package com.jongmin.mystorage.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

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

import com.jongmin.mystorage.controller.api.dto.UploadFileRequestDto;
import com.jongmin.mystorage.exception.FileAlreadyExistException;
import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.model.MyFolder;
import com.jongmin.mystorage.model.StorageInfo;
import com.jongmin.mystorage.model.enums.FileItemStatus;
import com.jongmin.mystorage.model.value.GradeMaxSize;
import com.jongmin.mystorage.repository.FileRepository;
import com.jongmin.mystorage.repository.FolderRepository;
import com.jongmin.mystorage.repository.StorageInfoRepository;
import com.jongmin.mystorage.service.file.FileService;
import com.jongmin.mystorage.service.response.FileResponse;
import com.jongmin.mystorage.utils.ioutils.FileIoUtils;
import com.jongmin.mystorage.utils.ioutils.FolderIolUtils;
import com.jongmin.mystorage.utils.repositorytutils.FileRepositoryUtils;
import com.jongmin.mystorage.utils.repositorytutils.FolderRepositoryUtils;
import com.jongmin.mystorage.utils.repositorytutils.StorageInfoRepositoryUtils;

import jakarta.persistence.EntityManager;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class FileServiceTest {
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
	private EntityManager entityManager;

	@AfterEach
	public void clearFolder() {
		File file = new File(baseDir);
		deleteDirectoryAndFiles(file);
	}

	@DisplayName("uploadFile : 정상 흐름")
	@Test
	@Transactional
	public void uploadFileTest() {
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);

		MockMultipartFile mockFile = new MockMultipartFile("test.txt", "test.txt", "text/plain", new byte[] {1});
		UploadFileRequestDto requestDto = new UploadFileRequestDto(mockFile, hello.getUuid());

		// when
		FileResponse fileResponse = fileService.uploadFile("testOwner", requestDto);

		// then
		assertThat(fileResponse.getFileName()).isEqualTo(mockFile.getOriginalFilename());
		assertThat(fileResponse.getSize()).isEqualTo(mockFile.getSize());
		assertThat(fileResponse.getFullPath()).isEqualTo("/hello/test.txt");
	}

	@DisplayName("uploadFile : 폴더 내 동일한 파일 이름 존재 -> FileAlreadyExistException ")
	@Test
	@Transactional
	public void uploadFileTestDuplicateNameFile() {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);
		MockMultipartFile mockFile1 = new MockMultipartFile("test.txt", "test.txt", "text/plain", new byte[] {1});
		MockMultipartFile mockFile2 = new MockMultipartFile("test.txt", "test.txt", "text/plain", new byte[] {12});

		UploadFileRequestDto requestDto1 = new UploadFileRequestDto(mockFile1, hello.getUuid());
		UploadFileRequestDto requestDto2 = new UploadFileRequestDto(mockFile2, hello.getUuid());

		fileService.uploadFile("testOwner", requestDto1);

		// when - then
		FileAlreadyExistException exception = assertThrows(
			FileAlreadyExistException.class, () -> fileService.uploadFile("testOwner", requestDto2)
		);
		assertThat(exception.getMessage()).isEqualTo("이미 동일한 이름의 파일이 존재합니다.");
	}

	@DisplayName("uploadFile : 폴더 내 용량을 초과하면 파일을 업로드할 수 없다.")
	@Test
	@Transactional
	public void uploadFileTestInvalidCapacity() {
		// given
		StorageInfo storageInfo = storageInfoRepositoryUtils.getStorageInfo("testOwner");
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);
		MockMultipartFile mockFile1 = new MockMultipartFile("test.txt", "test.txt", "text/plain", new byte[] {1, 2, 3});

		UploadFileRequestDto requestDto1 = new UploadFileRequestDto(mockFile1, hello.getUuid());

		storageInfo.setSize(GradeMaxSize.NORMAL - 1);

		// when - then
		RuntimeException exception = assertThrows(
			RuntimeException.class, () -> fileService.uploadFile("testOwner", requestDto1)
		);
		assertThat(exception.getMessage()).isEqualTo("자신의 스토리지 용량을 초과하여 저장할 수 없습니다.");
	}



	@DisplayName("readFile : 정상 흐름")
	@Test
	@Transactional
	public void readFileTest() {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);

		MockMultipartFile mockFile = new MockMultipartFile("file1.txt", "file1.txt", "text/plain", new byte[] {123});
		MyFile file = fileRepositoryUtils.createAndPersistFile(mockFile, "testOwner", hello);
		fileIoUtils.save(mockFile, file);

		// when
		FileResponse fileResponse = fileService.readFile("testOwner", file.getUuid());

		// then
		assertThat(fileResponse.getFileName()).isEqualTo(file.getFileName());
		assertThat(fileResponse.getSize()).isEqualTo(file.getSize());
		assertThat(fileResponse.getFullPath()).isEqualTo(file.getFullPath());
		assertThat(fileResponse.getUuid()).isEqualTo(file.getUuid());
	}

	@DisplayName("deleteFile : 정상 흐름")
	@Test
	@Transactional
	public void deleteFileTest() throws IOException {
		// given
		StorageInfo storageInfo = storageInfoRepositoryUtils.getStorageInfo("testOwner");

		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);

		MockMultipartFile mockFile = new MockMultipartFile("file.txt", "file.txt", "text/plain", new byte[] {1});
		MyFile myFile = fileRepositoryUtils.createAndPersistFile(mockFile, "testOwner", hello);
		fileIoUtils.save(mockFile, myFile);

		storageInfoRepositoryUtils.addFolder(storageInfo, 2L);
		storageInfoRepositoryUtils.addFile(storageInfo, myFile);

		// when
		fileService.deleteFile("testOwner", myFile.getUuid());

		// then
		MyFile deletedFile = fileRepository.findByUuid(myFile.getUuid()).get();
		assertThat(storageInfo.getSize()).isEqualTo(0);
		assertThat(deletedFile.getStatus()).isEqualTo(FileItemStatus.DELETED);
		assertThat(storageInfo.getSize()).isEqualTo(0L);
		assertThat(storageInfo.getFileCount()).isEqualTo(0L);
	}

	@DisplayName("moveFile : 정상 흐름")
	@Test
	@Transactional
	public void moveFileTest() {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);
		MyFolder world = folderRepositoryUtils.createAndPersistFolder("testOwner", "world", root);

		MockMultipartFile mockFile1 = new MockMultipartFile("file1.txt", "file1.txt", "text/plain", new byte[] {123});
		MyFile file1 = fileRepositoryUtils.createAndPersistFile(mockFile1, "testOwner", hello);
		fileIoUtils.save(mockFile1, file1);

		// when
		FileResponse fileResponse = fileService.moveFile("testOwner", file1.getUuid(), world.getUuid());

		// then
		assertThat(fileResponse.getFullPath()).isEqualTo("/world/file1.txt");
		assertThat(file1.getFullPath()).isEqualTo("/world/file1.txt");
	}

	@DisplayName("moveFile : 옮기려는 폴더에 동일한 이름의 파일이 존재하면 이동이 불가능하다.")
	@Test
	@Transactional
	public void moveFileTestSameFileNameExist() {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);
		MyFolder world = folderRepositoryUtils.createAndPersistFolder("testOwner", "world", root);

		MockMultipartFile mockFile1 = new MockMultipartFile("file1.txt", "file1.txt", "text/plain", new byte[] {123});
		MockMultipartFile mockFile2 = new MockMultipartFile("file1.txt", "file1.txt", "text/plain", new byte[] {12});
		MyFile file1 = fileRepositoryUtils.createAndPersistFile(mockFile1, "testOwner", hello);
		MyFile file2 = fileRepositoryUtils.createAndPersistFile(mockFile2, "testOwner", world);
		fileIoUtils.save(mockFile1, file1);
		fileIoUtils.save(mockFile2, file2);

		// when - then
		FileAlreadyExistException exception = assertThrows(FileAlreadyExistException.class,
			() -> fileService.moveFile("testOwner", file1.getUuid(), world.getUuid()));
		assertThat(exception.getMessage()).isEqualTo("옮기려는 폴더에 동일한 이름의 파일이 존재해 이동이 불가능 합니다.");
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

package com.jongmin.mystorage.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.assertj.core.api.Assertions;
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
import org.springframework.web.multipart.MultipartFile;

import com.jongmin.mystorage.controller.api.dto.UploadFileRequestDto;
import com.jongmin.mystorage.exception.FileAlreadyExistException;
import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.model.MyFolder;
import com.jongmin.mystorage.model.enums.FileItemStatus;
import com.jongmin.mystorage.repository.FileRepository;
import com.jongmin.mystorage.repository.FolderRepository;
import com.jongmin.mystorage.service.file.FileService;
import com.jongmin.mystorage.service.response.FileResponse;
import com.jongmin.mystorage.utils.ioutils.FileIoUtils;
import com.jongmin.mystorage.utils.ioutils.FolderIolUtils;
import com.jongmin.mystorage.utils.repositorytutils.FileRepositoryUtils;
import com.jongmin.mystorage.utils.repositorytutils.FolderRepositoryUtils;

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
	private EntityManager entityManager;

	@AfterEach
	public void clearFolder() {
		File file = new File(baseDir);
		deleteDirectoryAndFiles(file);
	}

	@DisplayName("uploadFile : 정상 흐름")
	@Test
	@Transactional
	public void uploadFile() throws IOException {
		Path path = Paths.get("src/test/resources/test.txt");
		byte[] content = Files.readAllBytes(path);

		UUID folderUuid = UUID.fromString("12345678-1111-1234-1234-123456789abc");
		String ownerName = "testOwner";

		MyFolder testFolderEntity = MyFolder.createMyFolderEntity(ownerName, "testFolder", null, folderUuid);
		folderIolUtils.createPhysicalFolder(ownerName, folderUuid);
		folderRepository.save(testFolderEntity);

		MockMultipartFile mockFile = new MockMultipartFile("test.txt", "test.txt", "text/plain", content);
		UploadFileRequestDto requestDto = new UploadFileRequestDto(mockFile, folderUuid);

		// when
		FileResponse fileResponse = fileService.uploadFile("testOwner", requestDto);

		// then
		Assertions.assertThat(fileResponse.getFileName()).isEqualTo(mockFile.getOriginalFilename());
		Assertions.assertThat(fileResponse.getSize()).isEqualTo(requestDto.getMultipartFile().getSize());
		Assertions.assertThat(fileResponse.getFullPath())
			.isEqualTo(testFolderEntity.getFullPath() + "/" + mockFile.getOriginalFilename());
	}

	@DisplayName("uploadFile : 폴더 내 동일한 파일 이름 존재 -> FileAlreadyExistException ")
	@Test
	@Transactional
	public void uploadFileDuplicateNameFile() throws IOException {
		// given
		Path path = Paths.get("src/test/resources/test.txt");
		byte[] content = Files.readAllBytes(path);

		UUID folderUuid = UUID.fromString("12345678-1111-1234-1234-123456789abc");
		String ownerName = "testOwner";

		MyFolder testFolderEntity = MyFolder.createMyFolderEntity(ownerName, "testFolder", null, folderUuid);
		folderIolUtils.createPhysicalFolder(ownerName, folderUuid);
		folderRepository.save(testFolderEntity);

		MockMultipartFile mockFile = new MockMultipartFile("test.txt", "test.txt", "text/plain", content);
		UploadFileRequestDto requestDto = new UploadFileRequestDto(mockFile, folderUuid);

		FileResponse fileResponse = fileService.uploadFile("testOwner", requestDto);

		MockMultipartFile mockFile2 = new MockMultipartFile("test.txt", "test.txt", "text/plain", new byte[] {123});
		UploadFileRequestDto requestDto2 = new UploadFileRequestDto(mockFile2, folderUuid);

		entityManager.clear();
		// when - then
		FileAlreadyExistException exception = assertThrows(
			FileAlreadyExistException.class, () -> fileService.uploadFile("testOwner", requestDto2)
		);
		Assertions.assertThat(exception.getMessage()).isEqualTo("이미 동일한 이름의 파일이 존재합니다.");
	}

	@DisplayName("readFile : 정상 흐름")
	@Test
	@Transactional
	public void readFile() throws IOException {
		// given
		String ownerName = "testOwner";
		UUID folderUuid = UUID.fromString("12345678-1111-1234-1234-123456789abc");
		UUID fileUuid = UUID.fromString("12345678-2222-1234-1234-123456789abc");

		Path path = Paths.get("src/test/resources/test.txt");
		byte[] content = Files.readAllBytes(path);
		MultipartFile testFile = new MockMultipartFile("test.txt", "test.txt", "text/plain", content);

		MyFolder testFolderEntity = MyFolder.createMyFolderEntity(ownerName, "testFolder", null, folderUuid);
		folderIolUtils.createPhysicalFolder(ownerName, folderUuid);
		folderRepository.save(testFolderEntity);

		MyFile testFileEntity = MyFile.createMyFileEntity(testFile, ownerName, testFolderEntity, fileUuid);
		fileIoUtils.save(testFile, testFileEntity);
		MyFile fileEntity = fileRepository.save(testFileEntity);

		// when
		FileResponse fileResponse = fileService.readFile(ownerName, fileUuid);

		// then
		Assertions.assertThat(fileResponse.getFileName()).isEqualTo(fileEntity.getFileName());
		Assertions.assertThat(fileResponse.getSize()).isEqualTo(fileEntity.getSize());
		Assertions.assertThat(fileResponse.getFullPath()).isEqualTo(fileEntity.getFullPath());
		Assertions.assertThat(fileResponse.getUuid()).isEqualTo(fileEntity.getUuid());
	}

	@DisplayName("deleteFile : 정상 흐름")
	@Test
	@Transactional
	public void deleteFile() throws IOException {

		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);
		MockMultipartFile mockFile = new MockMultipartFile("file1.txt", "file1.txt", "text/plain", new byte[] {123});

		UUID uuid = UUID.randomUUID();
		MyFile file = fileRepositoryUtils.createAndPersistFile(mockFile, "testOwner", hello, uuid);
		fileIoUtils.save(mockFile, file);

		// when
		fileService.deleteFile("testOwner", file.getUuid());

		// then
		MyFile deletedFile = fileRepository.findByUuid(uuid).get();
		Assertions.assertThat(deletedFile.getStatus()).isEqualTo(FileItemStatus.DELETED);
	}

	@DisplayName("moveFile : 정상 흐름")
	@Test
	@Transactional
	public void moveFile() {
		// given
		MyFolder root = folderRepositoryUtils.createAndPersistRootFolder("testOwner");
		MyFolder hello = folderRepositoryUtils.createAndPersistFolder("testOwner", "hello", root);
		MyFolder world = folderRepositoryUtils.createAndPersistFolder("testOwner", "world", root);

		MockMultipartFile mockFile1 = new MockMultipartFile("file1.txt", "file1.txt", "text/plain", new byte[] {123});
		MockMultipartFile mockFile2 = new MockMultipartFile("file2.txt", "file2.txt", "text/plain", new byte[] {123});
		MyFile file1 = fileRepositoryUtils.createAndPersistFile(mockFile1, "testOwner", hello);
		MyFile file2 = fileRepositoryUtils.createAndPersistFile(mockFile2, "testOwner", world);
		fileIoUtils.save(mockFile1, file1);
		fileIoUtils.save(mockFile2, file2);

		// when
		FileResponse fileResponse = fileService.moveFile("testOwner", file1.getUuid(), world.getUuid());

		// then
		Assertions.assertThat(fileResponse.getFullPath()).isEqualTo("/world/file1.txt");

		MyFile movedFile = fileRepository.findByUuid(file1.getUuid()).get();
		Assertions.assertThat(movedFile.getFullPath()).isEqualTo("/world/file1.txt");
	}

	@DisplayName("moveFile : 옮기려는 폴더에 동일한 이름의 파일이 존재하면 이동이 불가능하다.")
	@Test
	@Transactional
	public void moveFileToSameFileNameExist() {
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
		Assertions.assertThat(exception.getMessage()).isEqualTo("옮기려는 폴더에 동일한 이름의 파일이 존재해 이동이 불가능 합니다.");
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

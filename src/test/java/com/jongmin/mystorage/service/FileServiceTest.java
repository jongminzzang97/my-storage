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
import com.jongmin.mystorage.exception.FileNotInDatabaseException;
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

	@DisplayName("checkMyFileAndGet : 정상 흐름 파일이 물리적으로 존재하고 파일 정보가 DB에 저장되어 있으면서 요청한 파일의 주인이면 파일 엔티티를 찾아 제공할 수 있다.")
	@Test
	@Transactional
	public void checkMyFileAndGet() throws IOException {
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
		fileRepository.save(testFileEntity);

		// when
		MyFile getFile = fileService.checkMyFileAndGet(ownerName, fileUuid);

		// then
		Assertions.assertThat(getFile.getUuid()).isEqualTo(fileUuid);
	}

	@DisplayName("checkMyFileAndGet : 파일 정보가 DB에 존재하지 않음 -> FileNotInDatabaseException")
	@Test
	@Transactional
	public void checkMyFileAndGetNotInDB() throws IOException {
		// given
		String ownerName = "testOwner";
		UUID fileUuid = UUID.fromString("12345678-2222-1234-1234-123456789abc");

		// when - then
		assertThrows(
			FileNotInDatabaseException.class, () -> fileService.checkMyFileAndGet(ownerName, fileUuid)
		);
	}

	@DisplayName("checkMyFileAndGet : DB에 파일이 삭제된 상태로 기록 되어 있음 -> FileNotInDatabaseException")
	@Test
	@Transactional
	public void checkMyFileDeleted() throws IOException {
		// given
		String ownerName = "testOwner";
		UUID folderUuid = UUID.fromString("12345678-1111-1234-1234-123456789abc");
		UUID fileUuid = UUID.fromString("12345678-2222-1234-1234-123456789abc");
		Path path = Paths.get("src/test/resources/test.txt");
		byte[] content = Files.readAllBytes(path);

		MultipartFile testFile = new MockMultipartFile("test.txt", "test.txt", "text/plain", content);
		MyFolder testFolderEntity = MyFolder.createMyFolderEntity(ownerName, "testFolder", null, folderUuid);
		folderRepository.save(testFolderEntity);

		MyFile testFileEntity = MyFile.createMyFileEntity(testFile, ownerName, testFolderEntity, fileUuid);
		fileRepository.save(testFileEntity);

		// 삭제 진행
		fileRepositoryUtils.deleteFile(testFileEntity);

		// when - then
		RuntimeException exception = assertThrows(
			RuntimeException.class, () -> fileService.checkMyFileAndGet(ownerName, fileUuid)
		);
		Assertions.assertThat(exception.getMessage()).isEqualTo("파일이 삭제되어 있는 상태입니다.");
	}

	@DisplayName("checkMyFileAndGet : 파일이 디스크에 존재 하지 않음 -> RuntimeException(\"파일이 디스크 상에 존재하지 않습니다.\")")
	@Test
	@Transactional
	public void checkMyFileNotInDisk() throws IOException {
		// given
		String ownerName = "testOwner";
		UUID folderUuid = UUID.fromString("12345678-1111-1234-1234-123456789abc");
		UUID fileUuid = UUID.fromString("12345678-2222-1234-1234-123456789abc");
		Path path = Paths.get("src/test/resources/test.txt");
		byte[] content = Files.readAllBytes(path);
		MultipartFile testFile = new MockMultipartFile("test.txt", "test.txt", "text/plain", content);

		MyFolder testFolderEntity = MyFolder.createMyFolderEntity(ownerName, "testFolder", null, folderUuid);
		folderRepository.save(testFolderEntity);
		MyFile testFileEntity = MyFile.createMyFileEntity(testFile, ownerName, testFolderEntity, fileUuid);
		fileRepository.save(testFileEntity);

		// when - then
		RuntimeException exception = assertThrows(
			RuntimeException.class, () -> fileService.checkMyFileAndGet(ownerName, fileUuid)
		);
		Assertions.assertThat(exception.getMessage()).isEqualTo("파일이 디스크 상에 존재하지 않습니다.");
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
		fileService.deleteFile(ownerName, fileUuid);

		// then
		MyFile deletedFile = fileRepository.findByUuid(fileUuid).get();
		Assertions.assertThat(deletedFile.getStatus()).isEqualTo(FileItemStatus.DELETED);
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

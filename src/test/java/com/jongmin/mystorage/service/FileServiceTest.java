package com.jongmin.mystorage.service;

import static com.jongmin.mystorage.service.request.DefaultFileRequest.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.io.InputStream;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.jongmin.mystorage.exception.FileAlreadyExistException;
import com.jongmin.mystorage.exception.FileNotInDatabaseException;
import com.jongmin.mystorage.exception.FileNotInFileSystemException;
import com.jongmin.mystorage.exception.OwnerNameException;
import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.repository.FileRepository;
import com.jongmin.mystorage.service.file.FileService;
import com.jongmin.mystorage.service.file.FileServiceResponse;
import com.jongmin.mystorage.service.file.FileSystemWrapper;
import com.jongmin.mystorage.service.request.DefaultFileRequest;
import com.jongmin.mystorage.service.request.FileUploadRequest;
import com.jongmin.mystorage.service.response.StringResponse;

@ExtendWith(SpringExtension.class)
class FileServiceTest {

	@Mock
	private FileSystemWrapper fileSystemWrapper;

	@Mock
	private FileRepository fileRepository;

	@InjectMocks
	private FileService fileService;

	@DisplayName("정상적인 흐름의 파일 업로드는 정상적으로 실행되어야 한다.")
	@Test
	void uploadFileTest() throws Exception {
		// given
		String fileName = "test.txt";
		String owner = "user1";
		String fileDir = owner + "_" + fileName;
		MockMultipartFile mockMultipartFile =
			new MockMultipartFile("file", fileName, "text/plain", "test data".getBytes());
		FileUploadRequest request = new FileUploadRequest(fileName, owner, mockMultipartFile);

		given(fileSystemWrapper.fileExists(fileDir)).willReturn(false);
		given(fileSystemWrapper.copy(any(InputStream.class), eq(fileDir))).willReturn(mockMultipartFile.getSize());
		given(fileRepository.save(any(MyFile.class))).willReturn(new MyFile());

		// when
		FileServiceResponse response = fileService.uploadFile(request);

		// then
		assertNotNull(response);
		then(fileRepository).should(times(1)).save(any(MyFile.class));
		then(fileSystemWrapper).should(times(1)).fileExists(fileDir);
		then(fileSystemWrapper).should(times(1)).copy(any(InputStream.class), eq(fileDir));
	}

	// 빈 문자열에 대한 검사는 Controller의 @valid에서 검증을 진행하지만
	// Owner에 '_' 문자가 포함여부는 서비스 단에서 검사한다. -> 서비스의 로직이므로
	@DisplayName("파일 업로드시 Owner에 '_' 문자가 포함되어 있으면 OwnerNameException을 던져야 한다.")
	@Test
	void uploadFileWithInvalidOwnerTest() {
		// given
		String fileName = "test.txt";
		String owner = "user_1"; // 유효하지 않은 유저 이름
		MockMultipartFile mockMultipartFile =
			new MockMultipartFile("file", fileName, "text/plain", "test data".getBytes());
		FileUploadRequest request = new FileUploadRequest(fileName, owner, mockMultipartFile);

		// when - then
		assertThrows(OwnerNameException.class, () -> {
			fileService.uploadFile(request);
		});
	}

	@DisplayName("파일이 업로드시 파일 이름이 중복될 경우 FileAlreadyExistsException을 던져야 한다.")
	@Test
	void uploadDuplicateFileNameTest() {
		// given
		String fileName = "test.txt";
		String owner = "user1";
		String fileDir = owner + "_" + fileName;
		MockMultipartFile mockMultipartFile =
			new MockMultipartFile("file", fileName, "text/plain", "test data".getBytes());
		FileUploadRequest request = new FileUploadRequest(fileName, owner, mockMultipartFile);

		given(fileSystemWrapper.fileExists(fileDir)).willReturn(true);

		// when - then
		assertThrows(FileAlreadyExistException.class, () -> {
			fileService.uploadFile(request);
		});
	}

	@DisplayName("파일 다운로드시 문제가 없는 경우 resource를 반환해야 한다.")
	@Test
	void downloadFile() {
		// given
		String fileName = "test.txt";
		String owner = "user1";
		DefaultFileRequest request = defaultFileRequestFromFileNameAndOwner(fileName, owner);
		MyFile myFile = new MyFile();
		String fileDir = owner + "_" + fileName;
		Resource mockResource = mock(Resource.class);

		given(fileRepository.findByOwnerNameAndFileName(owner, fileName)).willReturn(Optional.of(myFile));
		given(fileSystemWrapper.fileNotExists(fileDir)).willReturn(false);
		given(fileSystemWrapper.fileDirToResource(fileDir)).willReturn(mockResource);

		// when
		Resource result = fileService.downloadFile(request);

		// then
		assertEquals(mockResource, result);
	}

	@DisplayName("파일 다운로드시 파일 정보가 DB에 존재하지 않을 때는 FileNotFoundException을 던져야 한다.")
	@Test
	void downloadFileWhenFileNotInDatabase() {
		// given
		String fileName = "test.txt";
		String owner = "user1";
		DefaultFileRequest request = defaultFileRequestFromFileNameAndOwner(fileName, owner);


		given(fileRepository.findByOwnerNameAndFileName(owner, fileName)).willReturn(Optional.empty());

		// when-then
		Exception exception = assertThrows(FileNotInDatabaseException.class, () -> {
			fileService.downloadFile(request);
		});
		assertEquals("파일에 대한 정보가 DB에 존재하지 않습니다.", exception.getMessage());
	}

	@DisplayName("파일 다운로드시 파일이 파일 시스템에 존재하지 않을 때는 FileNotFoundException을 던져야 한다.")
	@Test
	void downloadFileWhenFileNotInFileSystem() {
		// given
		String fileName = "test.txt";
		String owner = "user1";
		DefaultFileRequest request = defaultFileRequestFromFileNameAndOwner(fileName, owner);
		String fileDir = owner + "_" + fileName;

		given(fileRepository.findByOwnerNameAndFileName(owner, fileName)).willReturn(Optional.of(new MyFile()));
		given(fileSystemWrapper.fileNotExists(fileDir)).willReturn(true);

		// when-then
		Exception exception = assertThrows(FileNotInFileSystemException.class, () -> {
			fileService.downloadFile(request);
		});
		assertEquals("다운로드 하려는 파일이 존재하지 않습니다.", exception.getMessage());
	}

	@DisplayName("파일 삭제 요청이 정상적으로 처리된 경우 \"파일이 성공적으로 삭제되었습니다.\"를 반환해야한다.")
	@Test
	void deleteFile() {
		// given
		String fileName = "test.txt";
		String owner = "user1";
		DefaultFileRequest request = defaultFileRequestFromFileNameAndOwner(fileName, owner);
		MyFile myFile = new MyFile();
		String fileDir = owner + "_" + fileName;
		Resource mockResource = mock(Resource.class);

		given(fileRepository.findByOwnerNameAndFileName(owner, fileName)).willReturn(Optional.of(myFile));
		given(fileSystemWrapper.fileNotExists(fileDir)).willReturn(false);
		given(fileSystemWrapper.fileDirToResource(fileDir)).willReturn(mockResource);

		// when
		StringResponse stringResponse = fileService.deleteFile(request);

		// then
		assertEquals(stringResponse.getResponse(), "파일이 성공적으로 삭제되었습니다.");
	}

	@DisplayName("파일 삭제시 파일 정보가 DB에 존재하지 않을 때는 FileNotFoundException을 던져야 한다.")
	@Test
	void deleteFileWhenFileNotInDatabase() {
		// given
		String fileName = "test.txt";
		String owner = "user1";
		DefaultFileRequest request = defaultFileRequestFromFileNameAndOwner(fileName, owner);

		given(fileRepository.findByOwnerNameAndFileName(owner, fileName)).willReturn(Optional.empty());

		// when-then
		Exception exception = assertThrows(FileNotInDatabaseException.class, () -> {
			fileService.deleteFile(request);
		});
		assertEquals("파일에 대한 정보가 DB에 존재하지 않습니다.", exception.getMessage());
	}

	@DisplayName("파일 삭제시 파일이 파일 시스템에 존재하지 않을 때는 FileNotFoundException을 던져야 한다.")
	@Test
	void deleteFileWhenFileNotInFileSystem() {
		// given
		String fileName = "test.txt";
		String owner = "user1";
		DefaultFileRequest request = defaultFileRequestFromFileNameAndOwner(fileName, owner);
		String fileDir = owner + "_" + fileName;

		given(fileRepository.findByOwnerNameAndFileName(owner, fileName)).willReturn(Optional.of(new MyFile()));
		given(fileSystemWrapper.fileNotExists(fileDir)).willReturn(true);

		// when-then
		Exception exception = assertThrows(FileNotInFileSystemException.class, () -> {
			fileService.deleteFile(request);
		});
		assertEquals("삭제하려는 파일이 존재하지 않습니다.", exception.getMessage());
	}
}

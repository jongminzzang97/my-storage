package com.jongmin.mystorage.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.io.InputStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.jongmin.mystorage.exception.FileAlreadyExistException;
import com.jongmin.mystorage.exception.OwnerNameException;
import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.repository.FileRepository;
import com.jongmin.mystorage.service.file.FileService;
import com.jongmin.mystorage.service.file.FileServiceResponse;
import com.jongmin.mystorage.service.file.FileSystemWrapper;
import com.jongmin.mystorage.service.request.FileUploadRequest;

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

}

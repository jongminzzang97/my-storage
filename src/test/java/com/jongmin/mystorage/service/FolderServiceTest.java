package com.jongmin.mystorage.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import com.jongmin.mystorage.model.MyFolder;
import com.jongmin.mystorage.repository.FolderRepository;
import com.jongmin.mystorage.service.folder.FolderService;
import com.jongmin.mystorage.service.response.FolderResponse;
import com.jongmin.mystorage.utils.ioutils.FolderIolUtils;
import com.jongmin.mystorage.utils.repositorytutils.FolderRepositoryUtils;

import jakarta.persistence.EntityManager;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class FolderServiceTest {
	@Autowired
	private FolderRepository folderRepository;
	@Autowired
	private FolderRepositoryUtils folderRepositoryUtils;
	@Autowired
	private FolderIolUtils folderIolUtils;
	@Autowired
	private FolderService folderService;
	@Autowired
	private EntityManager entityManager;

	@DisplayName("폴더가 없는 상태에서 폴더 생성 요청시 요청한 폴더와 root폴더가 같이 생성한다.")
	@Test
	@Transactional
	void createFolderTestFirst() {
		// given
		String ownerName = "testOwner";
		String folderName = "testFolder";
		UUID parentFolderUuid = null;

		// when
		FolderResponse createdFolder = folderService.createFolder(ownerName, folderName, null);
		List<MyFolder> myFolders = folderRepository.findAll();
		System.out.println("myFolders.size() = " + myFolders.size());
		// then
		assertThat(createdFolder.getUuid()).isNotNull();
		assertThat(createdFolder.getFolderName()).isEqualTo(folderName);
		// 처음 폴더를 생성할 때는 root 폴더를 같이 생성한다.
		assertThat(myFolders.size()).isEqualTo(2);
	}

	@DisplayName("폴더 생성 시 parentFolderUuid의 값이 유효하다면 해당 폴더 밑에 폴더가 생성된다.")
	@Test
	@Transactional
	void createFolderTest() {
		// given
		String ownerName = "testOwner";
		String folderName1 = "testFolder1";
		String folderName2 = "testFolder2";
		FolderResponse createdFolderResponse1 = folderService
			.createFolder(ownerName, folderName1, null);

		// when
		FolderResponse createdFolderResponse2 = folderService
			.createFolder(ownerName, folderName2, createdFolderResponse1.getUuid());

		// then
		Optional<MyFolder> createdFolder2 = folderRepository.findByUuid(createdFolderResponse2.getUuid());

		assertThat(createdFolder2).isNotNull();
		assertThat(createdFolder2.get().getParentFolder().getUuid()).isEqualTo(createdFolderResponse1.getUuid());
	}

	@DisplayName("폴더 생성시 부모 폴더가 내 폴더가 아니면 오류가 발생한다.")
	@Test
	@Transactional
	void createFolderTestOtherOwner() {
		// given
		String ownerName1 = "testOwner1";
		String ownerName2 = "testOwner2";
		String folderName1 = "testFolder1";
		String folderName2 = "testFolder2";

		// when - then
		FolderResponse createdFolderResponse1 = folderService.createFolder(ownerName1, folderName1, null);
		assertThrows(
			RuntimeException.class, () -> folderService
				.createFolder(ownerName2, folderName2, createdFolderResponse1.getUuid())
		);
	}

	@DisplayName("폴더 생성 시 해당 부모 폴더 밑에 동일한 이름의 폴더가 존재하면 폴더 생성에 실패한다.")
	@Test
	@Transactional
	void createFolderWithDuplicateName() {
		// given
		String ownerName1 = "testOwner1";
		String folderName1 = "testFolder1";

		// when
		FolderResponse createdFolderResponse1 = folderService.createFolder(ownerName1, folderName1, null);
		entityManager.clear();

		// then
		assertThrows(
				RuntimeException.class, () -> folderService.createFolder(ownerName1, folderName1, null)
		);
	}

}

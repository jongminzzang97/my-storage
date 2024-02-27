package com.jongmin.mystorage.service.folder;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jongmin.mystorage.model.MyFolder;
import com.jongmin.mystorage.repository.FolderRepository;
import com.jongmin.mystorage.service.response.FolderInfoResponse;
import com.jongmin.mystorage.service.response.FolderResponse;
import com.jongmin.mystorage.utils.ioutils.FolderIolUtils;
import com.jongmin.mystorage.utils.repositorytutils.FolderRepositoryUtils;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class FolderService {

	private final FolderRepository folderRepository;
	private final FolderRepositoryUtils folderRepositoryUtils;
	private final FolderIolUtils folderIolUtils;

	private MyFolder getFolderOrRoot(UUID folderId, String ownerName) {
		MyFolder folder;
		if (folderId == null) {
			folder = folderRepositoryUtils.getRootFolder(ownerName);
		} else {
			folder = folderRepositoryUtils.getFolderByUuid(folderId);
			if (!folder.getOwnerName().equals(ownerName)) {
				throw new RuntimeException("상위 폴더의 소유자가 본인이 아닙니다.");
			}
		}
		return folder;
	}

	@Transactional
	public FolderResponse createFolder(String ownerName, String folderName, UUID parentFolderUuid) {

		MyFolder parentFolder = getFolderOrRoot(parentFolderUuid, ownerName);

		List<MyFolder> childFolders = parentFolder.getChildFolders();
		if (childFolders != null) {
			for (MyFolder folder : childFolders) {
				if (folder.getFolderName().equals(folderName)) {
					throw new RuntimeException("동일한 이름의 폴더가 이미 존재합니다.");
				}
			}
		}

		MyFolder createdFolder = MyFolder.builder()
										.uuid(UUID.randomUUID())
										.folderName(folderName)
										.ownerName(ownerName)
										.fullPath(parentFolder.getFullPath() + "/" + folderName)
										.parentFolder(parentFolder)
										.parentPath(parentFolder.getFullPath())
										.build();


		folderIolUtils.createPhysicalFolder(ownerName, createdFolder.getUuid());
		folderRepository.save(createdFolder);

		return FolderResponse.fromMyFolder(createdFolder);
	}

	public FolderInfoResponse readFolder(String ownerName, UUID folderId) {
		MyFolder folder = getFolderOrRoot(folderId, ownerName);
		return FolderInfoResponse.fromMyFolder(folder);
	}

}

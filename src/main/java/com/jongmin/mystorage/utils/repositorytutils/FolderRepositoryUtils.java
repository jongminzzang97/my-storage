package com.jongmin.mystorage.utils.repositorytutils;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.model.MyFolder;
import com.jongmin.mystorage.repository.FolderRepository;
import com.jongmin.mystorage.utils.ioutils.FolderIolUtils;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class FolderRepositoryUtils {

	private final FolderRepository folderRepository;
	private final FolderIolUtils folderIolUtils;

	private MyFolder createRootFolder(String ownerName) {
		MyFolder root = MyFolder.createMyFolderEntity(ownerName, "", null);
		folderRepository.save(root);
		return root;
	}

	public MyFolder getRootFolder(String ownerName) {
		Optional<MyFolder> optional = folderRepository.findByOwnerNameAndFullPath(ownerName, "");
		MyFolder root;
		if (optional.isEmpty()) {
			root = createRootFolder(ownerName);
		} else {
			root = optional.get();
		}
		return root;
	}

	public MyFolder getFolderByUuid(UUID folderUuid) {
		MyFolder folder = null;;
		Optional<MyFolder> optionalFolder = folderRepository.findByUuid(folderUuid);
		if (optionalFolder.isEmpty()) {
			throw new RuntimeException("해당 UUID를 갖는 폴더가 존재하지 않습니다.");
		} else {
			folder = optionalFolder.get();
		}
		return folder;
	}

	public MyFolder getFolderAndValidate(String ownerName, UUID folderUuid) {
		MyFolder folder = null;
		Optional<MyFolder> optionalFolder = folderRepository.findByUuid(folderUuid);
		if (optionalFolder.isEmpty()) {
			throw new RuntimeException("해당 UUID를 갖는 폴더가 존재하지 않습니다.");
		} else {
			folder = optionalFolder.get();
		}

		if (!folder.getOwnerName().equals(ownerName)) {
			throw new RuntimeException("상위 폴더의 소유자가 본인이 아닙니다.");
		}
		return folder;
	}
}

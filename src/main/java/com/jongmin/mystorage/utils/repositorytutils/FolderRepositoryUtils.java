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
		MyFolder root = MyFolder.builder()
			.folderName("")
			.fullPath("")
			.uuid(UUID.randomUUID())
			.ownerName(ownerName)
			.build();
		folderIolUtils.createPhysicalFolder(ownerName, root.getUuid());
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
		Optional<MyFolder> optionalFolder = folderRepository.findByUuid(folderUuid);
		MyFolder folder;
		if (optionalFolder.isEmpty()) {
			throw new RuntimeException("해당 UUID를 갖는 폴더가 존재하지 않습니다.");
		} else {
			folder = optionalFolder.get();
		}
		return folder;
	}
}

package com.jongmin.mystorage.utils;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.context.annotation.Configuration;

import com.jongmin.mystorage.model.MyFolder;
import com.jongmin.mystorage.model.SharedFolder;
import com.jongmin.mystorage.repository.SharedFolderRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SharedFolderUtils {

	private final SharedFolderRepository sharedFolderRepository;

	public SharedFolder createAndPersistSharedFolder(MyFolder folder) {
		SharedFolder sharedFolder = SharedFolder.builder()
			.myFolder(folder)
			.expiredAt(LocalDateTime.now().plusHours(3))
			.sharedId(UUID.randomUUID())
			.ownerName(folder.getOwnerName())
			.fullPath(folder.getFullPath())
			.build();
		return sharedFolderRepository.save(sharedFolder);
	}

	// public SharedFolder getSharedFolder(UUID sharedFolderUuid) {
	// 	Optional<SharedFolder> bySharedId = sharedFolderRepository.findBySharedId(sharedFolderUuid);
	// 	SharedFolder sharedFolder = bySharedId.orElseThrow(() -> new RuntimeException("해당 uuid로 공유된 폴더가 존재하지 않습니다."));
	// 	if (sharedFolder.getExpiredAt().isBefore(LocalDateTime.now())) {
	// 		throw new RuntimeException("폴더 공유가 종료되었습니다.");
	// 	}
	// 	return sharedFolder;
	// }
}

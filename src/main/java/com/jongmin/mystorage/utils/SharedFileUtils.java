package com.jongmin.mystorage.utils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.model.SharedFile;
import com.jongmin.mystorage.repository.SharedFileRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class SharedFileUtils {

	private final SharedFileRepository sharedFileRepository;

	public SharedFile createAndPersistSharedFile(MyFile file) {
		SharedFile sharedFile = SharedFile.builder()
			.myFile(file)
			.expiredAt(LocalDateTime.now().plusHours(3))
			.uuid(UUID.randomUUID())
			.ownerName(file.getOwnerName())
			.fullPath(file.getFullPath())
			.fileName(file.getFileName())
			.build();
		return sharedFileRepository.save(sharedFile);
	}

	public SharedFile getSharedFile(UUID sharedFileUuid) {
		Optional<SharedFile> bySharedId = sharedFileRepository.findByUuid(sharedFileUuid);
		SharedFile sharedFile = bySharedId.orElseThrow(() -> new RuntimeException("해당 uuid로 공유된 파일이 존재하지 않습니다."));
		if (sharedFile.getExpiredAt().isBefore(LocalDateTime.now())) {
			throw new RuntimeException("파일 공유가 종료되었습니다.");
		}
		return sharedFile;
	}

}

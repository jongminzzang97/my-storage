package com.jongmin.mystorage.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jongmin.mystorage.model.MyFile;
import com.jongmin.mystorage.model.enums.FileItemStatus;

@Repository
public interface FileRepository extends JpaRepository<MyFile, Long> {
	Optional<MyFile> findByOwnerNameAndFileNameAndParentFolderIdAndStatus(String ownerName,
		String filename, Long id, FileItemStatus status);

	Optional<MyFile> findByUuid(UUID fileUuid);

	Optional<MyFile> findByFileName(String fileName);

	List<MyFile> findByOwnerNameAndFullPathStartingWith(String ownerName, String fullPath);
}

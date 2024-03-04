package com.jongmin.mystorage.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jongmin.mystorage.model.MyFolder;

@Repository
public interface FolderRepository extends JpaRepository<MyFolder, Long> {
	Optional<MyFolder> findByUuid(UUID uuid);

	Optional<MyFolder> findByFullPath(String fullPath);

	Optional<MyFolder> findByOwnerNameAndFullPath(String ownerName, String fullPath);

	List<MyFolder> findByOwnerNameAndFullPathStartingWith(String ownerName, String fullPath);
}

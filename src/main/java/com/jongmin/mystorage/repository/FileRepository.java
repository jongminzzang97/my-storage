package com.jongmin.mystorage.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jongmin.mystorage.model.MyFile;

@Repository
public interface FileRepository extends JpaRepository<MyFile, Long> {
	Optional<MyFile> findByOwnerNameAndFileName(String ownerName, String filename);

	Optional<MyFile> findByUuid(UUID fileUuid);
}

package com.jongmin.mystorage.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jongmin.mystorage.model.SharedFile;

@Repository
public interface SharedFileRepository extends JpaRepository<SharedFile, Long> {
	Optional<SharedFile> findByUuid(UUID uuid);
}

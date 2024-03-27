package com.jongmin.mystorage.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jongmin.mystorage.model.SharedFolder;

@Repository
public interface SharedFolderRepository extends JpaRepository<SharedFolder, Long> {
	Optional<SharedFolder> findBySharedId(UUID uuid);
}

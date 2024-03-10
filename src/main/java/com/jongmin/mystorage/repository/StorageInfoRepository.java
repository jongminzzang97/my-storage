package com.jongmin.mystorage.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jongmin.mystorage.model.StorageInfo;

@Repository
public interface StorageInfoRepository extends JpaRepository<StorageInfo, Long> {
	Optional<StorageInfo> findByOwnerName(String ownerName);
}

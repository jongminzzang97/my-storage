package com.jongmin.mystorage.service.storage;

import org.springframework.stereotype.Service;

import com.jongmin.mystorage.model.StorageInfo;
import com.jongmin.mystorage.service.response.StorageInfoResponse;
import com.jongmin.mystorage.utils.repositorytutils.StorageInfoRepositoryUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageInfoService {

	private final StorageInfoRepositoryUtils storageInfoRepositoryUtils;

	public StorageInfoResponse getStorageInfo(String ownerName) {
		StorageInfo storageInfo = storageInfoRepositoryUtils.getStorageInfo(ownerName);
		return StorageInfoResponse.fromStorageInfo(storageInfo);
	}

}

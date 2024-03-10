package com.jongmin.mystorage.controller.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.jongmin.mystorage.service.response.StorageInfoResponse;
import com.jongmin.mystorage.service.storage.StorageInfoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class StorageInfoApiController {

	private final StorageInfoService storageInfoService;

	@GetMapping("/api/storageInfo")
	public StorageInfoResponse getStorageInfo(@RequestHeader("ownerName") String ownerName) {
		return storageInfoService.getStorageInfo(ownerName);
	}
}

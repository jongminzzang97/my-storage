package com.jongmin.mystorage.service.request;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
public class DefaultFileRequest {

	private UUID fileUuid;
	private String ownerName;

	@Builder
	private DefaultFileRequest(UUID fileUuid, String owner) {
		this.fileUuid = fileUuid;
		this.ownerName = owner;
	}

	public static DefaultFileRequest defaultFileRequestFromFileNameAndOwner(UUID fileUuid, String owner) {
		return DefaultFileRequest.builder()
			.fileUuid(fileUuid)
			.owner(owner)
			.build();
	}
}

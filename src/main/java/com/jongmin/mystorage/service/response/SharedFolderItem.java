package com.jongmin.mystorage.service.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SharedFolderItem {

	private String type;
	private String relativePath;
	private String name;
	private Long size;

	@Builder
	public SharedFolderItem(String type, String relativePath, String name, Long size) {
		this.type = type;
		this.relativePath = relativePath;
		this.name = name;
		this.size = size;
	}
}

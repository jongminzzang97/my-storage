package com.jongmin.mystorage.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class SharedFile extends BaseEntity {
	private Long id;
	private UUID uuid;
	private String ownerName;
	private String fullPath;
	private String fileName;
	private LocalDateTime expiredAt;
	@ManyToOne
	private MyFile myFile;

	@Builder
	public SharedFile(Long id, UUID uuid, String ownerName, String fullPath, String fileName, LocalDateTime expiredAt,
		MyFile myFile) {
		this.id = id;
		this.uuid = uuid;
		this.ownerName = ownerName;
		this.fullPath = fullPath;
		this.fileName = fileName;
		this.expiredAt = expiredAt;
		this.myFile = myFile;
	}
}

package com.jongmin.mystorage.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class SharedFolder extends BaseEntity {
	private Long id;
	private UUID sharedId;
	private String ownerName;
	private String fullPath;
	private String folderName;
	private LocalDateTime expiredAt;
	@ManyToOne
	private MyFolder myFolder;

	@Builder
	public SharedFolder(Long id, UUID sharedId, String ownerName, String fullPath, LocalDateTime expiredAt,
		MyFolder myFolder) {
		this.id = id;
		this.sharedId = sharedId;
		this.ownerName = ownerName;
		this.fullPath = fullPath;
		this.expiredAt = expiredAt;
		this.myFolder = myFolder;
	}
}

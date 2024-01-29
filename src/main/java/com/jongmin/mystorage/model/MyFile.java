package com.jongmin.mystorage.model;

import com.jongmin.mystorage.model.enums.MyFileStatus;

import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class MyFile extends BaseEntity {

	private String owner;
	private String name;
	private Long size;
	private MyFileStatus status;

	@Builder
	private MyFile(String owner, String name, Long size) {
		this.owner = owner;
		this.name = name;
		this.size = size;
		this.status = MyFileStatus.SAVED;
	}

}


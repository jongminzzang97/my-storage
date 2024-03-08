package com.jongmin.mystorage.repository;

import lombok.Getter;

@Getter
public class CountAndSum {
	private Long count;
	private Long totalSize;

	public CountAndSum(Long count, Long totalSize) {
		this.count = count;
		this.totalSize = totalSize;
	}
}

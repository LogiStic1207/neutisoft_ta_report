package com.neutisoft.main.dto;

import lombok.Getter;

@Getter
public class SystemTimeResponse {

	private final Long systemTime;

	public SystemTimeResponse(Long systemTime) {
		this.systemTime = systemTime;
	}
}

package com.neutisoft.main.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neutisoft.main.dto.SystemTimeResponse;

@RestController
@RequestMapping(value = "/v1/system")
public class SystemController {

	@GetMapping(value = "/time")
	public ResponseEntity<SystemTimeResponse> time() {
		return ResponseEntity.ok(new SystemTimeResponse(System.currentTimeMillis()));
	}
}

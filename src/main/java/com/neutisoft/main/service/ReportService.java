package com.neutisoft.main.service;

import org.springframework.stereotype.Service;

import com.neutisoft.main.repository.KlineRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

	private final KlineRepository klineRepository;
}

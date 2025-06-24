package com.neutisoft.main.controller;

import com.neutisoft.main.dto.ReportResponse;
import com.neutisoft.main.service.ReportService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/load-candles")
    public ResponseEntity<String> loadCandles() {
        reportService.fetchAndStoreBinanceCandles();
        return ResponseEntity.ok("Candles loaded.");
    }

    @GetMapping("/summary")
    public ResponseEntity<ReportResponse> getSummaryReport() {
        return ResponseEntity.ok(reportService.simulateSimpleStrategy());
    }

    @GetMapping("/sma-strategy")
    public ResponseEntity<List<String>> runSmaStrategy() {
        return ResponseEntity.ok(reportService.runSmaCrossoverStrategy());
    }

}

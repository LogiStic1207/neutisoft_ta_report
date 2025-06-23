package com.neutisoft.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor // ✅ Enables new ReportResponse("a", "b", ...)
@NoArgsConstructor // Optional, for serialization
public class ReportResponse {
    private String startTime;
    private String endTime;
    private String duration;
    private String profitRate;
    private String totalProfit;
}

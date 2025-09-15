package com.example.ma_visualization_be.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MachineAnalysisRequest {
    private String month; // Format: "202507"
    private int monthBack = 12; // Default 12 months
    private int topLimit = 10; // Default top 10
    private List<String> divisions; // ["PRESS", "MOLD", "GUIDE"] or ["KVH"]
    private String machineName;
    private String monthFrom;
    private String monthTo;
}
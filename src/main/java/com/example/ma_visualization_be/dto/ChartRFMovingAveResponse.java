package com.example.ma_visualization_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChartRFMovingAveResponse {
    private String month;
    private Double repairFee;
}

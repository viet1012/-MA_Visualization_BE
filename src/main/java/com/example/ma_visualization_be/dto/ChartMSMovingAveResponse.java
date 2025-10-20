package com.example.ma_visualization_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChartMSMovingAveResponse {
    private String month;
    private Integer stopCase;
    private Double stopHour;
}

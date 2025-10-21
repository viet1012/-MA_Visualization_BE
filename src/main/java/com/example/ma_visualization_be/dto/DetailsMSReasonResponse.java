package com.example.ma_visualization_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailsMSReasonResponse {
    private String div;
    private String reason1;
    private String reason2;
    private int stopCase;
    private double stopHour;
}

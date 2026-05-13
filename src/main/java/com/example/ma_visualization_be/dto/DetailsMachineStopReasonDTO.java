package com.example.ma_visualization_be.dto;

import lombok.Data;

@Data
public class DetailsMachineStopReasonDTO {
    private String div;
    private String machineCode;
    private String machineType;
    private String reason1;
    private String reason2;
    private String reason3;
    private Double stopHour;
    private String linhKienVi;
}

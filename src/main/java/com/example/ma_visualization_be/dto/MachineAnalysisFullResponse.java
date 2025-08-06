package com.example.ma_visualization_be.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MachineAnalysisFullResponse {
    private String div;
    private Integer rank;
    private String macName;
    private BigDecimal repairFee;
    private Integer countMac;
    private BigDecimal aveRepairFee;
    private Integer stopCase;
    private BigDecimal stopHour;
    private BigDecimal aveStopHour;

}

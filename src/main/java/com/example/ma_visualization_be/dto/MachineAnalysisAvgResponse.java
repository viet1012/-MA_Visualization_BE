package com.example.ma_visualization_be.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MachineAnalysisAvgResponse {
    private String scale;
    private String div;
    private Integer rank;
    private String macName;
    private BigDecimal repairFee;
    private Integer stopCase;
    private BigDecimal stopHour;
    private BigDecimal aveRepairFee;
}

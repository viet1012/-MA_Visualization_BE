package com.example.ma_visualization_be.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MachineAnalysisResponse {
    private String div;
    private String rank;
    private String macName;
    private BigDecimal repairFee;
    private Integer stopCase;
    private BigDecimal stopHour;
}

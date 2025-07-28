package com.example.ma_visualization_be.dto;

import java.math.BigDecimal;

public interface IMachineTrendDTO {
    String getMonthUse();
    String getCate();
    String getMacId();
    String getMacName();
    BigDecimal getAct();
    BigDecimal getTtl();
    int getStt();
}

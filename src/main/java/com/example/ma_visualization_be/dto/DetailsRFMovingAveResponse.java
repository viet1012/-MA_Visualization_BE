package com.example.ma_visualization_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailsRFMovingAveResponse {
    private String div;       // Dept
    private String macGrp;
    private String macId;
    private String macName;
    private String cate;
    private String matnr;
    private String maktx;
    private Date useDate;
    private BigDecimal qty;
    private String unit;
    private BigDecimal act;
    private String note;
}

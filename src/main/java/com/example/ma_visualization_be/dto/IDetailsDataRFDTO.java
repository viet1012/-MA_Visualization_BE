package com.example.ma_visualization_be.dto;

public interface IDetailsDataRFDTO {
    String getDept();
    String getMatnr();
    String getMaktx();
    String getUseDate();  // hoặc java.sql.Date nếu cần kiểu ngày
    String getKostl();
    String getKonto();
    String getXblnr2();
    String getBktxt();
    Double getQty();
    String getUnit();
    Double getAct();
}

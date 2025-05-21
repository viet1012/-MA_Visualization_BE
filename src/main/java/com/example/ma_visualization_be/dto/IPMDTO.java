package com.example.ma_visualization_be.dto;

public interface IPMDTO {
    String getDate();

    String getDept();

    Double getFC_Day();  // FC_PM_Case => target theo tháng

    Double getFC_Month();// FC_PM_Case / CountDayAll * WD_Office => target theo ngày

    Integer getAct();
}

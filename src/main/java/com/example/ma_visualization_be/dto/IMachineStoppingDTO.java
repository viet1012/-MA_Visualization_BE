package com.example.ma_visualization_be.dto;

public interface IMachineStoppingDTO {

    String getDate();

    String getDept();

    Integer getCountDay();

    Integer getWdOffice();

    Double getStopHourTgt();

    Double getStopHourTgtMtd();

    Double getStopHourAct();

}

package com.example.ma_visualization_be.dto;

public interface IDetailsDataMSDTO {
    String getSendDate();        // GREATEST(...)
    String getDiv();             // CASE WHEN ...
    String getGroupName();
    String getMachineCode();
    String getMachineType();
    String getStatusCode();
    String getConfirmDate();
    String getSendTime();
    String getStartTime();
    String getEsTime();
    String getFinishTime();
    Float getStopHour();
    String getIssueStatus();
}

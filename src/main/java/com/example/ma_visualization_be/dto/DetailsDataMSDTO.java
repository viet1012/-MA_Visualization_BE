package com.example.ma_visualization_be.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({
        "sendDate", "div", "groupName", "machineCode", "machineType", "statusCode",
        "confirmDate", "sendTime", "startTime", "esTime", "finishTime", "stopHour", "issueStatus"
})
public class DetailsDataMSDTO {
    private String sendDate;
    private String div;
    private String groupName;
    private String machineCode;
    private String machineType;
    private String statusCode;
    private String confirmDate;
    private String sendTime;
    private String startTime;
    private String esTime;
    private String finishTime;
    private Float stopHour;
    private String issueStatus;
}

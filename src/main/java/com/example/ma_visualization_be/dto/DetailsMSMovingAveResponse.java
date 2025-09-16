package com.example.ma_visualization_be.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailsMSMovingAveResponse {
    private String div;
    private String groupName;
    private String machineCode;
    private String machineType;
    private String refNo;
    private String reason;
    private Timestamp confirmDate;
    private String sendTime;
    private String startTime;
    private String finishTime;
    private BigDecimal tempRun;
    private BigDecimal stopHour;
    private String issueStatus;
}

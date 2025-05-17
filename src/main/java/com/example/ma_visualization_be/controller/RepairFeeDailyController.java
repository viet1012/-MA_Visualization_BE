package com.example.ma_visualization_be.controller;

import com.example.ma_visualization_be.dto.IRepairFeeDailyDTO;
import com.example.ma_visualization_be.service.RepairFeeDailyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/repair_fee/daily")
public class RepairFeeDailyController {
    @Autowired
    private RepairFeeDailyService service;

    @GetMapping
    public ResponseEntity<List<IRepairFeeDailyDTO>> getStopHourDataDaily(
            @RequestParam String month
    ){
        List<IRepairFeeDailyDTO> data = service.getStopHourDailyData(month);
        return  ResponseEntity.ok(data);
    }
}

package com.example.ma_visualization_be.controller;

import com.example.ma_visualization_be.dto.ChartMSMovingAveResponse;
import com.example.ma_visualization_be.dto.MachineAnalysisRequest;
import com.example.ma_visualization_be.service.ChartMSMovingAveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chart/MSMovingAve")
public class ChartMSMovingAveController {

    @Autowired
    private ChartMSMovingAveService chartMSMovingAveService;

    @GetMapping
    public ResponseEntity<List<ChartMSMovingAveResponse>> getChartMSMovingAve(
            @RequestParam String monthTo,
            @RequestParam List<String> divisions,
            @RequestParam String macName,
            @RequestParam int top
    ) {
        MachineAnalysisRequest request = new MachineAnalysisRequest();
        request.setMachineName(macName);
        request.setMonthTo(monthTo);
        request.setDivisions(divisions);
        request.setTopLimit(top);

        List<ChartMSMovingAveResponse> data = chartMSMovingAveService.getChartMSMovingAve(request);
        return ResponseEntity.ok(data);
    }
}

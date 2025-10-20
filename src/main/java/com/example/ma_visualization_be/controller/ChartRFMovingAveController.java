package com.example.ma_visualization_be.controller;

import com.example.ma_visualization_be.dto.ChartRFMovingAveResponse;
import com.example.ma_visualization_be.dto.MachineAnalysisRequest;
import com.example.ma_visualization_be.service.ChartRFMovingAveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chart/RFMovingAve")
public class ChartRFMovingAveController {

    @Autowired
    private ChartRFMovingAveService chartRFMovingAveService;

    @GetMapping
    public ResponseEntity<List<ChartRFMovingAveResponse>> getChartRFMovingAve(
            @RequestParam String monthTo,
            @RequestParam List<String> divisions,
            @RequestParam String macName,
            @RequestParam int top
    ) {

        MachineAnalysisRequest request = new MachineAnalysisRequest();
        request.setMonthTo(monthTo);
        request.setDivisions(divisions);
        request.setMachineName(macName);
        request.setTopLimit(top);
        List<ChartRFMovingAveResponse> rs = chartRFMovingAveService.getChartRFMovingAve(request);
        return ResponseEntity.ok(rs);
    }
}

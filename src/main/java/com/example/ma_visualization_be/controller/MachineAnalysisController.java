package com.example.ma_visualization_be.controller;

import com.example.ma_visualization_be.dto.MachineAnalysisRequest;
import com.example.ma_visualization_be.dto.MachineAnalysisResponse;
import com.example.ma_visualization_be.service.MachineAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/machine")
@CrossOrigin(origins = "*")
public class MachineAnalysisController {

    @Autowired
    private MachineAnalysisService machineAnalysisService;

    @PostMapping("/analysis")
    public ResponseEntity<List<MachineAnalysisResponse>> getMachineAnalysis(
            @RequestBody MachineAnalysisRequest request) {

        try {
            List<MachineAnalysisResponse> result = machineAnalysisService.getMachineAnalysis(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Alternative GET method with query parameters
    @GetMapping("/analysis")
    public ResponseEntity<List<MachineAnalysisResponse>> getMachineAnalysisGet(
            @RequestParam String month,
            @RequestParam(defaultValue = "12") int monthBack,
            @RequestParam(defaultValue = "10") int topLimit,
            @RequestParam List<String> divisions) {

        try {
            MachineAnalysisRequest request = new MachineAnalysisRequest();
            request.setMonth(month);
            request.setMonthBack(monthBack);
            request.setTopLimit(topLimit);
            request.setDivisions(divisions);

            List<MachineAnalysisResponse> result = machineAnalysisService.getMachineAnalysis(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
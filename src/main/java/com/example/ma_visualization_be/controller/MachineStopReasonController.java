package com.example.ma_visualization_be.controller;


import com.example.ma_visualization_be.dto.DetailsMSReasonResponse;
import com.example.ma_visualization_be.dto.MachineAnalysisRequest;
import com.example.ma_visualization_be.dto.MachineStopReasonResponse;
import com.example.ma_visualization_be.service.MachineStopReasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/machine_stopping")
public class MachineStopReasonController {

    @Autowired
    private MachineStopReasonService service;

    @GetMapping("/reason")
    public ResponseEntity<List<MachineStopReasonResponse>> getMSReason(
            @RequestParam String month,
            @RequestParam List<String> divisions) {
        try {
            MachineAnalysisRequest request = new MachineAnalysisRequest();
            request.setMonth(month);
            request.setDivisions(divisions);

            List<MachineStopReasonResponse> result = service.getMSReason(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/reason/details")
    public ResponseEntity<List<DetailsMSReasonResponse>> getDetailsMSReason(
            @RequestParam String month,
            @RequestParam List<String> divisions) {
        try {
            MachineAnalysisRequest request = new MachineAnalysisRequest();
            request.setMonth(month);
            request.setDivisions(divisions);

            List<DetailsMSReasonResponse> result = service.getDetailsMSReason(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}


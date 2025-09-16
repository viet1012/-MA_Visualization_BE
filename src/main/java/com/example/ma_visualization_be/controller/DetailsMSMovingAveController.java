package com.example.ma_visualization_be.controller;

import com.example.ma_visualization_be.dto.DetailsMSMovingAveResponse;
import com.example.ma_visualization_be.dto.MachineAnalysisRequest;
import com.example.ma_visualization_be.service.DetailsDataMSMovingAveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/details_data/MSMovingAve")
public class DetailsMSMovingAveController {
    @Autowired
    private DetailsDataMSMovingAveService detailsMSMovingAveService;

    @GetMapping
    public ResponseEntity<List<DetailsMSMovingAveResponse>> getDailyDetailsRepairFee(@RequestParam String monthFrom,
                                                                                     @RequestParam String monthTo,
                                                                                     @RequestParam List<String> divisions,
                                                                                     @RequestParam String macName) {
        MachineAnalysisRequest request = new MachineAnalysisRequest();
        request.setMonthFrom(monthFrom);
        request.setMonthTo(monthTo);
        request.setDivisions(divisions);
        request.setMachineName(macName);
        List<DetailsMSMovingAveResponse> data = detailsMSMovingAveService.getDetailsMSMovingAve(request);
        return ResponseEntity.ok(data);
    }


}

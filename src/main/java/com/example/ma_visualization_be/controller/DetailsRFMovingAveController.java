package com.example.ma_visualization_be.controller;

import com.example.ma_visualization_be.dto.DetailsRFMovingAveResponse;
import com.example.ma_visualization_be.dto.IDetailsDataPMDTO;
import com.example.ma_visualization_be.dto.MachineAnalysisRequest;
import com.example.ma_visualization_be.service.DetailsDataPMService;
import com.example.ma_visualization_be.service.DetailsRFMovingAveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/details_data/RFMovingAve")
public class DetailsRFMovingAveController {

    @Autowired
    private DetailsRFMovingAveService detailsRFMovingAveService;


    @GetMapping
    public ResponseEntity<List<DetailsRFMovingAveResponse>> getDailyDetailsRepairFee( @RequestParam String monthFrom,
                                                                             @RequestParam String monthTo,
                                                                             @RequestParam List<String> divisions,
                                                                             @RequestParam String macName) {
        MachineAnalysisRequest request = new MachineAnalysisRequest();
        request.setMonthFrom(monthFrom);
        request.setMonthTo(monthTo);
        request.setDivisions(divisions);
        request.setMachineName(macName);
        List<DetailsRFMovingAveResponse> data = detailsRFMovingAveService.getDetailsRFMovingAve(request);
        return ResponseEntity.ok(data);
    }


}

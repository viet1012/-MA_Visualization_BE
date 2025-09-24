package com.example.ma_visualization_be.controller;

import com.example.ma_visualization_be.dto.IDetailsDataRFDTO;
import com.example.ma_visualization_be.service.DetailsDataRFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/details_data/repair_fee")
public class DetailsDataRFController {
    @Autowired
    private DetailsDataRFService service;

    @GetMapping
    public ResponseEntity<List<IDetailsDataRFDTO>> getDailyDetailsRepairFee(
            @RequestParam String month, @RequestParam String dept
    ) {
        List<IDetailsDataRFDTO> data = service.getDailyDetailsRepairFee(month, dept);
        return ResponseEntity.ok(data);
    }

}

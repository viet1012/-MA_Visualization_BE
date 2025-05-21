package com.example.ma_visualization_be.controller;

import com.example.ma_visualization_be.dto.IDetailsDataPMDTO;
import com.example.ma_visualization_be.service.DetailsDataPMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/details_data/pm")

public class DetailsDataPMController {
    @Autowired
    private DetailsDataPMService service;

    @GetMapping
    public ResponseEntity<List<IDetailsDataPMDTO>> getDailyDetailsRepairFee(@RequestParam String month) {
        List<IDetailsDataPMDTO> data = service.getDailyDetailsPM(month);
        return ResponseEntity.ok(data);
    }
}

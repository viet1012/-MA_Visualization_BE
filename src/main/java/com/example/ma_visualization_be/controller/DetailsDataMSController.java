package com.example.ma_visualization_be.controller;

import com.example.ma_visualization_be.dto.IDetailsDataMSDTO;
import com.example.ma_visualization_be.service.DetailsDataMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/details_data/machine_stopping")
public class DetailsDataMSController {
    @Autowired
    private DetailsDataMSService service;

    @GetMapping
    public ResponseEntity<List<IDetailsDataMSDTO>> getDailyDetailsMachineStopping (@RequestParam String month){
        List<IDetailsDataMSDTO> data = service.getDailyDetailsMachineStopping(month);
        return  ResponseEntity.ok(data);
    }
}

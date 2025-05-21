package com.example.ma_visualization_be.controller;

import com.example.ma_visualization_be.dto.IDetailsDataMSDTO;
import com.example.ma_visualization_be.dto.IPMDTO;
import com.example.ma_visualization_be.service.PMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pm")
public class PMController {
    @Autowired
    private PMService service;

    @GetMapping
    public ResponseEntity<List<IPMDTO>> getPMData(@RequestParam String month){
        List<IPMDTO> data = service.getPMData(month);
        return  ResponseEntity.ok(data);
    }
}

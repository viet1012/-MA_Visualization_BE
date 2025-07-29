package com.example.ma_visualization_be.controller;

import com.example.ma_visualization_be.dto.IMachineAnalysisDTO;
import com.example.ma_visualization_be.service.MachineAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/machine/analysis")
public class MachineAnalysisController {
    @Autowired
    private MachineAnalysisService service;

    @GetMapping()
    public ResponseEntity<List<IMachineAnalysisDTO>> getMachineDataAnalysis(@RequestParam String month, @RequestParam String dept){
        List<IMachineAnalysisDTO> data = service.getMachineAnalysis(month,dept);
        return ResponseEntity.ok(data);
    }
}

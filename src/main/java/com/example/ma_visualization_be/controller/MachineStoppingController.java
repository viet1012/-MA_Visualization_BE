package com.example.ma_visualization_be.controller;

import com.example.ma_visualization_be.dto.IMachineStoppingDTO;
import com.example.ma_visualization_be.service.MachineStoppingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/machine_stopping")
public class MachineStoppingController {
    @Autowired
    private MachineStoppingService service;

    @GetMapping
    public ResponseEntity<List<IMachineStoppingDTO>> getStopHourData(
            @RequestParam String month
    ){
        List<IMachineStoppingDTO> data = service.getStopHourData(month);
        return ResponseEntity.ok(data);
    }
}

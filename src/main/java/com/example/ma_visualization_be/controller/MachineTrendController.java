package com.example.ma_visualization_be.controller;

import com.example.ma_visualization_be.dto.IMachineTrendDTO;
import com.example.ma_visualization_be.service.MachineTrendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/machine_trend")
public class MachineTrendController {


    @Autowired
    private  MachineTrendService machineTrendService;


    @GetMapping
    public ResponseEntity<List<IMachineTrendDTO>> getTrend(
            @RequestParam int mn,
            @RequestParam String div,
            @RequestParam(defaultValue = "10") int top,
            @RequestParam String month // 🆕 người dùng truyền vào yyyyMM
    ) {
        return ResponseEntity.ok(machineTrendService.getMachineTrend(mn, div, top, month));
    }

}

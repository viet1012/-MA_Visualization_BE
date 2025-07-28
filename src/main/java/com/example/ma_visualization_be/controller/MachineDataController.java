package com.example.ma_visualization_be.controller;

import com.example.ma_visualization_be.dto.IMachineDataByCateDTO;
import com.example.ma_visualization_be.dto.IMachineDataByGroupDTO;
import com.example.ma_visualization_be.repository.MachineDataByCateDTO;
import com.example.ma_visualization_be.repository.MachineDataByGroupDTO;
import com.example.ma_visualization_be.service.MachineDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/machineData")
public class MachineDataController {
    @Autowired
    private MachineDataService service;

    @GetMapping("/group")
    public ResponseEntity<List<MachineDataByGroupDTO>> getMachineDataByGroup (@RequestParam String month, @RequestParam String dept){
        List<MachineDataByGroupDTO> data = service.getMachineDataByGroup(month,dept);
        return  ResponseEntity.ok(data);
    }

    @GetMapping("/cate")
    public ResponseEntity<List<MachineDataByCateDTO>> getMachineDataByCate (@RequestParam String month, @RequestParam String dept){
        List<MachineDataByCateDTO> data = service.getMachineDataByCate(month,dept);
        return  ResponseEntity.ok(data);
    }
}
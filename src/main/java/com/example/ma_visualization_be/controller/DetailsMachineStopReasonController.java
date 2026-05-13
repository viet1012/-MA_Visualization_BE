package com.example.ma_visualization_be.controller;


import com.example.ma_visualization_be.dto.DetailsMachineStopReasonDTO;
import com.example.ma_visualization_be.dto.MachineAnalysisRequest;
import com.example.ma_visualization_be.service.DetailsMachineStopReasonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/details_of/machine_stopping/reason")
@RequiredArgsConstructor
public class DetailsMachineStopReasonController {

    private final DetailsMachineStopReasonService service;

    @GetMapping("/details")
    public List<DetailsMachineStopReasonDTO> getMachineStop(
            @RequestParam String month,
            @RequestParam String div,      // ví dụ: "PRESS,MOLD"
            @RequestParam(defaultValue = "%") String reason
    ) {
        List<String> divisionList = Arrays.stream(div.split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        if (reason != null && reason.isBlank()) {
            reason = null; // convert chuỗi rỗng thành null
        }

        MachineAnalysisRequest req = new MachineAnalysisRequest();
        req.setMonth(month);
        req.setDivisions(divisionList);
        req.setReason(reason);


        return service.getDataDetails(
                req
        );
    }

    @GetMapping
    public List<DetailsMachineStopReasonDTO> getMachineStop(
            @RequestParam String month,
            @RequestParam String div   // ví dụ: "PRESS,MOLD"
    ) {
        List<String> divisionList = Arrays.stream(div.split(","))
                .map(String::trim)
                .collect(Collectors.toList());


        MachineAnalysisRequest req = new MachineAnalysisRequest();
        req.setMonth(month);
        req.setDivisions(divisionList);


        return service.getData(
                req
        );
    }
}

package com.example.ma_visualization_be.controller;

import com.example.ma_visualization_be.dto.MachineAnalysisAvgResponse;
import com.example.ma_visualization_be.dto.MachineAnalysisFullResponse;
import com.example.ma_visualization_be.dto.MachineAnalysisRequest;
import com.example.ma_visualization_be.dto.MachineAnalysisResponse;
import com.example.ma_visualization_be.service.MachineAnalysisAvgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestController
@RequestMapping("/api/machine")
public class MachineAnalysisAvgController {

    @Autowired
    private MachineAnalysisAvgService machineAnalysisAvgService;

    private static final Logger log = LoggerFactory.getLogger(MachineAnalysisController.class);

    @GetMapping("/analysis/avg")
    public ResponseEntity<List<MachineAnalysisAvgResponse>> getMachineAnalysisAvg(
            @RequestParam String month,
            @RequestParam(defaultValue = "12") int monthBack,
            @RequestParam(defaultValue = "10") int topLimit,
            @RequestParam List<String> divisions,
            @RequestParam(defaultValue = "Null") String macName

            ){
        try {
            MachineAnalysisRequest request = new MachineAnalysisRequest();
            request.setMonth(month);
            request.setMonthBack(monthBack);
            request.setTopLimit(topLimit);
            request.setDivisions(divisions);
            request.setMachineName(macName);
            log.info("macName param = '{}'", request.getMachineName());

            List<MachineAnalysisAvgResponse> result = machineAnalysisAvgService.getMachineAnalysisAvg(
                    request
            );
            return ResponseEntity.ok(result);
        }
        catch (Exception e){
            log.error("Error in getMachineAnalysisAvg", e); // 👈 log lỗi chi tiết
            return  ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/analysis/avg/full")
    public ResponseEntity<?> getMachineAnalysisAvgFull(
            @RequestParam String month,
            @RequestParam(defaultValue = "12") int monthBack,
            @RequestParam(defaultValue = "10") int topLimit,
            @RequestParam List<String> divisions
    ) {
        try {
            MachineAnalysisRequest request = new MachineAnalysisRequest();
            request.setMonth(month);
            request.setMonthBack(monthBack);
            request.setTopLimit(topLimit);
            request.setDivisions(divisions);

            List<MachineAnalysisFullResponse> result = machineAnalysisAvgService.getMachineAnalysisAvgFullResponse(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Log lỗi để debug dễ hơn
            e.printStackTrace();

            // Trả mã lỗi 500 kèm message lỗi
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred: " + e.getMessage());
        }
    }

}

package com.example.ma_visualization_be.service;

import com.example.ma_visualization_be.dto.MachineAnalysisRequest;
import com.example.ma_visualization_be.dto.MachineStopReasonResponse;
import com.example.ma_visualization_be.repository.MachineStopReasonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MachineStopReasonService {
    @Autowired
    private MachineStopReasonRepository repository;


    public List<MachineStopReasonResponse> getMachineAnalysis(MachineAnalysisRequest request) {
        // Validate input
        if (request.getMonth() == null || request.getMonth().length() != 6) {
            throw new IllegalArgumentException("Month must be in format YYYYMM");
        }

        if (request.getDivisions() == null || request.getDivisions().isEmpty()) {
            throw new IllegalArgumentException("Divisions cannot be empty");
        }

        return repository.getMachineStoppingReason(
                request.getMonth(),
                request.getDivisions()
        );
    }
}

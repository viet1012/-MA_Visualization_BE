package com.example.ma_visualization_be.service;

import com.example.ma_visualization_be.dto.MachineAnalysisRequest;
import com.example.ma_visualization_be.dto.MachineAnalysisResponse;
import com.example.ma_visualization_be.repository.IMachineAnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MachineAnalysisService {

    @Autowired
    private IMachineAnalysisRepository machineAnalysisRepository;

    public List<MachineAnalysisResponse> getMachineAnalysis(MachineAnalysisRequest request) {
        // Validate input
        if (request.getMonth() == null || request.getMonth().length() != 6) {
            throw new IllegalArgumentException("Month must be in format YYYYMM");
        }

        if (request.getDivisions() == null || request.getDivisions().isEmpty()) {
            throw new IllegalArgumentException("Divisions cannot be empty");
        }

        return machineAnalysisRepository.getMachineAnalysis(
                request.getMonth(),
                request.getMonthBack(),
                request.getTopLimit(),
                request.getDivisions()
        );
    }
}

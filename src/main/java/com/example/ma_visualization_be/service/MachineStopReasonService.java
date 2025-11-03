package com.example.ma_visualization_be.service;

import com.example.ma_visualization_be.dto.DetailsMSReasonResponse;
import com.example.ma_visualization_be.dto.MachineAnalysisRequest;
import com.example.ma_visualization_be.dto.MachineStopReasonResponse;
import com.example.ma_visualization_be.repository.DetailsMSReasonRepository;
import com.example.ma_visualization_be.repository.MachineStopReasonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MachineStopReasonService {
    @Autowired
    private MachineStopReasonRepository repository;

    @Autowired
    private DetailsMSReasonRepository detailsMSReasonRepository;

    public List<MachineStopReasonResponse> getMSReason(MachineAnalysisRequest request) {
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

    public List<DetailsMSReasonResponse> getDetailsMSReason(MachineAnalysisRequest request) {
        // Validate input
        if (request.getMonth() == null || request.getMonth().length() != 6) {
            throw new IllegalArgumentException("Month must be in format YYYYMM");
        }

        if (request.getDivisions() == null || request.getDivisions().isEmpty()) {
            throw new IllegalArgumentException("Divisions cannot be empty");
        }

        return detailsMSReasonRepository.getDetailsMSReason(
                request.getMonth(),
                request.getDivisions(),
                request.getReason()
        );
    }
}

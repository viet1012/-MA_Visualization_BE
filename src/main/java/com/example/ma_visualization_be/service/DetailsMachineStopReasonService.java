package com.example.ma_visualization_be.service;

import com.example.ma_visualization_be.dto.DetailsMachineStopReasonDTO;
import com.example.ma_visualization_be.dto.MachineAnalysisRequest;
import com.example.ma_visualization_be.repository.DetailsMachineStopReasonDetailsRepository;
import com.example.ma_visualization_be.repository.DetailsMachineStopReasonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DetailsMachineStopReasonService {

    private final DetailsMachineStopReasonDetailsRepository repo;
    private final DetailsMachineStopReasonRepository repoDetails;

    public List<DetailsMachineStopReasonDTO> getDataDetails(MachineAnalysisRequest request) {

        if (request.getMonth() == null || request.getMonth().length() != 6) {
            throw new IllegalArgumentException("Month must be in format YYYYMM");
        }

        if (request.getDivisions() == null || request.getDivisions().isEmpty()) {
            throw new IllegalArgumentException("Divisions cannot be empty");
        }

        return repo.getDetailsMSReason(
                request.getMonth(),
                request.getDivisions(),
                request.getReason()

        );


    }

    public List<DetailsMachineStopReasonDTO> getData(MachineAnalysisRequest request) {

        if (request.getMonth() == null || request.getMonth().length() != 6) {
            throw new IllegalArgumentException("Month must be in format YYYYMM");
        }

        if (request.getDivisions() == null || request.getDivisions().isEmpty()) {
            throw new IllegalArgumentException("Divisions cannot be empty");
        }

        return repoDetails.getDetailsMSReason(
                request.getMonth(),
                request.getDivisions()

        );


    }
}

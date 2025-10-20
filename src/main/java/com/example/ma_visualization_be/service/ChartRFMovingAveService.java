package com.example.ma_visualization_be.service;

import com.example.ma_visualization_be.dto.ChartRFMovingAveResponse;
import com.example.ma_visualization_be.dto.MachineAnalysisRequest;
import com.example.ma_visualization_be.repository.ChartRFMovingAveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class ChartRFMovingAveService {

    @Autowired
    private ChartRFMovingAveRepository chartRFMovingAveRepository;

    public List<ChartRFMovingAveResponse> getChartRFMovingAve(MachineAnalysisRequest request) {

        List<ChartRFMovingAveResponse> result = chartRFMovingAveRepository.getChartRFMovingAve(
                request.getMonthTo(),
                request.getDivisions(),
                request.getMachineName(),
                request.getTopLimit()
        );

        return result;
    }
}

package com.example.ma_visualization_be.service;

import com.example.ma_visualization_be.dto.ChartMSMovingAveResponse;
import com.example.ma_visualization_be.dto.MachineAnalysisRequest;
import com.example.ma_visualization_be.repository.ChartMSMovingAveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChartMSMovingAveService {
    @Autowired
    private ChartMSMovingAveRepository chartMSMovingAveRepository;


    public List<ChartMSMovingAveResponse> getChartMSMovingAve(MachineAnalysisRequest request) {

        return chartMSMovingAveRepository.getChartMSMovingAve(
                request.getMonthTo(),
                request.getDivisions(),
                request.getMachineName(),
                request.getTopLimit()
        );
    }

}

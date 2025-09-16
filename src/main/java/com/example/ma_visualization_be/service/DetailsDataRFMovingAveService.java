package com.example.ma_visualization_be.service;

import com.example.ma_visualization_be.dto.DetailsRFMovingAveResponse;
import com.example.ma_visualization_be.dto.MachineAnalysisRequest;
import com.example.ma_visualization_be.repository.DetailsRFMovingAveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetailsDataRFMovingAveService {
    @Autowired
    private DetailsRFMovingAveRepository detailsRFMovingAveRepository;

    public List<DetailsRFMovingAveResponse> getDetailsRFMovingAve(MachineAnalysisRequest request) {

        System.out.println("MonthFrom: " + request.getMonthFrom());
        System.out.println("MonthTo: " + request.getMonthTo());
        System.out.println("Divisions: " + request.getDivisions());
        System.out.println("MachineName: " + request.getMachineName());

        List<DetailsRFMovingAveResponse> result = detailsRFMovingAveRepository.getDetailsRFMovingAve(
                request.getMonthFrom(),
                request.getMonthTo(),
                request.getDivisions(),
                request.getMachineName()
        );


        return result;
    }

}

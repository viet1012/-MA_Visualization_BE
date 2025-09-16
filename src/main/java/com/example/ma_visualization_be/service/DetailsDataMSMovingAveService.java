package com.example.ma_visualization_be.service;

import com.example.ma_visualization_be.dto.DetailsMSMovingAveResponse;
import com.example.ma_visualization_be.dto.MachineAnalysisRequest;
import com.example.ma_visualization_be.repository.DetailsMSMovingAveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class DetailsDataMSMovingAveService {
    @Autowired
    private DetailsMSMovingAveRepository detailsMSMovingAveRepository;


    public List<DetailsMSMovingAveResponse> getDetailsMSMovingAve(MachineAnalysisRequest request) {

        System.out.println("MonthFrom: " + request.getMonthFrom());
        System.out.println("MonthTo: " + request.getMonthTo());
        System.out.println("Divisions: " + request.getDivisions());
        System.out.println("MachineName: " + request.getMachineName());

        List<DetailsMSMovingAveResponse> result = detailsMSMovingAveRepository.getDetailsMSMovingAve(
                request.getMonthFrom(),
                request.getMonthTo(),
                request.getDivisions(),
                request.getMachineName()
        );

        return result;
    }

}

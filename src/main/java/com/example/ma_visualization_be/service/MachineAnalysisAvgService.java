package com.example.ma_visualization_be.service;

import com.example.ma_visualization_be.dto.MachineAnalysisFullResponse;
import com.example.ma_visualization_be.dto.MachineAnalysisRequest;
import com.example.ma_visualization_be.dto.MachineAnalysisResponse;
import com.example.ma_visualization_be.repository.MachineAnalysisAvgFullRepository;
import com.example.ma_visualization_be.repository.MachineAnalysisAvgRepository;
import com.example.ma_visualization_be.repository.MachineAnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MachineAnalysisAvgService {

    @Autowired
    private MachineAnalysisAvgRepository machineAnalysisAvgRepository;

    @Autowired
    private MachineAnalysisAvgFullRepository machineAnalysisAvgFullRepository;

    public List<MachineAnalysisResponse> getMachineAnalysisAvg (MachineAnalysisRequest request)  {

        if(request.getMonth() == null || request.getMonth().length() != 6){
            throw  new IllegalArgumentException("Month must be in format YYYYMM");
        }

        if(request.getDivisions() == null || request.getDivisions().isEmpty()){
            throw new IllegalArgumentException("Divisions cannot be empty");
        }

        return machineAnalysisAvgRepository.getMachineAnalysisAve(
                request.getMonth(),
                request.getMonthBack(),
                request.getTopLimit(),
                request.getDivisions()
        );
    }

    public List<MachineAnalysisFullResponse> getMachineAnalysisAvgFullResponse (MachineAnalysisRequest request)  {

        if(request.getMonth() == null || request.getMonth().length() != 6){
            throw  new IllegalArgumentException("Month must be in format YYYYMM");
        }

        if(request.getDivisions() == null || request.getDivisions().isEmpty()){
            throw new IllegalArgumentException("Divisions cannot be empty");
        }

        return machineAnalysisAvgFullRepository.getMachineAnalysisAve(
                request.getMonth(),
                request.getMonthBack(),
                request.getTopLimit(),
                request.getDivisions()
        );
    }
}

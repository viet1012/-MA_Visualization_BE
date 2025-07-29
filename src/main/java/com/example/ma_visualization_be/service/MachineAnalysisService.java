package com.example.ma_visualization_be.service;

import com.example.ma_visualization_be.dto.IMachineAnalysisDTO;
import com.example.ma_visualization_be.repository.IMachineAnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MachineAnalysisService {
    @Autowired
    IMachineAnalysisRepository repository;

    public List<IMachineAnalysisDTO> getMachineAnalysis(String month, String dept){
        return repository.getMachineDataAnalysis(month,dept);
    }
}

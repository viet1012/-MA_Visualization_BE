package com.example.ma_visualization_be.service;

import com.example.ma_visualization_be.dto.IDetailsDataRFDTO;
import com.example.ma_visualization_be.repository.IDetailsDataRFDailyRepository;
import com.example.ma_visualization_be.repository.IDetailsDataRFRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetailsDataRFDailyService {
    @Autowired
    IDetailsDataRFDailyRepository repository;

    public List<IDetailsDataRFDTO> getDailyDetailsRepairFeeDaily(String month, String dept) {
        return repository.getDailyDetailsRepairFeeDaily(month,dept);

    }
}

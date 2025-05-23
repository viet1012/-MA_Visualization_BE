package com.example.ma_visualization_be.service;

import com.example.ma_visualization_be.dto.IDetailsDataRFDTO;
import com.example.ma_visualization_be.dto.IRepairFeeDTO;
import com.example.ma_visualization_be.repository.IDetailsDataRFRepository;
import com.example.ma_visualization_be.repository.IRepairFeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DetailsDataRFService {
    @Autowired
    IDetailsDataRFRepository repository;

    public List<IDetailsDataRFDTO> getDailyDetailsRepairFee(String month, String dept) {
        return repository.getDailyDetailsRepairFee(month,dept);

    }
}

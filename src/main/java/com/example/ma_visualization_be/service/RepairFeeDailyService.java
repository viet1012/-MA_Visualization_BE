package com.example.ma_visualization_be.service;

import com.example.ma_visualization_be.dto.IRepairFeeDailyDTO;
import com.example.ma_visualization_be.repository.IRepairFeeDailyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepairFeeDailyService {

    @Autowired
    IRepairFeeDailyRepository repository;

    public List<IRepairFeeDailyDTO> getStopHourDailyData(String month){
        return repository.getStopHourDailyData(month);
    }
}

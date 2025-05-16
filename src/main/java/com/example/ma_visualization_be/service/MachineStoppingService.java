package com.example.ma_visualization_be.service;

import com.example.ma_visualization_be.dto.IMachineStoppingDTO;
import com.example.ma_visualization_be.repository.IMachineStoppingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MachineStoppingService {
    @Autowired
    IMachineStoppingRepository repository;

    public List<IMachineStoppingDTO> getStopHourData(String month){
        return repository.getStopHourData(month);
    }
}

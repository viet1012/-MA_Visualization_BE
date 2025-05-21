package com.example.ma_visualization_be.service;

import com.example.ma_visualization_be.dto.IDetailsDataPMDTO;
import com.example.ma_visualization_be.dto.IDetailsDataRFDTO;
import com.example.ma_visualization_be.repository.IDetailsDataPMRepository;
import com.example.ma_visualization_be.repository.IDetailsDataRFRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetailsDataPMService {
    @Autowired
    IDetailsDataPMRepository repository;

    public List<IDetailsDataPMDTO> getDailyDetailsPM(String month) {
        return repository.getDailyDetailsPM(month);

    }
}

package com.example.ma_visualization_be.service;

import com.example.ma_visualization_be.dto.IPMDTO;
import com.example.ma_visualization_be.repository.IPMRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PMService {
    @Autowired
    IPMRepository repository;

    public List<IPMDTO> getPMData(String month){
        return repository.getPMData(month);
    }
}

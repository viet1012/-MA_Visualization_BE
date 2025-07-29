package com.example.ma_visualization_be.service;

import com.example.ma_visualization_be.repository.IMachineDataRepository;
import com.example.ma_visualization_be.dto.MachineDataByCateDTO;
import com.example.ma_visualization_be.dto.MachineDataByGroupDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MachineDataService {
    @Autowired
    IMachineDataRepository repository;
    public List<MachineDataByGroupDTO> getMachineDataByGroup(String month, String dept) {
        return repository.getMachineDataByGroup(month,dept).stream()
                .map(MachineDataByGroupDTO::new)
                .collect(Collectors.toList());
    }


    public List<MachineDataByCateDTO> getMachineDataByCate(String month, String dept) {
        return repository.getMachineDataByCate(month, dept).stream()
                .map(MachineDataByCateDTO::new)
                .collect(Collectors.toList());
    }



}

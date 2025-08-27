package com.example.ma_visualization_be.service;


import com.example.ma_visualization_be.repository.DemoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DEMO {

    private final DemoRepository demoRepository;

    public DEMO(DemoRepository demoRepository) {
        this.demoRepository = demoRepository;
    }

    @Transactional
    public boolean updateGioVao(String maNv, LocalDate ngayCong, String gioVao, String gioRa) {
        return demoRepository.updateGioVaoVaGioRaByMaNvAndNgayCong(maNv, ngayCong, gioVao, gioRa) > 0;
    }
}

package com.example.ma_visualization_be.repository;

import com.example.ma_visualization_be.dto.IMachineDataByCateDTO;
import com.example.ma_visualization_be.dto.IMachineDataByGroupDTO;

public class MachineDataByGroupDTO {
    private String macGrp;
    private String macId;
    private String macName;
    private Double act;

    public MachineDataByGroupDTO(IMachineDataByGroupDTO raw) {
        this.macGrp = raw.getMacGrp() != null ? raw.getMacGrp().toUpperCase() : null;
        this.macId = raw.getMacId();
        this.macName = raw.getMacName();
        this.act = raw.getAct();
    }

    // Getters
    public String getMacGrp() { return macGrp; }
    public String getMacId() { return macId; }
    public String getMacName() { return macName; }
    public Double getAct() { return act; }
}

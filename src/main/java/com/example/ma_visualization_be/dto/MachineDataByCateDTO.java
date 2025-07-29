package com.example.ma_visualization_be.dto;

public class MachineDataByCateDTO {
    private String cate;
    private String macId;
    private String macName;
    private Double act;

    public MachineDataByCateDTO(IMachineDataByCateDTO raw) {
        this.cate = raw.getCate() != null ? raw.getCate().toUpperCase() : null;
        this.macId = raw.getMacId();
        this.macName = raw.getMacName();
        this.act = raw.getAct();
    }

    // Getters
    public String getCate() { return cate; }
    public String getMacId() { return macId; }
    public String getMacName() { return macName; }
    public Double getAct() { return act; }
}

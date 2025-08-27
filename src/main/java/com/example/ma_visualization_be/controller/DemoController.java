


package com.example.ma_visualization_be.controller;

import com.example.ma_visualization_be.service.DEMO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/hr")
public class DemoController {

    private final DEMO demoService;

    public DemoController(DEMO demoService) {
        this.demoService = demoService;
    }

    @PutMapping("/update-gio-vao")
    public ResponseEntity<?> updateGioVao(
            @RequestParam String maNv,
            @RequestParam String ngayCong,  // yyyy-MM-dd
            @RequestParam String gioVao,
            @RequestParam String gioRa
    ) {
        boolean updated = demoService.updateGioVao(maNv, LocalDate.parse(ngayCong), gioVao,gioRa);
        if (updated) {
            return ResponseEntity.ok("? C?p nh?t thành công: Ma_NV=" + maNv + ", Ngay_cong=" + ngayCong + ", Gio_vao=" + gioVao);
        } else {
            return ResponseEntity.badRequest().body("? Không tìm th?y record d? c?p nh?t!");
        }
    }
}

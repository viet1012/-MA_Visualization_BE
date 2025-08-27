package com.example.ma_visualization_be.repository;

import com.example.ma_visualization_be.model.DummyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
@Repository
public interface DemoRepository extends JpaRepository<DummyEntity, Long> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE F2_HR_WT " +
            "SET Gio_vao = :gioVao, " +
            "    Gio_ra  = :gioRa " +
            "WHERE Ma_NV = :maNv AND Ngay_cong = :ngayCong", nativeQuery = true)
    int updateGioVaoVaGioRaByMaNvAndNgayCong(String maNv, LocalDate ngayCong, String gioVao, String gioRa);
}

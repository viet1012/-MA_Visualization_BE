package com.example.ma_visualization_be.repository;

import com.example.ma_visualization_be.dto.IDetailsDataPMDTO;
import com.example.ma_visualization_be.dto.IDetailsDataRFDTO;
import com.example.ma_visualization_be.model.DummyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IDetailsDataPMRepository extends JpaRepository<DummyEntity, Long> {
    @Query(value = """
            DECLARE @date DATE = :month
            SELECT  CASE
            			WHEN dt.C_DEPT LIKE '%GUIDE' THEN 'GUIDE'
            			WHEN dt.C_DEPT = 'Mold' or dt.C_DEPT Like 'KM%' THEN 'MOLD'
            			ELSE 'PRESS'
            		END as Dept,
            		dt.C_LINE,dt.EMPNO, dt.EMPNAME, dt.MACHINECODE,dt.ACTIONCODE,
            		dt.SENDTIME,		
            		dt.ESTIME, dt.STARTTIME,dt.FINISHTIME,
            		dt.ISSUESTATUS
            FROM F2_MA_MACHINE_DATA dt
            WHERE dt.ACTIONCODE IN ('AC03','AC08','AC09') --PM
            AND dt.ISSUESTATUS <> 'CANCEL'
            AND CONVERT(DATE,dt.SENDTIME,112) = @date
            """, nativeQuery = true)
    List<IDetailsDataPMDTO> getDailyDetailsPM(@Param("month") String month);

}

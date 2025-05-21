package com.example.ma_visualization_be.repository;

import com.example.ma_visualization_be.dto.IMachineStoppingDTO;
import com.example.ma_visualization_be.dto.IPMDTO;
import com.example.ma_visualization_be.model.DummyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPMRepository extends JpaRepository<DummyEntity, Long> {
    @Query(value = """
            DECLARE @month VARCHAR(6) = REPLACE(:month, '-', '');
            
            SELECT wd.[Date],fc.Dept, fc.FC_PM_Case as FC_Month, fc.FC_PM_Case/dc.CountDayAll*wd.WD_Office as FC_Day, Count(tb.Dept) as ACT
            FROM F2Database.dbo.F2_MA_FC_StopHour fc
            INNER JOIN F2Database.dbo.F2_Working_Date wd ON fc.Year_Month = FORMAT(wd.[Date],'yyyy-MM')\s
            INNER JOIN\s
            		(
            			SELECT FORMAT([Date],'yyyy-MM') as mth,SUM(WD_Office) as CountDayAll,\s
            			SUM(IIF([Date]<CAST(GETDATE() as date),1,0)*WD_Office) as CountDayMTD
            			FROM F2Database.dbo.F2_Working_Date
            			WHERE FORMAT([Date],'yyyyMM') = @month
            			GROUP BY FORMAT([Date],'yyyy-MM')
            		) as dc	ON fc.Year_Month = dc.mth
            LEFT JOIN
            	(
            	SELECT  CASE
            				WHEN dt.C_DEPT LIKE '%GUIDE' THEN 'GUIDE'
            				WHEN dt.C_DEPT = 'Mold' or dt.C_DEPT Like 'KM%' THEN 'MOLD'
            				ELSE 'PRESS'
            			END as Dept,
            			dt.C_LINE,dt.EMPNO, dt.EMPNAME, dt.MACHINECODE,dt.ACTIONCODE,
            			dt.SENDTIME,
            			dt.ESTIME, dt.STARTTIME,dt.FINISHTIME,'PM' as PM,
            			dt.ISSUESTATUS
            	FROM F2_MA_MACHINE_DATA dt
            	WHERE dt.ACTIONCODE IN ('AC03','AC08','AC09') --PM
            	AND dt.ISSUESTATUS <> 'CANCEL'
            	AND Format(dt.SENDTIME,'yyyyMM') = @month) as tb
            ON fc.Dept = tb.Dept AND wd.[Date] = CONVERT(DATE,tb.SENDTIME,112)
            GROUP BY wd.[Date],fc.Dept, fc.FC_PM_Case,dc.CountDayAll,wd.WD_Office
            """, nativeQuery = true)
    List<IPMDTO> getPMData(@Param("month") String month);
}

package com.example.ma_visualization_be.repository;

import com.example.ma_visualization_be.dto.IRepairFeeDailyDTO;
import com.example.ma_visualization_be.model.DummyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface IRepairFeeDailyRepository extends JpaRepository<DummyEntity, Long> {

    @Query(value = """
                DECLARE @month VARCHAR(6) = REPLACE(:month, '-', '');
            
                SELECT wd.[Date],Replace(fc.Dept,'MA_','') as Dept, fc.FC_USD, dc.CountDayAll, wd.WD_Office,
                      fc.FC_USD*wd.WD_Office/dc.CountDayAll as FC_Day,
                      SUM(tb.ACT) as ACT
                FROM F2Database.dbo.F2_CostFC_Tool_ByDept fc
                INNER JOIN F2Database.dbo.F2_Working_Date wd ON fc.Year_Month = FORMAT(wd.[Date],'yyyy-MM')\s
                INNER JOIN\s
                      (
                          SELECT FORMAT([Date],'yyyy-MM') as mth,SUM(WD_Office) as CountDayAll,\s
                          SUM(IIF([Date]<CAST(GETDATE() as date),1,0)*WD_Office) as CountDayMTD
                          FROM F2Database.dbo.F2_Working_Date
                          WHERE FORMAT([Date],'yyyyMM') = @month
                          GROUP BY FORMAT([Date],'yyyy-MM')
                      ) as dc
                      ON fc.Year_Month = dc.mth
                LEFT JOIN
                  (
                  SELECT\s
                      CASE
                      WHEN XBLNR2 LIKE '1566-%' THEN\s
                          CASE
                              WHEN LEFT(KOSTL,6) IN ('614100','614000') THEN 'PRESS'
                              WHEN LEFT(KOSTL,6) = '614200' THEN 'MOLD'
                              WHEN LEFT(KOSTL,6) IN ('614600', '614700') THEN 'GUIDE'
                          END
                      ELSE 'OTHER'
                  END as Dept,
                      iss.MATNR,
                      part.MAKTX,
                      CONVERT(DATE,BLDAT,112) as UseDate,
                      KOSTL,
                      KONTO,
                      XBLNR2,
                      BKTXT,
                      ERFMG as QTY,
                      ERFME as UNIT,
                      IIF(ERFMG<0,-1,1)*AMOUNT as ACT
                  FROM MANUFASPCPD.dbo.MANUFA_F_PD_DTM_ISSUE iss
                  INNER JOIN MANUFASPCPD.dbo.MANUFA_F_PD_GRB_PART part ON iss.MATNR = part.MATNR
                  WHERE KONTO = '570600'
                  AND BLDAT < CONVERT(varchar,GETDATE(),112)
                  ) as tb
                  ON fc.Dept = 'MA_' + tb.Dept AND tb.UseDate = wd.[Date]
                WHERE Format(wd.[Date],'yyyyMM') = @month	
                  AND fc.Dept Like 'MA_%'\s
                GROUP BY wd.[Date],fc.Dept,fc.FC_USD, dc.CountDayAll,wd.WD_Office
                ORDER BY wd.[Date]
            """, nativeQuery = true)
    List<IRepairFeeDailyDTO> getStopHourDailyData(@Param("month") String month);
}


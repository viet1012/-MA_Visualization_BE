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
            
            SELECT\s
                wd.[Date] AS Date,
                stp.Dept AS Dept,
                dc.CountDay AS CountDayAll,
                wd.WD_Office AS WD_Office,
                stp.FC_StopHour AS FC_USD,
                stp.FC_StopHour * wd.WD_Office / dc.CountDay AS FC_Day,
                SUM(Stop_Hour) AS Act
            FROM F2Database.dbo.F2_Working_Date wd
            INNER JOIN (
                SELECT\s
                    FORMAT([Date], 'yyyy-MM') AS mth,
                    SUM(WD_Office) AS CountDay
                FROM F2Database.dbo.F2_Working_Date
                WHERE FORMAT([Date], 'yyyyMM') = @month
                GROUP BY FORMAT([Date], 'yyyy-MM')
            ) dc ON FORMAT(wd.[Date], 'yyyy-MM') = dc.mth
            INNER JOIN F2Database.dbo.F2_MA_FC_StopHour stp\s
                ON FORMAT(wd.[Date], 'yyyy-MM') = stp.Year_Month
            LEFT JOIN (
                SELECT
                    GREATEST(CONVERT(DATE, dt.SENDTIME, 112), CONVERT(DATE, @month + '01', 112)) AS SendDate,
                    dt.ISSUESTATUS,
                    CASE
                        WHEN mst.DIVISION LIKE '%GUIDE' THEN 'GUIDE'
                        WHEN mst.DIVISION LIKE 'SUPPORT%' THEN 'PRESS'
                        ELSE mst.DIVISION
                    END AS DIV,
                    GROUPNAME,
                    MACHINECODE,
                    MACHINE_TYPE,
                    STATUSCODE,
                    CONFIRM_DATE,
                    CONVERT(VARCHAR, ISNULL(CONFIRM_DATE, SENDTIME), 120) AS SENDTIME,
                    CONVERT(VARCHAR, STARTTIME, 120) AS STARTTIME,
                    CONVERT(VARCHAR, ESTIME, 120) AS ESTIME,
                    CONVERT(VARCHAR, FINISHTIME, 120) AS FINISHTIME,
                    CASE
                        WHEN STATUSCODE = 'ST02' THEN\s
                            CAST(ROUND((DATEDIFF(MINUTE, GREATEST(ISNULL(CONFIRM_DATE, SENDTIME), CONVERT(DATE, @month + '01', 112)), COALESCE(FINISHTIME, GETDATE()))) / 60.0 * 20 / 24, 2) AS FLOAT)
                        WHEN STATUSCODE = 'ST01' THEN\s
                            CAST(ROUND((DATEDIFF(MINUTE, GREATEST(ISNULL(STARTTIME, SENDTIME), CONVERT(DATE, @month + '01', 112)), COALESCE(FINISHTIME, GETDATE()))) / 60.0 * 20 / 24, 2) AS FLOAT)
                    END AS Stop_Hour
                FROM F2Database.dbo.f2_ma_machine_data dt
                INNER JOIN F2Database.dbo.f2_ma_machine_master mst\s
                    ON dt.machinecode = mst.code
                WHERE\s
                    dt.SENDTIME >= CONVERT(DATE, @month + '01', 112)
                    AND dt.ISSUESTATUS NOT IN ('CANCEL')
            ) tb ON wd.[Date] = tb.SendDate AND stp.Dept = tb.DIV
            
            GROUP BY\s
                wd.[Date], stp.Dept, stp.FC_StopHour, wd.WD_Office, dc.CountDay
            ORDER BY\s
                wd.[Date], stp.Dept DESC;
            
            """, nativeQuery = true)
    List<IRepairFeeDailyDTO> getStopHourDailyData(@Param("month") String month);
}


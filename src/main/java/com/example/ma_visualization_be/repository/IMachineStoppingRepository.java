package com.example.ma_visualization_be.repository;

import com.example.ma_visualization_be.dto.IMachineStoppingDTO;
import com.example.ma_visualization_be.dto.IRepairFeeDTO;
import com.example.ma_visualization_be.model.DummyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IMachineStoppingRepository extends JpaRepository<DummyEntity, Long> {

    @Query(value = """
           DECLARE @month VARCHAR(6) = REPLACE(:month, '-', '');
            SELECT wd.[Date],stp.Dept, dc.CountDay,wd.WD_Office, stp.FC_StopHour as Stop_Hour_TGT, stp.FC_StopHour*wd.WD_Office/dc.CountDay as Stop_Hour_TGT_MTD, SUM(Stop_Hour) as Stop_Hour_Act
            FROM F2Database.dbo.F2_Working_Date wd\s
            INNER JOIN\s
            	(
            		SELECT FORMAT([Date],'yyyy-MM') as mth,SUM(WD_Office) as CountDay
            		FROM F2Database.dbo.F2_Working_Date
            		WHERE FORMAT([Date],'yyyyMM') = @month
            		GROUP BY FORMAT([Date],'yyyy-MM')
            	) as dc
            	ON Format(wd.[Date],'yyyy-MM') = dc.mth
            INNER JOIN F2Database.dbo.F2_MA_FC_StopHour stp ON Format(wd.[Date],'yyyy-MM') = stp.Year_Month
            LEFT JOIN
            	(
            	SELECT\s
            		GREATEST(CONVERT(DATE,dt.SENDTIME,112),CONVERT(DATE,@month + '01',112)) as SendDate,
            		dt.ISSUESTATUS,
            		CASE
            			WHEN mst.DIVISION Like '%GUIDE' THEN 'GUIDE'
            			WHEN mst.DIVISION Like 'SUPPORT%' THEN 'PRESS'
            			ELSE mst.DIVISION
            		END as DIV,
            		GROUPNAME,
            		MACHINECODE,
            		MACHINE_TYPE,
            		STATUSCODE,
            		CONFIRM_DATE,
            		convert(varchar,ISNULL(CONFIRM_DATE,SENDTIME),120) SENDTIME,
            		convert(varchar,STARTTIME,120) STARTTIME,
            		convert(varchar,ESTIME,120) ESTIME,
            		convert(varchar,FINISHTIME,120) FINISHTIME,
            		CASE
            			WHEN STATUSCODE = 'ST02'
            				THEN CAST(ROUND((DATEDIFF(MINUTE,GREATEST(ISNULL(CONFIRM_DATE,SENDTIME),CONVERT(DATE,@month + '01',112)),COALESCE(FINISHTIME,GETDATE()))) /60.0*20/24,2) AS FLOAT)
            			WHEN STATUSCODE = 'ST01'
            				THEN CAST(ROUND((DATEDIFF(MINUTE,GREATEST(ISNULL(STARTTIME,SENDTIME),CONVERT(DATE,@month + '01',112)),COALESCE(FINISHTIME,GETDATE()))) /60.0*20/24,2) AS FLOAT)
            		END Stop_Hour
            		FROM F2Database.dbo.f2_ma_machine_data dt
            	INNER JOIN F2Database.dbo.f2_ma_machine_master mst ON dt.machinecode = mst.code\s
            	WHERE (SENDTIME >= CONVERT(DATE,@month + '01',112))
            	AND ISSUESTATUS not in ('CANCEL')
            	) as tb
            	ON wd.[Date] = tb.SendDate AND stp.Dept = tb.DIV
            
            GROUP BY wd.[Date], stp.Dept, stp.FC_StopHour, wd.WD_Office,dc.CountDay
            ORDER BY wd.[Date], stp.Dept desc
            """, nativeQuery = true)
    List<IMachineStoppingDTO> getStopHourData(@Param("month") String month);
}


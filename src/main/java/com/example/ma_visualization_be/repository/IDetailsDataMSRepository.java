package com.example.ma_visualization_be.repository;

import com.example.ma_visualization_be.dto.IDetailsDataMSDTO;
import com.example.ma_visualization_be.model.DummyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDetailsDataMSRepository extends JpaRepository<DummyEntity, Long> {

    @Query(value = """
          DECLARE @month VARCHAR(6) = REPLACE(:month, '-', '');
          DECLARE @fromD DATETIME = CONVERT(DATETIME,@month + '01',112)
          DECLARE @toD DATETIME = Least(GETDATE(),DATEADD(MILLISECOND, -3, DATEADD(DAY, 1, CAST(EOMONTH(@fromD) AS DATETIME))))
          PRINT  @fromD
          PRINT  @toD
          SELECT\s
           GREATEST(CONVERT(DATE,dt.SENDTIME,112),CONVERT(DATE,@month + '01',112)) as SendDate,
           CASE
            WHEN mst.DIVISION Like '%GUIDE' THEN 'GUIDE'
            WHEN mst.DIVISION Like 'SUPPORT%' THEN 'PRESS'
            ELSE mst.DIVISION
           END as DIV,
           GROUPNAME,
           MACHINECODE,
           MACHINE_TYPE,
              STATUSCODE,
           dt.REF_NO,
           CONFIRM_DATE,
              convert(varchar,ISNULL(CONFIRM_DATE,SENDTIME),120) SENDTIME,
              convert(varchar,STARTTIME,120) STARTTIME,
              convert(varchar,ESTIME,120) ESTIME,
              convert(varchar,FINISHTIME,120) FINISHTIME,\s
              tempR.Min_Stop, tempR.Max_Stop, tempR.Temp_Run,
           CASE
            WHEN STATUSCODE = 'ST02'
             THEN CAST(ROUND((DATEDIFF(MINUTE,GREATEST(ISNULL(CONFIRM_DATE,SENDTIME),CONVERT(DATE,@month + '01',112)),COALESCE(FINISHTIME,@toD))) /60.0*20/24,2) AS FLOAT)
            WHEN STATUSCODE = 'ST01'
             THEN CAST(ROUND((DATEDIFF(MINUTE,GREATEST(ISNULL(STARTTIME,SENDTIME),CONVERT(DATE,@month + '01',112)),COALESCE(FINISHTIME,@toD))) /60.0*20/24,2) AS FLOAT)
           END - COALESCE(tempR.Temp_Run,0) As Stop_Hour,
           dt.ISSUESTATUS\s
          \s
              FROM F2Database.dbo.f2_ma_machine_data dt
          INNER JOIN F2Database.dbo.f2_ma_machine_master mst ON dt.machinecode = mst.code\s
          LEFT JOIN (
            SELECT REF_NO, Min_Stop, Max_Stop,
             CAST(ROUND(DATEDIFF(MINUTE,Min_Stop,Max_Stop) /60.0*20/24,2) AS FLOAT) as Temp_Run
            FROM (
             SELECT REF_NO, MIN(GREATEST(STOP_DATE,@fromD)) as Min_Stop,MAX(STOP_DATE) as Max_Stop
             FROM F2_MA_MACHINE_STOPTIME
             GROUP BY REF_NO
             ) as t1\s
            ) as tempR
            ON dt.REF_NO = tempR.REF_NO
          WHERE ((SENDTIME BETWEEN @fromD AND @toD) OR (ISSUESTATUS = 'WAIT' AND SENDTIME < @fromD))\s
          AND ACTIONCODE LIKE 'AC01%'
          AND ISSUESTATUS not in ('CANCEL')
          AND dt.STATUSCODE = 'ST02'

          ORDER BY SENDTIME

        """, nativeQuery = true)
    List<IDetailsDataMSDTO> getDailyDetailsMachineStopping(@Param("month") String month);
}

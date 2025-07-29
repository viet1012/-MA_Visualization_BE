package com.example.ma_visualization_be.repository;

import com.example.ma_visualization_be.dto.IMachineAnalysisDTO;
import com.example.ma_visualization_be.model.DummyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IMachineAnalysisRepository extends JpaRepository<DummyEntity,Long> {
    @Query(value = """
            DECLARE @mn INT = 12
            DECLARE @top INT = 10
            DECLARE @month VARCHAR(6) = REPLACE(:month, '-', '');

            DECLARE @monthDate DATE = CONVERT(DATE, @month + '01')
            DECLARE @fromMonth VARCHAR(6) = FORMAT(DATEADD(MONTH, -@mn, @monthDate),'yyyyMM')
            DECLARE @fromDate VARCHAR(8) = @fromMonth + '01'
            DECLARE @div NVARCHAR(10) = :div

            --for StopHour
            DECLARE @fromD DATETIME = CONVERT(DATETIME,@month + '01',112)
            DECLARE @toD DATETIME = Least(GETDATE(),DATEADD(MILLISECOND, -3, DATEADD(DAY, 1, CAST(EOMONTH(@fromD) AS DATETIME))))

            IF OBJECT_ID('tempdb..#tempTB') IS NOT NULL
                DROP TABLE #tempTB;
            CREATE TABLE #tempTB
            (
             MacName NVARCHAR(100),
             Act Decimal(18,2)
            );

            --1. FROM MA_DETAILS
            WITH MA_Details AS (
             SELECT
              COALESCE(ratio.DIV,
              CASE
               WHEN XBLNR2 LIKE '1566-%' THEN
                CASE
                 WHEN LEFT(KOSTL,6) IN ('614100','614000') THEN 'PRESS'
                 WHEN LEFT(KOSTL,6) = '614200' THEN 'MOLD' 
                 WHEN LEFT(KOSTL,6) IN ('614600', '614700') THEN 'GUIDE'
                END
               ELSE 'OTHER'
              END) as Dept,
              mac.ATTRIBUTE1 as MacGrp,
              IIF(AUFNR LIKE 'M%',RIGHT(AUFNR,LEN(AUFNR)-1),AUFNR) As MacID,
              mac.MACHINE_TYPE as MacName,
              SGTXT as Cate,
              iss.MATNR,
              part.MAKTX,
              CONVERT(DATE,BLDAT,112) as UseDate,
              KOSTL,
              KONTO,
              XBLNR2,
              BKTXT,
              ERFMG as QTY,
              ERFME as UNIT,
              IIF(ERFMG<0,-1,1)*AMOUNT*COALESCE(ratio.Ratio,1) as ACT,
              'Shared ' + Format(ratio.Ratio,'0.0%') as Note
             FROM MANUFASPCPD.dbo.MANUFA_F_PD_DTM_ISSUE iss
             INNER JOIN MANUFASPCPD.dbo.MANUFA_F_PD_GRB_PART part ON iss.MATNR = part.MATNR
             LEFT JOIN F2Database.dbo.F2_MA_MACHINE_MASTER mac ON IIF(AUFNR LIKE 'M%',RIGHT(AUFNR,LEN(AUFNR)-1),AUFNR) = mac.CODE
             LEFT JOIN F2Database.dbo.F2_Cost_Center_Share share ON iss.KOSTL = share.Cost_Center
             LEFT JOIN F2Database.dbo.F2_Cost_Center_Share_Ratio ratio ON share.Group_Share = ratio.Dept
                            AND CASE
                             WHEN XBLNR2 LIKE '1566-%' THEN
                              CASE
                               WHEN LEFT(KOSTL,6) IN ('614100','614000') THEN 'PRESS' 
                               WHEN LEFT(KOSTL,6) = '614200' THEN 'MOLD'   
                               WHEN LEFT(KOSTL,6) IN ('614600', '614700') THEN 'GUIDE'
                              END
                             ELSE 'OTHER'
                             END <> 'OTHER'
             WHERE KONTO = '570600'
             AND LEFT(BLDAT,6) >= @fromDate
             AND BLDAT < CONVERT(varchar,GETDATE(),112)
             AND COALESCE(ratio.DIV,
              CASE
               WHEN XBLNR2 LIKE '1566-%' THEN
                CASE
                 WHEN LEFT(KOSTL,6) IN ('614100','614000') THEN 'PRESS'   
                 WHEN LEFT(KOSTL,6) = '614200' THEN 'MOLD'  
                 WHEN LEFT(KOSTL,6) IN ('614600', '614700') THEN 'GUIDE'
                END
               ELSE 'OTHER'
              END) = @div
            )
            --
            INSERT INTO #tempTB
             (MacName, Act)
            (SELECT MacName, Sum(Act) as Act
            FROM MA_Details
            GROUP BY MacName)
            
            --2. FROM STOP HOUR
            IF OBJECT_ID('tempdb..#tempStopHour') IS NOT NULL
                DROP TABLE #tempStopHour;
            CREATE TABLE #tempStopHour
            (
             MacName NVARCHAR(100),
             Stop_Hour Decimal(18,2),
             Stop_Case Int
            );
            WITH StopHour AS (SELECT
            
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
                convert(varchar,FINISHTIME,120) FINISHTIME,
                tempR.Min_Stop, tempR.Max_Stop, tempR.Temp_Run,
             CASE
              WHEN STATUSCODE = 'ST02'
               THEN CAST(ROUND((DATEDIFF(MINUTE,ISNULL(CONFIRM_DATE,SENDTIME),COALESCE(FINISHTIME,@toD))) /60.0*20/24,2) AS FLOAT)
              WHEN STATUSCODE = 'ST01'
               THEN CAST(ROUND((DATEDIFF(MINUTE,ISNULL(STARTTIME,SENDTIME),COALESCE(FINISHTIME,@toD))) /60.0*20/24,2) AS FLOAT)
             END - COALESCE(tempR.Temp_Run,0) As Stop_Hour,
             dt.ISSUESTATUS
            
                FROM F2Database.dbo.f2_ma_machine_data dt
             INNER JOIN F2Database.dbo.f2_ma_machine_master mst ON dt.machinecode = mst.code
             LEFT JOIN (
               SELECT REF_NO, Min_Stop, Max_Stop,
                CAST(ROUND(DATEDIFF(MINUTE,Min_Stop,Max_Stop) /60.0*20/24,2) AS FLOAT) as Temp_Run
               FROM (
                --SELECT REF_NO, MIN(GREATEST(STOP_DATE,@fromD)) as Min_Stop,MAX(STOP_DATE) as Max_Stop
                SELECT REF_NO, MIN(STOP_DATE) as Min_Stop,MAX(STOP_DATE) as Max_Stop
                FROM F2Database.dbo.F2_MA_MACHINE_STOPTIME
                GROUP BY REF_NO
                ) as t1
               ) as tempR
               ON dt.REF_NO = tempR.REF_NO
             WHERE CONFIRM_DATE >= @fromDate
             AND ACTIONCODE LIKE 'AC01%'
             AND ISSUESTATUS not in ('CANCEL')
             AND dt.STATUSCODE = 'ST02'
             )
            
            
            INSERT INTO #tempStopHour
             (MacName, Stop_Hour, Stop_Case)
            (SELECT MACHINE_TYPE, Sum(Stop_Hour) as Act, Count(MACHINECODE) as StopCase
            FROM StopHour
            GROUP BY MACHINE_TYPE)
            
            --FINAL
            SELECT topUse.STT as [Rank], #tempTB.MacName, #tempTB.Act as RepairFee,
             #tempStopHour.Stop_Case, #tempStopHour.Stop_Hour
            FROM #tempTB
            INNER JOIN
             (
             SELECT TOP (@top) MacName,Sum(Act) as TTL,
              ROW_NUMBER() OVER (ORDER BY SUM(Act) DESC) as STT
             FROM #tempTB
             GROUP BY MacName
             HAVING MacName is not null
             ORDER BY STT
             ) as topUse
             ON #tempTB.MacName = topUse.MacName
            LEFT JOIN #tempStopHour ON #tempTB.MacName = #tempStopHour.MacName
            ORDER BY [Rank]
            
     """, nativeQuery = true)
    List<IMachineAnalysisDTO> getMachineDataAnalysis(@Param("month") String month, @Param("div") String div);

}

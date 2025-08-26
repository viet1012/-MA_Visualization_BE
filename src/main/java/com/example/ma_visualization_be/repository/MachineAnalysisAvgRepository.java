package com.example.ma_visualization_be.repository;

import org.springframework.stereotype.Repository;
import com.example.ma_visualization_be.dto.MachineAnalysisAvgResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MachineAnalysisAvgRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<MachineAnalysisAvgResponse> getMachineAnalysisAve(String month, int monthBack,
                                                               int topLimit, List<String> divisions,
                                                               String machineName) {
        String divisionValues = divisions.stream()
                .map(div -> "'" + div + "'")
                .collect(Collectors.joining(", "));

        String sql = buildDynamicQuery(divisionValues);

        Object[] params = new Object[4];
        params[0] = monthBack;
        params[1] = topLimit;
        params[2] = month;
        // for @macName parameter
        params[3] = (machineName == null || machineName.isEmpty()) ? null : machineName;

        return jdbcTemplate.query(sql, params, new MachineAnalysisRowMapper());
    }

    private String buildDynamicQuery(String divisionValues) {
        return """
            DECLARE @mn INT = ?
            DECLARE @top INT = ?
            DECLARE @month VARCHAR(6) = ?
            DECLARE @monthDate DATE = CONVERT(DATE, @month + '01')
            DECLARE @fromMonth VARCHAR(6) = FORMAT(DATEADD(MONTH, -@mn+1, @monthDate),'yyyyMM')
            DECLARE @EOMonth DATE = EOMONTH(@month + '01')
            DECLARE @fromDate VARCHAR(8) = @fromMonth + '01'
            DECLARE @toDate VARCHAR(8) = FORMAT(CASE
                     WHEN GETDATE() < @EOMonth THEN GETDATE()
                     ELSE @EOMonth
                    END, 'yyyyMMdd')
            DECLARE @macName NVARCHAR(100) = ?
            DECLARE @div TABLE (Value NVARCHAR(50))
            INSERT INTO @div (Value)
            VALUES """ + divisionValues.replaceAll("'([^']+)'", "('$1')") + """
       
            --for StopHour
           DECLARE @fromD DATETIME = CONVERT(DATETIME,@month + '01',112)
           DECLARE @toD DATETIME = Least(GETDATE(),DATEADD(MILLISECOND, -3, DATEADD(DAY, 1, CAST(EOMONTH(@fromD) AS DATETIME))))
        
           IF OBJECT_ID('tempdb..#Rpfee_ORG') IS NOT NULL DROP TABLE #Rpfee_ORG;
           IF OBJECT_ID('tempdb..#Rpfee_Ave') IS NOT NULL DROP TABLE #Rpfee_Ave;
           IF OBJECT_ID('tempdb..#Rpfee_MovAve') IS NOT NULL DROP TABLE #Rpfee_MovAve;
           IF OBJECT_ID('tempdb..#StopHour_ORG') IS NOT NULL DROP TABLE #StopHour_ORG;
           IF OBJECT_ID('tempdb..#StopHour_Ave') IS NOT NULL DROP TABLE #StopHour_Ave;
           IF OBJECT_ID('tempdb..#StopHour_MovAve') IS NOT NULL DROP TABLE #StopHour_MovAve;
           IF OBJECT_ID('tempdb..#MonthMachine') IS NOT NULL DROP TABLE #MonthMachine;
        
           --1. FROM MA_DETAILS
           SELECT
            COALESCE(ratio.DIV,
            CASE
             WHEN XBLNR2 LIKE '1566-%' THEN
              CASE
               WHEN LEFT(KOSTL,6) IN ('614100') THEN 'PRESS' -- B? COMMON '614000', không s? d?ng Share
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
           INTO #Rpfee_ORG
           FROM MANUFASPCPD.dbo.MANUFA_F_PD_DTM_ISSUE iss
           INNER JOIN MANUFASPCPD.dbo.MANUFA_F_PD_GRB_PART part ON iss.MATNR = part.MATNR
           LEFT JOIN F2Database.dbo.F2_MA_MACHINE_MASTER mac ON IIF(AUFNR LIKE 'M%',RIGHT(AUFNR,LEN(AUFNR)-1),AUFNR) = mac.CODE
           LEFT JOIN F2Database.dbo.F2_Cost_Center_Share share ON iss.KOSTL = share.Cost_Center
           LEFT JOIN F2Database.dbo.F2_Cost_Center_Share_Ratio ratio ON share.Group_Share = ratio.Dept
                          AND CASE
                           WHEN XBLNR2 LIKE '1566-%' THEN\s
                            CASE
                             WHEN LEFT(KOSTL,6) IN ('614100') THEN 'PRESS'   \s
                             WHEN LEFT(KOSTL,6) = '614200' THEN 'MOLD'   \s
                             WHEN LEFT(KOSTL,6) IN ('614600', '614700') THEN 'GUIDE'\s
                            END
                           ELSE 'OTHER'
                           END <> 'OTHER'
           WHERE KONTO = '570600'
           AND (BLDAT BETWEEN @fromDate AND @toDate)
           AND ((ratio.Dept <> 'F2' AND ratio.Dept <> 'HT2') or ratio.Dept is Null)
        
           SELECT Dept as Div, MacID, MacName, Sum(Act) as Act, Count(MacID) as CountMac
           INTO #Rpfee_Ave
           FROM #Rpfee_ORG
           GROUP BY Dept, MacID, MacName
        
           --2. FROM STOP HOUR
        
           --MOVING AVERAGE ***
        
        
           IF OBJECT_ID('tempdb..#MonthTB') IS NOT NULL
               DROP TABLE #MonthTB;
           SELECT\s
               FORMAT(DATEADD(MONTH, -n, GETDATE()), 'yyyy-MM') AS MonthCur,
               FORMAT(DATEADD(MONTH, -n-2, GETDATE()), 'yyyy-MM') AS MonthLas3M,
            DATEADD(MONTH, DATEDIFF(MONTH, 0, DATEADD(MONTH, -n-2, GETDATE())), 0) AS StartDT,
               DATEADD(SECOND, -1, DATEADD(MONTH, 1, DATEADD(MONTH, DATEDIFF(MONTH, 0, DATEADD(MONTH, -n, GETDATE())), 0))) AS EndDT,
            'MovingAve-' + CAST(n+1 as nvarchar(2)) as MonthDescribe
           INTO #MonthTB
           FROM (VALUES (0),(1),(2),(3),(4)) AS v(n);
        
           WITH StopHour AS (SELECT \s
            CASE
             WHEN mst.DIVISION Like '%GUIDE' THEN 'GUIDE'
             WHEN mst.DIVISION Like 'SUPPORT%' THEN\s
              CASE
               WHEN mst.FACTORY = 'B' THEN 'GUIDE'
               WHEN mst.FACTORY = 'C' THEN 'MOLD'
               ELSE 'PRESS'
              END
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
              THEN CAST(ROUND((DATEDIFF(MINUTE,ISNULL(CONFIRM_DATE,SENDTIME),COALESCE(FINISHTIME,@toD))) /60.0*20/24,2) AS FLOAT)
             WHEN STATUSCODE = 'ST01'
              THEN CAST(ROUND((DATEDIFF(MINUTE,ISNULL(STARTTIME,SENDTIME),COALESCE(FINISHTIME,@toD))) /60.0*20/24,2) AS FLOAT)
            END - COALESCE(tempR.Temp_Run,0) As Stop_Hour,
            dt.ISSUESTATUS\s
           \s
               FROM F2Database.dbo.f2_ma_machine_data dt
            INNER JOIN F2Database.dbo.f2_ma_machine_master mst ON dt.machinecode = mst.code\s
            LEFT JOIN (
              SELECT REF_NO, Min_Stop, Max_Stop,
               CAST(ROUND(DATEDIFF(MINUTE,Min_Stop,Max_Stop) /60.0*20/24,2) AS FLOAT) as Temp_Run
              FROM (
               --SELECT REF_NO, MIN(GREATEST(STOP_DATE,@fromD)) as Min_Stop,MAX(STOP_DATE) as Max_Stop
               SELECT REF_NO, MIN(STOP_DATE) as Min_Stop,MAX(STOP_DATE) as Max_Stop
               FROM F2Database.dbo.F2_MA_MACHINE_STOPTIME
               GROUP BY REF_NO
               ) as t1\s
              ) as tempR
              ON dt.REF_NO = tempR.REF_NO
            WHERE ACTIONCODE LIKE 'AC01%'
            AND ISSUESTATUS not in ('CANCEL')
            AND dt.STATUSCODE = 'ST02'
           )
           SELECT *
           INTO #StopHour_ORG
           FROM StopHour;
        
           SELECT DIV, MACHINECODE as MacID, MACHINE_TYPE as MacName, Sum(Stop_Hour) as Stop_Hour, Count(MACHINECODE) as Stop_Case
           INTO #StopHour_Ave
           FROM #StopHour_ORG
           WHERE ((SENDTIME BETWEEN @fromDate AND @toDate) OR (ISSUESTATUS = 'WAIT' AND SENDTIME < @fromD))
           GROUP BY DIV, MACHINECODE, MACHINE_TYPE;
        
           SELECT DIV, MACHINECODE as MacID, MACHINE_TYPE as MacName, Sum(Stop_Hour) as Stop_Hour, Count(MACHINECODE) as Stop_Case
           INTO #StopHour_MovAve
           FROM #StopHour_ORG
           GROUP BY DIV, MACHINECODE, MACHINE_TYPE;
        
           SELECT Machines.Div, Machines.MacName, m.MonthDescribe, m.StartDT, m.EndDT\s
           INTO #MonthMachine
           FROM #MonthTB m
           CROSS JOIN (
            SELECT DISTINCT Div, MacName
            FROM #Rpfee_Ave
            )
            as Machines
        
           --FINAL
           --KVH
           IF EXISTS (
               SELECT 1
               FROM @div
               HAVING COUNT(*) = 1 AND MAX(Value) = 'KVH'
           )
           BEGIN
            WITH topUse AS (
             SELECT
              MacName,
              SUM(Act) as Act,
              SUM(CountMac) as CountMac,
              SUM(Act)/SUM(CountMac) AS Ave_RepairFee,
              ROW_NUMBER() OVER (ORDER BY SUM(Act)/SUM(CountMac) DESC) AS STT
             FROM #Rpfee_Ave
             WHERE MacID IS NOT NULL
             GROUP BY MacName
            ),
            stpHour AS (
             SELECT MacName, SUM(Stop_Hour) as Stop_Hour, SUM(Stop_Case) as Stop_Case
             FROM #StopHour_Ave
             GROUP BY MacName)
        
            SELECT 'AVE' as Scale, 'KVH' as Div, topUse.STT as [Rank], topUse.MacName,\s
             topUse.Act as RepairFee, topUse.CountMac,
             topUse.Ave_RepairFee,
             stpHour.Stop_Case, stpHour.Stop_Hour
            FROM topUse
            LEFT JOIN stpHour ON topUse.MacName = stpHour.MacName
            WHERE topUse.STT <= @top
            --ORDER BY [Rank]
      
           END
        
           --<>KVH
           ELSE
           BEGIN
            WITH topUse AS (
             SELECT
              Div,
              MacName,
              SUM(Act) as Act,
              SUM(CountMac) as CountMac,
              SUM(Act)/SUM(CountMac) AS Ave_RepairFee,
              ROW_NUMBER() OVER (PARTITION BY Div ORDER BY SUM(Act)/SUM(CountMac) DESC) AS STT
             FROM #Rpfee_Ave
             WHERE MacID IS NOT NULL
             GROUP BY Div, MacName
            ),
            stpHour AS (
             SELECT Div, MacName, SUM(Stop_Hour) as StopHour, SUM(Stop_Case) as StopCase
             FROM #StopHour_Ave
             GROUP BY Div, MacName)
        
            SELECT 'AVE' as Scale, topUse.DIV, topUse.STT as [Rank], topUse.MacName,\s
             topUse.Act as RepairFee, stpHour.StopHour,
             topUse.CountMac,
             topUse.Ave_RepairFee,
             stpHour.StopCase,
             stpHour.StopHour/stpHour.StopCase as Ave_StopHour
            FROM topUse
            LEFT JOIN stpHour ON topUse.MacName = stpHour.MacName AND topUse.Div = stpHour.Div
            WHERE topUse.STT <= @top
            AND topUse.Div COLLATE SQL_Latin1_General_CP1_CI_AS
             IN (SELECT value FROM @div)
            --ORDER BY Div,[Rank]

            UNION ALL
        
            SELECT mm.MonthDescribe as Scale, mm.Div, Null, mm.MacName,
             rpf.ACT, stp.StopHour,
             rpf.CountRP, --CountMac
             rpf.AveRP, --repair fee ave
             stp.StopCase,--StopCase
             stp.AveStp --Ave_StopHour

            FROM #MonthMachine mm
            INNER JOIN (
             SELECT mm1.Div, mm1.MacName,MonthDescribe
              ,COUNT(Act) As CountRP
              ,SUM(COALESCE(rpf1.Act,0)) as ACT
              ,SUM(COALESCE(rpf1.Act,0))/COUNT(COALESCE(Act,0)) as AveRP
             FROM #MonthMachine mm1
             LEFT JOIN #Rpfee_ORG rpf1 ON rpf1.UseDate BETWEEN mm1.StartDT AND mm1.EndDT
                    AND mm1.Div = rpf1.Dept
                    AND mm1.MacName = rpf1.MacName
             GROUP BY  mm1.Div, mm1.MacName, MonthDescribe
             --ORDER BY  mm1.Div, mm1.MacName, MonthDescribe
             ) as rpf ON mm.Div = rpf.Div
               AND mm.MacName = rpf.MacName
               AND mm.MonthDescribe = rpf.MonthDescribe
            LEFT JOIN (
             SELECT mm1.Div, mm1.MacName,MonthDescribe
              ,COUNT(stp.Stop_Hour) As StopCase
              ,SUM(COALESCE(stp.Stop_Hour,0)) as StopHour
              ,SUM(COALESCE(stp.Stop_Hour,0))/COUNT(COALESCE(stp.Stop_Hour,0)) as AveStp
             FROM #MonthMachine mm1
             LEFT JOIN #StopHour_ORG stp ON stp.SENDTIME BETWEEN mm1.StartDT AND mm1.EndDT
                    AND mm1.Div = stp.DIV
                    AND mm1.MacName = stp.MACHINE_TYPE
             GROUP BY  mm1.Div, mm1.MacName, MonthDescribe
             --ORDER BY  mm1.Div, mm1.MacName, MonthDescribe
             ) as stp ON mm.Div = stp.Div
               AND mm.MacName = stp.MacName
               AND mm.MonthDescribe = stp.MonthDescribe
            WHERE rpf.MacName = @macName
            AND mm.Div IN (SELECT value FROM @div)
            ORDER BY Div, [Rank]
           END
          
            """;
    }

    private static class MachineAnalysisRowMapper implements RowMapper<MachineAnalysisAvgResponse> {
        @Override
        public MachineAnalysisAvgResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            MachineAnalysisAvgResponse response = new MachineAnalysisAvgResponse();
            response.setScale(rs.getString("Scale"));
            response.setDiv(rs.getString("Div"));

            // Handle nullable Rank column
            Object rankObj = rs.getObject("Rank");
            response.setRank(rankObj != null ? rs.getInt("Rank") : null);

            response.setMacName(rs.getString("MacName"));
            response.setRepairFee(rs.getBigDecimal("RepairFee"));
            response.setAveRepairFee(rs.getBigDecimal("Ave_RepairFee"));

            // Handle different column names for stop data
            try {
                response.setStopCase(rs.getObject("Stop_Case", Integer.class));
            } catch (SQLException e) {
                response.setStopCase(rs.getObject("StopCase", Integer.class));
            }

            try {
                response.setStopHour(rs.getBigDecimal("Stop_Hour"));
            } catch (SQLException e1) {
                try {
                    response.setStopHour(rs.getBigDecimal("StopHour"));
                } catch (SQLException e2) {
                    response.setStopHour(rs.getBigDecimal("Ave_StopHour"));
                }
            }

            return response;
        }
    }
}
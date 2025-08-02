package com.example.ma_visualization_be.repository;

import org.springframework.stereotype.Repository;
import com.example.ma_visualization_be.dto.MachineAnalysisResponse;
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

    public List<MachineAnalysisResponse> getMachineAnalysisAve(String month, int monthBack, int topLimit, List<String> divisions){

        String divisionValues = divisions.stream()
                .map(div -> "(?)")
                .collect(Collectors.joining(", "));

        String sql = buildDyamicQuery(divisionValues);
        Object[] params = new Object[3 + divisions.size()];
        params[0] = monthBack;
        params[1] = topLimit;
        params[2] = month;

        for(int i = 0; i < divisions.size() ; i++){
            params[3+i] = divisions.get(i);
        }
        return jdbcTemplate.query(sql, params, new MachineAnalysisRowMapper());
    }

    public String buildDyamicQuery(String divisionValues){
        return """
                DECLARE @mn INT = ?
                DECLARE @top INT = ?
                DECLARE  @month VARCHAR(6)  = ?
                DECLARE @monthDate DATE = CONVERT(DATE, @month + '01')
                DECLARE @fromMonth VARCHAR(6) = FORMAT(DATEADD(MONTH, -@mn, @monthDate),'yyyyMM')
                DECLARE @fromDate VARCHAR(8) = @fromMonth + '01'
                DECLARE @div TABLE (Value NVARCHAR(50))
                INSERT INTO @div (Value)
                VALUES """+ divisionValues + """
                --VALUES ('KVH')
                
                --for StopHour
                DECLARE @fromD DATETIME = CONVERT(DATETIME,@month + '01',112)
                DECLARE @toD DATETIME = Least(GETDATE(),DATEADD(MILLISECOND, -3, DATEADD(DAY, 1, CAST(EOMONTH(@fromD) AS DATETIME))))
                
                IF OBJECT_ID('tempdb..#tempTB') IS NOT NULL
                    DROP TABLE #tempTB;
                CREATE TABLE #tempTB
                (
                 Div NVARCHAR(10),
                 MacID NVARCHAR(50),
                 MacName NVARCHAR(100),
                 Act Decimal(18,2),
                 CountMac Int
                );
                
                --1. FROM MA_DETAILS
                WITH MA_Details AS (
                 SELECT\s
                  COALESCE(ratio.DIV,
                  CASE
                   WHEN XBLNR2 LIKE '1566-%' THEN\s
                    CASE
                     WHEN LEFT(KOSTL,6) IN ('614100','614000') THEN 'PRESS'   \s
                     WHEN LEFT(KOSTL,6) = '614200' THEN 'MOLD'   \s
                     WHEN LEFT(KOSTL,6) IN ('614600', '614700') THEN 'GUIDE'\s
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
                                 WHEN XBLNR2 LIKE '1566-%' THEN\s
                                  CASE
                                   WHEN LEFT(KOSTL,6) IN ('614100','614000') THEN 'PRESS'   \s
                                   WHEN LEFT(KOSTL,6) = '614200' THEN 'MOLD'   \s
                                   WHEN LEFT(KOSTL,6) IN ('614600', '614700') THEN 'GUIDE'\s
                                  END
                                 ELSE 'OTHER'
                                 END <> 'OTHER'\s
                 WHERE KONTO = '570600'
                 AND LEFT(BLDAT,6) >= @fromDate
                 AND BLDAT < CONVERT(varchar,GETDATE(),112)
                )
                --
                INSERT INTO #tempTB\s
                 (Div, MacID, MacName, Act, CountMac)
                (SELECT Dept, MacID, MacName, Sum(Act) as Act, Count(MacID) as CountMac
                FROM MA_Details
                GROUP BY Dept, MacID, MacName)
                
                
                --2. FROM STOP HOUR
                IF OBJECT_ID('tempdb..#tempStopHour') IS NOT NULL
                    DROP TABLE #tempStopHour;
                CREATE TABLE #tempStopHour
                (
                 Div NVARCHAR(50),
                 MacID NVARCHAR(50),
                 MacName NVARCHAR(100),
                 Stop_Hour Decimal(18,2),
                 Stop_Case Int
                );
                WITH StopHour AS (SELECT \s
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
                 WHERE CONFIRM_DATE >= @fromDate
                 AND ACTIONCODE LIKE 'AC01%'
                 AND ISSUESTATUS not in ('CANCEL')
                 AND dt.STATUSCODE = 'ST02'
                )
                
                INSERT INTO #tempStopHour\s
                 (Div, MacID, MacName, Stop_Hour, Stop_Case)
                (SELECT DIV, MACHINECODE, MACHINE_TYPE, Sum(Stop_Hour) as Act, Count(MACHINECODE) as StopCase
                FROM StopHour
                GROUP BY DIV, MACHINECODE, MACHINE_TYPE);
                
                --FINAL
                --KVH
                IF EXISTS (
                    SELECT 1\s
                    FROM @div\s
                    HAVING COUNT(*) = 1 AND MAX(Value) = 'KVH'
                )
                BEGIN
                 WITH topUse AS (
                  SELECT\s
                   MacName,   \s
                   SUM(Act) as Act,
                   SUM(CountMac) as CountMac,
                   SUM(Act)/SUM(CountMac) AS Ave_RepairFee,
                   ROW_NUMBER() OVER (ORDER BY SUM(Act)/SUM(CountMac) DESC) AS STT
                  FROM #tempTB
                  WHERE MacID IS NOT NULL
                  GROUP BY MacName
                 ),
                 stpHour AS (
                  SELECT MacName, SUM(Stop_Hour) as Stop_Hour, SUM(Stop_Case) as Stop_Case
                  FROM #tempStopHour
                  GROUP BY MacName)
                
                 SELECT 'KVH' AS Div, topUse.STT as [Rank], topUse.MacName,\s
                  topUse.Act as RepairFee, topUse.CountMac, \s
                  topUse.Ave_RepairFee,
                  stpHour.Stop_Case, stpHour.Stop_Hour
                 FROM topUse
                 LEFT JOIN stpHour ON topUse.MacName = stpHour.MacName
                 WHERE topUse.STT <= @top
                 ORDER BY [Rank]
                END
                
                --<>KVH
                ELSE
                BEGIN
                 WITH topUse AS (
                  SELECT\s
                   Div,
                   MacName,   \s
                   SUM(Act) as Act,
                   SUM(CountMac) as CountMac,
                   SUM(Act)/SUM(CountMac) AS Ave_RepairFee,
                   ROW_NUMBER() OVER (PARTITION BY Div ORDER BY SUM(Act)/SUM(CountMac) DESC) AS STT
                  FROM #tempTB
                  WHERE MacID IS NOT NULL
                  GROUP BY Div, MacName
                 ),
                 stpHour AS (
                  SELECT Div, MacName, SUM(Stop_Hour) as Stop_Hour, SUM(Stop_Case) as Stop_Case
                  FROM #tempStopHour
                  GROUP BY Div, MacName)
                
                 SELECT topUse.DIV, topUse.STT as [Rank], topUse.MacName,\s
                  topUse.Act as RepairFee, topUse.CountMac, \s
                  topUse.Ave_RepairFee,
                  stpHour.Stop_Case, stpHour.Stop_Hour
                 FROM topUse
                 LEFT JOIN stpHour ON topUse.MacName = stpHour.MacName AND topUse.Div = stpHour.Div
                 WHERE topUse.STT <= @top
                 AND topUse.Div COLLATE SQL_Latin1_General_CP1_CI_AS
                  IN (SELECT value FROM @div)
                 ORDER BY Div,[Rank]
                END
                """;
    }
    private static class MachineAnalysisRowMapper implements RowMapper<MachineAnalysisResponse> {
        @Override
        public MachineAnalysisResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            MachineAnalysisResponse response = new MachineAnalysisResponse();
            response.setDiv(rs.getString("Div"));
            response.setRank(rs.getInt("Rank"));
            response.setMacName(rs.getString("MacName"));
            response.setRepairFee(rs.getBigDecimal("Ave_RepairFee"));
            response.setStopCase(rs.getObject("Stop_Case", Integer.class));
            response.setStopHour(rs.getBigDecimal("Stop_Hour"));
            return response;
        }
    }
}

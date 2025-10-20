package com.example.ma_visualization_be.repository;

import com.example.ma_visualization_be.dto.ChartMSMovingAveResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Repository
public class ChartMSMovingAveRepository {

    private static final Logger logger = LoggerFactory.getLogger(ChartMSMovingAveRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<ChartMSMovingAveResponse> getChartMSMovingAve(
            String monthTo,
            List<String> divisions,
            String machineName,
            int top
    ) {

        if (divisions == null || divisions.isEmpty()) {
            logger.warn("getChartMSMovingAve called with empty divisions -> returning empty list");
            return Collections.emptyList();
        }

        // chỉ lấy division đầu tiên (vì query đang dùng 1 biến @div)
        String div = divisions.get(0).replace("'", "''");

        String sql = buildDynamicQuery(machineName, div, top, monthTo);

        logger.debug("=== SQL Query ===\n{}", sql);

        return jdbcTemplate.query(sql, new ChartMSMovingAveRowMapper());
    }

    private String buildDynamicQuery(String macName, String div, int top, String monthTo) {

        return String.format("""
                DECLARE @macName NVARCHAR(100) = N'%s'
                DECLARE @div NVARCHAR(50) = N'%s'
                DECLARE @top INT = %d
                DECLARE @monthTo VARCHAR(6)  = '%s'
                DECLARE @monthToDate DATE = EOMONTH(CONVERT(DATE, @monthTo + '01'))
                DECLARE @monthFrom VARCHAR(6)  = FORMAT(DATEADD(MONTH, -@top + 1, @monthToDate),'yyyyMM')
                DECLARE @monthFromDate DATE = CONVERT(DATE, @monthFrom + '01')
                DECLARE @toD DATETIME = DATEADD(ms, -3, DATEADD(DAY, 1, CAST(@monthToDate AS DATETIME)))
                
                IF OBJECT_ID('tempdb..#STP') IS NOT NULL DROP TABLE #STP;
                
                SELECT
                    CASE
                        WHEN mst.DIVISION LIKE '%%GUIDE' THEN 'GUIDE'
                        WHEN mst.DIVISION LIKE 'SUPPORT%%' THEN
                            CASE
                                WHEN mst.FACTORY = 'B' THEN 'GUIDE'
                                WHEN mst.FACTORY = 'C' THEN 'MOLD'
                                ELSE 'PRESS'
                            END
                        ELSE mst.DIVISION
                    END AS DIV,
                    GROUPNAME,
                    dt.MACHINECODE,
                    MACHINE_TYPE,
                    dt.REF_NO,
                    cnt.C_LINHKIEN_VI AS Reason,
                    CONFIRM_DATE,
                    CONVERT(VARCHAR, ISNULL(CONFIRM_DATE, SENDTIME), 120) AS SENDTIME,
                    CONVERT(VARCHAR, STARTTIME, 120) AS STARTTIME,
                    CONVERT(VARCHAR, FINISHTIME, 120) AS FINISHTIME,
                    tempR.Temp_Run,
                    CASE
                        WHEN STATUSCODE = 'ST02'
                            THEN CAST(ROUND((DATEDIFF(MINUTE, ISNULL(CONFIRM_DATE, SENDTIME), COALESCE(FINISHTIME, @monthToDate))) / 60.0 * 20 / 24, 2) AS FLOAT)
                        WHEN STATUSCODE = 'ST01'
                            THEN CAST(ROUND((DATEDIFF(MINUTE, ISNULL(STARTTIME, SENDTIME), COALESCE(FINISHTIME, @monthToDate))) / 60.0 * 20 / 24, 2) AS FLOAT)
                    END - COALESCE(tempR.Temp_Run, 0) AS Stop_Hour,
                    dt.ISSUESTATUS
                INTO #STP
                FROM F2Database.dbo.f2_ma_machine_data dt
                INNER JOIN F2Database.dbo.f2_ma_machine_master mst ON dt.machinecode = mst.code
                LEFT JOIN (
                    SELECT REF_NO, Min_Stop, Max_Stop,
                        CAST(ROUND(DATEDIFF(MINUTE, Min_Stop, Max_Stop) / 60.0 * 20 / 24, 2) AS FLOAT) AS Temp_Run
                    FROM (
                        SELECT REF_NO, MIN(STOP_DATE) AS Min_Stop, MAX(STOP_DATE) AS Max_Stop
                        FROM F2Database.dbo.F2_MA_MACHINE_STOPTIME
                        GROUP BY REF_NO
                    ) AS t1
                ) AS tempR
                ON dt.REF_NO = tempR.REF_NO
                LEFT JOIN F2Database.dbo.F2_MA_MACHINE_CONTENT cnt ON dt.REF_NO = cnt.REF_NO
                WHERE ACTIONCODE LIKE 'AC01%%'
                    AND ISSUESTATUS NOT IN ('CANCEL')
                    AND dt.STATUSCODE = 'ST02'
                    AND ((SENDTIME BETWEEN @monthFromDate AND @toD)
                        OR (ISSUESTATUS = 'WAIT' AND SENDTIME < @monthFromDate))
                    AND mst.MACHINE_TYPE = @macName
                    AND CASE
                            WHEN mst.DIVISION LIKE '%%GUIDE' THEN 'GUIDE'
                            WHEN mst.DIVISION LIKE 'SUPPORT%%' THEN
                                CASE
                                    WHEN mst.FACTORY = 'B' THEN 'GUIDE'
                                    WHEN mst.FACTORY = 'C' THEN 'MOLD'
                                    ELSE 'PRESS'
                                END
                            ELSE mst.DIVISION
                        END = @div
                ORDER BY Stop_Hour DESC;
                
                SELECT 
                    FORMAT(CONVERT(DATE, SENDTIME), 'yyyyMM') AS [Month],
                    COUNT(Stop_Hour) AS Stop_Case,
                    SUM(Stop_Hour) AS Stop_Hour
                FROM #STP
                GROUP BY FORMAT(CONVERT(DATE, SENDTIME), 'yyyyMM');
                """, macName, div, top, monthTo);
    }

    private static class ChartMSMovingAveRowMapper implements RowMapper<ChartMSMovingAveResponse> {
        @Override
        public ChartMSMovingAveResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            ChartMSMovingAveResponse response = new ChartMSMovingAveResponse();
            response.setMonth(rs.getString("Month"));
            response.setStopCase(rs.getInt("Stop_Case"));
            response.setStopHour(rs.getDouble("Stop_Hour"));
            return response;
        }
    }
}

package com.example.ma_visualization_be.repository;

import com.example.ma_visualization_be.dto.ChartRFMovingAveResponse;
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
public class ChartRFMovingAveRepository {
    private static final Logger logger = LoggerFactory.getLogger(ChartRFMovingAveRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<ChartRFMovingAveResponse> getChartRFMovingAve(
            String monthTo,
            List<String> divisions,
            String machineName,
            int top
    ) {

        if (divisions == null || divisions.isEmpty()) {
            logger.warn("getChartRFMovingAve called with empty divisions -> returning empty list");
            return Collections.emptyList();
        }

        // chỉ lấy division đầu tiên (vì query đang dùng 1 biến @div)
        String div = divisions.get(0).replace("'", "''");

        String sql = buildDynamicQuery(machineName, div, top, monthTo);

        logger.debug("=== SQL Query ===\n{}", sql);

        return jdbcTemplate.query(sql, new ChartRFMovingAveRepository.ChartRFMovingAveRowMapper());
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
                 DECLARE @toD DATETIME = DATEADD(DAY, -3, DATEADD(DAY, 1, CAST(@monthToDate AS DATETIME)))
                
                 IF OBJECT_ID('tempdb..#RPF') IS NOT NULL DROP TABLE #RPF;
                
                 SELECT
                 COALESCE(ratio.DIV,
                 CASE
                  WHEN XBLNR2 LIKE '1566-%%' THEN
                   CASE
                    WHEN LEFT(KOSTL,6) IN ('614100') THEN 'PRESS'
                    WHEN LEFT(KOSTL,6) = '614200' THEN 'MOLD'   
                    WHEN LEFT(KOSTL,6) IN ('614600', '614700') THEN 'GUIDE'
                   END
                  ELSE 'OTHER'
                 END) as Dept,
                 mac.ATTRIBUTE1 as MacGrp,
                 IIF(AUFNR LIKE 'M%%',RIGHT(AUFNR,LEN(AUFNR)-1),AUFNR) As MacID,
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
                 'Shared ' + Format(ratio.Ratio,'0.0%%') as Note
                 INTO #RPF
                FROM MANUFASPCPD.dbo.MANUFA_F_PD_DTM_ISSUE iss
                INNER JOIN MANUFASPCPD.dbo.MANUFA_F_PD_GRB_PART part ON iss.MATNR = part.MATNR
                LEFT JOIN F2Database.dbo.F2_MA_MACHINE_MASTER mac ON IIF(AUFNR LIKE 'M%%',RIGHT(AUFNR,LEN(AUFNR)-1),AUFNR) = mac.CODE
                LEFT JOIN F2Database.dbo.F2_Cost_Center_Share share ON iss.KOSTL = share.Cost_Center
                LEFT JOIN F2Database.dbo.F2_Cost_Center_Share_Ratio ratio ON share.Group_Share = ratio.Dept
                               AND CASE
                                WHEN XBLNR2 LIKE '1566-%%' THEN
                                 CASE
                                  WHEN LEFT(KOSTL,6) IN ('614100') THEN 'PRESS'   
                                  WHEN LEFT(KOSTL,6) = '614200' THEN 'MOLD'   
                                  WHEN LEFT(KOSTL,6) IN ('614600', '614700') THEN 'GUIDE'
                                 END
                                ELSE 'OTHER'
                                END <> 'OTHER'
                WHERE KONTO = '570600'
                AND (BLDAT BETWEEN @monthFromDate AND @monthToDate)
                AND ((ratio.Dept <> 'F2' AND ratio.Dept <> 'HT2') or ratio.Dept is Null)
                AND mac.MACHINE_TYPE = @macName
                AND CASE
                  WHEN XBLNR2 LIKE '1566-%%' THEN
                   CASE
                    WHEN LEFT(KOSTL,6) IN ('614100') THEN 'PRESS'
                    WHEN LEFT(KOSTL,6) = '614200' THEN 'MOLD'   
                    WHEN LEFT(KOSTL,6) IN ('614600', '614700') THEN 'GUIDE'
                   END
                  ELSE 'OTHER'
                 END = @div
                ORDER BY IIF(ERFMG<0,-1,1)*AMOUNT*COALESCE(ratio.Ratio,1) DESC
                
                SELECT FORMAT(UseDate,'yyyyMM') as [Month], SUM(ACT) as [RepairFee]
                FROM #RPF
                GROUP BY FORMAT(UseDate,'yyyyMM');
                """, macName, div, top, monthTo);
    }


    private static class ChartRFMovingAveRowMapper implements RowMapper<ChartRFMovingAveResponse> {
        @Override
        public ChartRFMovingAveResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            ChartRFMovingAveResponse response = new ChartRFMovingAveResponse();
            response.setMonth(rs.getString("Month"));
            response.setRepairFee(rs.getDouble("RepairFee"));
            return response;
        }
    }
}

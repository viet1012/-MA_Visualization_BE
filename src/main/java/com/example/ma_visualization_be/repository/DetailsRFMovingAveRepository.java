package com.example.ma_visualization_be.repository;

import org.springframework.stereotype.Repository;
import com.example.ma_visualization_be.dto.DetailsRFMovingAveResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class DetailsRFMovingAveRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<DetailsRFMovingAveResponse> getDetailsRFMovingAve(
            String monthFrom,
            String monthTo,
            List<String> divisions,
            String machineName) {

        // build dynamic division list
        String divisionValues = divisions.stream()
                .map(div -> "('" + div + "')")
                .collect(Collectors.joining(", "));

        String sql = buildDynamicQuery(divisionValues);

        // truyền tham số lần lượt theo thứ tự "?" trong query
        Object[] params = new Object[]{
                monthFrom,
                monthTo,
                machineName
        };

        return jdbcTemplate.query(sql, params, new MachineAnalysisRowMapper());
    }

    private String buildDynamicQuery(String divisionValues) {
        return """
            DECLARE @monthFrom VARCHAR(6) = ?
            DECLARE @monthTo VARCHAR(6) = ?
            DECLARE @monthFromDate DATE = CONVERT(DATE, @monthFrom + '01')
            DECLARE @monthToDate DATE = EOMONTH(CONVERT(DATE, @monthTo + '01'))
            DECLARE @toD DATETIME = DATEADD(ms,-3,DATEADD(DAY,1,CAST(@monthToDate as DATETIME)))
            DECLARE @macName NVARCHAR(100) = ?
            DECLARE @div TABLE (Value NVARCHAR(50))
            INSERT INTO @div (Value)
            VALUES """ + divisionValues + """

            -- chính là query SQL của bạn, mình giữ nguyên
            SELECT 
             COALESCE(ratio.DIV,
             CASE
              WHEN XBLNR2 LIKE '1566-%' THEN 
               CASE
                WHEN LEFT(KOSTL,6) IN ('614100') THEN 'PRESS' 
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
             'Shared ' + FORMAT(ratio.Ratio,'0.0%') as Note
            FROM MANUFASPCPD.dbo.MANUFA_F_PD_DTM_ISSUE iss
            INNER JOIN MANUFASPCPD.dbo.MANUFA_F_PD_GRB_PART part ON iss.MATNR = part.MATNR
            LEFT JOIN F2Database.dbo.F2_MA_MACHINE_MASTER mac ON IIF(AUFNR LIKE 'M%',RIGHT(AUFNR,LEN(AUFNR)-1),AUFNR) = mac.CODE
            LEFT JOIN F2Database.dbo.F2_Cost_Center_Share share ON iss.KOSTL = share.Cost_Center
            LEFT JOIN F2Database.dbo.F2_Cost_Center_Share_Ratio ratio ON share.Group_Share = ratio.Dept
                           AND CASE
                            WHEN XBLNR2 LIKE '1566-%' THEN 
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
              WHEN XBLNR2 LIKE '1566-%' THEN 
               CASE
                WHEN LEFT(KOSTL,6) IN ('614100') THEN 'PRESS'
                WHEN LEFT(KOSTL,6) = '614200' THEN 'MOLD'    
                WHEN LEFT(KOSTL,6) IN ('614600', '614700') THEN 'GUIDE' 
               END
              ELSE 'OTHER'
             END IN (SELECT Value FROM @div)
            ORDER BY UseDate DESC
            """;
    }

    private static class MachineAnalysisRowMapper implements RowMapper<DetailsRFMovingAveResponse> {
        @Override
        public DetailsRFMovingAveResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            DetailsRFMovingAveResponse response = new DetailsRFMovingAveResponse();
            response.setDiv(rs.getString("Dept"));
            response.setMacName(rs.getString("MacName"));
            response.setMacGrp(rs.getString("MacGrp"));
            response.setMacId(rs.getString("MacID"));
            response.setCate(rs.getString("Cate"));
            response.setMatnr(rs.getString("MATNR"));
            response.setMaktx(rs.getString("MAKTX"));
            response.setUseDate(rs.getDate("UseDate"));
            response.setQty(rs.getBigDecimal("QTY"));
            response.setUnit(rs.getString("UNIT"));
            response.setAct(rs.getBigDecimal("ACT"));
            response.setNote(rs.getString("Note"));
            return response;
        }
    }
}

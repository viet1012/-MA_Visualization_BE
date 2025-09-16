package com.example.ma_visualization_be.repository;

import com.example.ma_visualization_be.dto.DetailsMSMovingAveResponse;
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
import java.util.stream.Collectors;

@Repository
public class DetailsMSMovingAveRepository {

    private static final Logger logger = LoggerFactory.getLogger(DetailsMSMovingAveRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public List<DetailsMSMovingAveResponse> getDetailsMSMovingAve(
            String monthFrom,
            String monthTo,
            List<String> divisions,
            String machineName) {

        // guard: nếu không có division thì trả về list rỗng (bạn có thể đổi hành vi nếu muốn)
        if (divisions == null || divisions.isEmpty()) {
            logger.warn("getDetailsMSMovingAve called with empty divisions -> returning empty list");
            return Collections.emptyList();
        }

        // escape single-quote trong giá trị division để tránh lỗi SQL
        String divisionValues = divisions.stream()
                .map(div -> "('" + div.replace("'", "''") + "')")
                .collect(Collectors.joining(", "));

        String sql = buildDynamicQuery(divisionValues);

        // truyền tham số theo đúng thứ tự dấu ? trong SQL: monthFrom, monthTo, macName
        Object[] params = new Object[]{
                monthFrom,
                monthTo,
                machineName
        };

        logger.debug("=== SQL Query ===\n{}", sql);
        logger.debug("=== Params === monthFrom={} monthTo={} machineName={} divisions={}",
                monthFrom, monthTo, machineName, divisions);

        return jdbcTemplate.query(sql, params, new DetailsMSMovingAveRowMapper());
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
                
                SELECT
                  CASE
                   WHEN mst.DIVISION Like '%GUIDE' THEN 'GUIDE'
                   WHEN mst.DIVISION Like 'SUPPORT%' THEN
                    CASE
                     WHEN mst.FACTORY = 'B' THEN 'GUIDE'
                     WHEN mst.FACTORY = 'C' THEN 'MOLD'
                     ELSE 'PRESS'
                    END
                   ELSE mst.DIVISION
                  END as DIV,
                  GROUPNAME,
                  dt.MACHINECODE,
                  MACHINE_TYPE,
                  dt.REF_NO,
                  cnt.C_LINHKIEN_VI as Reason,
                  CONFIRM_DATE,
                     convert(varchar,ISNULL(CONFIRM_DATE,SENDTIME),120) SENDTIME,
                     convert(varchar,STARTTIME,120) STARTTIME,
                     convert(varchar,FINISHTIME,120) FINISHTIME, tempR.Temp_Run,
                  CASE
                   WHEN STATUSCODE = 'ST02'
                    THEN CAST(ROUND((DATEDIFF(MINUTE,ISNULL(CONFIRM_DATE,SENDTIME),COALESCE(FINISHTIME,@monthToDate))) /60.0*20/24,2) AS FLOAT)
                   WHEN STATUSCODE = 'ST01'
                    THEN CAST(ROUND((DATEDIFF(MINUTE,ISNULL(STARTTIME,SENDTIME),COALESCE(FINISHTIME,@monthToDate))) /60.0*20/24,2) AS FLOAT)
                  END - COALESCE(tempR.Temp_Run,0) As Stop_Hour,
                  dt.ISSUESTATUS
                
                 FROM F2Database.dbo.f2_ma_machine_data dt
                 INNER JOIN F2Database.dbo.f2_ma_machine_master mst ON dt.machinecode = mst.code
                 LEFT JOIN (
                   SELECT REF_NO, Min_Stop, Max_Stop,
                    CAST(ROUND(DATEDIFF(MINUTE,Min_Stop,Max_Stop) /60.0*20/24,2) AS FLOAT) as Temp_Run
                   FROM (
                    SELECT REF_NO, MIN(STOP_DATE) as Min_Stop,MAX(STOP_DATE) as Max_Stop
                    FROM F2Database.dbo.F2_MA_MACHINE_STOPTIME
                    GROUP BY REF_NO
                    ) as t1
                   ) as tempR
                   ON dt.REF_NO = tempR.REF_NO
                 LEFT JOIN F2Database.dbo.F2_MA_MACHINE_CONTENT cnt ON dt.REF_NO = cnt.REF_NO
                 WHERE ACTIONCODE LIKE 'AC01%'
                 AND ISSUESTATUS not in ('CANCEL')
                 AND dt.STATUSCODE = 'ST02'
                 AND ((SENDTIME BETWEEN @monthFromDate AND @toD) OR (ISSUESTATUS = 'WAIT' AND SENDTIME < @monthFromDate))
                 AND mst.MACHINE_TYPE = @macName
                 -- SỬA: so sánh với table variable bằng IN (SELECT Value FROM @div)
                 AND (CASE
                   WHEN mst.DIVISION Like '%GUIDE' THEN 'GUIDE'
                   WHEN mst.DIVISION Like 'SUPPORT%' THEN
                    CASE
                     WHEN mst.FACTORY = 'B' THEN 'GUIDE'
                     WHEN mst.FACTORY = 'C' THEN 'MOLD'
                     ELSE 'PRESS'
                    END
                   ELSE mst.DIVISION
                  END) IN (SELECT Value FROM @div)
                 Order By SENDTIME DESC
                """;
    }

    private static class DetailsMSMovingAveRowMapper implements RowMapper<DetailsMSMovingAveResponse> {

        @Override
        public DetailsMSMovingAveResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            DetailsMSMovingAveResponse response = new DetailsMSMovingAveResponse();
            response.setDiv(rs.getString("DIV"));
            response.setGroupName(rs.getString("GROUPNAME"));
            response.setMachineCode(rs.getString("MACHINECODE"));
            response.setMachineType(rs.getString("MACHINE_TYPE"));
            response.setRefNo(rs.getString("REF_NO"));
            response.setReason(rs.getString("Reason"));
            response.setConfirmDate(rs.getTimestamp("CONFIRM_DATE"));
            response.setSendTime(rs.getString("SENDTIME"));
            response.setStartTime(rs.getString("STARTTIME"));
            response.setFinishTime(rs.getString("FINISHTIME"));
            response.setTempRun(rs.getBigDecimal("Temp_Run"));
            response.setStopHour(rs.getBigDecimal("Stop_Hour"));
            response.setIssueStatus(rs.getString("ISSUESTATUS"));
            return response;
        }
    }
}

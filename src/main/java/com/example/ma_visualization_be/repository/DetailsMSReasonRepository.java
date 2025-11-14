package com.example.ma_visualization_be.repository;

import com.example.ma_visualization_be.dto.DetailsMSReasonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class DetailsMSReasonRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<DetailsMSReasonResponse> getDetailsMSReason(
            String month, List<String> divisions, String rs1) {

        // Ghép danh sách division động
        String divisionValues = divisions.stream()
                .map(div -> "(?)")
                .collect(Collectors.joining(", "));

        // Tạo query hoàn chỉnh
        String sql = buildDynamicQuery(divisionValues);

        // ⚠️ Thứ tự tham số: month → divisions → rs1
        Object[] params = new Object[1 + divisions.size() + 1];
        int index = 0;
        params[index++] = month;

        for (String div : divisions) {
            params[index++] = div;
        }

        params[index] = rs1;

        // In log để kiểm tra (nếu cần)
        System.out.println("🔹SQL đang chạy:\n" + sql);
        System.out.println("🔹Tham số: month=" + month + ", divisions=" + divisions + ", reason=" + rs1);

        return jdbcTemplate.query(sql, params, new DetailMSReasonRowMapper());
    }

    private String buildDynamicQuery(String divisionValues) {
        return """
                DECLARE @month VARCHAR(6) = ?
                DECLARE @fromD DATETIME = CONVERT(DATETIME, @month + '01', 112)
                DECLARE @toD DATETIME = Least(GETDATE(),DATEADD(MILLISECOND, -3, DATEADD(DAY, 1, CAST(EOMONTH(@fromD) AS DATETIME))))
                
                DECLARE @div TABLE (Value NVARCHAR(50))
                INSERT INTO @div (Value)
                VALUES """ + divisionValues + """
                
                DECLARE @rs1 NVARCHAR(50) = ?
                
                IF OBJECT_ID('tempdb..#rs') IS NOT NULL DROP TABLE #rs;
                
                SELECT
                    GREATEST(CONVERT(DATE, dt.SENDTIME, 112), CONVERT(DATE, @month + '01', 112)) AS SendDate,
                    CASE
                        WHEN mst.DIVISION LIKE '%GUIDE' THEN 'GUIDE'
                        WHEN mst.DIVISION LIKE 'SUPPORT%' THEN 'PRESS'
                        ELSE mst.DIVISION
                    END AS DIV,
                    GROUPNAME,
                    dt.MACHINECODE,
                    MACHINE_TYPE,
                    STATUSCODE,
                    dt.REF_NO,
                    CONVERT(VARCHAR, ISNULL(CONFIRM_DATE, SENDTIME), 120) AS SENDTIME,
                    CONVERT(VARCHAR, STARTTIME, 120) AS STARTTIME,
                    CONVERT(VARCHAR, FINISHTIME, 120) AS FINISHTIME,
                    tempR.Temp_Run,
                    CASE
                        WHEN STATUSCODE = 'ST02'
                            THEN CAST(ROUND((DATEDIFF(MINUTE, GREATEST(ISNULL(CONFIRM_DATE, SENDTIME), CONVERT(DATE, @month + '01', 112)), COALESCE(FINISHTIME, @toD))) / 60.0 * 20 / 24, 2) AS FLOAT)
                        WHEN STATUSCODE = 'ST01'
                            THEN CAST(ROUND((DATEDIFF(MINUTE, GREATEST(ISNULL(STARTTIME, SENDTIME), CONVERT(DATE, @month + '01', 112)), COALESCE(FINISHTIME, @toD))) / 60.0 * 20 / 24, 2) AS FLOAT)
                    END - COALESCE(tempR.Temp_Run, 0) AS Stop_Hour,
                    dt.ISSUESTATUS,
                    rs.C_LINHKIEN_VI,
                    PARSENAME(REPLACE(rs.C_LINHKIEN_VI, '--', '.'), 3) COLLATE SQL_Latin1_General_CP1_CI_AS AS reason1,
                    PARSENAME(REPLACE(rs.C_LINHKIEN_VI, '--', '.'), 2) COLLATE SQL_Latin1_General_CP1_CI_AS AS reason2,
                    PARSENAME(REPLACE(rs.C_LINHKIEN_VI, '--', '.'), 1) COLLATE SQL_Latin1_General_CP1_CI_AS AS reason3
                INTO #rs
                FROM F2Database.dbo.f2_ma_machine_data dt
                INNER JOIN F2Database.dbo.f2_ma_machine_master mst ON dt.machinecode = mst.code
                LEFT JOIN (
                    SELECT REF_NO, Min_Stop, Max_Stop,
                        CAST(ROUND(DATEDIFF(MINUTE, Min_Stop, Max_Stop) / 60.0 * 20 / 24, 2) AS FLOAT) AS Temp_Run
                    FROM (
                        SELECT REF_NO, MIN(GREATEST(STOP_DATE, @fromD)) AS Min_Stop, MAX(STOP_DATE) AS Max_Stop
                        FROM F2Database.dbo.F2_MA_MACHINE_STOPTIME
                        GROUP BY REF_NO
                    ) AS t1
                ) AS tempR ON dt.REF_NO = tempR.REF_NO
                LEFT JOIN F2Database.dbo.F2_MA_MACHINE_CONTENT rs ON dt.REF_NO = rs.REF_NO
                WHERE FINISHTIME BETWEEN @fromD AND @toD
                    AND ACTIONCODE LIKE 'AC01%'
                    AND ISSUESTATUS NOT IN ('CANCEL')
                    AND dt.STATUSCODE = 'ST02'
                ORDER BY SENDTIME;
                
                WITH cte AS (
                    SELECT 
                        #rs.DIV,
                        COALESCE(ts1.Reason_EN, #rs.reason1) AS Reason1,
                        COALESCE(ts2.Reason_EN, #rs.reason2) AS Reason2,
                        COUNT(Stop_Hour) AS Stop_Case,
                        SUM(Stop_Hour) AS Stop_Hour,
                        ROW_NUMBER() OVER (ORDER BY SUM(Stop_Hour) DESC) AS rn
                    FROM #rs
                    LEFT JOIN F2Database.dbo.F2_MA_Translate_Master ts1 ON #rs.reason1 = ts1.Reason_VN
                    LEFT JOIN F2Database.dbo.F2_MA_Translate_Master ts2 ON #rs.reason2 = ts2.Reason_VN
                    WHERE ts1.Reason_EN LIKE @rs1
                        AND #rs.DIV COLLATE SQL_Latin1_General_CP1_CI_AS IN (SELECT value FROM @div)
                    GROUP BY #rs.DIV,
                             COALESCE(ts1.Reason_EN, #rs.reason1),
                             COALESCE(ts2.Reason_EN, #rs.reason2)
                )
                SELECT DIV, Reason1, Reason2, Stop_Case, Stop_Hour
                FROM cte
                WHERE rn <= 10
                
                UNION ALL
                
                SELECT DIV, NULL AS Reason1, 'OTHERS' AS Reason2,
                       SUM(Stop_Case) AS Stop_Case, SUM(Stop_Hour) AS Stop_Hour
                FROM cte
                WHERE rn > 10
                GROUP BY DIV
                """;
    }

    private static class DetailMSReasonRowMapper implements RowMapper<DetailsMSReasonResponse> {
        @Override
        public DetailsMSReasonResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            DetailsMSReasonResponse response = new DetailsMSReasonResponse();
            response.setDiv(rs.getString("DIV"));
            response.setReason1(rs.getString("Reason1"));
            response.setReason2(rs.getString("Reason2"));
            response.setStopCase(rs.getObject("Stop_Case", Integer.class));
            response.setStopHour(rs.getDouble("Stop_Hour"));
            return response;
        }
    }
}

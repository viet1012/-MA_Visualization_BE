package com.example.ma_visualization_be.repository;

import com.example.ma_visualization_be.dto.DetailsMachineStopReasonDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class DetailsMachineStopReasonDetailsRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<DetailsMachineStopReasonDTO> getDetailsMSReason(
            String month, List<String> divisions, String rs1) {

        String divisionValues = divisions.stream()
                .map(div -> "(?)")
                .collect(Collectors.joining(", "));

        String sql = buildDynamicQuery(divisionValues);

        Object[] params = new Object[1 + divisions.size() + 1];
        int idx = 0;

        params[idx++] = month;
        for (String d : divisions) params[idx++] = d;
        params[idx] = rs1;

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
                    CASE WHEN dt.SENDTIME > CONVERT(DATE,@month+'01',112)
                         THEN dt.SENDTIME ELSE CONVERT(DATE,@month+'01',112) END AS SendDate,
                    CASE
                        WHEN mst.DIVISION LIKE '%GUIDE' THEN 'GUIDE'
                        WHEN mst.DIVISION LIKE 'SUPPORT%' THEN 'PRESS'
                        ELSE mst.DIVISION
                    END AS DIV,
                    dt.MACHINECODE,
                    MACHINE_TYPE,
                    rs.C_LINHKIEN_VI,
                    PARSENAME(REPLACE(rs.C_LINHKIEN_VI,'--','.'),3) COLLATE SQL_Latin1_General_CP1_CI_AS AS reason1,
                    PARSENAME(REPLACE(rs.C_LINHKIEN_VI,'--','.'),2) COLLATE SQL_Latin1_General_CP1_CI_AS AS reason2,
                    PARSENAME(REPLACE(rs.C_LINHKIEN_VI,'--','.'),1) COLLATE SQL_Latin1_General_CP1_CI_AS AS reason3,
                    CASE
                        WHEN dt.STATUSCODE = 'ST02'
                            THEN CAST(ROUND((DATEDIFF(MINUTE,
                                CASE WHEN ISNULL(CONFIRM_DATE,SENDTIME) > @fromD THEN CONFIRM_DATE ELSE @fromD END,
                                COALESCE(FINISHTIME,@toD))) /60.0*20/24,2) AS FLOAT)
                        WHEN dt.STATUSCODE = 'ST01'
                            THEN CAST(ROUND((DATEDIFF(MINUTE,
                                CASE WHEN ISNULL(STARTTIME,SENDTIME) > @fromD THEN STARTTIME ELSE @fromD END,
                                COALESCE(FINISHTIME,@toD))) /60.0*20/24,2) AS FLOAT)
                    END AS Stop_Hour
                INTO #rs
                FROM F2Database.dbo.f2_ma_machine_data dt
                INNER JOIN F2Database.dbo.f2_ma_machine_master mst ON dt.machinecode = mst.code
                LEFT JOIN F2Database.dbo.F2_MA_MACHINE_CONTENT rs ON dt.REF_NO = rs.REF_NO
                WHERE FINISHTIME BETWEEN @fromD AND @toD
                AND ACTIONCODE LIKE 'AC01%'
                AND ISSUESTATUS NOT IN ('CANCEL')
                AND dt.STATUSCODE = 'ST02';
                
                SELECT r.DIV, r.MACHINECODE, r.MACHINE_TYPE,
                    COALESCE(t1.Reason_EN, r.reason1) AS Reason1,
                    COALESCE(t2.Reason_EN, r.reason2) AS Reason2,
                    r.reason3,
                    r.Stop_Hour,
                    r.C_LINHKIEN_VI
                FROM #rs r
                LEFT JOIN F2Database.dbo.F2_MA_Translate_Master t1 ON r.reason1 = t1.Reason_VN
                LEFT JOIN F2Database.dbo.F2_MA_Translate_Master t2 ON r.reason2 = t2.Reason_VN
                WHERE t1.Reason_EN LIKE @rs1
                AND r.DIV IN (SELECT Value FROM @div)
                ORDER BY r.Stop_Hour DESC
                """;
    }

    private static class DetailMSReasonRowMapper implements RowMapper<DetailsMachineStopReasonDTO> {
        @Override
        public DetailsMachineStopReasonDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            DetailsMachineStopReasonDTO o = new DetailsMachineStopReasonDTO();
            o.setDiv(rs.getString("DIV"));
            o.setMachineCode(rs.getString("MACHINECODE"));
            o.setMachineType(rs.getString("Machine_Type"));
            o.setReason1(rs.getString("Reason1"));
            o.setReason2(rs.getString("Reason2"));
            o.setReason3(rs.getString("reason3"));
            o.setStopHour(rs.getDouble("Stop_Hour"));
            o.setLinhKienVi(rs.getString("C_LINHKIEN_VI"));
            return o;
        }
    }
}

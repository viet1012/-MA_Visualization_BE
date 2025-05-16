package com.example.ma_visualization_be.service;

import com.example.ma_visualization_be.dto.IDetailsDataMSDTO;
import com.example.ma_visualization_be.dto.IDetailsDataRFDTO;
import com.example.ma_visualization_be.repository.IDetailsDataMSRepository;
import com.example.ma_visualization_be.repository.IDetailsDataRFRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

//@Service
//public class DetailsDataMSService {
//    @Autowired
//    IDetailsDataMSRepository repository;
//
//    public List<IDetailsDataMSDTO> getDailyDetailsMachineStopping(String month) {
//        return repository.getDailyDetailsMachineStopping(month);
//
//    }
//}

import com.example.ma_visualization_be.dto.DetailsDataMSDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetailsDataMSService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<DetailsDataMSDTO> getDailyDetailsMachineStopping(String month) {
        String sql = """
            DECLARE @month VARCHAR(6) = REPLACE(:month, '-', '');
            SELECT
                GREATEST(CONVERT(DATE,dt.SENDTIME,112),CONVERT(DATE,@month + '01',112)) as sendDate,
                CASE
                    WHEN mst.DIVISION Like '%GUIDE' THEN 'GUIDE'
                    WHEN mst.DIVISION Like 'SUPPORT%' THEN 'PRESS'
                    ELSE mst.DIVISION
                END as div,
                GROUPNAME as groupName,
                MACHINECODE as machineCode,
                MACHINE_TYPE as machineType,
                STATUSCODE as statusCode,
                CONFIRM_DATE as confirmDate,
                convert(varchar,ISNULL(CONFIRM_DATE,SENDTIME),120) as sendTime,
                convert(varchar,STARTTIME,120) as startTime,
                convert(varchar,ESTIME,120) as esTime,
                convert(varchar,FINISHTIME,120) as finishTime,
                CASE
                    WHEN STATUSCODE = 'ST02'
                        THEN CAST(ROUND((DATEDIFF(MINUTE,GREATEST(ISNULL(CONFIRM_DATE,SENDTIME),CONVERT(DATE,@month + '01',112)),COALESCE(FINISHTIME,GETDATE()))) /60.0*20/24,2) AS FLOAT)
                    WHEN STATUSCODE = 'ST01'
                        THEN CAST(ROUND((DATEDIFF(MINUTE,GREATEST(ISNULL(STARTTIME,SENDTIME),CONVERT(DATE,@month + '01',112)),COALESCE(FINISHTIME,GETDATE()))) /60.0*20/24,2) AS FLOAT)
                END as stopHour,
                dt.ISSUESTATUS as issueStatus
            FROM F2Database.dbo.f2_ma_machine_data dt
            INNER JOIN F2Database.dbo.f2_ma_machine_master mst ON dt.machinecode = mst.code 
            WHERE (SENDTIME >= CONVERT(DATE,@month + '01',112) OR FINISHTIME Is NULL)
            AND ISSUESTATUS not in ('CANCEL')
            ORDER BY SENDTIME
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("month", month);

        return jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(DetailsDataMSDTO.class));
    }
}

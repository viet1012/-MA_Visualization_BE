package com.example.ma_visualization_be.repository;

import com.example.ma_visualization_be.dto.IDetailsDataMSDTO;
import com.example.ma_visualization_be.model.DummyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDetailsDataMSRepository extends JpaRepository<DummyEntity, Long> {

    @Query(value = """
        DECLARE @month VARCHAR(6) = REPLACE(:month, '-', '');
        SELECT
            GREATEST(CONVERT(DATE,dt.SENDTIME,112),CONVERT(DATE,@month + '01',112)) as SendDate,
            CASE
                WHEN mst.DIVISION Like '%GUIDE' THEN 'GUIDE'
                WHEN mst.DIVISION Like 'SUPPORT%' THEN 'PRESS'
                ELSE mst.DIVISION
            END as DIV,
            GROUPNAME,
            MACHINECODE,
            MACHINE_TYPE,
            STATUSCODE,
            CONFIRM_DATE,
            convert(varchar,ISNULL(CONFIRM_DATE,SENDTIME),120) SENDTIME,
            convert(varchar,STARTTIME,120) STARTTIME,
            convert(varchar,ESTIME,120) ESTIME,
            convert(varchar,FINISHTIME,120) FINISHTIME,
            CASE
                WHEN STATUSCODE = 'ST02'
                    THEN CAST(ROUND((DATEDIFF(MINUTE,GREATEST(ISNULL(CONFIRM_DATE,SENDTIME),CONVERT(DATE,@month + '01',112)),COALESCE(FINISHTIME,GETDATE()))) /60.0*20/24,2) AS FLOAT)
                WHEN STATUSCODE = 'ST01'
                    THEN CAST(ROUND((DATEDIFF(MINUTE,GREATEST(ISNULL(STARTTIME,SENDTIME),CONVERT(DATE,@month + '01',112)),COALESCE(FINISHTIME,GETDATE()))) /60.0*20/24,2) AS FLOAT)
            END Stop_Hour,
            dt.ISSUESTATUS
        FROM F2Database.dbo.f2_ma_machine_data dt
        INNER JOIN F2Database.dbo.f2_ma_machine_master mst ON dt.machinecode = mst.code 
        WHERE (SENDTIME >= CONVERT(DATE,@month + '01',112) OR FINISHTIME Is NULL)
        AND ISSUESTATUS not in ('CANCEL')
        ORDER BY SENDTIME
        """, nativeQuery = true)
    List<IDetailsDataMSDTO> getDailyDetailsMachineStopping(@Param("month") String month);
}

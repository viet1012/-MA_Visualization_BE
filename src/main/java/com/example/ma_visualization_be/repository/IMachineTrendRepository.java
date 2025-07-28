package com.example.ma_visualization_be.repository;


import com.example.ma_visualization_be.dto.IMachineTrendDTO;
import com.example.ma_visualization_be.model.DummyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.*;

import java.util.List;

@Repository
public interface IMachineTrendRepository extends JpaRepository<DummyEntity, Long> {
    @Query(value = """
    IF OBJECT_ID('tempdb..#tempTB') IS NOT NULL DROP TABLE #tempTB;
    CREATE TABLE #tempTB (
        MonthUse VARCHAR(6),
        Cate NVARCHAR(50),
        MacID NVARCHAR(50),
        MacName NVARCHAR(100),
        Act DECIMAL(18,2)
    );

    WITH MA_Details AS (
        SELECT
            COALESCE(ratio.DIV,
                CASE
                    WHEN XBLNR2 LIKE '1566-%' THEN
                        CASE
                            WHEN LEFT(KOSTL,6) IN ('614100','614000') THEN 'PRESS'
                            WHEN LEFT(KOSTL,6) = '614200' THEN 'MOLD'
                            WHEN LEFT(KOSTL,6) IN ('614600','614700') THEN 'GUIDE'
                        END
                    ELSE 'OTHER'
                END
            ) AS Dept,
            mac.ATTRIBUTE1 AS MacGrp,
            IIF(AUFNR LIKE 'M%', RIGHT(AUFNR, LEN(AUFNR)-1), AUFNR) AS MacID,
            mac.MACHINE_TYPE AS MacName,
            SGTXT AS Cate,
            CONVERT(DATE, BLDAT, 112) AS UseDate,
            IIF(ERFMG < 0, -1, 1) * AMOUNT * COALESCE(ratio.Ratio, 1) AS ACT
        FROM MANUFASPCPD.dbo.MANUFA_F_PD_DTM_ISSUE iss
        INNER JOIN MANUFASPCPD.dbo.MANUFA_F_PD_GRB_PART part ON iss.MATNR = part.MATNR
        LEFT JOIN F2Database.dbo.F2_MA_MACHINE_MASTER mac ON 
            IIF(AUFNR LIKE 'M%', RIGHT(AUFNR, LEN(AUFNR)-1), AUFNR) = mac.CODE
        LEFT JOIN F2Database.dbo.F2_Cost_Center_Share share ON iss.KOSTL = share.Cost_Center
        LEFT JOIN F2Database.dbo.F2_Cost_Center_Share_Ratio ratio ON 
            share.Group_Share = ratio.Dept AND 
            CASE 
                WHEN XBLNR2 LIKE '1566-%' THEN 
                    CASE 
                        WHEN LEFT(KOSTL,6) IN ('614100','614000') THEN 'PRESS'
                        WHEN LEFT(KOSTL,6) = '614200' THEN 'MOLD'
                        WHEN LEFT(KOSTL,6) IN ('614600','614700') THEN 'GUIDE'
                    END
                ELSE 'OTHER'
            END <> 'OTHER'
        WHERE KONTO = '570600'
          AND LEFT(BLDAT,6) >= :fromDate
          AND LEFT(BLDAT,6) <= :month
          AND COALESCE(ratio.DIV,
                CASE 
                    WHEN XBLNR2 LIKE '1566-%' THEN 
                        CASE 
                            WHEN LEFT(KOSTL,6) IN ('614100','614000') THEN 'PRESS'
                            WHEN LEFT(KOSTL,6) = '614200' THEN 'MOLD'
                            WHEN LEFT(KOSTL,6) IN ('614600','614700') THEN 'GUIDE'
                        END
                    ELSE 'OTHER'
                END
          ) = :div
    )

    INSERT INTO #tempTB
    SELECT 
        FORMAT(UseDate,'yyyyMM') AS MonthUse,
        Cate,
        MacID,
        MacName,
        SUM(ACT) AS Act
    FROM MA_Details
    GROUP BY FORMAT(UseDate,'yyyyMM'), Cate, MacID, MacName;

    SELECT 
        t.MonthUse,
        t.Cate,
        t.MacID,
        t.MacName,
        t.Act,
        u.TTL,
        u.STT
    FROM #tempTB t
    JOIN (
        SELECT TOP (:top) 
            MacID,
            MacName,
            SUM(Act) AS TTL,
            ROW_NUMBER() OVER (ORDER BY SUM(Act) DESC) AS STT
        FROM #tempTB
        WHERE MacID IS NOT NULL
        GROUP BY MacID, MacName
    ) u ON t.MacID = u.MacID
    ORDER BY u.STT, t.MonthUse
""", nativeQuery = true)
    List<IMachineTrendDTO> getMachineTrend(
            @Param("fromDate") String fromDate, // yyyyMMdd
            @Param("month") String month,       // yyyyMM
            @Param("div") String div,
            @Param("top") int top
    );



}

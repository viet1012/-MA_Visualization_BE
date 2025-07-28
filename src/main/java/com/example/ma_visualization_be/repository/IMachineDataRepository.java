package com.example.ma_visualization_be.repository;

import com.example.ma_visualization_be.dto.IMachineDataByCateDTO;
import com.example.ma_visualization_be.dto.IMachineDataByGroupDTO;
import com.example.ma_visualization_be.model.DummyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IMachineDataRepository extends JpaRepository<DummyEntity, Long> {

    @Query(value = """
        DECLARE @month VARCHAR(6) = REPLACE(:month, '-', '');
        DECLARE @div NVARCHAR(10) = :div;
        
        IF OBJECT_ID('tempdb..#tempTB') IS NOT NULL
            DROP TABLE #tempTB;
        CREATE TABLE #tempTB
        (
         MacGrp NVARCHAR(50),
         MacID NVARCHAR(50),
         MacName NVARCHAR(100),
         Act Decimal(18,2)
        );
        
        --From MA_Details
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
         AND LEFT(BLDAT,6) = @month
         AND BLDAT < CONVERT(varchar,GETDATE(),112)
         AND COALESCE(ratio.DIV,
          CASE
           WHEN XBLNR2 LIKE '1566-%' THEN\s
            CASE
             WHEN LEFT(KOSTL,6) IN ('614100','614000') THEN 'PRESS'   \s
             WHEN LEFT(KOSTL,6) = '614200' THEN 'MOLD'   \s
             WHEN LEFT(KOSTL,6) IN ('614600', '614700') THEN 'GUIDE'\s
            END
           ELSE 'OTHER'
          END) = @div
        )
        --
        
        INSERT INTO #tempTB\s
         (MacGrp, MacID, MacName, Act)
        (SELECT MacGrp, MacID, MacName, Sum(Act) as Act
        FROM MA_Details
        GROUP BY MacGrp, MacID, MacName
        HAVING Sum(Act)<>0)
        
        SELECT * FROM #tempTB
        """, nativeQuery = true)
    List<IMachineDataByGroupDTO> getMachineDataByGroup(@Param("month") String month, @Param("div") String div);

    @Query(value = """
        DECLARE @month VARCHAR(6) = REPLACE(:month, '-', '');
        DECLARE @div NVARCHAR(10) = :div;

        IF OBJECT_ID('tempdb..#tempTB') IS NOT NULL
            DROP TABLE #tempTB;
        CREATE TABLE #tempTB
        (
         Cate NVARCHAR(50),
         MacID NVARCHAR(50),
         MacName NVARCHAR(100),
         Act Decimal(18,2)
        );
        
        --From MA_Details
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
         AND LEFT(BLDAT,6) = @month
         AND BLDAT < CONVERT(varchar,GETDATE(),112)
         AND COALESCE(ratio.DIV,
          CASE
           WHEN XBLNR2 LIKE '1566-%' THEN\s
            CASE
             WHEN LEFT(KOSTL,6) IN ('614100','614000') THEN 'PRESS'   \s
             WHEN LEFT(KOSTL,6) = '614200' THEN 'MOLD'   \s
             WHEN LEFT(KOSTL,6) IN ('614600', '614700') THEN 'GUIDE'\s
            END
           ELSE 'OTHER'
          END) = @div
        )
        --
        
        INSERT INTO #tempTB\s
         (Cate, MacID, MacName, Act)
        (SELECT Cate, MacID, MacName, Sum(Act) as Act
        FROM MA_Details
        GROUP BY Cate, MacID, MacName
        HAVING Sum(Act)<>0)
        
        SELECT * FROM #tempTB
        """, nativeQuery = true)
    List<IMachineDataByCateDTO> getMachineDataByCate(@Param("month") String month, @Param("div") String div);

}

package com.example.ma_visualization_be.service;

import com.example.ma_visualization_be.dto.IMachineTrendDTO;
import com.example.ma_visualization_be.repository.IMachineTrendRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MachineTrendService {

    @Autowired
    private IMachineTrendRepository machineTrendRepository;

    /**
     * Trả về danh sách xu hướng theo máy
     *
     * @param mn    Số tháng lùi về
     * @param div   Phân xưởng (PRESS, MOLD, GUIDE)
     * @param top   Số lượng máy top N
     * @param month Tháng hiện tại dạng yyyyMM (ví dụ "202407")
     * @return Danh sách IMachineTrendDTO
     */

    public List<IMachineTrendDTO> getMachineTrend(int mn, String div, int top, String month) {
        YearMonth yearMonth;

        // Dùng YearMonth để parse "202507"
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
            yearMonth = YearMonth.parse(month, formatter);
        } catch (Exception ex) {
            // Nếu không đúng định dạng yyyyMM, thử yyyy-MM
            DateTimeFormatter formatterAlt = DateTimeFormatter.ofPattern("yyyy-MM");
            yearMonth = YearMonth.parse(month, formatterAlt);
        }

        // Chuyển thành ngày đầu tháng
        LocalDate monthDate = yearMonth.atDay(1);
        LocalDate fromDateLocal = monthDate.minusMonths(mn);

        String fromDate = fromDateLocal.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String monthStr = yearMonth.format(DateTimeFormatter.ofPattern("yyyyMM")); // vẫn là yyyyMM cho query

        return machineTrendRepository.getMachineTrend(fromDate, monthStr, div, top);
    }

}

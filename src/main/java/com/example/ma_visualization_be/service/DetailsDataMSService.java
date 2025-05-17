package com.example.ma_visualization_be.service;
import com.example.ma_visualization_be.repository.IDetailsDataMSRepository;
import com.example.ma_visualization_be.dto.IDetailsDataMSDTO;
import com.example.ma_visualization_be.repository.IDetailsDataRFRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DetailsDataMSService {

    @Autowired
    IDetailsDataMSRepository repository;
    public List<IDetailsDataMSDTO> getDailyDetailsMachineStopping(String month) {
        return repository.getDailyDetailsMachineStopping(month);
    }
}
